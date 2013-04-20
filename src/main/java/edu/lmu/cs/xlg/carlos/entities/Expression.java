package edu.lmu.cs.xlg.carlos.entities;

import edu.lmu.cs.xlg.util.Log;

/**
 * Superclass for all expressions.
 */
public abstract class Expression extends Entity {

    Type type;

    public Type getType() {
        return type;
    }

    /**
     * Returns whether this expression is compatible with (that is, "can
     * be assigned to an object of") a given type.
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

    // Helpers for semantic analysis, called from the analyze methods of other expressions.
    // These are by no means necessary, but they are very convenient.

    void assertAssignableTo(Type otherType, Log log, String errorKey) {
        if (!this.isCompatibleWith(otherType)) {
            log.error(errorKey, otherType.getName(), this.type.getName());
        }
    }

    void assertArithmetic(String context, Log log) {
        if (!(type == Type.INT || type == Type.REAL)) {
            log.error("non_arithmetic", context);
        }
    }

    void assertInteger(String context, Log log) {
        if (!(type == Type.INT)) {
            log.error("non_integer", context);
        }
    }

    void assertBoolean(String context, Log log) {
        if (!(type == Type.BOOLEAN)) {
            log.error("non_boolean", context);
        }
    }

    void assertChar(String context, Log log) {
        if (!(type == Type.CHAR)) {
            log.error("non_char", context);
        }
    }

    void assertArray(String context, Log log) {
        if (!(type instanceof ArrayType)) {
            log.error("non_array", context);
        }
    }

    void assertString(String context, Log log) {
        if (!(type == Type.STRING)) {
            log.error("non_string", context);
        }
    }

    void assertArrayOrString(String context, Log log) {
        if (!(type == Type.STRING || type instanceof ArrayType)) {
            log.error("non_array_or_string", context);
        }
    }
}
