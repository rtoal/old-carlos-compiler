package edu.lmu.cs.xlg.carlos.entities;

/**
 * A superclass for anything that can be declared, such as types, variables, and functions.
 */
public abstract class Declarable extends Entity {

    private String name;

    public Declarable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Optimizes this entity.  Intended to be overridden by subclasses, but a default do-nothing
     * implementation is provided here because not all declarables require any optimization.
     */
    public void optimize() {
        // Intentionally empty
    }
}
