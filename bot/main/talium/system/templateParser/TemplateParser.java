package talium.system.templateParser;

import talium.system.templateParser.exeptions.TemplateSyntaxException;
import talium.system.templateParser.exeptions.UnsupportedComparisonOperator;
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

    public TemplateParser(TemplateLexer src) {
        this.src = src;
    }

    /**
     * Parses the stream of major tokens into a stream of statements.
     * Invokes minor if and for parsers if needed.
     */
    public List<Statement> parse() throws UnsupportedComparisonOperator {
        List<Statement> statements = new ArrayList<>();
        while (!src.isEOF()) {
            statements.add(parseToken());
        }
        return statements;
    }

    /**
     * Parses the next statement out of the stream of tokens
     */
    public Statement parseToken() throws UnsupportedComparisonOperator {
        TemplateToken current = src.next();
        if (current == null) {
            return null;
        }
        if (current.kind() == TemplateTokenKind.TEXT) {
            return new TextStatement(current.value());
        }
        if (current.kind() == TemplateTokenKind.VAR) {
            //TODO check for valid java object references
            // make illegal:
            // VarStatement[name=;kO'!1NQD;Mdejc/QF>hs=43LCl@!3yB4+*|I%YGH&Z]
            return new VarStatement(current.value());
        }
        if (current.kind() == TemplateTokenKind.IF_HEAD) {
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
        }
        if (current.kind() == TemplateTokenKind.FOR_HEAD) {
            String[] head = current.value().split(" in ");
            if (head.length < 2) {
                throw new TemplateSyntaxException("Missing \"in\" in for definition!");
            }
            if (StringUtils.countMatches(head[1], "[*]") > 1) {
                throw new TemplateSyntaxException("Only one wildcard allowed in a for statement");
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
            return new LoopStatement(head[0], head[1], body);
        }
        return null;
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
                System.out.println(STR."\{indent}Var(\{varStatement.name()}),");
            }
            if (statement instanceof IfStatement ifStatement) {
                System.out.println(STR."\{indent}IF: \{ifStatement.comparison()}");
                debugPrint(ifStatement.then(), depth + 1);
                System.out.println(STR."\{indent}} else {");
                debugPrint(ifStatement.other(), depth + 1);
                System.out.println(STR."\{indent}},");
            }
            if (statement instanceof LoopStatement forStatement) {
                System.out.println(STR."\{indent}FOR: \{forStatement.name()} in \{forStatement.var()} {");
                debugPrint(forStatement.body(), depth + 1);
                System.out.println(STR."\{indent}},");
            }
        }
    }
}
