package talium.system.templateParser;

import talium.system.templateParser.exeptions.UnexpectedEndOfInputException;
import talium.system.templateParser.exeptions.UnexpectedTokenException;

/**
 * A indexed Stream of characters for parsing.
 */
public class CharakterStream {
    String src;
    int pos;

    public CharakterStream(String src) {
        this.src = src;
    }

    public Character peek() throws UnexpectedEndOfInputException {
        if (isEOF())
            throw new UnexpectedEndOfInputException();
        return src.charAt(pos);
    }

    public Character next() throws UnexpectedEndOfInputException {
        if (isEOF())
            throw new UnexpectedEndOfInputException();
        char c = src.charAt(pos);
        pos += 1;
        return c;
    }

    /**
     * Consumes the next character if it is the expected character.
     * If a different character is encountered, a syntax exception is thrown.
     * @param c the next expected character
     */
    public void consume(Character c) throws UnexpectedTokenException, UnexpectedEndOfInputException {
        if (isEOF()) {
            throw new UnexpectedTokenException(c, "END-OF-INPUT", pos - 1, src);
        }
        Character next = next();
        if (next != c) {
            throw new UnexpectedTokenException(c, next, pos - 1, src);
        }
    }

    public boolean isEOF() {
        return pos == src.length();
    }

    /**
     * consume all characters up to and excluding the first found target character
     *
     * @param c target character
     * @return return all consumed characters up to the target
     */
    public String readUntil(char c) throws UnexpectedEndOfInputException {
        skipWhitespace();
        String buffer = "";
        boolean escapeNext = false;
        while (!isEOF() && peek() != c || escapeNext) {
            if (peek() == '\\') { //TODO this impossible, we need to save separately if the last char was a -> \ (or check in the buffer) (also, if \ is the target char, we should exit)
                escapeNext = true;
            }
            if (escapeNext) {
                escapeNext = false;
            }
            buffer += next();
        }
        return buffer;
    }

    /**
     * consume all characters up to and excluding the first character that is not a digit
     *
     * @return return all consumed characters up to the non digit char
     * @see Character#isDigit(char)
     */
    public String readTillNotDigit() throws UnexpectedEndOfInputException {
        skipWhitespace();
        String buffer = "";
        while (!Character.isDigit(peek())) {
            buffer += next();
        }
        return buffer;
    }

    /**
     * consume all characters up to and excluding the first character that is whitespace
     *
     * @return return all consumed characters up to the whitespace
     * @see Character#isWhitespace(char)
     */
    public String readTillWhitespace() throws UnexpectedEndOfInputException {
        skipWhitespace();
        String buffer = "";
        while (!isEOF() && !Character.isWhitespace(peek())) {
            buffer += next();
        }
        return buffer;
    }

    /**
     * consumes all characters until a non whitespace character is encountered
     */
    public void skipWhitespace() throws UnexpectedEndOfInputException {
        while (!isEOF() && Character.isWhitespace(peek())) {
            pos += 1;
        }
    }

    public String src() {
        return src;
    }

    public int pos() {
        return pos;
    }
}