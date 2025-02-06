package talium;

import jakarta.annotation.PreDestroy;
import jakarta.persistence.PreRemove;
import org.springframework.context.ConfigurableApplicationContext;
import talium.inputs.TipeeeStream.DonationRepo;
import talium.inputs.TipeeeStream.TipeeeConfig;
import talium.inputs.TipeeeStream.TipeeeInput;
import talium.inputs.Twitch4J.Twitch4JInput;
import talium.inputs.Twitch4J.TwitchConfig;
import talium.system.StopWatch;
import talium.system.coinsWatchtime.WIPWatchtimeCommandServer;
import talium.system.coinsWatchtime.WatchtimeUpdateService;
import talium.system.inputSystem.BotInput;
import talium.system.inputSystem.HealthManager;
import talium.system.inputSystem.InputStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import talium.system.twitchCommands.triggerEngine.TriggerProvider;

@SpringBootApplication
@EnableJpaRepositories
public class TwitchBot {

    public static boolean requestedShutdown = false;
    private static final Logger logger = LoggerFactory.getLogger(TwitchBot.class);

    public static void main(String[] args) {
        startup();
    }

    private static ConfigurableApplicationContext ctx;
    private static BotInput twitch;
    private static BotInput tipeee;

    public static void startup() {
        StopWatch time = new StopWatch(StopWatch.TYPE.STARTUP);
        ctx = SpringApplication.run(TwitchBot.class);
        System.out.println();
        System.out.println("DateFormat: DayNumber-Hour:Minute:Second:Millis");
        System.out.println("DDD-HH:mm:ss.SSS |LEVEL| [THREAD]        LOGGER (Source Class)               - MSG");
        System.out.println("-----------------|-----|-[-------------]---------------------------------------------------------------------------------------------------------------------------------------------");
        var serverPort = ctx.getEnvironment().getProperty("server.port");
        logger.info("Server started on Port: {}", serverPort);

        twitch = startInput(new Twitch4JInput(
                ctx.getBean(TwitchConfig.class)
        ));
        tipeee = startInput(new TipeeeInput(
                ctx.getBean(TipeeeConfig.class),
                ctx.getBean(DonationRepo.class))
        );

        logger.info("Inputs started, initializing other system components...");
        // This section is used to pass the execution/control to different parts of the bot to do some initialisation
        WatchtimeUpdateService.init();
        WIPWatchtimeCommandServer.init();

        TriggerProvider.rebuildTriggerCache();
        //TODO remove all templates that were once registered automatically, but are no longer

        time.close();
    }

    @PreDestroy
    @PreRemove
    public static void shutdown() {
        StopWatch time = new StopWatch(StopWatch.TYPE.SHUTDOWN);
        requestedShutdown = true;
        stopInput(twitch);
        stopInput(tipeee);
        time.close();
    }

    private static BotInput startInput(BotInput input) {
        try {
            input.startup();
        } catch (Exception e) {
            logger.error("Exception starting Input: {} because: {}", input.getClass().getCanonicalName(), e.getMessage());
            HealthManager.reportStatus(input.getClass(), InputStatus.DEAD);
        }
        return input;
    }

    private static void stopInput(BotInput input) {
        try {
            input.shutdown();
        } catch (Exception e) {
            logger.error("Exception stopping Input: {} because: {}", input.getClass().getCanonicalName(), e.getMessage());
            HealthManager.reportStatus(input.getClass(), InputStatus.DEAD);
        }
    }

    public static boolean reconnectTwitch() {
        twitch.shutdown();
        var twitch = new Twitch4JInput(
                ctx.getBean(TwitchConfig.class)
        );
        try {
            twitch.startup();
            TwitchBot.twitch = twitch;
            return true;
        } catch (Exception e) {
            TwitchBot.twitch = twitch;
            return false;
        }
    }
}
