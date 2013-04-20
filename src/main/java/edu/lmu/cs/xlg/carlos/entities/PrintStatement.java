package edu.lmu.cs.xlg.carlos.entities;

import java.util.List;

/**
 * A simple print statement.
 */
public class PrintStatement extends Statement {

    private List<Expression> args;

    public PrintStatement(List<Expression> args) {
        this.args = args;
    }

    public List<Expression> getArgs() {
        return args;
    }

    public void analyze(AnalysisContext context) {
        for (Expression a : args) {
            a.analyze(context);
        }
    }
}
