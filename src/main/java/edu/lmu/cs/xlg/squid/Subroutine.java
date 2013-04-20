package edu.lmu.cs.xlg.squid;

/**
 * A squid subroutine.
 */
public abstract class Subroutine {

    /**
     * Returns the static nesting level of the subroutine.  Top-level
     * subroutines have level 0.
     */
    public abstract int getLevel();
}
