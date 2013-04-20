package edu.lmu.cs.xlg.squid;

/**
 * An argument to a tuple that represents a variable or parameter
 * that was declared in some source program.  Variables have a static
 * nesting level to support source languages with arbitrary nesting of
 * subroutines and access to non-local variables. Level 0 is the
 * top-level at which globals appear.  The next level is 1.
 */
public class Var {
    private String name;
    private int level;

    public Var(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public String toString() {
        return name;
    }
}
