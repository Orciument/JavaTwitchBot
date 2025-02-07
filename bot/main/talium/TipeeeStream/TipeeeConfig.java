package talium.TipeeeStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public record TipeeeConfig(
        // we can set this manually as a backup if the response of the tipeeeSocketInfoUrl isn't updated
        Optional<String> socketUrl,
        String apiKey,
        String channelName,
        String tipeeeSocketInfoUrl
) {

    @Autowired
    public TipeeeConfig(@Value("${tipeeeSocketUrl:}") Optional<String> socketUrl,
                        @Value("${tipeeeApikey}") String apiKey,
                        @Value("${tipeeeChannel}") String channelName) {
        this(socketUrl, apiKey, channelName, "https://api.tipeeestream.com/v2.0/site/socket");
    }

    public TipeeeConfig(TipeeeConfig defaults, String socketUrl) {
        this(defaults.socketUrl, socketUrl, defaults.apiKey, defaults.channelName);
    }

    boolean hasSocketUrl() {
        return socketUrl.isPresent() && !socketUrl.get().isEmpty();
    }

    boolean hasApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }

    boolean hasChannelName() {
        return channelName != null && !channelName.isEmpty();
    }

    boolean hasTipeeeSocketInfoUrl() {
        return tipeeeSocketInfoUrl != null && !tipeeeSocketInfoUrl.isEmpty();
    }

    boolean isDisabled() {
        return !hasApiKey() && !hasChannelName();
    }
}
