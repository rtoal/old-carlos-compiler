package edu.lmu.cs.xlg.carlos.entities;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.lmu.cs.xlg.util.Log;

/**
 * Superclass for all entities in the compiler.
 *
 * The front end of the compiler produces an abstract syntax tree which is then decorated
 * (or  transformed) into a semantic graph.  The nodes this structure are called entities.
 * The hierarchy for entities will look something like this:
 *
 * Entity
 *    Block
 *       Program
 *    Declarable
 *       Variable
 *       Type
 *          ArrayType
 *          StructType
 *       Function
 *    Statement
 *       Declaration
 *       IncrementStatement
 *       CallStatement
 *       AssignmentStatement
 *       BreakStatement
 *       ContinueStatement
 *       ReturnStatement
 *       PrintStatement
 *       IfStatement
 *       WhileStatement
 *       ClassicForStatement
 *    Expression
 *       Literal
 *          IntegerLiteral
 *          RealLiteral
 *          BooleanLiteral
 *          CharLiteral
 *          StringLiteral
 *       VariableExpression
 *          SimpleVariableReference
 *          SubscriptedVariable
 *          DottedVariable
 *          CallExpression
 *       EmptyArray
 *       ArrayAggregate
 *       StructAggregate
 *       PrefixExpression
 *       PostfixExpression
 *       InfixExpression
 *
 * Each concrete entity class has a constructor to fill in the "syntactic" part of the entity.
 * For example, we know the name of a variable reference while generating the abstract syntax
 * tree, so that is filled in by the constructor.  However, we don't know until semantic analysis
 * exactly which variable is being referred to, so that field is not filled in by the constructor.
 */
public abstract class Entity {

    /**
     * Collection of all entities that have ever been created, as a map with the entities as keys
     * and their ids as values.
     */
    private static Map<Entity, Integer> all = new LinkedHashMap<Entity, Integer>();

    /**
     * Creates an entity, "assigning" it a new unique id by placing it in a global map mapping the
     * entity to its id.
     */
    public Entity() {
        synchronized (all) {
            all.put(this, all.size());
        }
    }

    /**
     * Returns the integer id of this entity.
     */
    public Integer getId() {
        return all.get(this);
    }

    /**
     * Returns a short string containing this entity's id.
     */
    @Override
    public String toString() {
        return "#" + getId();
    }

    /**
     * Writes a simple, indented, syntax tree rooted at the given entity to the given print
     * writer.  Each level is indented two spaces.
     */
    public final void printSyntaxTree(String indent, String prefix, PrintWriter out) {

        // Prepare the line to be written
        String classname = getClass().getName();
        String kind = classname.substring(classname.lastIndexOf('.') + 1);
        String line = indent + prefix + "(" + kind + ")";

        // Process the fields, adding plain attributes to the line, but storing all the entity
        // children in a linked hash map to be processed after the line is written.  We use a
        // linked hash map because the order of output is important.
        Map<String, Entity> children = new LinkedHashMap<String, Entity>();
        for (Map.Entry<String, Object> entry: attributes().entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            } else if (value instanceof Entity) {
                children.put(name, Entity.class.cast(value));
            } else if (value instanceof Iterable<?>) {
                try {
                    int index = 0;
                    for (Object child : (Iterable<?>) value) {
                        children.put(name + "[" + (index++) + "]", (Entity) child);
                    }
                } catch (ClassCastException cce) {
                    // Special case for non-entity collections
                    line += " " + name + "=" + value;
                }
            } else {
                // Simple attribute, attach description to node name
                line += " " + name + "=" + value;
            }
        }
        out.println(line);

        // Now we can go through all the entity children that were saved up earlier
        for (Map.Entry<String, Entity> child: children.entrySet()) {
            child.getValue().printSyntaxTree(indent + "  ", child.getKey() + ": ", out);
        }
    }

    /**
     * Traverses the semantic graph starting at this entity, applying visitor v to each entity.
     */
    public void traverse(Visitor v, Set<Entity> visited) {

        // The graph may have cycles, so skip this entity if we have seen it before.  If we
        // haven't, mark it seen.
        if (visited.contains(this)) {
            return;
        }
        visited.add(this);

        v.onEntry(this);
        for (Map.Entry<String, Object> entry: attributes().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Entity) {
                Entity.class.cast(value).traverse(v, visited);
            } else if (value instanceof Iterable<?>) {
                for (Object child : (Iterable<?>) value) {
                    if (child instanceof Entity) {
                        Entity.class.cast(child).traverse(v, visited);
                    }
                }
            }
        }
        v.onExit(this);
    }

    public static interface Visitor {
        void onEntry(Entity e);
        void onExit(Entity e);
    }

    /**
     * Writes a concise line with the entity's id number, class, and non-null properties.  For any
     * property that is itself an entity, or a collection of entities, only the entity id is
     * written.
     */
    private void writeDetailLine(PrintWriter writer) {
        String classname = getClass().getName();
        String kind = classname.substring(classname.lastIndexOf('.') + 1);
        writer.print(this + "\t(" + kind + ")");

        for (Map.Entry<String, Object> entry: attributes().entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value.getClass().isArray()) {
                value = Arrays.asList((Object[]) value);
            }
            writer.print(" " + name + "=" + value);
        }
        writer.println();
    }

    public final void printEntities(final PrintWriter writer) {
        traverse(new Visitor() {
            public void onEntry(Entity e) {
                e.writeDetailLine(writer);
            }
            public void onExit(Entity e) {
                // Intentionally empty
            }
        }, new HashSet<Entity>());
    }

    /**
     * Returns a map of name-value pairs for the given entity's fields and their values.  The
     * set of fields computed here are the non-static declared fields of its class, together with
     * the relevant fields of its ancestor classes, up to but not including the class Entity
     * itself.
     */
    private Map<String, Object> attributes() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (Class<?> c = getClass(); c != Entity.class; c = c.getSuperclass()) {
            for (Field field: c.getDeclaredFields()) {
                if ((field.getModifiers() & Modifier.STATIC) == 0) {
                    try {
                        field.setAccessible(true);
                        result.put(field.getName(), field.get(this));
                    } catch (IllegalAccessException cannotHappen) {
                    }
                }
            }
        }
        return result;
    }

    /**
     * Context for semantic analysis.
     *
     * Context objects bundle four properties:
     * <ul>
     *   <li>A logger to count errors and log messages.
     *   <li>The symbol table in which all identifiers should be looked up.
     *   <li>The innermost enclosing function of the entity being analyzed.
     *   <li>Whether we are in a loop, necessary for checking break statements, for example.
     * </ul>
     */
    public static class AnalysisContext {
        private Log log;
        private SymbolTable table;
        private Function function;
        private boolean inLoop;

        private AnalysisContext(Log log, SymbolTable table, Function function, boolean inLoop) {
            this.log = log;
            this.table = table;
            this.function = function;
            this.inLoop = inLoop;
        }

        public static AnalysisContext makeGlobalContext(Log log) {
            AnalysisContext context = new AnalysisContext(log, null, null, false);
            SymbolTable global = new SymbolTable(null);
            global.insert(Type.INT, context.getLog());
            global.insert(Type.REAL, context.getLog());
            global.insert(Type.BOOLEAN, context.getLog());
            global.insert(Type.CHAR, context.getLog());
            global.insert(Type.STRING, context.getLog());
            global.insert(Function.GET_STRING, context.getLog());
            global.insert(Function.SUBSTRING, context.getLog());
            global.insert(Function.SQRT, context.getLog());
            global.insert(Function.PI, context.getLog());
            global.insert(Function.SIN, context.getLog());
            global.insert(Function.COS, context.getLog());
            global.insert(Function.ATAN, context.getLog());
            global.insert(Function.LN, context.getLog());
            return context.withTable(global);
        }

        public AnalysisContext withTable(SymbolTable table) {
            return new AnalysisContext(this.log, table, this.function, this.inLoop);
        }

        public AnalysisContext withFunction(Function function) {
            return new AnalysisContext(this.log, this.table, function, this.inLoop);
        }

        public AnalysisContext withInLoop(boolean inLoop) {
            return new AnalysisContext(this.log, this.table, this.function, inLoop);
        }

        public Log getLog() {
            return log;
        }

        public SymbolTable getTable() {
            return table;
        }

        public Function getFunction() {
            return function;
        }

        public boolean isInLoop() {
            return inLoop;
        }

        public Type lookupType(String name) {
            return getTable().lookupType(name, getLog());
        }

        public Variable lookupVariable(String name) {
            return getTable().lookupVariable(name, getLog());
        }

        public Function lookupFunction(String name, List<Expression> args) {
            return getTable().lookupFunction(name, args, getLog());
        }

        public void error(String errorKey, Object... arguments) {
            log.error(errorKey, arguments);
        }
    }

    /**
     * Performs semantic analysis on this entity, and (necessarily) on its descendants.
     */
    public abstract void analyze(AnalysisContext context);
}
