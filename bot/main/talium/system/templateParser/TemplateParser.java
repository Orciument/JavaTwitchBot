package talium.system.templateParser;

import org.springframework.lang.NonNull;
import talium.system.templateParser.exeptions.*;
import talium.system.templateParser.ifParser.IfParser;
import talium.system.templateParser.statements.*;
import talium.system.templateParser.tokens.Comparison;
import talium.system.templateParser.tokens.TemplateTokenKind;
import talium.system.templateParser.tokens.TemplateToken;
import org.apache.commons.lang.StringUtils;

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
            statements.add(parseToken());
        }
        return statements;
    }

    /**
     * Parses the next statement out of the stream of tokens
     */
    @NonNull
    public Statement parseToken() throws ParsingException {
        TemplateToken current = src.next();
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
                var curr = src.peek();
                if (curr.kind() == TemplateTokenKind.IF_ELSE) {
                    isElse = true;
                    src.consume(TemplateTokenKind.IF_ELSE);
                    continue;
                }
                if (curr.kind() == TemplateTokenKind.IF_END) {
                    src.consume(TemplateTokenKind.IF_END);
                    break;
                }
                if (!isElse) {
                    then.add(parseToken());
                } else {
                    other.add(parseToken());
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
                var curr = src.peek();
                if (curr.kind() == TemplateTokenKind.FOR_END) {
                    src.consume(TemplateTokenKind.FOR_END);
                    break;
                }
                body.add(parseToken());
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

    /**
     * Prints the list and content of statements to the screen
     * @param statements statements to print out
     * @param depth indentation depth for content of statement (may be recursively applied)
     */
    public static void debugPrint(List<Statement> statements, int depth) {
        String indent = " ".repeat(depth);
        for (var statement : statements) {
            if (statement instanceof TextStatement textStatement) {
                System.out.println(STR."\{indent}Text(\{textStatement.text()}),");
            }
            if (statement instanceof VarStatement varStatement) {
                System.out.println(STR."\{indent}Var(\{varStatement.accessExpr()}),");
            }
            if (statement instanceof IfStatement ifStatement) {
                System.out.println(STR."\{indent}IF: \{ifStatement.comparison()}");
                debugPrint(ifStatement.then(), depth + 1);
                System.out.println(STR."\{indent}} else {");
                debugPrint(ifStatement.other(), depth + 1);
                System.out.println(STR."\{indent}},");
            }
            if (statement instanceof LoopStatement forStatement) {
                System.out.println(STR."\{indent}FOR: \{forStatement.varName()} in \{forStatement.var()} {");
                debugPrint(forStatement.body(), depth + 1);
                System.out.println(STR."\{indent}},");
            }
        }
    }
}
