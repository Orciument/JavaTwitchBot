package talium.system.templateParser;

import org.springframework.lang.NonNull;
import talium.system.templateParser.exeptions.*;
import talium.system.templateParser.ifParser.IfParser;
import talium.system.templateParser.statements.*;
import talium.system.templateParser.tokens.Comparison;
import talium.system.templateParser.tokens.TemplateTokenKind;
import talium.system.templateParser.tokens.TemplateToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the stream of major tokens into a stream of statements.
 * Invokes minor if and for parsers if needed.
 */
public class TemplateParser {
    TemplateLexer src;

    public TemplateParser(String src) {
        this.src = new TemplateLexer(src);
    }

    /**
     * Parses the stream of major tokens into a stream of statements.
     * Invokes minor if and for parsers if needed.
     */
    public List<Statement> parse() throws ParsingException {
        List<Statement> statements = new ArrayList<>();
        while (!src.isEOF()) {
            statements.add(parseToken(src.next()));
        }
        return statements;
    }

    /**
     * Parses the next statement out of the stream of tokens
     */
    @NonNull
    public Statement parseToken(TemplateToken current) throws ParsingException {
        if (current.kind() == TemplateTokenKind.TEXT) {
            return new TextStatement(current.value());
        } else if (current.kind() == TemplateTokenKind.VAR) {
            return VarStatement.create(current.value());
        } else if (current.kind() == TemplateTokenKind.IF_HEAD) {
            Comparison comparison = IfParser.parse(current.value());
            List<Statement> then = new ArrayList<>();
            List<Statement> other = new ArrayList<>();
            boolean isElse = false;
            while (!src.isEOF()) { // mostly equivalent to while(true), but with a overrun check
                // we need to check the next token, instead of the next statement, because we only want to parse
                // the next statement if we have reached the end of the for statement.
                // But when we get the next token to check, we have already consumed that token, so we need to give it to
                // parseToken(...) if we need to parse it, because it wasn't the end
                var curr = src.next();
                if (curr.kind() == TemplateTokenKind.IF_ELSE) {
                    isElse = true;
                    continue;
                }
                if (curr.kind() == TemplateTokenKind.IF_END) {
                    break;
                }
                if (!isElse) {
                    then.add(parseToken(curr));
                } else {
                    other.add(parseToken(curr));
                }
            }
            return new IfStatement(comparison, then, other);
        } else if (current.kind() == TemplateTokenKind.FOR_HEAD) {
            String[] head = current.value().split(" in ");
            if (head.length < 2) {
                throw new TemplateSyntaxException("Missing \"in\" in for definition!");
            }
            List<Statement> body = new ArrayList<>();
            while (!src.isEOF()) { // mostly equivalent to while(true), but with a overrun check
                // we need to check the next token, instead of the next statement, because we only want to parse
                // the next statement if we have reached the end of the for statement.
                // But when we get the next token to check, we have already consumed that token, so we need to give it to
                // parseToken(...) if we need to parse it, because it wasn't the end
                var curr = src.next();
                if (curr.kind() == TemplateTokenKind.FOR_END) {
                    break;
                }
                body.add(parseToken(curr));
            }
            VarStatement listVar = VarStatement.create(head[1].trim());
            return new LoopStatement(head[0].trim(), listVar, body);
        } else {
            //covers:
            //TemplateTokenKind.FOR_END
            //TemplateTokenKind.IF_ELSE
            //TemplateTokenKind.IF_END
            return new TextStatement(current.value());
        }
    }
}
