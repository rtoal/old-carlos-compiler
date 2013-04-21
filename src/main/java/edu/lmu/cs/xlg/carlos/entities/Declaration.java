package edu.lmu.cs.xlg.carlos.entities;

/**
 * A declaration statement.  This statement declares a new entity.
 */
public class Declaration extends Statement {

    private Declarable declarable;

    public Declaration(Declarable declarable) {
        this.declarable = declarable;
    }

    public Declarable getDeclarable() {
        return declarable;
    }

    @Override
    public void analyze(AnalysisContext context) {
        declarable.analyze(context);
    }

    @Override
    public Statement optimize() {
        declarable.optimize();
        return this;
    }
}
