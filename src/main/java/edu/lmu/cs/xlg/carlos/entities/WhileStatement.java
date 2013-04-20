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
        condition.assertBoolean("while_condition_not_boolean", context.getLog());
        body.analyze(context.withInLoop(true));
    }
}
