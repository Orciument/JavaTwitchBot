package talium.system.templateParser;

import talium.system.templateParser.exeptions.*;
import talium.system.templateParser.statements.*;
import talium.system.templateParser.tokens.Comparison;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static talium.system.templateParser.TemplateInterpreter.getNestedReplacement;
import static talium.system.templateParser.TemplateInterpreter.populate;

public class TemplateInterpreterTest {
    static List<Statement> TEMPLATE_VAR;
    static List<Statement> TEMPLATE_IF;
    static List<Statement> TEMPLATE_LOOP;
    static List<Statement> NESTED_LOOP;

    static {
        try {
            TEMPLATE_VAR = Arrays.asList(
                    new TextStatement("Hello, "),
                    VarStatement.create("name"),
                    new TextStatement("!")
            );
            TEMPLATE_IF = Arrays.asList(
                    new TextStatement("Hello, "),
                    new IfStatement(new Comparison(VarStatement.create("compLeft"), Equals.EQUALS, "testString"),
                            List.of(VarStatement.create("compLeft")),
                            List.of(new TextStatement("unnamed"))
                    ),
                    new TextStatement("!")
            );
            TEMPLATE_LOOP = List.of(
                    new TextStatement("Hello,"),
                    new LoopStatement("varName", VarStatement.create("loopVar"), Arrays.asList(
                            new TextStatement(" "),
                            VarStatement.create("varName")
                    )),
                    new TextStatement("!")
            );
            NESTED_LOOP = Arrays.asList(
                    new TextStatement("Hello,"),
                    new LoopStatement("innerLoop", VarStatement.create("loopVar"), Arrays.asList(
                            new LoopStatement("varName", VarStatement.create("innerLoop"), Arrays.asList(
                                    new TextStatement(" "),
                                    VarStatement.create("varName")
                            )),
                            new TextStatement(" |")
                    )),
                    new TextStatement("!")
            );
        } catch (TemplateSyntaxException _) {}
    }

    @Nested
    class Populate {
        @Test
        void iterate_non_iterable() throws InterpretationException {
            HashMap<String, Object> map = new HashMap<>();
            map.put("loopVar", "this_is_not_a_list");
            try {
                System.out.println(populate(TEMPLATE_LOOP, map));
                fail("Should have thrown UnIterableArgumentException");
            } catch (UnIterableArgumentException _) {
            }
        }

        @Test
        void var() throws InterpretationException {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", "dummy name");
            assertEquals("Hello, dummy name!", populate(TEMPLATE_VAR, map));
        }

        @Test
        void iterate_empty_list() throws InterpretationException {
            HashMap<String, Object> map = new HashMap<>();
            map.put("loopVar", new ArrayList<>());
            assertEquals("Hello,!", populate(TEMPLATE_LOOP, map));
        }

        @Test
        void nestedLoop() throws InterpretationException  {
            HashMap<String, Object> map = new HashMap<>();
            ArrayList<List<String>> list = new ArrayList<>();
            list.add(List.of("t-1.1", "t-1.2"));
            list.add(List.of("t-2.1", "t-2.2"));
            map.put("loopVar", list);
            assertEquals("Hello, t-1.1 t-1.2 | t-2.1 t-2.2 |!", populate(NESTED_LOOP, map));
        }

        @Test
        void if_() throws InterpretationException {
            HashMap<String, Object> map = new HashMap<>();
            map.put("compLeft", "testString");
            assertEquals("Hello, testString!", populate(TEMPLATE_IF, map));
        }

        @Test
        void wrong_condition_type() throws InterpretationException {
            HashMap<String, Object> map = new HashMap<>();
            map.put("compLeft", false);
            try {
                populate(TEMPLATE_IF, map);
                fail("Should have thrown ImpossibleComparisonException");
            } catch (ImpossibleComparisonException _) {}
        }
    }

    @Nested
    class NestedReplacement {
        private record TestClass(String testString, int testInteger) {
        }

        @Test
        void string() throws InterpretationException, TemplateSyntaxException {
            HashMap<String, Object> map = new HashMap<>();
            map.put("test", "testing String");
            map.put("dummyVar", 23899);
            assertEquals("testing String", getNestedReplacement(VarStatement.create("test"), map));
        }

        @Test
        void integer() throws InterpretationException, TemplateSyntaxException {
            HashMap<String, Object> map = new HashMap<>();
            map.put("integerValue", 23899);
            map.put("dummyVar", "testing String");
            assertEquals(23899, getNestedReplacement(VarStatement.create("integerValue"), map));
        }

        @Test
        void object_path() throws InterpretationException, TemplateSyntaxException {
            TestClass testClass = new TestClass("testString", 23899);
            HashMap<String, Object> map = new HashMap<>();
            map.put("testObject", testClass);
            map.put("dummyVar", "testing String");
            assertEquals(23899, getNestedReplacement(VarStatement.create("testObject.testInteger"), map));
        }

        @Test
        void list() throws InterpretationException, TemplateSyntaxException {
            HashMap<String, Object> map = new HashMap<>();
            ArrayList<String> list = new ArrayList<>();
            list.add("dummyValue");
            list.add("dummyString");
            map.put("listVar", list);
            map.put("dummyVar", "testing String");
            assertEquals(list, getNestedReplacement(VarStatement.create("listVar"), map));
        }

        @Test
        void toplevel_null() throws InterpretationException, TemplateSyntaxException {
            HashMap<String, Object> map = new HashMap<>();
            map.put("testObject", null);
            map.put("dummyVar", "testing String");
            try {
                getNestedReplacement(VarStatement.create("testObject.testInteger"), map);
                fail("Should have thrown NullArgumentException");
            } catch (VariableValueNullException _) {
            }
        }

        @Test
        void end_value_null() throws InterpretationException, TemplateSyntaxException {
            TestClass testClass = new TestClass(null, 23899);
            HashMap<String, Object> map = new HashMap<>();
            map.put("testObject", testClass);
            map.put("dummyVar", "testing String");
            assertNull(getNestedReplacement(VarStatement.create("testObject.testString"), map));
        }

        @Test
        void private_var() throws InterpretationException, TemplateSyntaxException {
            class PrivateClass {
                private final String testString;
                final int testInteger;

                public PrivateClass(String testString, int testInteger) {
                    this.testString = testString;
                    this.testInteger = testInteger;
                }
            }
            PrivateClass testClass = new PrivateClass("testString", 23899);
            HashMap<String, Object> map = new HashMap<>();
            map.put("testObject", testClass);
            map.put("dummyVar", "testing String");
            assertEquals("testString", getNestedReplacement(VarStatement.create("testObject.testString"), map));
        }

        @Test
        void missing_field() throws InterpretationException, TemplateSyntaxException {
            TestClass testClass = new TestClass("testString", 23899);
            HashMap<String, Object> map = new HashMap<>();
            map.put("testObject", testClass);
            map.put("dummyVar", "testing String");
            try {
                getNestedReplacement(VarStatement.create("testObject.testBoolean"), map);
            } catch (FieldDoesNotExistException _) {}
        }
    }
}
