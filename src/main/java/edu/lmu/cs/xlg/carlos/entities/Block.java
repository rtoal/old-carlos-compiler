package edu.lmu.cs.xlg.carlos.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A block, which is a container of a sequence of statements with its own symbol table.
 */
public class Block extends Entity {

    private List<Statement> statements;
    private SymbolTable table = null;

    public Block(List<Statement> statements) {
        this.statements = statements;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    /**
     * Returns the list of all the types declared immediately within this block, in the order
     * they are declared.
     */
    public List<Type> getTypes() {
        List<Type> result = new ArrayList<Type>();
        for (Statement s: statements) {
            if (s instanceof Declaration) {
                Declarable d = ((Declaration)s).getDeclarable();
                if (d instanceof Type) {
                    result.add((Type)d);
                }
            }
        }
        return result;
    }

    /**
     * Returns the list of all the functions declared immediately within this block, in the order
     * they are declared.
     */
    public List<Function> getFunctions() {
        List<Function> result = new ArrayList<Function>();
        for (Statement s: statements) {
            if (s instanceof Declaration) {
                Declarable d = ((Declaration)s).getDeclarable();
                if (d instanceof Function) {
                    result.add((Function)d);
                }
            }
        }
        return result;
    }

    public SymbolTable getTable() {
        return table;
    }

    public void createTable(SymbolTable parent) {
        if (parent != null) {
            // TODO: Throw exception if table already present?
        }
        table = new SymbolTable(parent);
    }

    @Override
    public void analyze(AnalysisContext context) {
        List<Type> types = getTypes();
        List<Function> functions = getFunctions();

        // Create the table if it hasn't already been created.  For blocks
        // that are bodies of functions or for-statements, the analyze()
        // method of the function or statmement will have created this
        // table already, since it is the table in which the parameters
        // or for-statement index belong.  For blocks that are programs,
        // the table will have already been created, too.  All other
        // blocks will need their tables created here.
        if (table == null) {
            table = new SymbolTable(context.getTable());
        }

        // Struct types should go into the table first.  They can be
        // used for everything in this scope: function return types,
        // parameter types, variable types, field types, ....  Note
        // that they are going into the symbol table WITHOUT being
        // analyzed, because when analyzing them we have to check the
        // types of their fields, and these fields may refer to other
        // struct types declared in this block.
        for (Type type: types) {
            table.insert(type, context.getLog());
        }

        // Pre-analyze structure types so the fields are available.
        // This has to be done AFTER all the struct types have been
        // added to the symbol table, but BEFORE any variables are
        // handled, since the variables may refer to struct fields in
        // their initializing expressions.
        for (Type type: types) {
            type.analyze(context.withTable(table));
        }

        // Insert the functions into the table, but analyze ONLY the
        // parameters and return types.  We can't analyze the function
        // bodies until all the functions have been put into the
        // table (with analyzed parameters) because within any function
        // body there can be a call to any other function, and we have
        // to be able to analyze the call.  Notice also that the
        // functions are going in before any variables are being looked
        // at since variables can call any function in their initializing
        // expressions.
        for (Function function: functions) {
            function.analyzeSignature(context.withTable(table));
            table.insert(function, context.getLog());
        }

        // Now just go through all the items in order and analyze
        // everything, inserting variables as you go.  The variables
        // have to be inserted during this final pass, since they are
        // only in scope from their point of declaration onward.
        // (In other words, if we tried to first insert all the variables
        // and then analyze them later, that would have been wrong.)
        for (Statement s: statements) {
            if (s instanceof Declaration) {
                Declarable d = ((Declaration)s).getDeclarable();
                if (d instanceof Variable) {
                    table.insert(d, context.getLog());
                }
                if (d instanceof Type) {
                    // Don't analyze types again
                    continue;
                }
            }
            s.analyze(context.withTable(table));
        }
    }

    /**
     * Performs local optimizations on this block.  In particular we ask each statement in the
     * block to optimize itself.  In cases where the optimization of a statement detects dead
     * or unreachable code, we remove that statement.
     */
    public void optimize() {
        for (ListIterator<Statement> it = statements.listIterator(); it.hasNext();) {
            Statement original = it.next();
            Statement optimized = original.optimize();
            if (optimized == null) {
                it.remove();
            } else if (optimized != original) {
                it.set(optimized);
            }
        }
    }
}
