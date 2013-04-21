package edu.lmu.cs.xlg.carlos.entities;

import java.util.Collections;
import java.util.List;

/**
 * The If statement.
 */
public class IfStatement extends Statement {

    private List<Case> cases;
    private Block elsePart;

    public IfStatement(List<Case> cases, Block elsePart) {
        this.cases = cases;
        this.elsePart = elsePart;
    }

    public IfStatement(Case c) {
        this.cases = Collections.singletonList(c);
    }

    public List<Case> getCases() {
        return cases;
    }

    public Block getElsePart() {
        return elsePart;
    }

    @Override
    public void analyze(AnalysisContext context) {
        for (Case c: cases) {
            c.analyze(context);
        }
        if (elsePart != null) {
            elsePart.analyze(context);
        }
    }
}
