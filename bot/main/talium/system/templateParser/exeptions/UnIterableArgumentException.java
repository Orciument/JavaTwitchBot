package talium.system.templateParser.exeptions;

public class UnIterableArgumentException extends InterpretationException {
    public UnIterableArgumentException(String argName) {
        super(STR."\{argName == null ? "Argument" : argName} must implement java.lang.Iterable.");
    }
}
