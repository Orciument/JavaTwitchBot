package talium.system.templateParser.exeptions;

/**
 * Comparison is not supported between two types for a specific operator
 */
public class UnsupportedComparandType extends InterpretationException {
    public UnsupportedComparandType(String s) {
        super(s);
    }
}