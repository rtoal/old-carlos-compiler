package edu.lmu.cs.xlg.carlos.entities;

/**
 * A variable of the form v[e]. v must be an array or string, and e must be an integer.
 */
public class SubscriptedVariable extends VariableExpression {

    private VariableExpression sequence;
    private Expression index;

    public SubscriptedVariable(VariableExpression v, Expression e) {
        this.sequence = v;
        this.index = e;
    }

    public VariableExpression getSequence() {
        return sequence;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public void analyze(AnalysisContext context) {
        sequence.analyze(context);
        index.analyze(context);

        sequence.assertArrayOrString("[]", context);
        index.assertInteger("[]", context);
        type = (sequence.type.isString()) ? Type.CHAR
                : sequence.type.isArray() ? ArrayType.class.cast(sequence.type).getBaseType()
                : Type.ARBITRARY;
    }

    public boolean isWritable() {
        // It's writable if an array, but not writable if it is a string
        return sequence.type.isArray();
    }
}
