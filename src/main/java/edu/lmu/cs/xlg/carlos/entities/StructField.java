package edu.lmu.cs.xlg.carlos.entities;

/**
 * A field within a structure type.
 */
public class StructField extends Entity {

    private String name;
    private String typename;
    private Type type;

    /**
     * An arbitrary field, useful in semantic analysis to take the place of a field that has not
     * been declared.  This field is type-compatible with everything, so its use serves to prevent
     * a flood of spurious error messages.
     */
    public static final StructField ARBITRARY = new StructField("<unknown>", Type.ARBITRARY.getName());
    static {ARBITRARY.type = Type.ARBITRARY;}

    public StructField(String name, String typename) {
        this.name = name;
        this.typename = typename;
    }

    public String getName() {
        return name;
    }

    public String getTypename() {
        return typename;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void analyze(AnalysisContext context) {
        type = context.lookupType(typename);
    }
}
