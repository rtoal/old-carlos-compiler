package edu.lmu.cs.xlg.carlos.entities;

/**
 * A statement of the form v++, v--, ++v, or --v, where v is a variable.
 * Note that whether the operator is "prefix" or "postfix" does not matter
 * for statements.
 */
public class IncrementStatement extends Statement {

    private VariableExpression target;
    private String op;

    public IncrementStatement(VariableExpression target, String op) {
        this.target = target;
        this.op = op;
    }

    public String getOp() {
        return op;
    }

    public VariableExpression getTarget() {
        return target;
    }

    @Override
    public void analyze(AnalysisContext context) {
        target.analyze(context);
        target.assertInteger(op, context);
    }

    @Override
    public Statement optimize() {
        target = VariableExpression.class.cast(target.optimize());
        return this;
    }
}
