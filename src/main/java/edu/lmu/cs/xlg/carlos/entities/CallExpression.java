package edu.lmu.cs.xlg.carlos.entities;

import java.util.List;

/**
 * The result of a function call, which is a variable.
 */
public class CallExpression extends VariableExpression {

    private String functionName;
    private List<Expression> args;
    private Function function;

    public CallExpression(String functionName, List<Expression> args) {
        this.functionName = functionName;
        this.args = args;
    }

    public List<Expression> getArgs() {
        return args;
    }

    public Function getFunction() {
        return function;
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public void analyze(AnalysisContext context) {

        // Analyze all the arguments
        for (Expression a: args) {
            a.analyze(context);
        }

        // Find out which function we're calling
        function = context.getTable().lookupFunction(functionName, args, context.getLog());

        if (function == null) {
            // If we can't find the function, just forget it
            type = Type.ARBITRARY;
            return;
        }

        // Since called from expression, must have a return type
        if (function.getReturnType() == null) {
            context.getLog().error("void_function_in_expression", functionName);
            type = Type.ARBITRARY;
        } else {
            type = function.getReturnType();
        }
    }

    /**
     * Returns false, as one can never write to a function call result.
     */
    public boolean isWritable() {
        return false;
    }
}
