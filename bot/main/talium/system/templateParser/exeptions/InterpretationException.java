package talium.system.templateParser.exeptions;

public class InterpretationException extends StringTemplateException {
    public InterpretationException(String message) {
        super(message);
    }

    public InterpretationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterpretationException(Throwable cause) {
        super(cause);
    }

    public InterpretationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InterpretationException() {
    }

}
