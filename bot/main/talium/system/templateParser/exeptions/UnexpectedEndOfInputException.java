package talium.system.templateParser.exeptions;


public class UnexpectedEndOfInputException extends ParsingException {
    //TODO maybe this should only be thrown be low level api's and higher level API's should about not getting an expected token
    public UnexpectedEndOfInputException() {
        super();
    }
}
