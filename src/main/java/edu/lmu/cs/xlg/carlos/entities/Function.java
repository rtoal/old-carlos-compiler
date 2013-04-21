package edu.lmu.cs.xlg.carlos.entities;

import static edu.lmu.cs.xlg.carlos.entities.Type.INT;
import static edu.lmu.cs.xlg.carlos.entities.Type.REAL;
import static edu.lmu.cs.xlg.carlos.entities.Type.STRING;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A class for Functions.
 */
public class Function extends Declarable {

    private String returnTypeName;
    private List<Variable> parameters;
    private Block body;
    private Type returnType;
    private Function overload;

    public static final Function GET_STRING = new Function(STRING, "getString");
    public static final Function SUBSTRING = new Function(STRING, "substring", STRING, INT, INT);
    public static final Function SQRT = new Function(REAL, "sqrt", REAL);
    public static final Function PI = new Function(REAL, "pi");
    public static final Function SIN = new Function(REAL, "sin", REAL);
    public static final Function COS = new Function(REAL, "cos", REAL);
    public static final Function ATAN = new Function(REAL, "atan", REAL, REAL);
    public static final Function LN = new Function( REAL, "ln", REAL);

    public Function(String returnTypeName, String name, List<Variable> parameters, Block body) {
        super(name);
        this.returnTypeName = returnTypeName;
        this.parameters = parameters;
        this.body = body;
    }

    /**
     * Creates a function object for one of the built-in functions; these functions do not record
     * their parameters, have no body, and are never overloaded.
     *
     * <p>Note that standard functions are created before a program is ever semantically analyzed,
     * so there's no need to run semantic analysis on standard functions. Instead, we just
     * assign their types at creation time.</p>
     */
    public Function(Type returnType, String name, Type... parameterTypes) {
        super(name);
        this.returnTypeName = returnType == null ? "void" : returnType.getName();
        this.returnType = returnType;
        List<Variable> parameters = new ArrayList<Variable>();
        for (Type type: parameterTypes) {
            parameters.add(new Variable(null, type));
        }
        this.parameters = parameters;
        this.body = null;
        this.overload = null;
    }

    public Block getBody() {
        return body;
    }

    public Function getOverload() {
        return overload;
    }

    public void setOverload(Function f) {
        this.overload = f;
    }

    public List<Variable> getParameters() {
        return parameters;
    }

    public Type getReturnType() {
        return returnType;
    }

    public String getReturnTypeName() {
        return returnTypeName;
    }

    /**
     * Returns whether this function is a "void" function.  (The reason for this function is that
     * voidness can be implemented in several ways, such as a return type of null, or the return
     * type set to some special void-type-object.  If we change strategies, we can just make the
     * change here.
     */
    public boolean isVoid() {
        return returnType == null;
    }

    /**
     * Performs semantic analysis on the function's signature and return type, but not the body.
     */
    public void analyzeSignature(AnalysisContext context) {
        returnType = returnTypeName == "void" ? null : context.lookupType(returnTypeName);
        body.createTable(context.getTable());
        for (Variable parameter: parameters) {
            body.getTable().insert(parameter, context.getLog());
            parameter.analyze(context.withTable(body.getTable()));
        }
    }

    /**
     * Performs semantics analysis on the body.  This is done after the signature has been
     * analyzed, so the body's symbol table has already been constructed and the parameters
     * have already been loaded.
     */
    @Override
    public void analyze(AnalysisContext context) {
        body.analyze(context.withFunction(this).withTable(body.getTable()).withInLoop(false));
    }

    /**
     * Returns whether the given argument list matches this function's signature.
     */
    public boolean canBeCalledWith(List<Expression> args) {
        if (args.size() != parameters.size()) {
            return false;
        }

        // Check each parameter against the corresponding argument.
        Iterator<Expression> ai = args.iterator();
        Iterator<Variable> pi = parameters.iterator();
        while (pi.hasNext()) {
            Expression arg = ai.next();
            Variable parameter = pi.next();
            if (!arg.isCompatibleWith(parameter.getType())) {
                return false;
            }
        }

        // All checks passed
        return true;
    }

    @Override
    public void optimize() {
        body.optimize();

        // TODO Look for and handle tail recursion if applicable.
    }
}
