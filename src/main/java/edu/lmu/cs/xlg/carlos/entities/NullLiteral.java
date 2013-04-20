package edu.lmu.cs.xlg.carlos.entities;

/**
 * The literal null.
 */
public class NullLiteral extends Literal {

    public static NullLiteral INSTANCE = new NullLiteral();

    // Constructor is private because this class is a singleton.
    private NullLiteral() {
        super("null");
    }

    @Override
    public void analyze(AnalysisContext context) {
        type = Type.NULL_TYPE;
    }
}
