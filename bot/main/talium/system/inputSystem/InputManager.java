package talium.system.inputSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import talium.system.ASCIIProgressbar;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import talium.system.stringTemplates.TemplateService;
import talium.system.twitchCommands.triggerEngine.TriggerProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Component
public class InputManager {

    private static final Logger logger = LoggerFactory.getLogger(InputManager.class);
    private static final List<BotInput> inputs = new ArrayList<>();
    private static boolean startupFinished = false;
    private static TemplateService templateService;

    @Autowired
    public InputManager(TemplateService templateService) {
        InputManager.templateService = templateService;
    }

    public static void startAllInputs() {
        var scannedInputs = scanForInputs().stream().toList();
        logger.info("Starting Inputs...");
        for (BotInput input : scannedInputs) {
            startInput(input);
        }
        TriggerProvider.rebuildTriggerCache();
        startupFinished = true;
    }

    public static void startInput(BotInput input) {
        try {
//            new Thread(input, "LAUNCHER-" + input.threadName()).start();
            inputs.add(input);
        } catch (RuntimeException e) {
            logger.error("Exception starting Input: {} because: {}", input.getClass().getName(), e.getMessage());
            HealthManager.reportStatus(input, InputStatus.DEAD);
        }
        try {
            var conf = input.getConfiguration();
            if (conf == null) {
                return;
            }
            TriggerProvider.addCommandsFromCodeConfig(conf.commands());
            templateService.saveIfAbsent(conf.templates());
        } catch (RuntimeException e) {
            logger.error("Exception while setting Input Configuration: {} because: {}", input.getClass().getName(), e.getMessage());
            HealthManager.reportStatus(input, InputStatus.INJURED);
        }
    }

    public static void stopAllInputs() {
        logger.info("Stopping Inputs...");
        for (BotInput input : inputs) {
            stopInput(input);
        }
    }

    public static void stopInput(BotInput input) {
        new Thread(() -> {
            try {
                input.shutdown();
                HealthManager.reportStatus(input, InputStatus.STOPPED);
            } catch (Exception e) {
                logger.error("Exception stopping Input: {} because: {}", input.getClass().getName(), e.getMessage());
                HealthManager.reportStatus(input, InputStatus.DEAD);
            }
            inputs.remove(input);
        }, "CLOSER-" + input.threadName()).start();
    }

    public static boolean finishedShutdown() {
        return !inputs.isEmpty();
    }

    private static HashSet<BotInput> scanForInputs() {
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages("talium.inputs").addScanners(Scanners.TypesAnnotated));
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Input.class);
        HashSet<BotInput> inputSet = new HashSet<>();

        //get all Classes Annotated with Input.class, and if they implement the TwitchBotInput interface,
        //then a new instance will be created, and get added to the Set of Inputs
        for (Class<?> aClass : typesAnnotatedWith) {
            if (Arrays.stream(aClass.getInterfaces()).toList().contains(BotInput.class)) {
                //Try to create a new instance of that class
                try {
                    inputSet.add((BotInput) (aClass.getConstructor().newInstance()));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        logger.info("Scanned for Inputs, found: {}, configured correctly: {}", typesAnnotatedWith.size(), ASCIIProgressbar.consoleBar(inputSet.size(), typesAnnotatedWith.size()));
        return inputSet;
    }
}
