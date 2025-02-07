package talium.templateParser.exeptions;

public class StringTemplateException extends Exception {
    public StringTemplateException(String message) {
        super(message);
    }

    public StringTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public StringTemplateException(Throwable cause) {
        super(cause);
    }

    public StringTemplateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public StringTemplateException() {
    }
}
