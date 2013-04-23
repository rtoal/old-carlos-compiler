package edu.lmu.cs.xlg.carlos.entities;

import java.util.ArrayList;

/**
 * A statement of the form "for (t i = e1; e2; e3) block" where each of the three sections
 * (the parts separated by semicolons) are optional.
 */
public class ClassicForStatement extends Statement {

    private String tyname;
    private String index;
    private Expression init, test;
    private Statement each;
    private Block body;
    private Variable indexVariable;

    public ClassicForStatement(String tyname, String index, Expression init, Expression test,
            Statement each, Block body) {
        this.tyname = tyname;
        this.index = index;
        this.init = init;
        this.test = test;
        this.each = each;
        this.body = body;
    }

    public Block getBody() {
        return body;
    }

    public Statement getEach() {
        return each;
    }

    public String getIndex() {
        return index;
    }

    public Variable getIndexVariable() {
        return indexVariable;
    }

    public Expression getInit() {
        return init;
    }

    public Expression getTest() {
        return test;
    }

    public String getTyname() {
        return tyname;
    }

    @Override
    public void analyze(AnalysisContext context) {

        // If the first section is present, we will need to construct a new symbol table for this
        // statement, otherwise we won't. We'll just (cleverly) update the context parameter of
        // this method to the new table if we need it.  The rest of the analysis then doesn't know
        // or care whether we made a new table or not.
        if (tyname != null && index != null && init != null) {
            indexVariable = new Variable(index, tyname, init);
            body.createTable(context.getTable());
            body.getTable().insert(indexVariable, context.getLog());
            context = context.withTable(body.getTable());

            // Analyzing the variable ALSO analyzes the type and the expression e1 as a side effect.
            indexVariable.analyze(context);
        }

        // The second part must be a boolean expression.
        if (test != null) {
            test.analyze(context);
            test.assertBoolean("for_loop_termination_not_boolean", context);
        }

        // No constraints on the third part.
        if (each != null) {
            each.analyze(context);
        }

        // Analyze the body, noting that it *is* a loop body.
        body.analyze(context.withInLoop(true));
    }

    @Override
    public Statement optimize() {
        if (init != null) {
            init = init.optimize();
        }
        if (test != null) {
            test = test.optimize();
            if (test.isFalse()) {
                if (init != null) {
                    // Don't need to do the loop, but we have to execute init for its side-effects
                    each = null;
                    body = new Block(new ArrayList<Statement>());
                } else {
                    // This whole statement is a giant no-op
                    return null;
                }
            }
        }
        if (each != null) {
            each = each.optimize();
        }
        body.optimize();
        return this;
    }
}
