package talium.inputSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HealthManager {

    public static class StatusDescription {
        InputStatus status;
        String identifier;
        String title;
        String description;

        public StatusDescription(InputStatus status, String identifier, String title, String description) {
            this.status = status;
            this.identifier = identifier;
            this.title = title;
            this.description = description;
        }
    }

    public record Customization(
            String title,
            String description
    ) {}

    private static final Logger logger = LoggerFactory.getLogger(HealthManager.class);
    private static final ArrayList<StatusDescription> STATUS_DESCRIPTIONS = new ArrayList<>();
    private static final HashMap<String, Customization> reporterCustomisation = new HashMap<>();
    private static volatile InputStatus inputStatus = InputStatus.STOPPED;

    public static void reportStatus(Class<?> self, InputStatus status) {
        reportStatus(self.getCanonicalName(), status);
    }

    public static void reportStatus(String self, InputStatus status) {
        var statusOptional = STATUS_DESCRIPTIONS.stream().filter(s -> s.identifier.equals(self)).findFirst();
        if (statusOptional.isEmpty()) {
            var customization = reporterCustomisation.getOrDefault(self, new Customization(self, ""));
            StatusDescription e = new StatusDescription(status, self, customization.title, customization.description);
            synchronized (STATUS_DESCRIPTIONS) {
                STATUS_DESCRIPTIONS.add(e);
            }
        } else {
            statusOptional.get().status = status;
        }
        checkOverallStatusChange();
    }

    public static void addCustomization(String self, String title, String description) {
        reporterCustomisation.put(self, new Customization(title, description));
    }

    private static void checkOverallStatusChange() {
        var worstFound = calcOverallStatus();
        if (inputStatus != worstFound) {
            inputStatus = worstFound;
            logger.info("INPUTS: " + inputStatus);
        }
    }

    private static InputStatus calcOverallStatus() {
        InputStatus worstYet = InputStatus.STOPPED;
        ArrayList<StatusDescription> copyied;
        synchronized (STATUS_DESCRIPTIONS) {
            copyied = (ArrayList<StatusDescription>) STATUS_DESCRIPTIONS.clone();
        }
        for (StatusDescription status : copyied) {
            if (status.status.compareTo(worstYet) > 0) {
                worstYet = status.status;
            }
        }
        return worstYet;
    }

    public record StringStatus(String name, InputStatus status) {
    }

    public static List<StringStatus> allStatuses() {
        return STATUS_DESCRIPTIONS.stream().map(status -> new StringStatus(status.title, status.status)).toList();
    }

    public static InputStatus get(Class<?> identifier) {
        return get(identifier.getCanonicalName());
    }

    public static InputStatus get(String identifier) {
        return STATUS_DESCRIPTIONS.stream().filter(s -> s.identifier.equals(identifier)).findFirst().get().status;
    }
}
