package talium.templateParser.tokens;

/**
 * Indicates the encountered token type while parsing the IF statements head.
 * COMPARISON stands for the actual comparison operator
 */
public enum IfTokenKind {
    STRING,
    INT,
    DOUBLE,
    BOOLEAN,
    VAR,
    COMPARISON,
}
