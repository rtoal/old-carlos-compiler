package edu.lmu.cs.xlg.carlos.entities;

/**
 * Superclass of all statements.
 */
public abstract class Statement extends Entity {

    /**
     * Optimizes this statement, returning either (1) null, if the statement if found to be dead
     * or unreachable code, (2) an optimized equivalent statement, or (3) the statement itself.
     * Intended to be overridden in subclasses, but since not all subclasses have optimizations,
     * the default is to do nothing.
     */
    public Statement optimize() {
        return this;
    }
}
