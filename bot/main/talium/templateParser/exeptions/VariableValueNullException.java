package talium.templateParser.exeptions;

/// Is thrown when a variable in a variable reference is null, but is not the final reference (field on this object are supposed to be 'opened')
public class VariableValueNullException extends InterpretationException {
    public VariableValueNullException(String argName) {
        super(STR."\{argName == null ? "Argument" : STR."\"\{argName}\""} must not be null.");
    }
}
