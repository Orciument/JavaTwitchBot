package talium.inputs.TipeeeStream;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.EngineIOException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.util.UriComponentsBuilder;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Arrays;

public class TipeeeInput implements BotInput {

    public static class TipeeeStreamException extends Exception {
        public TipeeeStreamException(String message) {
            super(message);
        }

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
        public SocketInfoEndpointException(String message) {
            super(message);
        }

        public SocketInfoEndpointException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TipeeeInput.class);
    private Socket socket;
    private final TipeeeConfig config;

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

        URI socketUrl;
        try {
        if (config.hasSocketUrl()) {
            socketUrl = URI.create(config.socketUrl().get());
        } else {
            LOGGER.info("Getting current Tipeee Socket.io url...");

            var info_response = getSocketUrlFromUrl(config.tipeeeSocketInfoUrl());
            socketUrl = parseSocketInfoUrlResponse(info_response, config.apiKey());
        }
        } catch (SocketInfoEndpointException | IllegalArgumentException e) {
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

    static String getSocketUrlFromUrl(String socketInfoUrl) throws SocketInfoEndpointException {
        try {
            var client = HttpClient.newBuilder().build();
            var request = HttpRequest.newBuilder(URI.create(socketInfoUrl)).GET().build();
            var httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() != 200) {
                throw new SocketInfoEndpointException("Info endpoint responded with response code: " + httpResponse.statusCode());
            }
            client.close();
            return httpResponse.body();
        } catch (IOException | InterruptedException e) {
            throw new SocketInfoEndpointException("Error while requesting tipeeeStream socket url!", e);
        }
    }

    static @NotNull URI parseSocketInfoUrlResponse(String body, String apiKey) throws SocketInfoEndpointException {
        try {
            var datas = new JSONObject(body).getJSONObject("datas");
            return UriComponentsBuilder.fromHttpUrl(datas.getString("host"))
                    .queryParam("access_token", apiKey)
                    .build().toUri();
        } catch (JSONException e) {
            throw new SocketInfoEndpointException("TipeeeStream socket info Url responded with unknown format!", e);
        } catch (IllegalArgumentException e) {
            throw new SocketInfoEndpointException("Returned Host ist not a valid URL!", e);
        }
    }
}