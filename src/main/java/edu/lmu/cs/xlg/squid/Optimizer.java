package edu.lmu.cs.xlg.squid;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;

/**
 * An object that can perform various optimizations on tuples.
 */
public class Optimizer {

    // Simple flag recording whether or not any changes have been made
    // to the current tuple list.  This is cleared every time we begin
    // optimizing a tuple list, and is set whenever a change is made.
    private boolean changed;

    /**
     * Optimizes (improves) the given user subroutine, and all subroutines
     * that it calls.
     */
    public void optimizeSubroutine(UserSubroutine s) {
        optimizeSubroutine(s, new HashSet<UserSubroutine>());
    }

    /**
     * Optimizes (improves) the given user subroutine, and all subroutines
     * that it calls that are not in a given set of visited subroutines.
     */
    private void optimizeSubroutine(UserSubroutine s,
            Set<UserSubroutine> visited) {
        visited.add(s);

        // Keep optimizing this list as long as changes are made
        do {
            changed = false;
            for (ListIterator<Tuple> i = s.tuples.listIterator(); i.hasNext();) {
                Tuple t = i.next();
                foldConstants(i, t);
                reduce(t);
                simplifyConditionalJump(i, t);
                propagateCopy(t, s.tuples.listIterator(i.nextIndex()));
            }
            eliminateDeadCode(s);
            eliminateUnreachableCode(s);
            // TODO: Remove all labels that aren't targets
        } while (changed);

        // Optimize any funtions called by this subroutine
        for (UserSubroutine callee: s.callees) {
            if (!visited.contains(callee)) {
                optimizeSubroutine(callee, visited);
            }
        }
    }

    /**
     * Replaces a binary operation on constant arguments with a copy
     * in a given tuple.
     *
     * Example:  (SUB, 5, 2, t0) ==> (COPY, 3, t0)
     */
    public void foldConstants(ListIterator<Tuple> i, Tuple t) {
        int value;

        // First fold constants in tuples with one input operand
        if (t.x instanceof Integer && t.z == null) {
            int x = (Integer)t.x;

            if (t.op == Op.NOT) value = x == 0 ? 1 : 0;
            else if (t.op == Op.NEG) value = -x;
            else if (t.op == Op.COMP) value = ~x;
            else return;
            changeTuple(t, Op.COPY, new Integer(value), t.y, null);

        // Next fold constants in tuples with two input operands
        } else if (t.x instanceof Integer && t.y instanceof Integer) {
            int x = (Integer)t.x;
            int y = (Integer)t.y;

            if (t.op == Op.ADD) value = x + y;
            else if (t.op == Op.SUB) value = x - y;
            else if (t.op == Op.MUL) value = x * y;
            else if (t.op == Op.DIV) value = x / y;
            else if (t.op == Op.MOD) value = x % y;
            else if (t.op == Op.SHL) value = x << y;
            else if (t.op == Op.SHR) value = x >> y;
            else if (t.op == Op.AND) value = x & y;
            else if (t.op == Op.OR) value = x | y;
            else if (t.op == Op.XOR) value = x ^ y;
            else if (t.op == Op.LT) value = x < y ? 1 : 0;
            else if (t.op == Op.LE) value = x <= y ? 1 : 0;
            else if (t.op == Op.EQ) value = x == y ? 1 : 0;
            else if (t.op == Op.NE) value = x != y ? 1 : 0;
            else if (t.op == Op.GT) value = x > y ? 1 : 0;
            else if (t.op == Op.GE) value = x >= y ? 1 : 0;
            else return;
            changeTuple(t, Op.COPY, new Integer(value), t.z, null);
        }
    }

    /**
     * Performs strength reduction on a single tuple.
     */
    public void reduce(Tuple t) {
        makeCopyIf(t, Op.ADD, t.x, 0, t.x);     // x + 0 = x
        makeCopyIf(t, Op.ADD, 0, t.y, t.y);     // 0 + y = y
        makeCopyIf(t, Op.SUB, t.x, 0, t.x);     // x - 0 = x
        makeCopyIf(t, Op.SUB, t.x, t.x, 0);     // x - x = 0
        makeCopyIf(t, Op.MUL, t.x, 0, 0);       // x * 0 = 0
        makeCopyIf(t, Op.MUL, 0, t.y, 0);       // 0 * y = 0
        makeCopyIf(t, Op.DIV, 0, t.y, 0);       // 0 / y = 0
        makeCopyIf(t, Op.MOD, 0, t.y, 0);       // 0 % y = 0
        makeCopyIf(t, Op.OR, t.x, 0, t.x);      // x | 0 = x
        makeCopyIf(t, Op.OR, 0, t.y, t.y);      // 0 | y = y
        makeCopyIf(t, Op.AND, t.x, 0, 0);       // x & 0 = 0
        makeCopyIf(t, Op.AND, 0, t.y, 0);       // 0 & y = 0
        makeCopyIf(t, Op.COS, 0, 1.0);          // cos(0) = 1.0

        //
        // TODO: More cases
        //

        // Convert JZ|JNZ to an unconditional jump if possible.
        if (t.op == Op.JZERO && t.x instanceof Integer && (Integer)t.x == 0) {
            t.set(Op.JUMP, t.y);
        }
        if (t.op == Op.JNZERO && t.x instanceof Integer && (Integer)t.x != 0) {
            t.set(Op.JUMP, t.y);
        }
    }

    /**
     * If t is of the form (op, x, y, r) for some r, then reduces to
     * to (COPY, z, r).
     */
    private void makeCopyIf(Tuple t, Op op, Object x, Object y, Object z) {
        if (t.op.equals(op) && t.x.equals(x) && t.y.equals(y)) {
            changeTuple(t, Op.COPY, z, t.z, null);
        }
    }

    /**
     * If t is of the form (op, x, r) for some r, then reduces to
     * to (COPY, y, r).
     */
    private void makeCopyIf(Tuple t, Op op, Object x, Object y) {
        if (t.op.equals(op) && t.x.equals(x)) {
            changeTuple(t, Op.COPY, y, t.y, null);
        }
    }

    /**
     * Replaces a sequence of two tuples, in which the first evaluates
     * a relational expression and the second is a conditional branch
     * based on the result of that expression being false, with a
     * single relational conditional branch tuple.
     *
     * Example: [(GT, x, y, t4), (JZERO, t4, L2)] ==> (JLE, x, y, L2)
     */
    public void simplifyConditionalJump(ListIterator<Tuple> i, Tuple t) {

        // Current tuple must be a relational operator
        if (t.op != Op.LT && t.op != Op.LE && t.op != Op.EQ
                && t.op != Op.NE && t.op != Op.GT && t.op != Op.GE) {
            return;
        }

        // If there is no next tuple then this optimization doesn't apply
        if (!i.hasNext()) {
            return;
        }

        // If the operands, all line up, do the optimization
        Tuple nextTuple = i.next();
        if (nextTuple.op == Op.JZERO && nextTuple.x == t.z) {
            t.op =
                t.op == Op.LT ? Op.JGE :
                t.op == Op.LE ? Op.JGT :
                t.op == Op.EQ ? Op.JNE :
                t.op == Op.NE ? Op.JEQ :
                t.op == Op.GT ? Op.JLE :
                t.op == Op.GE ? Op.JLT : t.op;
            t.z = nextTuple.y;
            removeTupleAt(i);
        } else if (nextTuple.op == Op.JNZERO && nextTuple.x == t.z) {
            t.op =
                t.op == Op.LT ? Op.JLT :
                t.op == Op.LE ? Op.JLE :
                t.op == Op.EQ ? Op.JEQ :
                t.op == Op.NE ? Op.JNE :
                t.op == Op.GT ? Op.JGT :
                t.op == Op.GE ? Op.JGE : t.op;
            t.z = nextTuple.y;
            removeTupleAt(i);
        } else {
            // Undo the advancement we used to peek ahead
            i.previous();
        }
    }

    /**
     * Propagates a copy up until a label or a write to either of the
     * arguments in the copy.  Potential aliasing problems are ignored.
     */
    private void propagateCopy(Tuple t, ListIterator<Tuple> i) {
        if (t.op != Op.COPY) return;
        Object source = t.x;
        Object dest = t.y;
        while (i.hasNext()) {
            Tuple t2 = i.next();
            if (t2.op == Op.LABEL) return;
            if (t2.writesTo(source)) return;
            if (t2.writesTo(dest)) return;
            if (t2.x == dest) t2.x = source;
            if (t2.y == dest) t2.y = source;
        }
    }
    /**
     * Removes tuples following an unconditional transfer of control
     * (RETP, RETF, JUMP, up to, but not including the next LABEL.
     */
    private void eliminateUnreachableCode(UserSubroutine s) {
        for (Iterator<Tuple> i = s.tuples.iterator(); i.hasNext();) {
            Tuple t = i.next();
            if (t.op == Op.RETP || t.op == Op.RETF || t.op == Op.JUMP) {
                while (true) {
                    if (!i.hasNext()) return;
                    Tuple t2 = i.next();
                    if (t2.op == Op.LABEL) break;
                    removeTupleAt(i);
                }
            }
        }
    }

    /**
     * Removes a tuple if it has no effect.  Examples:
     *
     *   (COPY, x, x)
     *   Copy to a temporary never subsequently used
     *   (ADD, x, 0, x)
     *   (MUL, x, 1, x)
     *   Tons of other arithmetic and logical identities
     *   (NULL_CHECK, x) where x is a non-zero intlit
     *   (ASSERT_POSITIVE, x) where x is a positive intlit
     *   (BOUND, x, y, z) where all are intlits and y &lt;= x &lt; z
     *   (JZERO, x, L) where x != 0
     *   (JNZERO, x, L) where x == 0
     *   [(JUMP, L), (LABEL, L)] ==> (LABEL, L)
     */
    private void eliminateDeadCode(UserSubroutine s) {
        for (ListIterator<Tuple> i = s.tuples.listIterator(); i.hasNext();) {
            Tuple t = i.next();

            // Copy to self
            if (t.op == Op.COPY && t.x.equals(t.y)) {
                removeTupleAt(i);

            // Asserting that a positive is positive
            } else if (t.op == Op.ASSERT_POSITIVE && t.x instanceof Integer
                    && (Integer)t.x > 0) {
                removeTupleAt(i);

            // Jumps not taken
            } else if (t.op == Op.JNZERO && t.x instanceof Integer
                    && (Integer)t.x == 0) {
                removeTupleAt(i);
            } else if (t.op == Op.JZERO && t.x instanceof Integer
                    && (Integer)t.x != 0) {
                removeTupleAt(i);

            // Assert not null on a non-null constant
            } else if (t.op == Op.NULL_CHECK && t.x instanceof Integer
                    && (Integer)t.x != 0) {
                removeTupleAt(i);

            // Remove JUMP to a label that comes right after it
            } else if (t.op == Op.JUMP) {
                if (i.hasNext()) {
                    Tuple t2 = i.next();
                    if (t2.op == Op.LABEL && t2.x == t.x) {
                        removeTupleAt(i);
                        t.set(Op.LABEL, t.x);
                    }
                }

            // TODO: Bound tuple check

            // TODO: Copy to temporary never subsequently used

            // Elimination of "identity" operations
            } else {
                makeNopIf(t, Op.ADD, 0, t.y, t.y);      // y := 0 + y
                makeNopIf(t, Op.ADD, t.x, 0, t.x);      // x := 0 + x
                makeNopIf(t, Op.MUL, 1, t.y, t.y);      // y := 1 * y
                makeNopIf(t, Op.MUL, t.x, 1, t.x);      // x := x * 1
                makeNopIf(t, Op.DIV, t.x, 1, t.x);      // x := x / 1
                makeNopIf(t, Op.MOD, t.x, 1, t.x);      // x := x % 1
                makeNopIf(t, Op.AND, t.x, 1, t.x);      // x := x & 1
                makeNopIf(t, Op.OR, t.x, 1, t.x);       // x := x | 1
                if (t.op == Op.NO_OP) removeTupleAt(i);
            }
        }
    }

    // TODO: Inlining

    // TODO: Tail Recursion Elimination

    // TODO: Other optimizations

    // Helper for eliminate dead code.  If t matches (op, x, y, z) make it
    // a NOP.
    private void makeNopIf(Tuple t, Op op, Object x, Object y, Object z) {
        if (t.op.equals(op) && t.x.equals(x) && t.y.equals(y)
            && t.z.equals(z)) {
            changeTuple(t, Op.NO_OP, null, null, null);
        }
    }

    // Helper for methods that change a tuple.  Changes t and marks
    // the subroutine changed.
    private void changeTuple(Tuple t, Op op, Object x, Object y, Object z) {
        t.set(op, x, y, z);
        changed = true;
    }

    // Helper for methods that eliminate code.  Removes the tuple and marks
    // the subroutine changed.
    private void removeTupleAt(Iterator<Tuple> i) {
        i.remove();
        changed = true;
    }
}
