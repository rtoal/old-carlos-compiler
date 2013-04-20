package edu.lmu.cs.xlg.squid;

/**
 * An instruction in a three-address code (quadruple) language.  A tuple
 * has one of the following forms:
 * <pre>
 *   (op)
 *   (op, x)
 *   (op, x, y)
 *   (op, x, y, z)
 * </pre>
 * <p>The arguments of a tuple can be literals, variables, temporaries,
 * subroutines and labels.  Variables and temporaries have a kind of
 * "type": they can hold (1) word data, (2) double-precision float data,
 * or (3) the "address" of something else.</p>
 */
public class Tuple {

    public Op op;
    public Object x;
    public Object y;
    public Object z;

    /**
     * Creates a tuple.
     */
    public Tuple(Op op, Object... args) {
        set(op, args);
    }

    /**
     * Sets the fields of a tuple.
     */
    public void set(Op op, Object... args) {
        this.op = op;
        this.x = args.length > 0 ? args[0] : null;
        this.y = args.length > 1 ? args[1] : null;
        this.z = args.length > 2 ? args[2] : null;
    }

    /**
     * Returns a human-readable representation of this tuple.
     */
    public String toString() {
        return op.format(x, y, z);
    }

    /**
     * Returns whether the given argument is written to by this tuple.
     */
    public boolean writesTo(Object a) {
        if (a == null) return false;
        if (op.getOutputArgumentIndex() == null) return false;
        if (op.getOutputArgumentIndex() == 0 && a.equals(x)) return true;
        if (op.getOutputArgumentIndex() == 1 && a.equals(y)) return true;
        if (op.getOutputArgumentIndex() == 2 && a.equals(z)) return true;
        return false;
    }
}
