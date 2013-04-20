package edu.lmu.cs.xlg.carlos.entities;

/**
 * Superclass for all types.
 */
public class Type extends Declarable {

    public static final Type INT = new Type("int");
    public static final Type REAL = new Type("real");
    public static final Type BOOLEAN = new Type("boolean");
    public static final Type CHAR = new Type("char");
    public static final Type STRING = new Type("string");

    /**
     * A type representing the union of all types.  It is assigned to an entity whose typename
     * is not in scope.  It is compatible with all other types.  It exists to avoid spurious
     * errors during compilation.
     */
    public static final Type ARBITRARY = new Type("<arbitrary>");

    /**
     * The type whose sole member is the literal null.
     */
    public static final Type NULL_TYPE = new Type("<type_of_null>");

    /**
     * A type representing the union of the string type and all array types.  This is used for
     * the parameter of the special length function from the standard library.
     */
    public static final Type ARRAY_OR_STRING = new Type("<array_or_string>");

    /**
     * The type which is an array of this type.  It will be null unless needed.  When it is
     * needed, it will be created.
     */
    private ArrayType arrayOfThisType = null;

    Type(String name) {
        super(name);
    }

    public boolean isReference() {
        return this == STRING
            || this instanceof ArrayType
            || this instanceof StructType
            || this == ARRAY_OR_STRING
            || this == ARBITRARY;
    }

    public boolean isArithmetic() {
        return this == INT || this == REAL;
    }

    public boolean isString() {
        return this == STRING;
    }

    public boolean isArray() {
        return this instanceof ArrayType;
    }

    /**
     * Returns the type that is an array of this type, lazily creating it.
     */
    public Type array() {
        if (arrayOfThisType == null) {
            arrayOfThisType = new ArrayType(this);
        }
        return arrayOfThisType;
    }

    @Override
    public void analyze(AnalysisContext context) {
        // Intentionally empty - here only because it's nice to have primitives be of this class.
        // The subclasses ArrayType and StructType still need to override this.
    }
}
