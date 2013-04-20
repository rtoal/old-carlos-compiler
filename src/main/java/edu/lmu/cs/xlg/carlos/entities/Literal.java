package edu.lmu.cs.xlg.carlos.entities;

/**
 * Abstract superclass for all literals.
 */
public abstract class Literal extends Expression {

    private String lexeme;

    /**
     * A convenient synonym for NullLiteral.INSTANCE, because Literal.NULL looks a lot better.
     */
    public static final Literal NULL = NullLiteral.INSTANCE;

    public Literal(String lexeme) {
        this.lexeme = lexeme;
    }

    public String getLexeme() {
        return lexeme;
    }
}
