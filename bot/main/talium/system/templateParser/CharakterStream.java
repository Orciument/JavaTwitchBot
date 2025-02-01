package talium.system.templateParser;

import talium.system.templateParser.exeptions.TemplateSyntaxException;
import talium.system.templateParser.exeptions.UnexpectedEndOfInputException;

/**
 * A indexed Stream of characters for parsing.
 */
public class CharakterStream implements TokenStream<Character> {
    String src;
    int pos;

    public CharakterStream(String src) {
        this.src = src;
    }

    @Override
    public Character peek() throws UnsupportedOperationException {
        if (isEOF())
            throw new UnexpectedEndOfInputException();
        return src.charAt(pos);
    }

    @Override
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
    public void consume(Character c) {
        if (isEOF()) {
            throw new TemplateSyntaxException(String.valueOf(c), "END-OF-INPUT", pos - 1, src);
        }
        Character next = next();
        if (next != c) {
            throw new TemplateSyntaxException(c, next, pos - 1, src);
        }
    }

    @Override
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