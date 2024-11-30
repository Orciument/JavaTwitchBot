package talium.inputs.TipeeeStream;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.EngineIOException;
import org.apache.commons.lang.StringUtils;
import talium.TwitchBot;
import talium.system.inputSystem.BotInput;
import talium.system.inputSystem.HealthManager;
import talium.system.inputSystem.Input;
import talium.system.inputSystem.InputStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import talium.system.inputSystem.configuration.InputConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Arrays;

@Input
public class TipeeeInput implements BotInput {
    private static final String tipeeeSocketInfoUrl = "https://api.tipeeestream.com/v2.0/site/socket";

    // we can set this manually as a backup if the response of the tipeeeSocketInfoUrl isn't updated
    @Value("${tipeeeSocketUrl}")
    public static void setTipeeeSocketUrl(String tipeeeSocketUrl) {
        TipeeeInput.tipeeeSocketUrl = tipeeeSocketUrl;
    }

    private static String tipeeeSocketUrl;

    @Value("${tipeeeApikey}")
    public void setApiKey(String apiKey) {
        TipeeeInput.apiKey = apiKey;
    }

    private static String apiKey;

    @Value("${tipeeeChannel}")
    public void setChannelName(String channelName) {
        TipeeeInput.channelName = channelName;
    }

    private static String channelName;

    private Socket socket;
    private InputStatus health;
    private static final Logger LOGGER = LoggerFactory.getLogger(TipeeeInput.class);

    @Override
    public void shutdown() {
        if (socket != null) {
            socket.close();
        }
        LOGGER.info("Shut down Tipeee input");
        report(InputStatus.STOPPED);
    }

    @Override
    public void run() {
        if (tipeeeSocketUrl == null || tipeeeSocketUrl.isEmpty() && apiKey == null || apiKey.isEmpty() && channelName == null || channelName.isEmpty()) {
            LOGGER.warn("TipeeeStream Module is disabled");
            report(InputStatus.STOPPED);
            return;
        }
        report(InputStatus.STARTING);
        LOGGER.info("Stating TipeeeInput for Channel {}", channelName);
        if (tipeeeSocketUrl == null || tipeeeSocketUrl.isEmpty()) {
            LOGGER.info("Getting current Tipeee Socket.io url...");
            tipeeeSocketUrl = STR."\{getSocketUrlFromUrl()}?access_token=";
        }

        URI socketUrl;
        try {
            socketUrl = new URI(tipeeeSocketUrl + apiKey);
        } catch (URISyntaxException e) {
            LOGGER.error("tipeeeSocketUrl is not a valid url", e);
            report(InputStatus.DEAD);
            throw new RuntimeException(e);
        }

        LOGGER.info("using Socket.io url: {}", socketUrl);
        socket = IO.socket(socketUrl);


        socket.on(Socket.EVENT_CONNECT_ERROR, objects -> {
            report(InputStatus.DEAD);
            if (objects.length == 1 && objects[0] instanceof EngineIOException && ((EngineIOException) objects[0]).getCause() instanceof IOException io) {
                if (io.getMessage().equals("403")) {
                    LOGGER.error("TipeeeStream Authentication failed, likely because the Apikey is invalid");
                    socket.disconnect();
                } else if (StringUtils.isNumeric(io.getMessage())) {
                    LOGGER.error("TipeeeStream Socket connection failed with (what likely is) Http Status code: {}", io.getMessage());
                    ((EngineIOException) objects[0]).printStackTrace();
                }
            } else {
                LOGGER.error("Failed to connect to tipeeeStream Websocket: {}", Arrays.toString(objects));
                throw new RuntimeException(Arrays.toString(objects));
            }
        });

        socket.on(Socket.EVENT_CONNECT, _ -> {
            socket.emit("join-room", STR."{ room: '\{apiKey}', username: '\{channelName}'}");
            report(InputStatus.HEALTHY);
        });

        socket.on("new-event", data -> TipeeeEventHandler.handleDonationEvent(Arrays.toString(data)));

        socket.connect();
        //Wait for the Socket to connect, because it will be opened in a new Thread and
        // so will otherwise not be done by the time we check if the starting has worked
        Instant end = Instant.now().plusSeconds(10);
        while (!socket.connected() || end.isBefore(Instant.now()) && !TwitchBot.requestedShutdown) Thread.onSpinWait();

        LOGGER.info("Tipeee socket connected");
    }

    private String getSocketUrlFromUrl() {
        try {
            var client = HttpClient.newBuilder().build();
            var request = HttpRequest.newBuilder(URI.create(tipeeeSocketInfoUrl)).GET().build();
            var httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            client.close();
            var d = new JSONObject(httpResponse.body()).getJSONObject("datas");
            return STR."\{d.getString("host")}:\{d.getString("port")}";
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error while requesting tipeeeStream socket url!", e);
            throw new RuntimeException("Error while requesting tipeeeStream socket url!", e);
        } catch (JSONException e) {
            LOGGER.error("TipeeeStream socket info Url responded with unknown format!", e);
            throw new RuntimeException("TipeeeStream socket info Url responded with unknown format!", e);
        }
    }

    @Override
    public InputStatus getHealth() {
        return health;
    }

    @Override
    public InputConfiguration getConfiguration() {
        return null;
    }

    @Override
    public String threadName() {
        return "TipeeeWebsocket";
    }

    private void report(InputStatus health) {
        HealthManager.reportStatus(this, health);
        this.health = health;
    }
}
