package talium.system.templateParser;


import talium.system.templateParser.exeptions.UnexpectedEndOfInputException;
import talium.system.templateParser.exeptions.UnexpectedTokenException;
import talium.system.templateParser.exeptions.UnsupportedDirective;
import talium.system.templateParser.tokens.TemplateTokenKind;
import talium.system.templateParser.tokens.TemplateToken;

import java.util.ArrayList;
import java.util.List;

/**
 * A Lexer for String Templates that consumes a CharacterStream and provides the tokens as a TokenStream
 */
public class TemplateLexer {
    CharakterStream src;
    List<TemplateToken> tokens;
    int pos;

    public TemplateLexer(String src) {
        this.src = new CharakterStream(src);
        this.tokens = new ArrayList<>();
    }

    /**
     * performs the first parse pass and returns a list of major tokens
     *
     * @return list of major tokens
     */
    public List<TemplateToken> parse() throws UnexpectedTokenException, UnsupportedDirective, UnexpectedEndOfInputException {
        List<TemplateToken> list = new ArrayList<>();
        while (!src.isEOF()) {
            TemplateToken next = next();
            if (next == null) {
                break;
            }
            list.add(next);
        }
        return list;
    }

    public TemplateToken peek() throws UnexpectedTokenException, UnsupportedDirective, UnexpectedEndOfInputException {
        if (pos >= tokens.size()) {
            tokens.add(parseToken());
        }
        return tokens.get(pos);
    }

    public TemplateToken next() throws UnexpectedTokenException, UnsupportedDirective, UnexpectedEndOfInputException {
        if (pos >= tokens.size()) {
            tokens.add(parseToken());
        }
        TemplateToken t = tokens.get(pos);
        pos += 1;
        return t;
    }

    /**
     * Consumes the next token if it is the expected token.
     * If a different token is encountered, a syntax exception is thrown.
     *
     * @param token the next expected token
     */
    public void consume(TemplateTokenKind token) throws UnexpectedTokenException, UnsupportedDirective, UnexpectedEndOfInputException {
        if (isEOF()) {
            throw new UnexpectedTokenException(token.name(), "END-OF-INPUT", src.pos() - 1, src.src());
        }
        TemplateToken next = next();
        if (next.kind() != token) {
            throw new UnexpectedTokenException(token.name(), next.kind().name(), src.pos() - 1, src.src());
        }
    }

    /**
     * consume a singe major token from the character stream
     *
     * @return the next token
     */
    private TemplateToken parseToken() throws UnexpectedTokenException, UnexpectedEndOfInputException, UnsupportedDirective {
        if (src.isEOF()) {
            return null;
        }
        if (src.peek() == '$') {
            src.consume('$');
            src.consume('{');
            String varName = src.readUntil('}');
            src.consume('}');
            return new TemplateToken(TemplateTokenKind.VAR, varName);

        } else if (src.peek() == '%') {
            // Get type of directive if or for
            src.consume('%');
            src.consume('{');
            String directive = src.readTillWhitespace();
            src.skipWhitespace();

            if (directive.equals("if")) {
                String condition = src.readUntil('}');
                src.skipWhitespace();
                src.consume('}');
                return new TemplateToken(TemplateTokenKind.IF_HEAD, condition);
            } else if (directive.equals("else")) {
                src.skipWhitespace();
                src.consume('}');
                return new TemplateToken(TemplateTokenKind.IF_ELSE, "");
            } else if (directive.equals("endif")) {
                src.skipWhitespace();
                src.consume('}');
                return new TemplateToken(TemplateTokenKind.IF_END, "");
            } else if (directive.equals("for")) {
                String head = src.readUntil('}');
                src.skipWhitespace();
                src.consume('}');
                return new TemplateToken(TemplateTokenKind.FOR_HEAD, head);
            } else if (directive.equals("endfor")) {
                src.skipWhitespace();
                src.consume('}');
                return new TemplateToken(TemplateTokenKind.FOR_END, "");
            } else {
                throw new UnsupportedDirective(STR."Directive not supported: \{directive}");
            }
        } else {
            String buffer = "";
            // while no var or directive is encountered, all chars are consumed into a TEXT token
            while (!src.isEOF() && src.peek() != '$' && src.peek() != '%') {
                buffer += src.next();
            }
            return new TemplateToken(TemplateTokenKind.TEXT, buffer);
        }
    }

    public boolean isEOF() {
        return src.isEOF();
    }
}