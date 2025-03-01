package talium.templateParser.statements;

import talium.templateParser.exeptions.TemplateSyntaxException;

import java.util.Objects;
import java.util.regex.Pattern;

public final class VarStatement implements Statement {

    // Matches for valid field access expressions. (https://www.w3schools.com/java/java_variables_identifiers.asp)
    // matches correct variable name first, and then any number of field accesses (with are . followed by the variable name check)
    private static final Pattern syntax = Pattern.compile("^[\\w_$]\\w*(?:\\.[\\w_$]\\w*)*$");
    private final String accessExpr;

    private VarStatement(String accessExpr) {
        this.accessExpr = accessExpr;
    }

    public static VarStatement create(String expression) throws TemplateSyntaxException {
        if (!syntax.matcher(expression).matches()) {
            throw new TemplateSyntaxException(expression + " is not a correct field access expression!");
        }
        return new VarStatement(expression);
    }

    public String accessExpr() {
        return accessExpr;
    }

    @Override
    public String toString() {
        return "VarStatement(" + accessExpr + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VarStatement that)) return false;

        return Objects.equals(accessExpr, that.accessExpr);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(accessExpr);
    }
}