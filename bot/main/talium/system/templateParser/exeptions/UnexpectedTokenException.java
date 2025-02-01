package talium.system.templateParser.exeptions;

public class UnexpectedTokenException extends TemplateSyntaxException {
    public UnexpectedTokenException(String expected, String actual, int atIndex, String source) {
        super(expected, actual, atIndex, source);
    }

    public UnexpectedTokenException(Character expected, String actual, int atIndex, String source) {
        super(String.valueOf(expected), actual, atIndex, source);
    }

    public UnexpectedTokenException(Character expected, Character actual, int atIndex, String source) {
        super(expected, actual, atIndex, source);
    }

}
