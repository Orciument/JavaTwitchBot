package talium.system.templateParser.ifParser;

import talium.system.templateParser.exeptions.ImpossibleComparisonException;
import talium.system.templateParser.statements.Equals;
import talium.system.templateParser.tokens.Comparison;

import java.util.Objects;

/**
 * interprets a parsed and populated if expression into a boolean value
 */
public class IfInterpreter {

    /**
     * interprets a parsed and populated if expression into a boolean value.
     * Allowed types for comparands are: <br>
     * - Booleans <br>
     * - Strings <br>
     * - chars <br>
     * - VarStatement <br>
     * - things that extend Number (like, Int, Float, Long, Double, ...) <br>
     * All other Types are currently not handled and will cause the comparison to throw an exception
     * <br>
     * <br>
     * All ints and floats are cast into longs and floats respectively.
     * @param comp prepared comparison
     * @return boolean result
     */
    public static boolean compare(Comparison comp) throws ImpossibleComparisonException {
        Object l = comp.left();
        Object r = comp.right();
        if (l instanceof String || l instanceof Character && r instanceof String || r instanceof Character) {
            l = l.toString();
            r = r.toString();
            return switch (comp.equals()) {
                case EQUALS -> l.equals(r);
                case NOT_EQUALS -> !l.equals(r);
                case LESS_THAN, LESS_THAN_OR_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS ->
                        throw new ImpossibleComparisonException(STR."\{comp.equals().name()} is not a supported comparison operation between Strings/Charactern");
            };
        } else if (l instanceof Boolean && r instanceof Boolean) {
            return switch (comp.equals()) {
                case EQUALS -> l.equals(r);
                case NOT_EQUALS -> !l.equals(r);
                case LESS_THAN, LESS_THAN_OR_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS ->
                        throw new ImpossibleComparisonException(STR."\{comp.equals().name()} is not a supported comparison operation between booleans");
            };
        } else if ((l instanceof Double || l instanceof Float) && (r instanceof Double || r instanceof Float)) {
            Double ld = ((Number) l).doubleValue();
            Double rd = ((Number) r).doubleValue();
            return compareDoubles(ld, comp.equals(), rd);
        } else if ((l instanceof Integer || l instanceof Long) && (r instanceof Integer || r instanceof Long)) {
            Long ll = ((Number) l).longValue();
            Long rl = ((Number) r).longValue();
            return compareLongs(ll, comp.equals(), rl);
        } else {
            throw new ImpossibleComparisonException(STR."Types \{l.getClass()} and \{r.getClass()} are not comparable to each other");
        }
    }

    private static boolean compareLongs(Long l, Equals equals, Long r) {
        return switch (equals) {
            case EQUALS -> Objects.equals(l, r);
            case NOT_EQUALS -> !Objects.equals(l, r);
            case LESS_THAN -> l < r;
            case LESS_THAN_OR_EQUALS -> l <= r;
            case GREATER_THAN -> l > r;
            case GREATER_THAN_OR_EQUALS -> l >= r;
        };
    }

    private static boolean compareDoubles(Double l, Equals equals, Double r) {
        return switch (equals) {
            case EQUALS -> Objects.equals(l, r);
            case NOT_EQUALS -> !Objects.equals(l, r);
            case LESS_THAN -> l < r;
            case LESS_THAN_OR_EQUALS -> l <= r;
            case GREATER_THAN -> l > r;
            case GREATER_THAN_OR_EQUALS -> l >= r;
        };
    }
}
