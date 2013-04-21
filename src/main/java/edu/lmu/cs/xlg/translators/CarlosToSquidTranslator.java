package edu.lmu.cs.xlg.translators;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lmu.cs.xlg.carlos.entities.ArrayAggregate;
import edu.lmu.cs.xlg.carlos.entities.ArrayType;
import edu.lmu.cs.xlg.carlos.entities.AssignmentStatement;
import edu.lmu.cs.xlg.carlos.entities.Block;
import edu.lmu.cs.xlg.carlos.entities.BooleanLiteral;
import edu.lmu.cs.xlg.carlos.entities.BreakStatement;
import edu.lmu.cs.xlg.carlos.entities.CallExpression;
import edu.lmu.cs.xlg.carlos.entities.CallStatement;
import edu.lmu.cs.xlg.carlos.entities.Case;
import edu.lmu.cs.xlg.carlos.entities.CharLiteral;
import edu.lmu.cs.xlg.carlos.entities.ClassicForStatement;
import edu.lmu.cs.xlg.carlos.entities.Declaration;
import edu.lmu.cs.xlg.carlos.entities.DottedVariable;
import edu.lmu.cs.xlg.carlos.entities.EmptyArray;
import edu.lmu.cs.xlg.carlos.entities.Entity;
import edu.lmu.cs.xlg.carlos.entities.Expression;
import edu.lmu.cs.xlg.carlos.entities.Function;
import edu.lmu.cs.xlg.carlos.entities.IfStatement;
import edu.lmu.cs.xlg.carlos.entities.IncrementStatement;
import edu.lmu.cs.xlg.carlos.entities.InfixExpression;
import edu.lmu.cs.xlg.carlos.entities.IntegerLiteral;
import edu.lmu.cs.xlg.carlos.entities.NullLiteral;
import edu.lmu.cs.xlg.carlos.entities.PostfixExpression;
import edu.lmu.cs.xlg.carlos.entities.PrefixExpression;
import edu.lmu.cs.xlg.carlos.entities.PrintStatement;
import edu.lmu.cs.xlg.carlos.entities.Program;
import edu.lmu.cs.xlg.carlos.entities.RealLiteral;
import edu.lmu.cs.xlg.carlos.entities.ReturnStatement;
import edu.lmu.cs.xlg.carlos.entities.SimpleVariableReference;
import edu.lmu.cs.xlg.carlos.entities.Statement;
import edu.lmu.cs.xlg.carlos.entities.StringLiteral;
import edu.lmu.cs.xlg.carlos.entities.StructAggregate;
import edu.lmu.cs.xlg.carlos.entities.StructField;
import edu.lmu.cs.xlg.carlos.entities.StructType;
import edu.lmu.cs.xlg.carlos.entities.SubscriptedVariable;
import edu.lmu.cs.xlg.carlos.entities.Type;
import edu.lmu.cs.xlg.carlos.entities.Variable;
import edu.lmu.cs.xlg.carlos.entities.WhileStatement;
import edu.lmu.cs.xlg.squid.AddressTemporary;
import edu.lmu.cs.xlg.squid.ExternalSubroutine;
import edu.lmu.cs.xlg.squid.Label;
import edu.lmu.cs.xlg.squid.Op;
import edu.lmu.cs.xlg.squid.StringConstant;
import edu.lmu.cs.xlg.squid.Subroutine;
import edu.lmu.cs.xlg.squid.Temporary;
import edu.lmu.cs.xlg.squid.UserSubroutine;
import edu.lmu.cs.xlg.squid.Var;
import edu.lmu.cs.xlg.util.IdGenerator;

/**
 * A translator from Carlos semantic graphs to Squid objects.
 */
public class CarlosToSquidTranslator {

    // Carlos has a real type whose representation size can be larger
    // than that of integers, so we have to allow for two sizes.
    // These fields will be set in the translator's constructor.
    private final int WORD_SIZE;
    private final int DOUBLE_SIZE;

    // Used to generate unique names for Squid objects.
    private IdGenerator gen = new IdGenerator();

    // A bundle containing the current translation state: (1) the
    // current subroutine, (2) the current label that continue statements
    // should jump to (null if not in a loop), and (3) the current label
    // that break statements should jump to (null if not in a loop).
    private static class State {
        UserSubroutine subroutine;
        Label continuePoint;
        Label exitPoint;

        State(UserSubroutine s) {
            this(s, null, null);
        }

        State(UserSubroutine s, Label c, Label e) {
            subroutine = s;
            continuePoint = c;
            exitPoint = e;
        }

        void emit(Op op, Object... args) {
            subroutine.addTuple(op, args);
        }
    }

    // This maps Carlos entities to their Squid equivalents:
    //   carlos.entities.Variable -> squid.Var
    //   carlos.entities.Function -> squid.Subroutine
    //   carlos.entities.StructField -> integer offset within struct
    @SuppressWarnings("serial")
    private Map<Entity, Object> carlosToSquid = new HashMap<Entity, Object>() {{
        put(Function.SUBSTRING, new ExternalSubroutine("substring"));
        put(Function.GET_STRING, new ExternalSubroutine("get_string"));
    }};

    // Helper functions in the standard library
    private static final Subroutine ALLOCATE_ARRAY =
        new ExternalSubroutine("allocate_array");
    private static final Subroutine TERMINATE_PROGRAM =
        new ExternalSubroutine("terminate_program");
    private static final Subroutine OBJECT_TO_STRING =
        new ExternalSubroutine("object_to_string");

    /**
     * Creates a translator.
     */
    public CarlosToSquidTranslator(int wordSize, int doubleSize) {
        this.WORD_SIZE = wordSize;
        this.DOUBLE_SIZE = doubleSize;
    }

    // Returns the number of bytes taken up by a given type.
    private int sizeof(Type type) {
        return type == Type.REAL ? DOUBLE_SIZE : WORD_SIZE;
    }

    private Var createVar(int level, Type type) {
        return new Var(gen.id(type == Type.REAL ? "x" : "i"), level);
    }

    private Temporary createTemporary(Type type) {
        return new Temporary(gen.id(type == Type.REAL ? "f" : "r"));
    }

    private Temporary createAddressTemporary(Type type) {
        return new AddressTemporary(gen.id("r"), type == Type.REAL);
    }

    private Label createLabel() {
        return new Label(gen.id("L"));
    }

    private StringConstant createStringConstant(List<Integer> values) {
        return new StringConstant(gen.id("s"), values);
    }

    private StringConstant createStringConstant(String text) {
        return new StringConstant(gen.id("s"), text);
    }

    private UserSubroutine createUserSubroutine(UserSubroutine parent) {
        return new UserSubroutine(gen.id("p"), parent);
    }

    /**
     * Dispatches to the proper translate method based on the class of
     * the entity e.  For example, if e has class "IfStatement",
     * then we delegate to "translateIfStatement".
     */
    public Object translate(Entity e, State s) {
        return translate(e, s, false);
    }

    /**
     * Translates the given entity according to the given translation
     * state, with an optional adjustment to force the result to be
     * an rvalue.  If rvalue is true, and the translation produces
     * an address temporary (as it would in variable expressions
     * such as a[i] or s.x, we first emit a tuple to copy the
     * dereferenced value into a new temporary, and return the new
     * value.
     */
    public Object translate(Entity e, State s, boolean rvalue) {
        String className = e.getClass().getName();
        String shortName = className.substring(className.lastIndexOf('.') + 1);
        String methodName = "translate" + shortName;
        try {
            Object result = getClass()
                .getMethod(methodName, e.getClass(), State.class)
                .invoke(this, e, s);
            if (rvalue && result instanceof AddressTemporary) {
                Temporary t = ((AddressTemporary)result).referencesFloat
                    ? createTemporary(Type.REAL)
                    : createTemporary(Type.INT);
                s.emit(Op.COPY_FROM_DEREF, result, t);
                return t;
            }
            return result;
        } catch (NoSuchMethodException ex) {
            throw new Error("Internal Error: Bad method: " + methodName);
        } catch (IllegalAccessException ex) {
            throw new Error("Internal Error: Cannot access: " + methodName);
        } catch (InvocationTargetException ex) {
            throw new Error(ex.getCause());
        }
    }

    /**
     * Translates a block, by translating each item.  Returns the
     * current subroutine that owns this block.  If this block is
     * a Program object, the return value will be the main subroutine.
     */
    public void translateBlock(Block b, State s) {
        List<Function> functions = b.getFunctions();
        List<Type> types = b.getTypes();
        List<Statement> statements = b.getStatements();

        // Record offsets for all the fields in this block.
        for (Type type: types) {
            if (!(type instanceof StructType)) continue;
            List<StructField> fields = ((StructType)type).getFields();
            int offset = 0;
            for (StructField field: fields) {
                carlosToSquid.put(field, offset);
                offset += sizeof(field.getType());
            }
        }

        // Preprocess functions since they can be mutually recursive.
        // For each Carlos function, only a skeleton squid subroutine gets
        // built and recorded -- just enough to be resolved in a call.
        // The rest of the subroutine object will be filled in later.
        for (Function f: functions) {
            carlosToSquid.put(f, createUserSubroutine(s.subroutine));
        }

        // Now just translate every statement in order.  Function
        // declarations are fully translated here, so any partially
        // constructed tuple subroutine can get filled in now.
        for (Statement statement: statements) {
            translate(statement, s);
        }
    }

    /**
     * Returns the translation of a program.
     */
    public UserSubroutine translateProgram(Program p) {
        UserSubroutine main = createUserSubroutine(null);
        State initialState = new State(main);
        translateBlock(p, initialState);
        initialState.emit(Op.EXIT);
        return main;
    }

    /**
     * Translates a function.  The translation will produce a new
     * squid subroutine and place it in the cache of all functions.
     * Parameter objects will also be created for the subroutine, and
     * the body will be translated.
     */
    public void translateFunction(Function f, State s) {
        List<Variable> params = f.getParameters();

        // The function has to be translated in a new state
        s = new State((UserSubroutine)carlosToSquid.get(f));

        // Encode the parameters
        for (Variable param: params) {
            Var p = createVar(s.subroutine.getLevel(), param.getType());
            carlosToSquid.put(param, p);
            s.subroutine.addParameter(p);
        }
        translate(f.getBody(), s);

        // Now place an implicit return or die at the end
        if (f.isVoid()) {
            s.emit(Op.RETP);
        } else {
            s.emit(Op.CALLP, TERMINATE_PROGRAM, 0);
        }
    }

    /**
     * Translates a variable declaration.
     */
    public void translateVariable(Variable v, State s) {
        Var var = createVar(s.subroutine.getLevel(), v.getType());
        carlosToSquid.put(v, var);
        s.subroutine.addVariable(var);
        if (v.getInitializer() != null) {
            s.emit(Op.COPY, translate(v.getInitializer(), s, true), var);
        }
    }

    /**
     * Translates a declaration statement.
     */
    public void translateDeclaration(Declaration d, State s) {
        if (!(d.getDeclarable() instanceof Type)) {
            translate(d.getDeclarable(), s);
        }
    }

    /**
     * Translates an increment statement.
     */
    public void translateIncrementStatement(IncrementStatement is, State s) {
        Object operand = translate(is.getTarget(), s);
        processIncrementOrDecrement(is.getOp(), operand, s);
    }

    /**
     * Translates a call statement.
     */
    public void translateCallStatement(CallStatement cs, State s) {
        processCall(cs.getFunction(), cs.getArgs(), null, s);
    }

    /**
     * Translates a call expression.
     */
    public Object translateCallExpression(CallExpression e, State s) {
        Object result = createTemporary(e.getType());
        Function f = e.getFunction();
        List<Expression> args = e.getArgs();

        if (Arrays.asList(Function.ATAN, Function.SIN, Function.COS,
                Function.SQRT, Function.LN, Function.PI).contains(f)) {
            Object x = args.size() > 0 ? translate(args.get(0), s, true) : null;
            Object y = args.size() > 1 ? translate(args.get(1), s, true) : null;

            if (f == Function.ATAN) {
                s.emit(Op.ATAN, x, y, result);
            } else if (f == Function.SIN) {
                s.emit(Op.SIN, x, result);
            } else if (f == Function.COS) {
                s.emit(Op.COS, x, result);
            } else if (f == Function.SQRT) {
                s.emit(Op.SQRT, x, result);
            } else if (f == Function.LN) {
                s.emit(Op.LN, x, result);
            } else if (f == Function.PI) {
                s.emit(Op.COPY, Math.PI, result);
            } else {
                throw new Error("This compiler is awful");
            }
        } else {
            processCall(f, args, result, s);
        }
        return result;
    }

    /**
     * Translates v = e.
     */
    public void translateAssignmentStatement(AssignmentStatement as, State s) {
        Object source = translate(as.getRight(), s, true);
        Object target = translate(as.getLeft(), s);
        if (as.getLeft().getType() == Type.REAL
                && as.getRight().getType() != Type.REAL) {
            Object uncastedSource = source;
            source = createTemporary(Type.REAL);
            s.emit(Op.TO_FLOAT, uncastedSource, source);
        }
        if (target instanceof AddressTemporary) {
            s.emit(Op.COPY_TO_DEREF, source, target);
        } else {
            s.emit(Op.COPY, source, target);
        }
    }

    /**
     * Translates a break statement by emitting a jump to the current
     * exit point.
     */
    public void translateBreakStatement(BreakStatement bs, State s) {
        s.emit(Op.JUMP, s.exitPoint);
    }

    /**
     * Translates a print statement by emitting print tuples for
     * the string value of each argument.
     */
    public void translatePrintStatement(PrintStatement ps, State s) {
        for (Expression a: ps.getArgs()) {
            Object x = translate(a, s, true);

            // Printing a string?  No big.
            if (a.getType() == Type.STRING) {
                s.emit(Op.PRINT, x);
                return;
            }

            // Must convert to a string first.
            Temporary y = createTemporary(Type.STRING);
            if (a.getType() == Type.BOOLEAN) {
                s.emit(Op.BOOL_TO_STRING, x, y);
            } else if (a.getType() == Type.CHAR) {
                s.emit(Op.CHAR_TO_STRING, x, y);
            } else if (a.getType() == Type.INT) {
                s.emit(Op.INT_TO_STRING, x, y);
            } else if (a.getType() == Type.REAL) {
                s.emit(Op.FLOAT_TO_STRING, x, y);
            } else {
                s.emit(Op.PARAM, x);
                s.emit(Op.PARAM, createStringConstant(a.getType().getName()));
                s.emit(Op.CALLF, OBJECT_TO_STRING, WORD_SIZE * 2, y);
            }
            s.emit(Op.PRINT, y);
        }
    }

    /**
     * Translates a return statement by emitting either a RETP or a
     * RETF tuple, as appropriate.
     */
    public void translateReturnStatement(ReturnStatement rs, State s) {
        if (rs.getReturnExpression() == null) {
            s.emit(Op.RETP);
        } else {
            s.emit(Op.RETF, translate(rs.getReturnExpression(), s));
        }
    }

    /**
     * Translates an if statment by emitting code for each of its cases.
     */
    public void translateIfStatement(IfStatement is, State s) {
        for (Case c: is.getCases()) {
            translate(c, s);
        }
        if (is.getElsePart() != null) {
            translate(is.getElsePart(), s);
        }
    }

    /**
     * Translates a case.
     */
    public void translateCase(Case c, State s) {
        Label after = createLabel();
        s.emit(Op.JZERO, translate(c.getCondition(), s), after);
        translate(c.getBody(), s);
        s.emit(Op.LABEL, after);
    }

    /**
     * Translates a while statement.
     */
    public void translateWhileStatement(WhileStatement ws, State s) {
        Label top = createLabel();
        Label bottom = createLabel();
        s = new State(s.subroutine, top, bottom);
        s.emit(Op.JUMP, bottom);
        s.emit(Op.LABEL, top);
        translate(ws.getBody(), s);
        s.emit(Op.LABEL, bottom);
        s.emit(Op.JNZERO, translate(ws.getCondition(), s, true), top);
    }

    /**
     * Translates a classic for-statement.
     */
    public void translateClassicForStatement(ClassicForStatement fs, State s) {
        Label top = createLabel();
        Label bottom = createLabel();
        s = new State(s.subroutine, top, bottom);
        if (fs.getIndexVariable() != null) {
            Var tuplevar =
                createVar(s.subroutine.getLevel(),
                    fs.getIndexVariable().getType());
            carlosToSquid.put(fs.getIndexVariable(), tuplevar);
            s.subroutine.addVariable(tuplevar);
            if (fs.getIndexVariable().getInitializer() != null) {
                s.emit(Op.COPY,
                    translate(fs.getIndexVariable().getInitializer(), s, true),
                    tuplevar);
            }
        }
        s.emit(Op.LABEL, top);
        if (fs.getTest() != null) {
            s.emit(Op.JZERO, translate(fs.getTest(), s, true), bottom);
        }
        translate(fs.getBody(), s);
        if (fs.getEach() != null) {
            translate(fs.getEach(), s);
        }
        s.emit(Op.JUMP, top);
        s.emit(Op.LABEL, bottom);
    }

    /**
     * Translates an expression made up of a simple variable reference.
     * Returns the quad language variable that is mapped to by the
     * given Carlos variable.
     */
    public Object translateSimpleVariableReference(SimpleVariableReference v,
            State s) {
        return carlosToSquid.get(v.getReferent());
    }

    /**
     * Translates a subscripted variable.
     */
    public Object translateSubscriptedVariable(SubscriptedVariable v, State s) {

        // Generate code to check for null array
        Object base = translate(v.getSequence(), s, true);
        s.emit(Op.NULL_CHECK, base);

        // Generate code for bounds check
        Object index = translate(v.getIndex(), s, true);
        Object length = createTemporary(Type.INT);
        s.emit(Op.COPY_FROM_OFS, base, -WORD_SIZE, length);
        s.emit(Op.BOUND, index, 0, length);

        // Generate result
        Temporary offset = createTemporary(Type.INT);
        s.emit(Op.MUL, index, sizeof(v.getType()), offset);
        Temporary result = createAddressTemporary(v.getType());
        s.emit(Op.ADD, base, offset, result);
        return result;
    }

    /**
     * Translates a dotted variable.
     */
    public Object translateDottedVariable(DottedVariable v, State s) {
        Object base = translate(v.getStruct(), s, true);
        s.emit(Op.NULL_CHECK, base);
        Temporary result = createAddressTemporary(v.getType());
        int offset = (Integer)carlosToSquid.get(v.getField());
        s.emit(Op.ADD, base, offset, result);
        return result;
    }

    /**
     * Translates an array aggregate.
     */
    public Object translateArrayAggregate(ArrayAggregate a, State s) {
        List<Expression> components = a.getArgs();

        // Allocate space for the data and the word for the length
        Temporary t = createTemporary(Type.INT);
        int elementSize = sizeof(((ArrayType)a.getType()).getBaseType());
        s.emit(Op.ALLOC, elementSize * components.size() + WORD_SIZE, t);

        // Set length
        s.emit(Op.COPY_TO_DEREF, components.size(), t);

        // Advance to data area
        s.emit(Op.ADD, t, WORD_SIZE, t);

        // Copy elements
        int offset = 0;
        for (Expression c: components) {
            s.emit(Op.COPY_TO_OFS, translate(c, s, true), t, offset);
            offset += elementSize;
        }
        return t;
    }

    /**
     * Translates a struct aggregate.
     */
    public Object translateStructAggregate(StructAggregate a, State s) {
        List<Expression> components = a.getArgs();

        // Compute size to allocate
        int size = 0;
        for (Expression c: components) {
            size += sizeof(c.getType());
        }

        // Allocate memory for the aggregate
        Temporary t = createTemporary(Type.INT);
        s.emit(Op.ALLOC, size, t);

        // Copy the data in
        int offset = 0;
        for (Expression c: components) {
            s.emit(Op.COPY_TO_OFS, translate(c, s, true), t, offset);
            offset += sizeof(c.getType());
        }
        return t;
    }

    /**
     * Translates a prefix expression other than ++ or --.
     */
    public Object translatePrefixExpression(PrefixExpression e, State s) {
        String op = e.getOp();
        Object x = translate(e.getOperand(), s, !op.matches("--|\\+\\+"));
        Temporary y = createTemporary(e.getType());

        if ("!".equals(op)) {
            s.emit(Op.NOT, x, y);
        } else if ("~".equals(op)) {
            s.emit(Op.COMP, x, y);
        } else if ("-".equals(op)) {
            s.emit(Op.NEG, x, y);
        } else if ("++".equals(op) || "--".equals(op)) {
            processIncrementOrDecrement(op, x, s);
            s.emit(Op.COPY, x, y);
        } else if ("string".equals(op)) {
            if (e.getOperand().getType() == Type.STRING) {
                // string->string no op
                return x;
            }
            y = createTemporary(Type.STRING);
            if (e.getOperand().getType() == Type.INT) {
                s.emit(Op.INT_TO_STRING, x, y);
            } else if (e.getOperand().getType() == Type.REAL) {
                s.emit(Op.FLOAT_TO_STRING, x, y);
            } else if (e.getOperand().getType() == Type.BOOLEAN) {
                s.emit(Op.BOOL_TO_STRING, x, y);
            } else if (e.getOperand().getType() == Type.CHAR) {
                s.emit(Op.CHAR_TO_STRING, x, y);
            } else if ("string".equals(op)) {
                s.emit(Op.PARAM, x);
                s.emit(Op.PARAM, createStringConstant(
                        e.getOperand().getType().getName()));
                s.emit(Op.CALLF, OBJECT_TO_STRING, 8, y);
            }
        } else if ("length".equals(op)) {
            s.emit(Op.COPY_FROM_OFS, x, -WORD_SIZE, y);
        }
        return y;
    }

    /**
     * Translates an infix expression.
     */
    public Object translateInfixExpression(InfixExpression e, State s) {
        Expression e1 = e.getLeft();
        Expression e2 = e.getRight();
        String op = e.getOp();

        // Handle short-circuit operations first
        if (op.equals("&&")) return translateShortCircuit(e1, e2, false, s);
        if (op.equals("||")) return translateShortCircuit(e1, e2, true, s);

        // Non short-circuit operations evaluate both operands, then
        // apply the operator.
        Object x = translate(e1, s, true);
        Object y = translate(e2, s, true);
        Object z = createTemporary(e.getType());

        if (op.equals("<")) s.emit(Op.LT, x, y, z);
        else if (op.equals("<=")) s.emit(Op.LE, x, y, z);
        else if (op.equals("==")) s.emit(Op.EQ, x, y, z);
        else if (op.equals("!=")) s.emit(Op.NE, x, y, z);
        else if (op.equals(">")) s.emit(Op.GT, x, y, z);
        else if (op.equals(">=")) s.emit(Op.GE, x, y, z);
        else if (op.equals("+"))  s.emit(Op.ADD, x, y, z);
        else if (op.equals("-")) s.emit(Op.SUB, x, y, z);
        else if (op.equals("*")) s.emit(Op.MUL, x, y, z);
        else if (op.equals("/")) s.emit(Op.DIV, x, y, z);
        else if (op.equals("%")) s.emit(Op.MOD, x, y, z);
        else if (op.equals("<<")) s.emit(Op.SHL, x, y, z);
        else if (op.equals(">>")) s.emit(Op.SHR, x, y, z);
        else if (op.equals("&")) s.emit(Op.AND, x, y, z);
        else if (op.equals("|")) s.emit(Op.OR, x, y, z);
        else if (op.equals("^")) s.emit(Op.XOR, x, y, z);

        return z;
    }

    /**
     * Translates short-circuit logical ors and ands.  Returns a temporary
     * into which the evaluation of the expression will be stored.
     *
     * @param e1 the left-hand side expression
     * @param e2 the right-hand side expression
     * @param isOr true if this is an || expression, false if an &&.
     */
    private Temporary translateShortCircuit(Expression e1, Expression e2,
        boolean isOr, State s) {

        Label done = createLabel();
        Object x = translate(e1, s, true);
        Temporary z = createTemporary(e1.getType());
        s.emit(Op.COPY, x, z);
        s.emit((isOr ? Op.JNZERO : Op.JZERO), z, done);
        Object y = translate(e2, s, true);
        s.emit(Op.COPY, y, z);
        s.emit(Op.LABEL, done);
        return z;
    }

    /**
     * Translates new T[e1][e2]...[en];
     */
    public Object translateEmptyArray(EmptyArray a, State s) {
        List<Expression> bounds = a.getBounds();
        for (Expression bound: bounds) {
            s.emit(Op.PARAM, translate(bound, s));
        }
        s.emit(Op.PARAM, bounds.size());
        Temporary t = createTemporary(Type.INT);
        s.emit(Op.CALLF, ALLOCATE_ARRAY, (bounds.size() + 1) * WORD_SIZE, t);
        return t;
    }

    /**
     * Translates e++ and e--.  Generate the copy into the result
     * temporary and THEN do the side effect increment or decrement.
     */
    public Object translatePostfixExpression(PostfixExpression e, State s) {
        Temporary result = createTemporary(e.getType());
        Object operand = translate(e.getOperand(), s);
        if (operand instanceof AddressTemporary) {
            s.emit(Op.COPY_FROM_DEREF, operand, result);
        } else {
            s.emit(Op.COPY, operand, result);
        }
        processIncrementOrDecrement(e.getOp(), operand, s);
        return result;
    }

    /**
     * Carlos null ==> Squid 0.
     */
    public Object translateNullLiteral(NullLiteral literal, State s) {
        return 0;
    }

    /**
     * Carlos true ==> Squid 1; Carlos false ==> Squid 0.
     */
    public Object translateBooleanLiteral(BooleanLiteral b, State s) {
        return b == BooleanLiteral.TRUE ? 1 : 0;
    }

    /**
     * A Carlos integer ==> the Squid integer with the same value.
     */
    public Object translateIntegerLiteral(IntegerLiteral literal, State s) {
        return literal.getValue();
    }

    /**
     * A Carlos real ==> the Squid float with the same value.
     */
    public Object translateRealLiteral(RealLiteral literal, State s) {
        return literal.getValue();
    }

    /**
     * A Carlos character literal ==> the Squid integer with the value of
     * the character's codepoint.
     */
    public Object translateCharLiteral(CharLiteral c, State s) {
        return c.getValue();
    }

    /**
     * A Carlos string literal ==> the corresponding Squid string literal.
     */
    public Object translateStringLiteral(StringLiteral literal, State s) {
        return createStringConstant(literal.getValues());
    }

    // -----------------------------------------------------------------------
    // Helpers for the translator methods.
    // -----------------------------------------------------------------------

    /**
     * Helper for the translation of both call statements and call
     * expressions.  It takes care of emitting the STARTCALL, PARAM and
     * CALL tuples, performing any necessary implicit conversions, and
     * updating the callees set of the current subroutine.
     */
    private void processCall(Function function, List<Expression> args,
            Object result, State s) {
        Subroutine callee = (Subroutine)carlosToSquid.get(function);
        List<Variable> params = function.getParameters();

        // Only generate STARTCALL for nested functions
        if (callee.getLevel() >= 1) {
            s.emit(Op.STARTCALL, callee);
        }

        // Generate the PARAM tuples
        int argumentSize = processArgumentList(args, params, s);

        // Emit the proper call instruction, CALLP or CALLF
        if (function.isVoid()) {
            s.emit(Op.CALLP, callee, argumentSize);
        } else {
            s.emit(Op.CALLF, callee, argumentSize, result);
        }
    }

    /**
     * Emits the proper side-effect inc or dec tuple: either INC, DEC,
     * INC_DEREF, or DEC_DEREF, for the given operator ("++" or "--"),
     * and Squid argument.  Address temporaries generate dereferenced
     * instructions.
     */
    private void processIncrementOrDecrement(String op, Object argument,
            State s) {
        boolean isIncrement = op.equals("++");
        if (argument instanceof AddressTemporary) {
            s.emit((isIncrement ? Op.INC_DEREF : Op.DEC_DEREF), argument);
        } else {
            s.emit((isIncrement ? Op.INC : Op.DEC), argument);
        }
    }

    /**
     * Generates PARAM tuples for each argument.
     */
    private int processArgumentList(List<Expression> args,
            List<Variable> params, State state) {
        int argumentSize = 0;
        for (int i = args.size() - 1; i >= 0; i--) {
            Object argument = translate(args.get(i), state, true);
            if (i < params.size() && params.get(i).getType() == Type.REAL
                    && args.get(i).getType() != Type.REAL) {
                Object uncastedArgument = argument;
                argument = createTemporary(Type.REAL);
                state.emit(Op.TO_FLOAT, uncastedArgument, argument);
                argumentSize += DOUBLE_SIZE;
            } else {
                argumentSize += sizeof(args.get(i).getType());
            }
            state.emit(Op.PARAM, argument);
        }
        return argumentSize;
    }
}
