package talium.system.templateParser.exeptions;

public class UnsupportedDirective extends RuntimeException {
    public UnsupportedDirective(String message) {
        super(message);
    }
}
