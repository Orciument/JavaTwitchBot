package talium.system.templateParser;

import talium.system.templateParser.exeptions.*;
import talium.system.templateParser.ifParser.IfInterpreter;
import talium.system.templateParser.statements.*;
import talium.system.templateParser.tokens.Comparison;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
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
    public static String populate(List<Statement> template, HashMap<String, Object> values) throws InterpretationException  {
        if (values == null) {
            values = new HashMap<>();
        }
        String out = "";
        for (Statement statement : template) {
            if (statement instanceof TextStatement textStatement) {
                out += textStatement.text();
            } else if (statement instanceof VarStatement varStatement) {
                Object nestedReplacement = getNestedReplacement(varStatement, values);
                if (nestedReplacement == null) {
                    out += "null";
                } else {
                    out += nestedReplacement.toString();
                }
            } else if (statement instanceof IfStatement ifStatement) {
                // replace Vars with actual values
                Object left = ifStatement.comparison().left();
                if (left instanceof VarStatement leftVar) {
                    left = castToValidInput(getNestedReplacement(leftVar, values));
                }
                Object right = ifStatement.comparison().right();
                if (right instanceof VarStatement rightVar) {
                    right = castToValidInput(getNestedReplacement(rightVar, values));
                }

                boolean condition = IfInterpreter.compare(new Comparison(left, ifStatement.comparison().equals(), right));
                if (condition) {
                    out += populate(ifStatement.then(), values);
                } else {
                    out += populate(ifStatement.other(), values);
                }
            } else if (statement instanceof LoopStatement loop) {
                String[] varParts = loop.var().split("\\[*]");
                //TODO here is probably also an error
                // this varstatement should be created in parsing
                Object nestedReplacement = null;
                try {
                    nestedReplacement = getNestedReplacement(VarStatement.create(varParts[0]), values);
                } catch (TemplateSyntaxException _) {}

                if (!(nestedReplacement instanceof Iterable<?>)) {
                    throw new UnIterableArgumentException(varParts[0]);
                }

                //noinspection unchecked
                for (Object item : (Iterable<Object>) nestedReplacement) {
                    values.put(loop.name(), item);
                    if (varParts.length > 1) {
                        try {
                        values.put(loop.name(), getNestedReplacement(VarStatement.create(varParts[1]), values));
                        } catch (TemplateSyntaxException _) {}
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

    //TODO this should probably be tested more
    /**
     * Tries to resolve and get the Value at the given variable path.
     * Throws an Exception if the path does not exist.
     *
     * @param varExpr dot . delimited path of variable names
     * @param values  List of top level variables
     * @return value of the variable, this can be null, if the last field is returning the null value
     * @apiNote Does not support getters or any functions
     */
    public static Object getNestedReplacement(VarStatement varExpr, HashMap<String, Object> values) throws InterpretationException  {
        String[] variableNames = varExpr.accessExpr().split("\\.");
        if (variableNames.length == 0) {
            //TODO kinda a panic, we should never be here
            throw new InterpretationException("");
        }
        if (!values.containsKey(variableNames[0])) {
            throw new FieldDoesNotExistException(variableNames[0], values.keySet());
        }
        Object variable = values.get(variableNames[0]);
        for (int i = 1; i < variableNames.length; i++) {
            if (variable == null) {
                throw new VariableValueNullException(variableNames[i - 1]);
            }
            try {
                //TODO check if variable name is null
                Field declaredField = variable.getClass().getDeclaredField(variableNames[i]);
                declaredField.setAccessible(true);
                variable = declaredField.get(variable);
            } catch (NoSuchFieldException _) {
                throw new FieldDoesNotExistException(variableNames[i], variable.getClass());
            } catch (IllegalAccessException | InaccessibleObjectException | SecurityException e) {
                throw new FieldNotAccessibleException(variableNames[i], variable.getClass(), e);
            }
            catch (IllegalArgumentException _) {
                //TODO kinda panic, because we should never be here, this means we have mixed up the underlying type of
                // declaredField, because of type erasure in getDeclaredField(...)
                throw new InterpretationException("");
            }
            catch (ExceptionInInitializerError e) {
                //TODO kinda panic, because this indicates something very weird that should not happen.
                // not sure what this is, but sounds above my pay grade
                throw new InterpretationException("", e);
            }
            catch (NullPointerException _) {
                //TODO kinda panic, the name should never be null, not allowed by parsing
                throw new InterpretationException("");
            }
        }
        return variable;
    }
}


