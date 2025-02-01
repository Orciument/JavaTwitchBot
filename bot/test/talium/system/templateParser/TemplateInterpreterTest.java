package talium.system.templateParser;

import talium.system.templateParser.exeptions.VariableValueNullException;
import talium.system.templateParser.exeptions.UnIterableArgumentException;
import talium.system.templateParser.exeptions.UnsupportedComparandType;
import talium.system.templateParser.exeptions.UnsupportedComparisonOperator;
import talium.system.templateParser.statements.*;
import talium.system.templateParser.tokens.Comparison;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static talium.system.templateParser.TemplateInterpreter.getNestedReplacement;
import static talium.system.templateParser.TemplateInterpreter.populate;
import static org.junit.jupiter.api.Assertions.fail;

public class TemplateInterpreterTest {
    static List<Statement> TEMPLATE_VAR = Arrays.asList(
            new TextStatement("Hello, "),
            new VarStatement("name"),
            new TextStatement("!")
    );
    static List<Statement> TEMPLATE_IF = Arrays.asList(
            new TextStatement("Hello, "),
            new IfStatement(new Comparison(new VarStatement("compLeft"), Equals.EQUALS, "testString"),
                    List.of(new VarStatement("compLeft")),
                    List.of(new TextStatement("unnamed"))
            ),
            new TextStatement("!")
    );
    static List<Statement> TEMPLATE_LOOP = List.of(
            new TextStatement("Hello,"),
            new LoopStatement("varName", "loopVar", Arrays.asList(
                    new TextStatement(" "),
                    new VarStatement("varName")
            )),
            new TextStatement("!")
    );
    static List<Statement> NESTED_LOOP = Arrays.asList(
            new TextStatement("Hello,"),
            new LoopStatement("innerLoop", "loopVar", Arrays.asList(
                    new LoopStatement("varName", "innerLoop", Arrays.asList(
                            new TextStatement(" "),
                            new VarStatement("varName")
                    )),
                    new TextStatement(" |")
            )),
            new TextStatement("!")
    );

    @Nested
    class Populate {
        @Test
        void iterate_non_iterable() throws NoSuchFieldException, IllegalAccessException, VariableValueNullException, UnsupportedComparandType, UnsupportedComparisonOperator {
            HashMap<String, Object> map = new HashMap<>();
            map.put("loopVar", "this_is_not_a_list");
            try {
                System.out.println(populate(TEMPLATE_LOOP, map));
                fail("Should have thrown UnIterableArgumentException");
            } catch (UnIterableArgumentException _) {
            }
        }

        @Test
        void var() throws NoSuchFieldException, IllegalAccessException, VariableValueNullException, UnIterableArgumentException, UnsupportedComparandType, UnsupportedComparisonOperator {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", "dummy name");
            assert populate(TEMPLATE_VAR, map).equals("Hello, dummy name!");
        }

        @Test
        void iterate_empty_list() throws NoSuchFieldException, IllegalAccessException, VariableValueNullException, UnIterableArgumentException, UnsupportedComparandType, UnsupportedComparisonOperator {
            HashMap<String, Object> map = new HashMap<>();
            map.put("loopVar", new ArrayList<>());
            assert populate(TEMPLATE_LOOP, map).equals("Hello,!");
        }

        @Test
        void nestedLoop() throws NoSuchFieldException, IllegalAccessException, VariableValueNullException, UnIterableArgumentException, UnsupportedComparandType, UnsupportedComparisonOperator {
            HashMap<String, Object> map = new HashMap<>();
            ArrayList<List<String>> list = new ArrayList<>();
            list.add(List.of("t-1.1", "t-1.2"));
            list.add(List.of("t-2.1", "t-2.2"));
            map.put("loopVar", list);
            assert populate(NESTED_LOOP, map).equals("Hello, t-1.1 t-1.2 | t-2.1 t-2.2 |!");
        }

        @Test
        void if_() throws NoSuchFieldException, IllegalAccessException, VariableValueNullException, UnIterableArgumentException, UnsupportedComparandType, UnsupportedComparisonOperator {
            HashMap<String, Object> map = new HashMap<>();
            map.put("compLeft", "testString");
            assert populate(TEMPLATE_IF, map).equals("Hello, testString!");
        }

        @Test
        void wrong_condition_type() throws NoSuchFieldException, IllegalAccessException, VariableValueNullException, UnIterableArgumentException, UnsupportedComparisonOperator {
            HashMap<String, Object> map = new HashMap<>();
            map.put("compLeft", false);
            try {
                populate(TEMPLATE_IF, map);
                fail("Should have thrown UnIterableArgumentException");
            } catch (UnsupportedComparandType _) {
            }
        }
    }

    @Nested
    class NestedReplacement {
        private record TestClass(String testString, int testInteger) {
        }

        @Test
        void string() throws NoSuchFieldException, IllegalAccessException, VariableValueNullException {
            HashMap<String, Object> map = new HashMap<>();
            map.put("test", "testing String");
            map.put("dummyVar", 23899);
            assert getNestedReplacement("test", map).equals("testing String");
        }

        @Test
        void integer() throws NoSuchFieldException, IllegalAccessException, VariableValueNullException {
            HashMap<String, Object> map = new HashMap<>();
            map.put("integerValue", 23899);
            map.put("dummyVar", "testing String");
            assert getNestedReplacement("integerValue", map).equals(23899);
        }

        @Test
        void object_path() throws NoSuchFieldException, IllegalAccessException, VariableValueNullException {
            TestClass testClass = new TestClass("testString", 23899);
            HashMap<String, Object> map = new HashMap<>();
            map.put("testObject", testClass);
            map.put("dummyVar", "testing String");
            assert getNestedReplacement("testObject.testInteger", map).equals(23899);
        }

        @Test
        void list() throws NoSuchFieldException, IllegalAccessException, VariableValueNullException {
            HashMap<String, Object> map = new HashMap<>();
            ArrayList<String> list = new ArrayList<>();
            list.add("dummyValue");
            list.add("dummyString");
            map.put("listVar", list);
            map.put("dummyVar", "testing String");
            assert getNestedReplacement("listVar", map).equals(list);
        }

        @Test
        void toplevel_null() throws NoSuchFieldException, IllegalAccessException {
            HashMap<String, Object> map = new HashMap<>();
            map.put("testObject", null);
            map.put("dummyVar", "testing String");
            try {
                getNestedReplacement("testObject.testInteger", map);
                fail("Should have thrown NullArgumentException");
            } catch (VariableValueNullException _) {
            }
        }

        @Test
        void end_value_null() throws NoSuchFieldException, IllegalAccessException, VariableValueNullException {
            TestClass testClass = new TestClass(null, 23899);
            HashMap<String, Object> map = new HashMap<>();
            map.put("testObject", testClass);
            map.put("dummyVar", "testing String");
            assert getNestedReplacement("testObject.testString", map) == null;
        }

        @Test
        void private_var() throws NoSuchFieldException, IllegalAccessException, VariableValueNullException {
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
            assert getNestedReplacement("testObject.testString", map).equals("testString");
        }

        @Test
        void missing_field() throws IllegalAccessException, VariableValueNullException {
            TestClass testClass = new TestClass("testString", 23899);
            HashMap<String, Object> map = new HashMap<>();
            map.put("testObject", testClass);
            map.put("dummyVar", "testing String");
            try {
                getNestedReplacement("testObject.testBoolean", map);
            } catch (NoSuchFieldException _) {
            }
        }
    }
}
