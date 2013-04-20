package edu.lmu.cs.xlg.carlos.entities;

/**
 * A postfix expression.
 */
public class PostfixExpression extends Expression {

    private String op;
    private VariableExpression operand;

    public PostfixExpression(VariableExpression operand, String op) {
        this.op = op;
        this.operand = operand;
    }

    public String getOp() {
        return op;
    }

    public VariableExpression getOperand() {
        return operand;
    }

    @Override
    public void analyze(AnalysisContext context) {
        operand.analyze(context);
        operand.assertInteger(op, context);
        operand.assertWritable(context);
        type = Type.INT;
   }
}
