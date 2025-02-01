package talium.system.templateParser;

import talium.system.templateParser.exeptions.VariableValueNullException;
import talium.system.templateParser.exeptions.UnIterableArgumentException;
import talium.system.templateParser.exeptions.UnsupportedComparandType;
import talium.system.templateParser.exeptions.UnsupportedComparisonOperator;
import talium.system.templateParser.ifParser.IfInterpreter;
import talium.system.templateParser.statements.*;
import talium.system.templateParser.tokens.Comparison;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

/**
 * Populates variables and interprets parsed String templates
 */
public class TemplateInterpreter {

    /**
     * Populates variables and interprets parsed String templates
     *
     * @param template parsed template as list of statements
     * @param values a map with all top level variables and their names
     * @return the resulting string
     */
    public static String populate(List<Statement> template, HashMap<String, Object> values) throws NoSuchFieldException, VariableValueNullException, IllegalAccessException, UnIterableArgumentException, UnsupportedComparandType, UnsupportedComparisonOperator {
        if (values == null) {
            values = new HashMap<>();
        }
        String out = "";
        for (Statement statement : template) {
            if (statement instanceof TextStatement textStatement) {
                out += textStatement.text();
            } else if (statement instanceof VarStatement varStatement) {
                out += getNestedReplacement(varStatement.name(), values).toString();
            } else if (statement instanceof IfStatement ifStatement) {
                // replace Vars with actual values
                Object left = ifStatement.comparison().left();
                if (left instanceof VarStatement leftVar) {
                    left = castToValidInput(getNestedReplacement(leftVar.name(), values));
                }
                Object right = ifStatement.comparison().right();
                if (right instanceof VarStatement rightVar) {
                    right = castToValidInput(getNestedReplacement(rightVar.name(), values));
                }

                boolean condition = IfInterpreter.compare(new Comparison(left, ifStatement.comparison().equals(), right));
                if (condition) {
                    out += populate(ifStatement.then(), values);
                } else {
                    out += populate(ifStatement.other(), values);
                }
            } else if (statement instanceof LoopStatement loop) {
                String[] varParts = loop.var().split("\\[*]");
                Object nestedReplacement = getNestedReplacement(varParts[0], values);

                if (!(nestedReplacement instanceof Iterable<?>)) {
                    throw new UnIterableArgumentException(varParts[0]);
                }

                //noinspection unchecked
                for (Object item : (Iterable<Object>) nestedReplacement) {
                    values.put(loop.name(), item);
                    if (varParts.length > 1) {
                        values.put(loop.name(), getNestedReplacement(varParts[1], values));
                    }
                    out += populate(loop.body(), values);
                }
                values.remove(loop.name());
            }
        }
        return out;
    }

    /**
     * Casts all into String, Character, Boolean, or Number for later Comparison Operations.
     * <br><br>
     * If the input is of type String, Character, Boolean, or Number, nothing is done and the input is returned as-is.
     * If the Input is of a different type, Object.toString() is called on it to make it into a String.
     *
     * @apiNote This function garanties to only return values of type String, Character, Boolean, or Number
     * @see Object#toString()
     * @param input original object
     * @return the value, of type String, Character, Boolean, Number (superclass over int, float, short, ...)
     */
    public static Object castToValidInput(Object input) {
        if (input instanceof String || input instanceof Character || input instanceof Number || input instanceof Boolean) {
            return input;
        } else {
            return input.toString();
        }
    }

    /**
     * Tries to resolve and get the Value at the given variable path.
     * Throws an Exception if the path does not exist.
     *
     * @param varName dot . delimited path of variable names
     * @param values  List of top level variables
     * @return value of the variable
     * @apiNote Does not support getters or any functions
     */
    public static Object getNestedReplacement(String varName, HashMap<String, Object> values) throws VariableValueNullException, NoSuchFieldException, IllegalAccessException {
        String[] variableNames = varName.split("\\.");
        //TODO make explicit check if key exists, and throw specific error if it doesn't exist
        if (!values.containsKey(variableNames[0])) {
            throw new NoSuchFieldException("No key with name " + variableNames[0] + " in variable map");
        }
        Object variable = values.get(variableNames[0]);
        for (int i = 1; i < variableNames.length; i++) {
            if (variable == null) {
                throw new VariableValueNullException(variableNames[i - 1]);
            }
            Field declaredField = variable.getClass().getDeclaredField(variableNames[i]);
            declaredField.setAccessible(true);
            variable = declaredField.get(variable);
        }
        if (variable == null) {
            //TODO
        }
        return variable;
    }
}


