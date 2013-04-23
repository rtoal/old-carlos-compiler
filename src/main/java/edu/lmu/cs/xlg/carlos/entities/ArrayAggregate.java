package edu.lmu.cs.xlg.carlos.entities;

import java.util.List;

/**
 * An expression directly constructing an array, such as
 * <ul>
 * <li>new int[]{35, 22, 19}
 * <li>new char[]{'3', '%'}
 * <li>new string[]{}
 * </ul>
 */
public class ArrayAggregate extends Expression {

    private String typename;
    private List<Expression> args;

    public ArrayAggregate(String typename, List<Expression> args) {
        this.typename = typename;
        this.args = args;
    }

    public String getTypename() {
        return typename;
    }

    public List<Expression> getArgs() {
        return args;
    }

    /**
     * Analyzes the aggregate. We have to check that each item in the aggregate has a type that
     * is compatible with the base type of the aggregate.  For example, if the aggregate is
     * "real[]{2, 3, 9}" then the type of the aggregate is real[] but the "element type" is real
     * -- and each of the items must be type compatible with real.
     */
    @Override
    public void analyze(AnalysisContext context) {
        type = context.lookupType(typename);
        if (! (type instanceof ArrayType)) {
            context.error("not_an_array_type", type.getName());
            return;
        }

        Type elementType = ArrayType.class.cast(type).getBaseType();
        for (Expression a: args) {
            a.analyze(context);
            a.assertAssignableTo(elementType, "array_aggregate_type_mismatch", context);
        }
    }
}
