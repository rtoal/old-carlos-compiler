package edu.lmu.cs.xlg.carlos.entities;

/**
 * A class for Carlos variables.
 */
public class Variable extends Declarable {

    private String typename;
    private Expression initializer;
    private Type type;

    /**
     * An arbitrary variable, useful in semantic analysis to take the place of a variable that
     * has not been declared.  This variable is type-compatible with everything, so its use
     * serves to prevent a flood of spurious error messages.
     */
    public static final Variable ARBITRARY = new Variable("<unknown>", Type.ARBITRARY);

    public Variable(String name, String typename, Expression initializer) {
        super(name);
        this.typename = typename;
        this.initializer = initializer;
    }

    /**
     * Special constructor for variables created during semantic analysis (that is, not known
     * while parsing). Examples include parameters for external or built-in functions, and
     * special variables such as ARBITRARY.
     */
    public Variable(String name, Type type) {
        super(name);
        this.typename = type.getName();
        this.initializer = null;
        this.type = type;
    }

    public Expression getInitializer() {
        return initializer;
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

        // If an initializer is present, analyze it and check types.
        if (initializer != null) {
            initializer.analyze(context);
            initializer.assertAssignableTo(type, "variable_initialization_type_mismatch", context);
        }
    }

    @Override
    public void optimize() {
        if (initializer != null) {
            initializer = initializer.optimize();
        }
    }
}
