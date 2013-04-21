package edu.lmu.cs.xlg.carlos.entities;

/**
 * An infix expression, with an operator and two operands, a left
 * and a right.
 */
public class InfixExpression extends Expression {

    private String op;
    private Expression left;
    private Expression right;

    public InfixExpression(Expression left, String op, Expression right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public String getOp() {
        return op;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public void analyze(AnalysisContext context) {
        left.analyze(context);
        right.analyze(context);

        // num op num (for arithmetic op)
        if (op.matches("\\+|-|\\*|/")) {
            left.assertArithmetic(op, context);
            right.assertArithmetic(op, context);
            type = (left.type == Type.REAL || right.type == Type.REAL)
                ? Type.REAL : Type.INT;

        // int op int (for shift, mod, or bitwise op)
        } else if (op.matches("%|<<|>>|&|\\||\\^")) {
            left.assertInteger(op, context);
            right.assertInteger(op, context);
            type = Type.INT;

        // char/num/str op char/num/str (for inequalities except !=)
        } else if (op.matches("<|<=|>|>=")) {
            if (left.type == Type.CHAR) {
                right.assertChar(op, context);
            } else if (left.type == Type.STRING) {
                right.assertString(op, context);
            } else if (left.type.isArithmetic()){
                left.assertArithmetic(op, context);
                right.assertArithmetic(op, context);
            }
            type = Type.BOOLEAN;

        // any == any
        // any != any
        } else if (op.matches("==|!=")) {
            if (!(left.isCompatibleWith(right.type)
            || right.isCompatibleWith(left.type))) {
                context.error("non_compatible", op, left.type.getName(), right.type.getName());
            }
            type = Type.BOOLEAN;

        // bool && bool
        // bool || bool
        } else if (op.matches("&&|\\|\\|")) {
            left.assertBoolean(op, context);
            right.assertBoolean(op, context);
            type = Type.BOOLEAN;
        }
    }
}
