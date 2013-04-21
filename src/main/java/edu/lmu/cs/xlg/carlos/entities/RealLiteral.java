package edu.lmu.cs.xlg.carlos.entities;

/**
 * A real literal, like "2.3", "4.6634E-231", etc.
 */
public class RealLiteral extends Literal {

    private Double value;

    public RealLiteral(String lexeme) {
        super(lexeme);
    }

    public Double getValue() {
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

    // Back door for the optimizer to create these things.
    static RealLiteral fromValue(double value) {
        RealLiteral result = new RealLiteral(Double.toString(value));
        result.value = value;
        return result;
    }
}
