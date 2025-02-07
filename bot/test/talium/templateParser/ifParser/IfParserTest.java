package talium.templateParser.ifParser;

import talium.templateParser.exeptions.TemplateSyntaxException;
import talium.templateParser.exeptions.UnexpectedEndOfInputException;
import talium.templateParser.exeptions.UnsupportedComparisonOperator;
import talium.templateParser.statements.Equals;
import talium.templateParser.statements.VarStatement;
import talium.templateParser.tokens.Comparison;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static talium.templateParser.ifParser.IfParser.parse;

public class IfParserTest {

    @Test
    void test_success() throws UnsupportedComparisonOperator, TemplateSyntaxException, UnexpectedEndOfInputException {
        var comp = new Comparison(VarStatement.create("var.name"), Equals.NOT_EQUALS, "test");
        assertEquals(comp, parse("var.name != \"test\""));
    }

    @Test
    void invalid_operator() throws UnsupportedComparisonOperator, TemplateSyntaxException, UnexpectedEndOfInputException {
        try {
            parse("var.name !!== \"test\"");
            fail("Should have thrown UnsupportedComparisonOperator");
        } catch (UnsupportedComparisonOperator _) {
        }
        try {
            parse("var.name lessThan \"test\"");
            fail("Should have thrown TemplateSyntaxException");
        } catch (TemplateSyntaxException _) {
        }
    }

    @Test
    void test_types() throws UnsupportedComparisonOperator, TemplateSyntaxException, UnexpectedEndOfInputException {
        var bool = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, false);
        assertEquals(bool, parse("var.name == false"));

        var string = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, "test");
        assertEquals(string, parse("var.name == \"test\""));

        var character = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, "a");
        assertEquals(character, parse("var.name == \"a\""));

        var byte_ = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, 159);
        assertEquals(byte_, parse("var.name == 159"));

        var short_ = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, 16000);
        assertEquals(short_, parse("var.name == 16000"));

        var int_ = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, 273650);
        assertEquals(int_, parse("var.name == 273650"));

        var long_ = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, 1729010172331L);
        assertEquals(long_, parse("var.name == 1729010172331"));

        var float_ = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, 69.4200);
        assertEquals(float_, parse("var.name == 69.4200"));

        var double_ = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, 3.1415d);
        assertEquals(double_, parse("var.name == 3.1415"));
    }

    @Test
    void malformed_number() throws UnexpectedEndOfInputException, UnsupportedComparisonOperator, TemplateSyntaxException {
        try {
            parse("  1\"string%{ <= .SPECIAL TEXTendfor[*]SPECIAL TEXT");
            fail();
        } catch (TemplateSyntaxException e) {
            if (!(e.getCause() instanceof NumberFormatException)) {
                throw e;
            }
        }
    }
}
