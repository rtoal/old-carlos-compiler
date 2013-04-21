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

    void assertAssignableTo(Type otherType, AnalysisContext context, String construct) {
        if (!this.isCompatibleWith(otherType)) {
            context.error("type_mismatch", construct, otherType.getName(), this.type.getName());
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

    void assertBoolean(String operator, AnalysisContext context) {
        if (!(type == Type.BOOLEAN)) {
            context.error("non_boolean", operator);
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
}
