package edu.lmu.cs.xlg.carlos.entities;

import java.util.List;
import java.util.ListIterator;

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

    /**
     * Returns false, as one can never write to a function call result.
     */
    public boolean isWritable() {
        return false;
    }

    @Override
    public void analyze(AnalysisContext context) {

        // Analyze all the arguments
        for (Expression a: args) {
            a.analyze(context);
        }

        // Find out which function we're calling
        function = context.lookupFunction(functionName, args);

        if (function == null) {
            // If we can't find the function, just forget it
            type = Type.ARBITRARY;
            return;
        }

        // Since called from expression, must have a return type
        if (function.getReturnType() == null) {
            context.error("void_function_in_expression", functionName);
            type = Type.ARBITRARY;
        } else {
            type = function.getReturnType();
        }
    }

    @Override
    public Expression optimize() {
        for (ListIterator<Expression> it = args.listIterator(); it.hasNext();) {
            it.set(it.next().optimize());
        }
        return this;
    }
}
