package talium.templateParser.tokens;

/**
 * Token for parsing the If Statements head, used in the dedicated if parser pass.
 * @param kind what kind of token this is
 * @param value string value of the token
 */
public record IfToken(IfTokenKind kind, String value) {}
