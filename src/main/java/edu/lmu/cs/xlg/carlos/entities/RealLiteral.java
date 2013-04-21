package edu.lmu.cs.xlg.carlos.entities;

/**
 * A real literal, like "2.3", "4.6634E-231", etc.
 */
public class RealLiteral extends Literal {

    private double value;

    public RealLiteral(String lexeme) {
        super(lexeme);
    }

    public double getValue() {
        return value;
    }

    @Override
    public void analyze(AnalysisContext context) {
        type = Type.REAL;
        try {
            value = Double.parseDouble(getLexeme());
        } catch (NumberFormatException e) {
            context.error("bad_real", getLexeme());
        }
    }
}
