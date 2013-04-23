package edu.lmu.cs.xlg.carlos.entities;

import java.util.Iterator;
import java.util.List;

/**
 * A expression directly constructing a new instance of a structure type. Examples:
 * <ul>
 * <li>new Point{3, 4}
 * <li>new Person{1536277, "Alice", makeDate("2001-01-05")}
 * <li>new Amount{2.95, "USD"}
 * </ul>
 */
public class StructAggregate extends Expression {

    private String typename;
    private List<Expression> args;

    public StructAggregate(String typename, List<Expression> args) {
        this.typename = typename;
        this.args = args;
    }

    public String getTypename() {
        return typename;
    }

    public List<Expression> getArgs() {
        return args;
    }

    @Override
    public void analyze(AnalysisContext context) {

        type = context.lookupType(typename);
        if (!(type instanceof StructType)) {
            context.error("not_a_struct_type", type.getName());
            return;
        }

        List<StructField> fields = StructType.class.cast(type).getFields();
        if (args.size() != fields.size()) {
            context.error("wrong_number_of_fields", type.getName(), fields.size(), args.size());
            return;
        }

        Iterator<Expression> ai = args.iterator();
        Iterator<StructField> fi = fields.iterator();
        while (ai.hasNext()) {
            Expression a = ai.next();
            StructField f = fi.next();
            a.analyze(context);
            a.assertAssignableTo(f.getType(), "struct_aggregate_type_mismatch", context);
        }
    }
}
