package edu.lmu.cs.xlg.carlos.entities;

import java.util.List;
import java.util.ListIterator;

/**
 * A function call appearing in a statement.  The function being called must be a void function.
 */
public class CallStatement extends Statement {

    private String functionName;
    private List<Expression> args;
    private Function function;

    public CallStatement(String id, List<Expression> args) {
        this.functionName = id;
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

        // Analyze arguments first
        for (Expression arg: args) {
            arg.analyze(context);
        }

        // Find out which function we're referring to.
        function = context.lookupFunction(functionName, args);

        // Ensure it is void
        if (function != null && function.getReturnType() != null) {
            context.error("non_void_function_in_statement", functionName);
        }
    }

    @Override
    public Statement optimize() {
        for (ListIterator<Expression> it = args.listIterator(); it.hasNext();) {
            it.set(it.next().optimize());
        }
        return this;
    }
}
