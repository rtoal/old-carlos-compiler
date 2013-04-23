package edu.lmu.cs.xlg.carlos.entities;

/**
 * A prefix expression, containing an operator and an operand.
 */
public class PrefixExpression extends Expression {

    private String op;
    private Expression operand;

    public PrefixExpression(String op, Expression operand) {
        this.op = op;
        this.operand = operand;
    }

    public String getOp() {
        return op;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public void analyze(AnalysisContext context) {
        operand.analyze(context);

        if ("!".equals(op)) {
            operand.assertBoolean("boolean_operand_expected_for_not", context);
            type = Type.BOOLEAN;

        } else if ("-".equals(op)) {
            operand.assertArithmetic(op, context);
            type = operand.type;

        } else if ("~".equals(op)) {
            operand.assertInteger(op, context);
            type = Type.INT;

        } else if ("int".equals(op)) {
            operand.assertChar(op, context);
            type = Type.INT;

        } else if ("char".equals(op)) {
            operand.assertInteger(op, context);
            type = Type.CHAR;

        } else if ("string".equals(op)) {
            type = Type.STRING;

        } else if ("length".equals(op)) {
            operand.assertArrayOrString(op, context);
            type = Type.INT;

        } else if ("++".equals(op) || "--".equals(op)) {
            operand.assertInteger(op, context);
            VariableExpression.class.cast(operand).assertWritable(context);
            type = Type.INT;

        } else {
            context.error("compiler_bug");
            type = Type.ARBITRARY;
        }
    }
}
