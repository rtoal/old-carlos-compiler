package edu.lmu.cs.xlg.carlos.entities;

/**
 * An empty array expression, for example:
 * <ul>
 * <li>new int[30]
 * <li>new boolean[3]
 * </ul>
 */
public class EmptyArray extends Expression {

    private String tyname;
    private Expression bound;

    public EmptyArray(String tyname, Expression bound) {
        this.tyname = tyname;
        this.bound = bound;
    }

    public Expression getBound() {
        return bound;
    }

    public String getTyname() {
        return tyname;
    }

    public void analyze(AnalysisContext context) {
        type = context.lookupType(tyname);
        bound.analyze(context);
        bound.assertInteger("new_array_allocation", context);
        type = type.array();
    }
}
