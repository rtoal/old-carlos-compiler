package edu.lmu.cs.xlg.carlos.entities;

/**
 * A simple typsafe enum of the two boolean literals, true and false.
 */
public class BooleanLiteral extends Literal {

    public static final BooleanLiteral TRUE = new BooleanLiteral("true");
    public static final BooleanLiteral FALSE = new BooleanLiteral("false");

    private BooleanLiteral(String lexeme) {
        super(lexeme);
    }

    @Override
    public void analyze(AnalysisContext context) {
        this.type = Type.BOOLEAN;
    }

    // Back door for optimizer.  Returns one of the enum values.
    static BooleanLiteral fromValue(boolean value) {
        return value ? TRUE : FALSE;
    }
}
