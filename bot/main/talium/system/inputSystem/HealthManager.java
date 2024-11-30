package talium.system.inputSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HealthManager {

    public static class Status {
        InputStatus status;
        BotInput input;

        public Status(InputStatus status, BotInput input) {
            this.status = status;
            this.input = input;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(HealthManager.class);
    private static final ArrayList<Status> statuses = new ArrayList<>();
    private static volatile InputStatus inputStatus = InputStatus.STOPPED;
    private static volatile Consumer<InputStatus> callback;
    private static volatile InputStatus filter;

    public static void subscribeNextChange(Consumer<InputStatus> callback, InputStatus filterBy) {
        HealthManager.callback = callback;
        filter = filterBy;
    }

    private static void checkCallback() {
        if (filter != inputStatus) {
            return;
        }
        if (callback != null) {
            callback.accept(inputStatus);
        }
        callback = null;
    }

    public static void reportStatus(BotInput input, InputStatus status) {
        var statusOptional = statuses.stream().filter(s -> s.input == input).findFirst();
        if (statusOptional.isEmpty()) {
            synchronized (statuses) {
                statuses.add(new Status(status, input));
            }
        } else {
            statusOptional.get().status = status;
        }
        checkOverallStatusChange();
        checkCallback();
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
        ArrayList<Status> copyied;
        synchronized (statuses) {
            copyied = (ArrayList<Status>) statuses.clone();
        }
        for (Status status : copyied) {
            if (status.status.compareTo(worstYet) > 0) {
                worstYet = status.status;
            }
        }
        return worstYet;
    }

    public static InputStatus inputStatus() {
        return inputStatus;
    }

    public record StringStatus(String name, InputStatus status) {}

    public static List<StringStatus> allStatuses() {
        return statuses.stream().map(status -> new StringStatus(status.input.threadName(), status.status)).toList();
    }
}
