package edu.lmu.cs.xlg.carlos.entities;

/**
 * A variable reference that is made up of a simple identfier.
 */
public class SimpleVariableReference extends VariableExpression {

    private String name;
    private Variable referent;

    public SimpleVariableReference(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Variable getReferent() {
        return referent;
    }

    @Override
    public void analyze(AnalysisContext context) {
        referent = context.getTable().lookupVariable(name, context.getLog());
        type = referent.getType();
    }

    /**
     * Returns true, because simple variables are always writable
     * in Carlos.
     */
    public boolean isWritable() {
       return true;
    }
}
