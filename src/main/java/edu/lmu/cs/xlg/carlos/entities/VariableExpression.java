package edu.lmu.cs.xlg.carlos.entities;

/**
 * Superclass for all variable expressions. There are several kinds of variable expressions: simple
 * variable references (a single id), subscripted variable expressions for arrays and strings (v[e]),
 * dotted variable expressions (v.i), and function call results.
 */
public abstract class VariableExpression extends Expression {

    /**
     * Returns whether or not this variable expression denotes a non read-only variable.
     */
    public abstract boolean isWritable();

    /**
     * Logs an error if the variable expression denotes a read-only variable.
     */
    public void assertWritable(AnalysisContext context) {
        if (!isWritable()) {
            context.error("read_only_error");
        }
    }
}
