package edu.lmu.cs.xlg.carlos.entities;

/**
 * A type expression that appears as just an identifier.  We have to figure out the type that the
 * identifier refers to during semantic analysis.
 */
public class NamedType extends Type {

    private Type referent;

    public NamedType(String name) {
        super(name);
    }

    public Type getReferent() {
        return referent;
    }

    @Override
    public void analyze(AnalysisContext context) {
        referent = context.getTable().lookupType(getName(), context.getLog());
    }
}
