package talium.templateParser.statements;

import java.util.List;

public record LoopStatement(String varName, VarStatement var, List<Statement> body) implements Statement {}
