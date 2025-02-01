package talium.system.templateParser.exeptions;

/**
 * Comparison Operator (==, >, >=, ...) not supported
 */
public class UnsupportedComparisonOperator extends ParsingException {
    public UnsupportedComparisonOperator(String s) {
        super(s);
    }
}
