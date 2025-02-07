package talium.templateParser.tokens;

/**
 * Major (primary) token Object, used for first parser pass.
 * This object only stores the entire heads of if and for blocks for further processing, these sections are parsed later.
 * @param kind what kind of token this is
 * @param value string value of the token
 */
public record TemplateToken(TemplateTokenKind kind, String value) {

    public static TemplateToken text(String value) {
        return new TemplateToken(TemplateTokenKind.TEXT, value);
    }

    public static TemplateToken var(String value) {
        return new TemplateToken(TemplateTokenKind.VAR, value);
    }

    public static TemplateToken if_head(String value) {
        return new TemplateToken(TemplateTokenKind.IF_HEAD, value);
    }

    public static TemplateToken if_else() {
        return new TemplateToken(TemplateTokenKind.IF_ELSE, "%{ else }");
    }

    public static TemplateToken endif() {
        return new TemplateToken(TemplateTokenKind.IF_END, "%{ endif }");
    }

    public static TemplateToken for_head(String value) {
        return new TemplateToken(TemplateTokenKind.FOR_HEAD, value);
    }

    public static TemplateToken for_end() {
        return new TemplateToken(TemplateTokenKind.FOR_END, "%{ endfor }");
    }

    @Override
    public String toString() {
        return STR."\{kind}(\{value})";
    }
}