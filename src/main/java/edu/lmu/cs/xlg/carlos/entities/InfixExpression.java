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
            left.assertBoolean("logical_operand_not_boolean", context);
            right.assertBoolean("logical_operand_not_boolean", context);
            type = Type.BOOLEAN;
        }
    }

    @Override
    public Expression optimize() {
        left = left.optimize();
        right = right.optimize();
        if (left.getType().isArithmetic() && right.getType().isArithmetic()) {
            return optimizeArithmetic();
        } else if (left.getType().equals(Type.BOOLEAN) && right.getType().equals(Type.BOOLEAN)) {
            return optimizeBoolean();
        } else {
            return this;
        }
    }

    public Expression optimizeBoolean() {
        if (op == "&&") {
            if (left.isFalse() || right.isFalse()) {
                return BooleanLiteral.FALSE;
            } else if (left.isTrue()) {
                return right;
            } else if (right.isTrue()) {
                return left;
            } else if (left.sameVariableAs(right)) {
                return left;
            }
        } else if (op == "||") {
            if (left.isTrue() || right.isTrue()) {
                return BooleanLiteral.TRUE;
            } else if (left.isFalse()) {
                return right;
            } else if (right.isFalse()) {
                return left;
            } else if (left.sameVariableAs(right)) {
                return left;
            }
        }

        // Can't optimize it
        return this;
    }

    public Expression optimizeArithmetic() {

        if (left instanceof Literal && right instanceof Literal) {
            // Constant Folding
            double x = constantValue(left);
            double y = constantValue(right);

            if ("+".equals(op)) return RealLiteral.fromValue(x + y);
            else if ("-".equals(op)) return RealLiteral.fromValue(x - y);
            else if ("*".equals(op)) return RealLiteral.fromValue(x * y);
            else if ("/".equals(op) && y != 0) return RealLiteral.fromValue(x / y);
            else if ("<".equals(op)) return BooleanLiteral.fromValue(x < y);
            else if ("<=".equals(op)) return BooleanLiteral.fromValue(x <= y);
            else if ("==".equals(op)) return BooleanLiteral.fromValue(x == y);
            else if ("!=".equals(op)) return BooleanLiteral.fromValue(x != y);
            else if (">=".equals(op)) return BooleanLiteral.fromValue(x >= y);
            else if (">".equals(op)) return BooleanLiteral.fromValue(x > y);

        } else if ("+".equals(op)) {
            if (right.isZero()) return left;
            if (left.isZero()) return right;
        } else if ("-".equals(op)) {
            if (right.isZero()) return left;
            if (left.sameVariableAs(right)) return RealLiteral.fromValue(0);
        } else if ("*".equals(op)) {
            if (right.isOne()) return left;
            if (left.isOne()) return right;
            if (right.isZero()) return RealLiteral.fromValue(0);
            if (left.isZero()) return RealLiteral.fromValue(0);
        } else if ("/".equals(op)) {
            if (right.isOne()) return left;
            if (left.sameVariableAs(right)) return RealLiteral.fromValue(1);
        }

        // Could not find any optimizations
        return this;
    }
}
