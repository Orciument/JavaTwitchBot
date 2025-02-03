package talium.system.templateParser;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.jupiter.api.Test;
import talium.system.templateParser.exeptions.*;
import talium.system.templateParser.statements.TextStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateParserTest {
    private static final List<String> tokensStrings = new ArrayList<>();
    private static final Map<String, Object> environment = new HashMap<>();

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

        environment.put("test", "test");
        environment.put("object", new Object());
        environment.put("list", List.of("1", "2", "3"));
        environment.put("b", true);
        environment.put("f", 90293.129);
        environment.put("n", 2828);
        environment.put("nu", null);
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

    @Test
    void for_at_end_of_input() throws ParsingException {
        new TemplateParser("%{ for i in b.list }%{endfor}").parse();
    }

    @Test
    void if_at_end_of_input() throws ParsingException {
        new TemplateParser("%{ if a == b }%{endif}").parse();
    }

    @Test
    void working() throws StringTemplateException {
        class TestClassA {
            private final String testString;
            final int testInteger;

            public TestClassA(String testString, int testInteger) {
                this.testString = testString;
                this.testInteger = testInteger;
            }

            public TestClassA() {
                this.testString = "testString";
                this.testInteger = 8457;
            }
        }
        class TestClassB {
            final boolean testBoolean = true;
            List<TestClassA> listB = new ArrayList<>();
            final Object testObject = null;

            public TestClassB() {
                listB.add(new TestClassA());
                listB.add(new TestClassA("one", 1));
                listB.add(new TestClassA("two", 2));
                listB.add(new TestClassA("third", 3));
                listB.add(new TestClassA("fourth", 4));
            }
        }

        var env = new HashMap<String, Object>();
        env.put("test", "testTEXTtest");
        env.put("a", new TestClassA());
        env.put("b", new TestClassB());
        assertEquals("looooping: iteration: one, iteration: two, iteration: third, iteration: fourth, ", TemplateInterpreter.populate(new TemplateParser("looooping: %{ for i in b.listB }iteration: ${i.testString}, %{endfor}").parse(), env));
        assertEquals("CommandResponse: testTEXTtest", TemplateInterpreter.populate(new TemplateParser("CommandResponse: ${test}").parse(), env));
        assertEquals("CommandResponse: yes!", TemplateInterpreter.populate(new TemplateParser("CommandResponse: %{ if a.testInteger == 8457 }yes%{else}false%{endif}!").parse(), env));
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
    public void tokenFuzzing10K() {
        tokenFuzzing(10000, 20);
    }

    @Test
    public void fuzzing1M() {
        randomFuzzing(1000000, 200);
    }

    @Test
    public void tokenFuzzing1M() {
        tokenFuzzing(1000000, 20);
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
            if (statements.contains(null)) {
                fail("Statementlist contains null statement");
            }
            TemplateInterpreter.populate(statements, environment);
        } catch (StringTemplateException _) {
        } catch (Exception e) {
            System.out.println("i: " + iteration + " -> Source Input: " + src);
            fail("Unexpected Exception thrown: " + e.getMessage(), e);
        }
    }

    // TODO check for colliding assignment, when for variable assigns value, but a var was already there

    //TODO add end-to-end tests, input string and environment, test result string
}