package talium;

import jakarta.annotation.PreDestroy;
import jakarta.persistence.PreRemove;
import talium.inputs.TipeeeStream.TipeeeInput;
import talium.inputs.Twitch4J.Twitch4JInput;
import talium.system.StopWatch;
import talium.system.inputSystem.BotInput;
import talium.system.inputSystem.HealthManager;
import talium.system.inputSystem.InputManager;
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

    public static void startup() {
        StopWatch time = new StopWatch(StopWatch.TYPE.STARTUP);
        SpringApplication.run(TwitchBot.class);
        System.out.println();
        System.out.println("DateFormat: DayNumber-Hour:Minute:Second:Millis");
        System.out.println("DDD-HH:mm:ss.SSS |LEVEL| [THREAD]        LOGGER (Source Class)               - MSG");
        System.out.println("-----------------|-----|-[-------------]---------------------------------------------------------------------------------------------------------------------------------------------");


        //TWITCH
        BotInput twitch = new Twitch4JInput();
        try {
            twitch.run();
            //TODO register config stuff
        } catch (RuntimeException e) {
            logger.error("Exception starting Input: Twitch because: {}",  e.getMessage());
            HealthManager.reportStatus(twitch, InputStatus.DEAD);
        }
        while (HealthManager.get(twitch).equals(InputStatus.STARTING)) {
            Thread.onSpinWait();
        }

        //TIPEEE
        BotInput tipeee = new TipeeeInput();
        try {
            tipeee.run();
            //TODO register config stuff
        } catch (RuntimeException e) {
            logger.error("Exception starting Input: Twitch because: {}",  e.getMessage());
            HealthManager.reportStatus(tipeee, InputStatus.DEAD);
        }
        while (HealthManager.get(tipeee).equals(InputStatus.STARTING)) {
            Thread.onSpinWait();
        }

        InputManager.startAllInputs();
        HealthManager.subscribeNextChange(_ -> time.close(), InputStatus.HEALTHY);
    }

    @PreDestroy
    @PreRemove
    public static void shutdown() {
        try {
            StopWatch time = new StopWatch(StopWatch.TYPE.SHUTDOWN);
            requestedShutdown = true;
            InputManager.stopAllInputs();
            //Because we stop all inputs in separate Threads,
            //(so that one broken shutdown does not stop the other once from shutting down)
            //we need to wait here until all Inputs are shut down, because otherwise it will
            //clean up all the Spring Objects, who may be needed in a shutdown routine
            while (InputManager.finishedShutdown()) {
                Thread.onSpinWait();
            }
            time.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
