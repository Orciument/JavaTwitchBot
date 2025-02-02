package talium.system.templateParser;

import talium.system.templateParser.exeptions.*;
import talium.system.templateParser.ifParser.IfInterpreter;
import talium.system.templateParser.statements.*;
import talium.system.templateParser.tokens.Comparison;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Populates variables and interprets parsed String templates
 */
public class TemplateInterpreter {

    /**
     * Populates variables and interprets parsed String templates
     *
     * @param template parsed template as list of statements
     * @param environment a map with all top level variables and their names
     * @return the resulting string
     */
    public static String populate(List<Statement> template, Map<String, Object> environment) throws InterpretationException  {
        if (environment == null) {
            environment = new HashMap<>();
        }
        StringBuilder out = new StringBuilder();
        for (Statement statement : template) {
            if (statement instanceof TextStatement(String text)) {
                out.append(text);
            } else if (statement instanceof VarStatement varStatement) {
                Object nestedReplacement = getNestedReplacement(varStatement, environment);
                out.append(nestedReplacement);
            } else if (statement instanceof IfStatement(Comparison comparison, List<Statement> then, List<Statement> other)) {
                // replace Vars with actual values
                Object left = comparison.left();
                if (left instanceof VarStatement leftVar) {
                    left = castToValidInput(getNestedReplacement(leftVar, environment));
                }
                Object right = comparison.right();
                if (right instanceof VarStatement rightVar) {
                    right = castToValidInput(getNestedReplacement(rightVar, environment));
                }

                if (IfInterpreter.compare(new Comparison(left, comparison.equals(), right))) {
                    out.append(populate(then, environment));
                } else {
                    out.append(populate(other, environment));
                }
            } else if (statement instanceof LoopStatement(String varName, VarStatement var, List<Statement> body)) {
                Object list = getNestedReplacement(var, environment);

                if (!(list instanceof Iterable<?>)) {
                    throw new UnIterableArgumentException(var.accessExpr());
                }

                for (Object item : (Iterable<?>) list) {
                    environment.put(varName, item);
                    out.append(populate(body, environment));
                }
                environment.remove(varName);
            }
        }
        return out.toString();
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
     * Tries to resolve and get the Value at the given field access expression from the environment map.
     *
     * @param varExpr dot . delimited path of variable names
     * @param environment List of top level variables
     * @return value of the variable, this can be null, if the last field is returning the null value
     * @apiNote Does not support getters or executing any functions
     * @throws FieldDoesNotExistException if the variable to be accessed does not exist on the object, or in the environment
     * @throws VariableValueNullException if the value of a variable is null, but a field on this variable is supposed to be accessed
     * @throws FieldNotAccessibleException if the value of the field can not be retrieved because of java visibility checks (e.g. private
     * @throws InterpretationException is thrown when a state is reached that should not be possible. This indicates an actual bug, instead of an error with the template string.
     */
    public static Object getNestedReplacement(VarStatement varExpr, Map<String, Object> environment) throws InterpretationException  {
        String[] variableNames = varExpr.accessExpr().split("\\.");
        if (variableNames.length == 0) {
            //TODO kinda a panic, we should never be here
            throw new InterpretationException("");
        }
        if (!environment.containsKey(variableNames[0])) {
            throw new FieldDoesNotExistException(variableNames[0], environment.keySet());
        }
        Object variable = environment.get(variableNames[0]);
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


