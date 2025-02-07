package talium.templateParser.exeptions;

public class TemplateSyntaxException extends ParsingException {
    public TemplateSyntaxException(String expected, String actual, int atIndex, String source) {
        super(STR."Excepted: '\{expected}' but found '\{actual}', at Index: \{atIndex} in template \"\{source}\"");
    }

    public TemplateSyntaxException(Character expected, Character actual, int atIndex, String source) {
        super(STR."Excepted: '\{expected}' but found '\{actual}', at Index: \{atIndex} in template \"\{source}\"");
    }

    public TemplateSyntaxException(String message) {
        super(message);
    }

    public TemplateSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
