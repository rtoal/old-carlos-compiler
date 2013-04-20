package edu.lmu.cs.xlg.squid;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A subroutine in a Squid representation.  Each user-defined
 * subroutine keeps track of its parameters, local variables, the
 * subroutine it is nested inside of (if any), the subroutines that
 * it calls, and stacks for loop continue and exit points.
 */
public class UserSubroutine extends Subroutine {
    private String name;
    private int level;
    public UserSubroutine parent;
    public List<Var> parameters = new ArrayList<Var>();
    public List<Var> locals = new ArrayList<Var>();
    public List<Tuple> tuples = new ArrayList<Tuple>();
    public Set<UserSubroutine> callees = new HashSet<UserSubroutine>();
    public Set<ExternalSubroutine> externalCallees =
        new HashSet<ExternalSubroutine>();

    /**
     * Creates a user subroutine with the given name and parent.
     */
    public UserSubroutine(String name, UserSubroutine parent) {
        this.name = name;
        this.level = parent == null ? 0 : 1 + parent.level;
        this.parent = parent;
    }

    /**
     * Returns the name.
     */
    public String toString() {
        return name;
    }

    /**
     * Returns the static nesting level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the textually enclosing parent subroutine.
     */
    public UserSubroutine getParent() {
        return parent;
    }

    /**
     * Adds p to the end of this subroutine's parameter list.
     */
    public void addParameter(Var p) {
        parameters.add(p);
    }

    /**
     * Adds p to the end of this subroutine's local variable list.
     */
    public void addVariable(Var p) {
        locals.add(p);
    }

    /**
     * Adds a new tuple to the end of this subroutine's tuple list,
     * updating the lists of callees if necessary.
     */
    public void addTuple(Op op, Object... args) {
        tuples.add(new Tuple(op, args));
        for (Object arg: args) {
            if (arg instanceof UserSubroutine) {
                callees.add((UserSubroutine)arg);
            } else if (arg instanceof ExternalSubroutine) {
                externalCallees.add((ExternalSubroutine)arg);
            }
        }
    }

    /**
     * Dumps information about this subroutine, and all subroutines
     * called directly or indirectly by this subroutine, to the
     * given writer.  The dump consists of a comment line with
     * subroutine details, followed by the tuples, followed by the
     * string literals found during the dump.
     */
    public void dump(PrintWriter w) {
        Set<StringConstant> stringLits = new HashSet<StringConstant>();
        dump(w, new HashSet<UserSubroutine>(), stringLits);
        for (StringConstant s: stringLits) {
            w.println(s + ":");
            w.println("\t" + s.values);
        }
        w.println();
    }

    /**
     * Helper for dump(w) which does a depth first traversal of the
     * call graph dump.  The visited set gathers up all the subroutines
     * we see on the traversal, so as to not revisit subroutines; the
     * stringLits set collects all the stringlits seen so they can be
     * written at the end of the entire dump.
     */
    private void dump(PrintWriter w, Set<UserSubroutine> visited,
            Set<StringConstant> stringLits) {
        visited.add(this);
        w.println(this + ":");
        w.print("\t;level=" + level + " parent=" + parent);
        w.println(" params=" + parameters + " locals=" + locals);
        for (Tuple t: tuples) {
            w.println(t);
            if (t.x instanceof StringConstant) stringLits.add((StringConstant)t.x);
            if (t.y instanceof StringConstant) stringLits.add((StringConstant)t.y);
            if (t.z instanceof StringConstant) stringLits.add((StringConstant)t.z);
        }
        for (UserSubroutine callee: callees) {
            if (!visited.contains(callee)) {
                callee.dump(w, visited, stringLits);
            }
        }
    }
}
