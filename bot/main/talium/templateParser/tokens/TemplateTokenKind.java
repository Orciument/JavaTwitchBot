package talium.templateParser.tokens;

/**
 * Major (primary) token type, indicates what type of major token was encountered.
 * The entire head section of if and for statements are combined into one token type because they will be parsed later.
 */
public enum TemplateTokenKind {
    TEXT,
    VAR,
    FOR_HEAD,
    FOR_END,
    IF_HEAD,
    IF_ELSE,
    IF_END,
}
