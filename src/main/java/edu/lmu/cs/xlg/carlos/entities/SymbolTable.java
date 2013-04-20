package edu.lmu.cs.xlg.carlos.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.lmu.cs.xlg.util.Log;

/**
 * Simple symbol table.
 */
public class SymbolTable extends Entity {

    // The actual contents of the symbol table.  Names map to entities.
    Map<String, Entity> map = new HashMap<String, Entity>();

    // The table to look in if you can't find what you want here.
    SymbolTable parent;

    /**
     * Creates a symbol table with the given parent.
     */
    public SymbolTable(SymbolTable parent) {
        this.map = new HashMap<String, Entity>();
        this.parent = parent;
    }

    /**
     * Inserts an item into the table, checking to make sure the insertion
     * is allowed.  The basic rule is that no duplicate names are allowed
     * unless each declaration is a function.
     */
    public void insert(Declarable d, Log log) {
        Object oldValue = map.put(d.getName(), d);

        if (oldValue == null) {
            // No other entity in this symbol had this name, we're okay.
            return;

        } else if (!(oldValue instanceof Function && d instanceof Function)) {
            // If not both functions, no overloading is possible, ever.
            log.error("redeclared_identifier", d.getName());

        } else {
            // They can live together, so link the old one back in.
            ((Function)d).setOverload((Function)oldValue);
        }
    }

    /**
     * Looks up a type in this table, or if not found, searches along its
     * ancestor chain.
     *
     * @param name the name of the type being searched for.
     * @return the innermost visible type with that name.  If not found,
     * or if the value found is not a type object, logs an error message
     * and returns Type.ANY.
     */
    public Type lookupType(String name, Log log) {
        if (name.endsWith("[]")) {
            return lookupType(name.substring(0,name.length()-2), log).array();
        }

        Object value = map.get(name);
        if (value == null) {
            if (parent == null) {
                log.error("type_not_found", name);
                return Type.ARBITRARY;
            } else {
                return parent.lookupType(name, log);
            }
        } else if (value instanceof Type) {
            return (Type)value;
        } else {
            log.error("not_a_type", name);
            return Type.ARBITRARY;
        }
    }

    /**
     * Looks up a variable in this table, or if not found, searches along
     * its ancestor chain.
     *
     * @param name the name of the variable being searched for.
     * @return the innermost visible variable with that name.  If not found,
     * or if the value found is not a variable object, logs an error message
     * and returns Variable.ARBITRARY.
     */
    public Variable lookupVariable(String name, Log log) {
        Object value = map.get(name);
        if (value == null) {
            if (parent == null) {
                log.error("variable_not_found", name);
                return Variable.ARBITRARY;
            } else {
                return parent.lookupVariable(name, log);
            }
        } else if (value instanceof Variable) {
            return (Variable)value;
        } else {
            log.error("not_a_variable", name);
            return Variable.ARBITRARY;
        }
    }

    /**
     * Looks up a function in this table, or if not found, searches along
     * its ancestor chain.
     *
     * @param name the name of the function to search for.
     * @param args the argument list used to call the function, required
     * since the language supports overloading.
     * @param log the logger for messages and errors.
     * @return the innermost visible function with the given name that
     * matches the argument list provided there is only one match, or
     * null if there are either zero or more than one visible matches.
     */
    public Function lookupFunction(String name, List<Expression> args, Log log) {
        Object value = map.get(name);

        if (value == null) {
            // Not found here -- keep searching if possible, else error out.
            if (parent == null) {
                log.error("function_not_found", name);
                return null;
            } else {
                return parent.lookupFunction(name, args, log);
            }

        } else if (value instanceof Function) {
            // Got a function, make sure it is the ONLY callable one
            Function candidate = null;
            for (Function f = (Function)value; f != null; f = f.getOverload()) {
                if (f.canBeCalledWith(args)) {
                    if (candidate != null) {
                        // Second match.
                        log.error("multiple_callables", f.getName());
                        return null;
                    }
                    candidate = f;
                }
            }
            if (candidate != null) {
                // Sole match!
                return candidate;
            }

            // No matches.
            log.error("non_matching_args", name, args.size() + "");
            return null;

        } else {
            // Found something other than a function
            log.error("not_a_function", name);
            return null;
        }
    }

    /**
     * Returns all the entities in this symbol table that are instances
     * of class c or any descendant of c.  To get all the entities in the
     * table, pass in class Entity (or java.lang.Object).
     */
    public Set<Object> getEntitiesByClass(Class<?> c) {
        Set<Object> result = new HashSet<Object>();
        for (Object value: map.values()) {
            if (c.isInstance(value)) {
                result.add(value);
            }
        }
        return result;
    }

    @Override
    public void analyze(AnalysisContext context) {
        // Intentionally empty
    }
}
