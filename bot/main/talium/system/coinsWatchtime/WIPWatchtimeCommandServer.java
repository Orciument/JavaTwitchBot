package talium.system.coinsWatchtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import talium.Registrar;
import talium.inputs.Twitch4J.ChatMessage;
import talium.inputs.Twitch4J.TwitchApi;
import talium.system.Out;
import talium.system.coinsWatchtime.chatter.Chatter;
import talium.system.coinsWatchtime.chatter.ChatterService;

import java.util.HashMap;

/**
 * Serves all commands related to watchtime and coins
 */
@Component
public class WIPWatchtimeCommandServer {
    private static final Logger logger = LoggerFactory.getLogger(WIPWatchtimeCommandServer.class);
    private static ChatterService chatterService;

    @Autowired
    public void setChatterService(ChatterService chatterService) {
        WIPWatchtimeCommandServer.chatterService = chatterService;
    }

    public static void init() {
        new Registrar.Command("watchtime.getwatchtime", "!watchtime").registerActionCommand(WIPWatchtimeCommandServer::triggerGetWatchtime);
        new Registrar.Command("watchtime.getCoins", "!coins").registerActionCommand(WIPWatchtimeCommandServer::triggerGetCoins);
        Registrar.registerTemplate("coins.coins", "${wt.username} has ${wt.coins} Coins!");
        Registrar.registerTemplate("coins.watchtime", "${wt.username} has ${wt.daysRounded2} Days of watchtime!");
    }

    static class WatchtimeContext {
        String username;
        String coins;
        String watchtimeSeconds;
        String watchtimeHoursRounded;
        String watchtimeHoursRounded2;
        String daysRounded;
        String daysRounded2;

        public WatchtimeContext(Chatter chatter, String username) {
            this.username = username;
            this.coins = String.valueOf(chatter.coins);
            this.watchtimeSeconds = String.valueOf(chatter.watchtimeSeconds);
            this.watchtimeHoursRounded = String.valueOf(Math.round(chatter.watchtimeSeconds / 3600f));
            this.watchtimeHoursRounded2 = String.valueOf(Math.round((chatter.watchtimeSeconds * 100) / 3600f) / 100f);
            this.daysRounded = String.valueOf(Math.round(chatter.watchtimeSeconds / 86400f));
            this.daysRounded2 = String.valueOf(Math.round((chatter.watchtimeSeconds * 100) / 86400f) / 100f);
        }
    }

    public static void triggerGetWatchtime(String triggerId, ChatMessage message) {
        var values = new HashMap<String, Object>();
        var userId = message.user().id();
        var twitchUser = TwitchApi.getUserById(userId);
        if (twitchUser.isEmpty()) {
            logger.warn("Could not get watchtime, no twitch user found for Id: {}", userId);
            return;
        }
        var wt = chatterService.getDataForChatter(userId);
        values.put("wt", new WatchtimeContext(wt, twitchUser.get().getDisplayName()));
        Out.Twitch.sendNamedTemplate("coins.watchtime", values);
    }

    public static void triggerGetCoins(String triggerId, ChatMessage message) {
        var values = new HashMap<String, Object>();
        var userId = message.user().id();
        var twitchUser = TwitchApi.getUserById(userId);
        if (twitchUser.isEmpty()) {
            logger.warn("Could not get watchtime, no twitch user found for Id: {}", userId);
            return;
        }
        var wt = chatterService.getDataForChatter(userId);
        values.put("wt", new WatchtimeContext(wt, twitchUser.get().getDisplayName()));
        Out.Twitch.sendNamedTemplate("coins.coins", values);
    }
}
