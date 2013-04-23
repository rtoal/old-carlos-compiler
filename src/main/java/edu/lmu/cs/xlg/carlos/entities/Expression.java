package edu.lmu.cs.xlg.carlos.entities;

/**
 * Superclass for all expressions.
 */
public abstract class Expression extends Entity {

    Type type;

    public Type getType() {
        return type;
    }

    /**
     * Returns whether this expression is compatible with (that is, "can be assigned to an object
     * of") a given type.
     */
    public boolean isCompatibleWith(Type testType) {
        return this.type == testType
            || this.type == Type.INT && testType == Type.REAL
            || this.type == Type.NULL_TYPE && testType.isReference()
            || this.type instanceof ArrayType && testType == Type.ARRAY_OR_STRING
            || this.type == Type.STRING && testType == Type.ARRAY_OR_STRING
            || this.type == Type.ARBITRARY
            || testType == Type.ARBITRARY;
    }

    // Helpers for semantic analysis, called from the analyze methods of other expressions.  These
    // are by no means necessary, but they are very convenient.

    void assertAssignableTo(Type otherType, String resourceKey, AnalysisContext context) {
        if (!this.isCompatibleWith(otherType)) {
            context.error(resourceKey, otherType.getName(), this.type.getName());
        }
    }

    void assertArithmetic(String operator, AnalysisContext context) {
        if (!(type == Type.INT || type == Type.REAL)) {
            context.error("non_arithmetic", operator);
        }
    }

    void assertInteger(String operator, AnalysisContext context) {
        if (!(type == Type.INT)) {
            context.error("non_integer", operator);
        }
    }

    void assertBoolean(String resourceKey, AnalysisContext context) {
        if (!(type == Type.BOOLEAN)) {
            context.error(resourceKey);
        }
    }

    void assertChar(String operator, AnalysisContext context) {
        if (!(type == Type.CHAR)) {
            context.error("non_char", operator);
        }
    }

    void assertArray(String operator, AnalysisContext context) {
        if (!(type instanceof ArrayType)) {
            context.error("non_array", operator);
        }
    }

    void assertString(String operator, AnalysisContext context) {
        if (!(type == Type.STRING)) {
            context.error("non_string", operator);
        }
    }

    void assertArrayOrString(String operator, AnalysisContext context) {
        if (!(type == Type.STRING || type instanceof ArrayType)) {
            context.error("non_array_or_string", operator);
        }
    }

    /**
     * Optimizes this expression, returning an optimized version if possible, otherwise returns
     * the expression itself.  This method is intended to be overridden in subclasses; however,
     * since most forms of expressions require no optimization, a default implementation is
     * provided here.
     */
    public Expression optimize() {
        return this;
    }

    boolean isZero() {
        return (this instanceof IntegerLiteral && IntegerLiteral.class.cast(this).getValue() == 0)
                || (this instanceof RealLiteral && RealLiteral.class.cast(this).getValue() == 0);
    }

    boolean isOne() {
        return (this instanceof IntegerLiteral && IntegerLiteral.class.cast(this).getValue() == 1)
                || (this instanceof RealLiteral && RealLiteral.class.cast(this).getValue() == 1);
    }

    boolean isFalse() {
        return BooleanLiteral.FALSE.equals(this);
    }

    boolean isTrue() {
        return BooleanLiteral.TRUE.equals(this);
    }

    boolean sameVariableAs(Expression that) {
        return this instanceof SimpleVariableReference && that instanceof SimpleVariableReference &&
                SimpleVariableReference.class.cast(this).getReferent() ==
                SimpleVariableReference.class.cast(that).getReferent();
    }

    double constantValue(Expression e) {
        if (e instanceof RealLiteral) {
            return RealLiteral.class.cast(e).getValue();
        } else if (e instanceof IntegerLiteral) {
            return IntegerLiteral.class.cast(e).getValue();
        } else {
            throw new RuntimeException("Internal Error in Optimizer");
        }
    }
}
