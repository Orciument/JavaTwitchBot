package talium.inputs.TipeeeStream;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.EngineIOException;
import org.apache.commons.lang.StringUtils;
import talium.Registrar;
import talium.TwitchBot;
import talium.system.inputSystem.BotInput;
import talium.system.inputSystem.HealthManager;
import talium.system.inputSystem.InputStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Arrays;

public class TipeeeInput implements BotInput {
    private static final Logger LOGGER = LoggerFactory.getLogger(TipeeeInput.class);
    private Socket socket;
    private TipeeeConfig config;

    public TipeeeInput(TipeeeConfig config) {
        this.config = config;
    }

    @Override
    public void shutdown() {
        if (socket != null) {
            socket.close();
        }
        LOGGER.info("Shut down Tipeee input");
        HealthManager.reportStatus(TipeeeInput.class, InputStatus.STOPPED);
    }

    @Override
    public void run() {
        //TODO split all run() content into other function, that function thorws instead of logging, catch all and just log in run()
        // that way we can test the throwing behaviour of this class
        //TODO correction, this function should be renamed to "start"/startup, it is allowed to throw, that is caught by
        // TwitchBot.startInput and already handled well. But when creating a new thread in here,
        // the inside of the thread should have it's own error boundary, and log all errors
        Registrar.registerHealthDescription(TipeeeInput.class, "TipeeeStreamInput", "");
        if (config.isDisabled()) {
            LOGGER.warn("TipeeeStream Module is disabled");
            HealthManager.reportStatus(TipeeeInput.class, InputStatus.STOPPED);
            return;
        }
        HealthManager.reportStatus(TipeeeInput.class, InputStatus.STARTING);
        LOGGER.info("Stating TipeeeInput for Channel {}", config.channelName());
        if (!config.hasSocketUrl()) {
            LOGGER.info("Getting current Tipeee Socket.io url...");
            config = new TipeeeConfig(config, STR."\{getSocketUrlFromUrl(config.tipeeeSocketInfoUrl())}?access_token=");
        }

        URI socketUrl;
        try {
            socketUrl = new URI(config.socketUrl().get() + config.apiKey());
        } catch (URISyntaxException e) {
            LOGGER.error("tipeeeSocketUrl is not a valid url", e);
            HealthManager.reportStatus(TipeeeInput.class, InputStatus.DEAD);
            throw new RuntimeException(e);
        }

        LOGGER.info("using Socket.io url: {}", socketUrl);
        socket = IO.socket(socketUrl);


        socket.on(Socket.EVENT_CONNECT_ERROR, objects -> {
            HealthManager.reportStatus(TipeeeInput.class, InputStatus.DEAD);
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
            socket.emit("join-room", STR."{ room: '\{config.apiKey()}', username: '\{config.channelName()}'}");
            HealthManager.reportStatus(TipeeeInput.class, InputStatus.HEALTHY);
        });

        socket.on("new-event", data -> TipeeeEventHandler.handleDonationEvent(Arrays.toString(data)));

        // creates a new thread that receives the events and triggers our listeners
        socket.connect();
        //Wait for the Socket to connect, because it will be opened in a new Thread and
        // so will otherwise not be done by the time we check if the starting has worked
        Instant end = Instant.now().plusSeconds(10);
        while (!socket.connected() || end.isBefore(Instant.now()) && !TwitchBot.requestedShutdown) Thread.onSpinWait();

        LOGGER.info("Tipeee socket connected");
    }

    private String getSocketUrlFromUrl(String socketInfoUrl) {
        try {
            var client = HttpClient.newBuilder().build();
            var request = HttpRequest.newBuilder(URI.create(socketInfoUrl)).GET().build();
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

}