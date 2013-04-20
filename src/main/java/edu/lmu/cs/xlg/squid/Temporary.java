package edu.lmu.cs.xlg.squid;

/**
 * An argument to a tuple that represents the result of some
 * computation (a data computation or an address computation).
 */
public class Temporary {
    private String name;

    public Temporary(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
