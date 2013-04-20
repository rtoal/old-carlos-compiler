package edu.lmu.cs.xlg.carlos.entities;

import java.util.List;

/**
 * An empty array expression, for example:
 * <ul>
 * <li>new int[30]
 * <li>new boolean[3][20][17]
 * </ul>
 */
public class EmptyArray extends Expression {

    private String tyname;
    private List<Expression> bounds;

    public EmptyArray(String tyname, List<Expression> bounds) {
        this.tyname = tyname;
        this.bounds = bounds;
    }

    public List<Expression> getBounds() {
        return bounds;
    }

    public String getTyname() {
        return tyname;
    }

    /**
     * Analyzes this expression.  Each of the bounds must have integer
     * type.  The type of the overall expression is an array of something.
     */
    public void analyze(AnalysisContext context) {
        type = context.getTable().lookupType(tyname, context.getLog());

        for (Expression bound: bounds) {
            bound.analyze(context);
            bound.assertInteger("[]", context);
            type = type.array();
        }
    }
}
