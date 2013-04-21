package edu.lmu.cs.xlg.carlos.entities;

/**
 * A variable of the form v.f.
 */
public class DottedVariable extends VariableExpression {

    private VariableExpression struct;
    private String fieldName;
    private StructField field;

    public DottedVariable(VariableExpression struct, String fieldName) {
        this.struct = struct;
        this.fieldName = fieldName;
    }

    public StructField getField() {
        return field;
    }

    public String getFieldName() {
        return fieldName;
    }

    public VariableExpression getStruct() {
        return struct;
    }

    /**
     * Analyzes this variable, checking that the variable expression
     * before the dot has a type that is a struct; that the field
     * exists, etc.
     */
    @Override
    public void analyze(AnalysisContext context) {
        struct.analyze(context);

        if (!(struct.type instanceof StructType)) {
            context.error("not_a_struct");
            type = Type.ARBITRARY;
        } else {
            field = ((StructType)struct.type).getField(fieldName, context);

            // The type of the dotted variable is the type of the field.
            type = field.getType();
        }
    }

    /**
     * Returns true, as one can always write to a dotted variable in
     * Carlos.  In other languages we might allow for read-only fields,
     * but not Carlos.
     */
    public boolean isWritable() {
        return true;
    }
}
