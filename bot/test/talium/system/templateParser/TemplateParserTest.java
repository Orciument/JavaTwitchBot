package talium.system.templateParser;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.jupiter.api.Test;
import talium.system.templateParser.exeptions.TemplateSyntaxException;
import talium.system.templateParser.exeptions.UnexpectedEndOfInputException;
import talium.system.templateParser.exeptions.UnsupportedComparisonOperator;
import talium.system.templateParser.exeptions.UnsupportedDirective;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class TemplateParserTest {

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

    private void test(String src, int iteration) {
        try {
            var statements = new TemplateParser(src).parse();
            TemplateInterpreter.populate(statements, null);
            if (src.contains("$") || src.contains("%")) {
                System.out.println("i: " + iteration + " -> Statement Stream:");
                System.out.println(statements);
                fail("Should  likely have thrown for " + src);
            }
        } catch (TemplateSyntaxException | UnexpectedEndOfInputException | UnsupportedComparisonOperator |
                 UnsupportedDirective | NoSuchFieldException _) {
        } catch (Exception e) {
            System.out.println("i: " + iteration + " -> Source Input: " + src);
            fail("Unexpected Exception thrown: " + e.getMessage(), e);
        }
    }
}