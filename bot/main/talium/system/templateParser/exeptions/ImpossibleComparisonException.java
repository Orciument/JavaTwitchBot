package talium.system.templateParser.exeptions;

public class ImpossibleComparisonException extends InterpretationException {
    public ImpossibleComparisonException(String message) {
        super(message);
    }

    public ImpossibleComparisonException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImpossibleComparisonException(Throwable cause) {
        super(cause);
    }

    public ImpossibleComparisonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ImpossibleComparisonException() {
    }

}
