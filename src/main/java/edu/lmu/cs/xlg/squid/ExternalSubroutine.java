package edu.lmu.cs.xlg.squid;

/**
 * A subroutine that is external to the current compilation unit.
 */
public class ExternalSubroutine extends Subroutine {
    private String name;

    /**
     * Creates a subroutine with the given name.  It is the
     * responsibility of the caller to mangle the name first,
     * if necessary.
     */
    public ExternalSubroutine(String name) {
        this.name = name;
    }

    /**
     * Returns the static nesting level.
     */
    public int getLevel() {
        // Closures aren't supported, so always return 0.
        return 0;
    }

    /**
     * Returns the name, prefiexed with "__", to indicate an
     * external subroutine.
     */
    public String toString() {return "__" + name;}
}
