package edu.lmu.cs.xlg.carlos.entities;

/**
 * A statement of the form "while (e) b".
 */
public class WhileStatement extends Statement {

    private Expression condition;
    private Block body;

    public WhileStatement(Expression condition, Block body) {
        this.condition = condition;
        this.body = body;
    }

    public Expression getCondition() {
        return condition;
    }

    public Block getBody() {
        return body;
    }

    @Override
    public void analyze(AnalysisContext context) {
        condition.analyze(context);
        condition.assertBoolean("while_condition_not_boolean", context);
        body.analyze(context.withInLoop(true));
    }

    @Override
    public Statement optimize() {
        condition = condition.optimize();
        body.optimize();
        if  (condition.isFalse()) {
            // "while (false)" is a no-op
            return null;
        }
        return this;
    }
}
