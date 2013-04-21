package edu.lmu.cs.xlg.carlos.entities;

/**
 * A variable reference that consists solely of a simple identifier.
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
        referent = context.lookupVariable(name);
        type = referent.getType();
    }

    /**
     * Returns true, because simple variables are always writable in Carlos.
     */
    public boolean isWritable() {
       return true;
    }
}
