package edu.lmu.cs.xlg.carlos.entities;

import java.util.List;

/**
 * A literal of type string.
 */
public class StringLiteral extends Literal {

    private List<Integer> values;

    public StringLiteral(String lexeme) {
        super(lexeme);
    }

    public List<Integer> getValues() {
        return values;
    }

    @Override
    public void analyze(AnalysisContext context) {
        type = Type.STRING;
        values = CharLiteral.codepoints(getLexeme(), 1, getLexeme().length() - 1, context);
    }
}
