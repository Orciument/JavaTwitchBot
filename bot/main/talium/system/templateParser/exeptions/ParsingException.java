package talium.system.templateParser.exeptions;

public class ParsingException extends StringTemplateException {
    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingException() {
    }

}
