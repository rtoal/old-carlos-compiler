package edu.lmu.cs.xlg.carlos.entities;

import java.util.Collections;
import java.util.List;

/**
 * The If statement.
 */
public class IfStatement extends Statement {

    private List<Case> cases;

    public IfStatement(List<Case> cases) {
        this.cases = cases;
    }

    public IfStatement(Case c) {
        this.cases = Collections.singletonList(c);
    }

    public List<Case> getCases() {
        return cases;
    }

    @Override
    public void analyze(AnalysisContext context) {
        for (Case c: cases) {
            c.analyze(context);
        }
    }
}
