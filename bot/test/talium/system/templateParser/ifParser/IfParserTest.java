package talium.system.templateParser.ifParser;

import talium.system.templateParser.exeptions.TemplateSyntaxException;
import talium.system.templateParser.exeptions.UnexpectedEndOfInputException;
import talium.system.templateParser.exeptions.UnsupportedComparisonOperator;
import talium.system.templateParser.statements.Equals;
import talium.system.templateParser.statements.VarStatement;
import talium.system.templateParser.tokens.Comparison;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static talium.system.templateParser.ifParser.IfParser.parse;

public class IfParserTest {

    @Test
    void test_success() throws UnsupportedComparisonOperator, TemplateSyntaxException, UnexpectedEndOfInputException {
        var comp = new Comparison(VarStatement.create("var.name"), Equals.NOT_EQUALS, "test");
        assert parse("var.name != \"test\"").equals(comp);
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
        assert parse("var.name == false").equals(bool);

        var string = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, "test");
        assert parse("var.name == \"test\"").equals(string);

        var character = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, "a");
        assert parse("var.name == \"a\"").equals(character);

        var byte_ = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, 159);
        assert parse("var.name == 159").equals(byte_);

        var short_ = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, 16000);
        assert parse("var.name == 16000").equals(short_);

        var int_ = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, 273650);
        assert parse("var.name == 273650").equals(int_);

        var long_ = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, 1729010172331L);
        assert parse("var.name == 1729010172331").equals(long_);

        var float_ = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, 69.4200);
        assert parse("var.name == 69.4200").equals(float_);

        var double_ = new Comparison(VarStatement.create("var.name"), Equals.EQUALS, 3.1415d);
        assert parse("var.name == 3.1415").equals(double_);
    }
}
