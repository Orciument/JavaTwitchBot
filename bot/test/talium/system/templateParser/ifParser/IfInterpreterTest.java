package talium.system.templateParser.ifParser;

import talium.system.templateParser.exeptions.ImpossibleComparisonException;
import talium.system.templateParser.statements.Equals;
import talium.system.templateParser.tokens.Comparison;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class IfInterpreterTest {

    @Nested
    class OperatorTests {
        @Test
        void ints() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison(34993497, Equals.NOT_EQUALS, 348)));
            assertFalse(IfInterpreter.compare(new Comparison(34993497, Equals.NOT_EQUALS, 34993497)));

            assertTrue(IfInterpreter.compare(new Comparison(34993497, Equals.EQUALS, 34993497)));
            assertFalse(IfInterpreter.compare(new Comparison(34993497, Equals.EQUALS, 348)));

            assertTrue(IfInterpreter.compare(new Comparison(348, Equals.LESS_THAN, 34993497)));
            assertFalse(IfInterpreter.compare(new Comparison(34993497, Equals.LESS_THAN, 34993497)));

            assertTrue(IfInterpreter.compare(new Comparison(348, Equals.LESS_THAN_OR_EQUALS, 34993497)));
            assertFalse(IfInterpreter.compare(new Comparison(34993497, Equals.LESS_THAN_OR_EQUALS, 348)));
            assertTrue(IfInterpreter.compare(new Comparison(34993497, Equals.LESS_THAN_OR_EQUALS, 34993497)));

            assertTrue(IfInterpreter.compare(new Comparison(34993497, Equals.GREATER_THAN, 348)));
            assertFalse(IfInterpreter.compare(new Comparison(348, Equals.GREATER_THAN, 34993497)));

            assertTrue(IfInterpreter.compare(new Comparison(34993497, Equals.GREATER_THAN_OR_EQUALS, 348)));
            assertFalse(IfInterpreter.compare(new Comparison(348, Equals.GREATER_THAN_OR_EQUALS, 34993497)));
            assertTrue(IfInterpreter.compare(new Comparison(34993497, Equals.GREATER_THAN_OR_EQUALS, 34993497)));
        }

        @Test
        void longs() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison(6323833349178L, Equals.NOT_EQUALS, 19282L)));
            assertFalse(IfInterpreter.compare(new Comparison(6323833349178L, Equals.NOT_EQUALS, 6323833349178L)));

            assertTrue(IfInterpreter.compare(new Comparison(6323833349178L, Equals.EQUALS, 6323833349178L)));
            assertFalse(IfInterpreter.compare(new Comparison(6323833349178L, Equals.EQUALS, 19282L)));

            assertTrue(IfInterpreter.compare(new Comparison(19282L, Equals.LESS_THAN, 6323833349178L)));
            assertFalse(IfInterpreter.compare(new Comparison(6323833349178L, Equals.LESS_THAN, 6323833349178L)));

            assertTrue(IfInterpreter.compare(new Comparison(19282L, Equals.LESS_THAN_OR_EQUALS, 6323833349178L)));
            assertFalse(IfInterpreter.compare(new Comparison(6323833349178L, Equals.LESS_THAN_OR_EQUALS, 19282L)));
            assertTrue(IfInterpreter.compare(new Comparison(6323833349178L, Equals.LESS_THAN_OR_EQUALS, 6323833349178L)));

            assertTrue(IfInterpreter.compare(new Comparison(6323833349178L, Equals.GREATER_THAN, 19282L)));
            assertFalse(IfInterpreter.compare(new Comparison(19282L, Equals.GREATER_THAN, 6323833349178L)));

            assertTrue(IfInterpreter.compare(new Comparison(6323833349178L, Equals.GREATER_THAN_OR_EQUALS, 19282L)));
            assertFalse(IfInterpreter.compare(new Comparison(19282L, Equals.GREATER_THAN_OR_EQUALS, 6323833349178L)));
            assertTrue(IfInterpreter.compare(new Comparison(6323833349178L, Equals.GREATER_THAN_OR_EQUALS, 6323833349178L)));
        }
        @Test
        void floats() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison(687.000033339284F, Equals.NOT_EQUALS, 12.28434F)));
            assertFalse(IfInterpreter.compare(new Comparison(687.000033339284F, Equals.NOT_EQUALS, 687.000033339284F)));

            assertTrue(IfInterpreter.compare(new Comparison(687.000033339284F, Equals.EQUALS, 687.000033339284F)));
            assertFalse(IfInterpreter.compare(new Comparison(687.000033339284F, Equals.EQUALS, 12.28434F)));

            assertTrue(IfInterpreter.compare(new Comparison(12.28434F, Equals.LESS_THAN, 687.000033339284F)));
            assertFalse(IfInterpreter.compare(new Comparison(687.000033339284F, Equals.LESS_THAN, 687.000033339284F)));

            assertTrue(IfInterpreter.compare(new Comparison(12.28434F, Equals.LESS_THAN_OR_EQUALS, 687.000033339284F)));
            assertFalse(IfInterpreter.compare(new Comparison(687.000033339284F, Equals.LESS_THAN_OR_EQUALS, 12.28434F)));
            assertTrue(IfInterpreter.compare(new Comparison(687.000033339284F, Equals.LESS_THAN_OR_EQUALS, 687.000033339284F)));

            assertTrue(IfInterpreter.compare(new Comparison(687.000033339284F, Equals.GREATER_THAN, 12.28434F)));
            assertFalse(IfInterpreter.compare(new Comparison(12.28434F, Equals.GREATER_THAN, 687.000033339284F)));

            assertTrue(IfInterpreter.compare(new Comparison(687.000033339284F, Equals.GREATER_THAN_OR_EQUALS, 12.28434F)));
            assertFalse(IfInterpreter.compare(new Comparison(12.28434F, Equals.GREATER_THAN_OR_EQUALS, 687.000033339284F)));
            assertTrue(IfInterpreter.compare(new Comparison(687.000033339284F, Equals.GREATER_THAN_OR_EQUALS, 687.000033339284F)));
        }
    }

    @Nested
    class TypeTests {
        @Test
        void boolean_boolean() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison(true, Equals.NOT_EQUALS, false)));
            assertTrue(IfInterpreter.compare(new Comparison(false, Equals.EQUALS, false)));
            assertFalse(IfInterpreter.compare(new Comparison(false, Equals.EQUALS, true)));
        }

        @Test
        void string_string() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison("test", Equals.NOT_EQUALS, "wort")));
            assertTrue(IfInterpreter.compare(new Comparison("test", Equals.EQUALS, "test")));
            assertFalse(IfInterpreter.compare(new Comparison("test", Equals.EQUALS, "wort")));
        }

        @Test
        void int_int() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison(10, Equals.NOT_EQUALS, 11)));
            assertTrue(IfInterpreter.compare(new Comparison(10, Equals.EQUALS, 10)));
            assertFalse(IfInterpreter.compare(new Comparison(10, Equals.EQUALS, 11)));
        }

        @Test
        void long_long() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison(10L, Equals.NOT_EQUALS, 11L)));
            assertTrue(IfInterpreter.compare(new Comparison(10L, Equals.EQUALS, 10L)));
            assertFalse(IfInterpreter.compare(new Comparison(10L, Equals.EQUALS, 11L)));
        }

        @Test
        void float_float() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison(10.32323F, Equals.NOT_EQUALS, 10.52323F)));
            assertTrue(IfInterpreter.compare(new Comparison(10.32323F, Equals.EQUALS, 10.32323F)));
            assertFalse(IfInterpreter.compare(new Comparison(10.32323F, Equals.EQUALS, 10.52323F)));
        }

        @Test
        void double_double() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison(10.32323D, Equals.NOT_EQUALS, 10.52323D)));
            assertTrue(IfInterpreter.compare(new Comparison(10.32323D, Equals.EQUALS, 10.32323D)));
            assertFalse(IfInterpreter.compare(new Comparison(10.32323D, Equals.EQUALS, 10.52323D)));
        }

        @Test
        void char_char() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison('t', Equals.NOT_EQUALS, 'w')));
            assertTrue(IfInterpreter.compare(new Comparison('t', Equals.EQUALS, 't')));
            assertFalse(IfInterpreter.compare(new Comparison('t', Equals.EQUALS, 'w')));
        }

        @Test
        void int_long() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison(10, Equals.NOT_EQUALS, 11L)));
            assertTrue(IfInterpreter.compare(new Comparison(10, Equals.EQUALS, 10)));
            assertFalse(IfInterpreter.compare(new Comparison(10, Equals.EQUALS, 11L)));
        }

        @Test
        void float_double() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison(10.32323F, Equals.NOT_EQUALS, 10.52323D)));
            assertTrue(IfInterpreter.compare(new Comparison(10.32323F, Equals.EQUALS, 10.32323F)));
            assertFalse(IfInterpreter.compare(new Comparison(10.32323F, Equals.EQUALS, 10.52323D)));
        }

        @Test
        void string_int()  {
            try {
                IfInterpreter.compare(new Comparison("test", Equals.EQUALS, 1));
                fail("ImpossibleComparisonException not thrown!");
            } catch (ImpossibleComparisonException _) {}
        }

        @Test
        void string_double() {
            try {
                IfInterpreter.compare(new Comparison("test", Equals.EQUALS, 10.52323D));
                fail("ImpossibleComparisonException not thrown!");
            } catch (ImpossibleComparisonException _) {}
        }

        @Test
        void string_boolean() {
            try {
                IfInterpreter.compare(new Comparison("test", Equals.EQUALS, true));
                fail("ImpossibleComparisonException not thrown!");
            } catch (ImpossibleComparisonException _) {}
        }

        @Test
        void int_boolean() {
            try {
                IfInterpreter.compare(new Comparison(123, Equals.EQUALS, true));
                fail("ImpossibleComparisonException not thrown!");
            } catch (ImpossibleComparisonException _) {}
        }

        @Test
        void long_float() {
            try {
                IfInterpreter.compare(new Comparison(10L, Equals.NOT_EQUALS, 10.52323F));
                fail("ImpossibleComparisonException not thrown!");
            } catch (ImpossibleComparisonException _) {}
        }

        @Test
        void object_object() {
            try {
                IfInterpreter.compare(new Comparison(123, Equals.EQUALS, new ArrayList<Exception>()));
                fail("ImpossibleComparisonException not thrown!");
            } catch (ImpossibleComparisonException _) {}
        }

        @Test
        void object_boolean() {
            try {
                IfInterpreter.compare(new Comparison(new ArrayList<Exception>(), Equals.EQUALS, true));
                fail("ImpossibleComparisonException not thrown!");
            } catch (ImpossibleComparisonException _) {}
        }
    }

    @Nested
    class Nulls {
        @Test
        void null_null() throws ImpossibleComparisonException {
            assertTrue(IfInterpreter.compare(new Comparison(null, Equals.EQUALS, null)));
            assertFalse(IfInterpreter.compare(new Comparison(null, Equals.NOT_EQUALS, null)));
        }
        @Test
        void null_int() throws ImpossibleComparisonException {
            assertFalse(IfInterpreter.compare(new Comparison(null, Equals.EQUALS, 102021)));
            assertTrue(IfInterpreter.compare(new Comparison(9349834, Equals.NOT_EQUALS, null)));
        }
        @Test
        void null_double() throws ImpossibleComparisonException {
            assertFalse(IfInterpreter.compare(new Comparison(null, Equals.EQUALS, 29.12939349232)));
            assertTrue(IfInterpreter.compare(new Comparison(9289498293.39323, Equals.NOT_EQUALS, null)));
        }
        @Test
        void null_string() throws ImpossibleComparisonException {
            assertFalse(IfInterpreter.compare(new Comparison(null, Equals.EQUALS, 102021)));
            assertTrue(IfInterpreter.compare(new Comparison(9349834, Equals.NOT_EQUALS, null)));
        }
        @Test
        void null_boolean() throws ImpossibleComparisonException {
            assertFalse(IfInterpreter.compare(new Comparison(null, Equals.EQUALS, false)));
            assertTrue(IfInterpreter.compare(new Comparison(true, Equals.NOT_EQUALS, null)));
        }
        @Test
        void null_Object() throws ImpossibleComparisonException {
            assertFalse(IfInterpreter.compare(new Comparison(null, Equals.EQUALS, new Object())));
            assertTrue(IfInterpreter.compare(new Comparison(new ArrayList<Exception>(), Equals.NOT_EQUALS, null)));
        }
    }
}
