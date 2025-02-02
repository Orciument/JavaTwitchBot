package talium.system.templateParser;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.jupiter.api.Test;
import talium.system.templateParser.exeptions.*;
import talium.system.templateParser.statements.TextStatement;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class TemplateParserTest {
    private static final List<String> tokensStrings = new ArrayList<>();

    static {
        tokensStrings.add(" ");
        tokensStrings.add(" ");
        tokensStrings.add(" ");
        tokensStrings.add(" ");
        tokensStrings.add(" ");
        tokensStrings.add(" ");
        tokensStrings.add("text");
        tokensStrings.add("string");
        tokensStrings.add("something else");
        tokensStrings.add("SPECIAL TEXT");
        tokensStrings.add("121");
        tokensStrings.add("1");
        tokensStrings.add("0");
        tokensStrings.add("894903890238948389");
        tokensStrings.add("829.1");
        tokensStrings.add("2838.19489494");
        tokensStrings.add("333.33333");
        tokensStrings.add("true");
        tokensStrings.add("false");
        tokensStrings.add("$");
        tokensStrings.add("${");
        tokensStrings.add("}");
        tokensStrings.add("%");
        tokensStrings.add("%{");
        tokensStrings.add("}");
        tokensStrings.add("}");
        tokensStrings.add("if");
        tokensStrings.add("else");
        tokensStrings.add("endif");
        tokensStrings.add("=");
        tokensStrings.add("==");
        tokensStrings.add("!=");
        tokensStrings.add("<");
        tokensStrings.add("<=");
        tokensStrings.add(">");
        tokensStrings.add(">=");
        tokensStrings.add("for");
        tokensStrings.add("in");
        tokensStrings.add("[*]");
        tokensStrings.add("item");
        tokensStrings.add("itemList");
        tokensStrings.add("endfor");
        tokensStrings.add(".");
        tokensStrings.add("\"");
        tokensStrings.add("%{ object in objectList }");
        tokensStrings.add("%{ if variable.test == \"test\" }");
    }

    @Test
    public void fuzzing1M() {
        randomFuzzing(1000000, 200);
    }

    @Test
    public void fuzzing10K() {
        randomFuzzing(10000, 200);
    }

    @Test
    public void fuzzing10KLong() {
        randomFuzzing(10000, 2000);
    }

    @Test
    public void tokenFuzzing1M() {
        tokenFuzzing(1000000, 20);
    }

    @Test
    public void tokenFuzzing10K() {
        tokenFuzzing(10000, 20);
    }

    @Test
    public void tokenFuzzing10KLong() {
        tokenFuzzing(10000, 200);
    }

    private void randomFuzzing(int n, int length) {
        for (int i = 0; i < n; i++) {
            test(RandomStringUtils.randomAscii(length), i);
        }
    }

    private void tokenFuzzing(int n, int length) {
        for (int i = 0; i < n; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < length; j++) {
                builder.append(tokensStrings.get(RandomUtils.nextInt(tokensStrings.size())));
            }
            test(builder.toString(), i);
        }
    }

    private void test(String src, int iteration) {
        try {
            var statements = new TemplateParser(src).parse();
            var res = TemplateInterpreter.populate(statements, null);
            if (src.contains("$") || src.contains("%")) {
                System.out.println("i: " + iteration + " -> Statement Stream:");
                System.out.println(statements);
                fail("Should likely have thrown for: " + src);
            }
        } catch (StringTemplateException _) {
        } catch (Exception e) {
            System.out.println("i: " + iteration + " -> Source Input: " + src);
            fail("Unexpected Exception thrown: " + e.getMessage(), e);
        }
    }

    @Test
    void returnNonNull() throws UnexpectedTokenException, UnsupportedDirective, UnexpectedEndOfInputException {
        assert !new TemplateLexer("t%{   endif }").parse().contains(null);
    }

    @Test
    void unnecessary_tokens_to_text() throws ParsingException {
        assert new TemplateParser("t%{ endif }").parse().equals(List.of(new TextStatement("t"), new TextStatement("%{ endif }")));
        assert new TemplateParser("t%{ endfor }").parse().equals(List.of(new TextStatement("t"), new TextStatement("%{ endfor }")));
        assert new TemplateParser("t%{ else }").parse().equals(List.of(new TextStatement("t"), new TextStatement("%{ else }")));
    }
}