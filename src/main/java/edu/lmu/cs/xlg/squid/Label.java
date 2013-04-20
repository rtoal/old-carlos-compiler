package edu.lmu.cs.xlg.squid;

/**
 * A squid label.
 */
public class Label {
    private String name;

    /**
     * Creates a label with the given name.
     */
    public Label(String name) {
        this.name = name;
    }

    /**
     * Returns the name.
     */
    public String toString() {
        return name;
    }
}
