package edu.lmu.cs.xlg.carlos.entities;

/**
 * An assignment statement.
 */
public class AssignmentStatement extends Statement {

    private VariableExpression left;
    private Expression right;

    public AssignmentStatement(VariableExpression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public VariableExpression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public void analyze(AnalysisContext context) {
        left.analyze(context);
        right.analyze(context);
        left.assertWritable(context);
        right.assertAssignableTo(left.type, context, "assignment");
    }
}
