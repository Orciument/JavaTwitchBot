package talium.system.templateParser.exeptions;

public class FieldNotAccessibleException extends InterpretationException {
    public FieldNotAccessibleException(String fieldName, Class<?> onClass, Throwable cause) {
        super("Field " + fieldName + " on + " + onClass.getCanonicalName() + " is not accessible. Consider making the class and member fields public", cause);
    }
}
