package talium.templateParser.tokens;

import talium.templateParser.statements.Equals;

/**
 * Represents a Comparison in an if statements head.
 * <p>
 * Used in both the if-head parsing step, and the population and interpretation step.
 * comparands are of type Object because in the parsing state all types of tokens are allowed, but later for interpretation the only allowed types are:
 * <br>
 * - Booleans <br>
 * - Strings <br>
 * - chars <br>
 * - VarStatement <br>
 * - things that extend Number (like, Int, Float, Long, Double, ...) <br>
 * All other Types are currently not handled and will cause the comparison to throw an exception
 *
 * @see Number
 * @see talium.templateParser.statements.VarStatement
 * @see Equals
 * @param left comparand
 * @param equals operator
 * @param right comparands
 */
public record Comparison(Object left, Equals equals, Object right) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comparison that)) return false;

        return left.equals(that.left) && right.equals(that.right) && equals == that.equals;
    }

    @Override
    public int hashCode() {
        int result = left.hashCode();
        result = 31 * result + equals.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }
}
