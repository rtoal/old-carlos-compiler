package edu.lmu.cs.xlg.carlos.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A type which is a struct.
 */
public class StructType extends Type {

    private List<StructField> fields;

    public StructType(String name, List<StructField> fields) {
        super(name);
        this.fields = fields;
    }

    public List<StructField> getFields() {
        return fields;
    }

    @Override
    public void analyze(AnalysisContext context) {
        Set<String> fieldNames = new HashSet<String>();
        for (StructField field: fields) {
            if (! fieldNames.add(field.getName())) {
                context.error("duplicate_field", field.getName(), this.getName());
            }
        }

        for (StructField field: fields) {
            field.analyze(context);
        }
    }

    /**
     * Returns the field in this type with the given name.  If no
     * such field exists, log an error and return an "arbitrary" field.
     */
    public StructField getField(String fieldName, AnalysisContext context) {
        for (StructField field: fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }

        // Didn't find it, use the placeholder.
        context.error("no_such_field", this.getName(), fieldName);
        return StructField.ARBITRARY;
    }
}
