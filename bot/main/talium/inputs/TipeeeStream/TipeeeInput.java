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

    public static class TipeeeStreamException extends Exception {
        public TipeeeStreamException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public static class NoValidSocketUrl extends TipeeeStreamException {
        public NoValidSocketUrl(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public static class SocketInfoEndpointException extends TipeeeStreamException {
        public SocketInfoEndpointException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TipeeeInput.class);
    private Socket socket;
    private TipeeeConfig config;

    public TipeeeInput(TipeeeConfig config) {
        this.config = config;
    }

    public void shutdown() {
        if (socket != null) {
            socket.close();
        }
        LOGGER.info("Shut down Tipeee input");
        HealthManager.reportStatus(TipeeeInput.class, InputStatus.STOPPED);
    }

    @Override
    public void startup() throws TipeeeStreamException {
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
            try {
                String socketUrlFromUrl = getSocketUrlFromUrl(config.tipeeeSocketInfoUrl());
                config = new TipeeeConfig(config, STR."\{socketUrlFromUrl}?access_token=");
            } catch (SocketInfoEndpointException e) {
                throw new NoValidSocketUrl("Could not get up-to-date socket url from url Info endpoint", e);
            }
        }

        URI socketUrl;
        try {
            socketUrl = new URI(config.socketUrl().get() + config.apiKey());
        } catch (URISyntaxException e) {
            HealthManager.reportStatus(TipeeeInput.class, InputStatus.DEAD);
            throw new NoValidSocketUrl("Could not build valid socket Url: ", e);
        }

        LOGGER.info("using Socket.io url: {}", socketUrl);
        socket = IO.socket(socketUrl);

        socket.on(Socket.EVENT_CONNECT_ERROR, objects -> {
            HealthManager.reportStatus(TipeeeInput.class, InputStatus.DEAD);
            if (objects.length == 1 && objects[0] instanceof EngineIOException && ((EngineIOException) objects[0]).getCause() instanceof IOException io) {
                if (io.getMessage().equals("403")) {
                    LOGGER.error("TipeeeStream Authentication failed, likely because the Apikey is invalid");
                } else if (StringUtils.isNumeric(io.getMessage())) {
                    LOGGER.error("TipeeeStream Socket connection failed with (what likely is) Http Status code: {}", io.getMessage());
                    ((EngineIOException) objects[0]).printStackTrace();
                }
            } else {
                LOGGER.error("Failed to connect to tipeeeStream Websocket for unknown reason: {}", Arrays.toString(objects));
            }
            HealthManager.reportStatus(TipeeeInput.class, InputStatus.DEAD);
            socket.disconnect();
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

    private String getSocketUrlFromUrl(String socketInfoUrl) throws SocketInfoEndpointException {
        try {
            var client = HttpClient.newBuilder().build();
            var request = HttpRequest.newBuilder(URI.create(socketInfoUrl)).GET().build();
            var httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            client.close();
            var d = new JSONObject(httpResponse.body()).getJSONObject("datas");
            return STR."\{d.getString("host")}:\{d.getString("port")}";
        } catch (IOException | InterruptedException e) {
            throw new SocketInfoEndpointException("Error while requesting tipeeeStream socket url!", e);
        } catch (JSONException e) {
            throw new SocketInfoEndpointException("TipeeeStream socket info Url responded with unknown format!", e);
        }
    }

}