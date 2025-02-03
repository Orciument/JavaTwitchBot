package talium.system.templateParser.exeptions;

public class InterpretationException extends StringTemplateException {
    public InterpretationException(String message) {
        super(message);
    }

    public InterpretationException(String message, Throwable cause) {
        super(message, cause);
    }

}
