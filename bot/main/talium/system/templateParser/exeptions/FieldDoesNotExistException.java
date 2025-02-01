package talium.system.templateParser.exeptions;

import java.util.Arrays;
import java.util.Set;

public class FieldDoesNotExistException extends RuntimeException {
    public FieldDoesNotExistException(String name, Set<String> envNames) {
        super("No Field with name " + name + " exists in environment. Env: " + Arrays.toString(envNames.toArray()));
    }
    public FieldDoesNotExistException(String name, Class<?> onClass) {
        super("No Field with name " + name + " exists on class " + onClass);
    }
}
