package talium.inputs.Twitch4J;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record TwitchConfig(
        String channelName,
        String chatAccountName,
        String sendTo,
        String app_clientID,
        String app_clientSecret
) {
    @Autowired
    public TwitchConfig(
            @Value("${twitchChannelName}") String channelName,
            @Value("${twitchBotAccountName:}") String chatAccountName,
            @Value("${twitchOutputToChannel:}") String sendTo,
            @Value("${twitchAppId}") String app_clientID,
            @Value("${twitchAppSecret}") String app_clientSecret
    ) {
        this.channelName = channelName;
        this.chatAccountName = chatAccountName;
        this.sendTo = sendTo;
        this.app_clientID = app_clientID;
        this.app_clientSecret = app_clientSecret;
    }

    boolean hasChannelName() {
        return channelName != null && !channelName.isEmpty();
    }

    boolean hasChatAccountName() {
        return chatAccountName != null && !chatAccountName.isEmpty();
    }

    boolean hasSendTo() {
        return sendTo != null && !sendTo.isEmpty();
    }

    boolean hasAppClientID() {
        return app_clientID != null && !app_clientID.isEmpty();
    }

    boolean hasAppClientSecret() {
        return app_clientSecret != null && !app_clientSecret.isEmpty();
    }

    TwitchConfig setAccountName(String chatAccountName) {
        return new TwitchConfig(channelName, chatAccountName, sendTo, app_clientID, app_clientSecret);

    }
    TwitchConfig setSendTo(String sendTo) {
        return new TwitchConfig(channelName, chatAccountName, sendTo, app_clientID, app_clientSecret);
    }
}
