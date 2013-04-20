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
}
