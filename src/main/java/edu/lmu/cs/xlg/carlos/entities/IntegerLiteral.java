package edu.lmu.cs.xlg.carlos.entities;

/**
 * An integer literal.
 */
public class IntegerLiteral extends Literal {

    private int value;

    public IntegerLiteral(String lexeme) {
        super(lexeme);
    }

    public int getValue() {
        return value;
    }

    @Override
    public void analyze(AnalysisContext context) {
        type = Type.INT;
        try {
            value = Integer.parseInt(getLexeme());
        } catch (NumberFormatException e) {
            context.error("bad_int", getLexeme());
        }
    }
}
