package edu.lmu.cs.xlg.carlos.entities;

/**
 * A type that is an array.
 */
public class ArrayType extends Type {

    private Type baseType;

    public ArrayType(Type baseType) {
        super(baseType.getName() + "[]");
        this.baseType = baseType;
    }

    public Type getBaseType() {
        return baseType;
    }

    @Override
    public void analyze(AnalysisContext context) {
        baseType.analyze(context);
    }
}
