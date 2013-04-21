package edu.lmu.cs.xlg.translators;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import edu.lmu.cs.xlg.carlos.entities.AssignmentStatement;
import edu.lmu.cs.xlg.carlos.entities.Block;
import edu.lmu.cs.xlg.carlos.entities.BooleanLiteral;
import edu.lmu.cs.xlg.carlos.entities.BreakStatement;
import edu.lmu.cs.xlg.carlos.entities.CallStatement;
import edu.lmu.cs.xlg.carlos.entities.Case;
import edu.lmu.cs.xlg.carlos.entities.CharLiteral;
import edu.lmu.cs.xlg.carlos.entities.ClassicForStatement;
import edu.lmu.cs.xlg.carlos.entities.Declaration;
import edu.lmu.cs.xlg.carlos.entities.Expression;
import edu.lmu.cs.xlg.carlos.entities.Function;
import edu.lmu.cs.xlg.carlos.entities.IfStatement;
import edu.lmu.cs.xlg.carlos.entities.InfixExpression;
import edu.lmu.cs.xlg.carlos.entities.IntegerLiteral;
import edu.lmu.cs.xlg.carlos.entities.NullLiteral;
import edu.lmu.cs.xlg.carlos.entities.PostfixExpression;
import edu.lmu.cs.xlg.carlos.entities.PrefixExpression;
import edu.lmu.cs.xlg.carlos.entities.PrintStatement;
import edu.lmu.cs.xlg.carlos.entities.Program;
import edu.lmu.cs.xlg.carlos.entities.RealLiteral;
import edu.lmu.cs.xlg.carlos.entities.ReturnStatement;
import edu.lmu.cs.xlg.carlos.entities.Statement;
import edu.lmu.cs.xlg.carlos.entities.Variable;
import edu.lmu.cs.xlg.carlos.entities.VariableExpression;
import edu.lmu.cs.xlg.carlos.entities.WhileStatement;

/**
 * A translator from Carlos semantic graphs to JavaScript.
 */
public class CarlosToJavaScriptTranslator {

    private PrintWriter writer;
    private int indentPadding = 4;
    private int indentLevel = 0;

    public void translateProgram(Program program, PrintWriter writer) {
        this.writer = writer;
        emit("(function () {");
        generateBlock(program);
        emit("}());");
    }

    private void generateBlock(Block block) {
        indentLevel++;
        for (Statement s: block.getStatements()) {
            generateStatement(s);
        }
        indentLevel--;
    }

    private void generateStatement(Statement s) {

        if (s instanceof Declaration) {
            generateDeclaration(Declaration.class.cast(s));

        } else if (s instanceof AssignmentStatement) {
            generateAssignmentStatement(AssignmentStatement.class.cast(s));

        } else if (s instanceof CallStatement) {
            generateCallStatement(CallStatement.class.cast(s));

        } else if (s instanceof BreakStatement) {
            emit("break;");

        } else if (s instanceof ReturnStatement) {
            generateReturnStatement(ReturnStatement.class.cast(s));

        } else if (s instanceof PrintStatement) {
            generatePrintStatement(PrintStatement.class.cast(s));

        } else if (s instanceof IfStatement) {
            generateIfStatement(IfStatement.class.cast(s));

        } else if (s instanceof WhileStatement) {
            generateWhileStatement(WhileStatement.class.cast(s));

        } else if (s instanceof ClassicForStatement) {
            generateClassicForStatement(ClassicForStatement.class.cast(s));

        } else {
            throw new RuntimeException("Unknown statement class: " + s.getClass().getName());
        }
    }

    private void generateDeclaration(Declaration s) {
        if (s.getDeclarable() instanceof Variable) {
            generateVariableDeclaration(Variable.class.cast(s.getDeclarable()));
        } else if (s.getDeclarable() instanceof Function) {
            generateFunctionDeclaration(Function.class.cast(s.getDeclarable()));
        }
    }

    private void generateVariableDeclaration(Variable v) {
        if (v.getInitializer() == null) {
            emit ("var _v%d;", v.getId());
        } else {
            emit ("var _v%d = %s;", v.getId(), generateExpression(v.getInitializer()));
        }
    }

    private void generateFunctionDeclaration(Function f) {
        emit("var _f%d = function (%s) {", f.getId(), generateIdList(f.getParameters()));
        generateBlock(f.getBody());
        emit("}");

    }

    private void generateAssignmentStatement(AssignmentStatement s) {
        emit("%s = %s;", generateExpression(s.getLeft()), generateExpression(s.getRight()));
    }

    private void generateCallStatement(CallStatement s) {
        emit("_f%d(%s);", s.getFunction().getId(), generateExpressionList(s.getArgs()));
    }

    private void generateReturnStatement(ReturnStatement s) {
        if (s.getReturnExpression() == null) {
            emit("return;");
        } else {
            emit("return %s;", generateExpression(s.getReturnExpression()));
        }
    }

    private void generatePrintStatement(PrintStatement s) {
        emit("console.log(" + generateExpressionList(s.getArgs()) + ")");
    }

    private void generateIfStatement(IfStatement s) {
        String lead = "if";
        for (Case c: s.getCases()) {
            emit("%s (%s) {", lead, generateExpression(c.getCondition()));
            generateBlock(c.getBody());
            lead = "} else if";
        }
        if (s.getElsePart() != null) {
            emit("} else {");
            generateBlock(s.getElsePart());
        }
        emit("}");
    }

    private void generateWhileStatement(WhileStatement s) {
        emit("while (%s) {", generateExpression(s.getCondition()));
        generateBlock(s.getBody());
        emit("}");
    }

    private void generateClassicForStatement(ClassicForStatement s) {

    }

    private String generateExpression(Expression e) {
        if (e instanceof IntegerLiteral) {
            return IntegerLiteral.class.cast(e).getValue().toString();
        } else if (e instanceof CharLiteral) {
            return CharLiteral.class.cast(e).getValue().toString();
        } else if (e instanceof RealLiteral) {
            return RealLiteral.class.cast(e).getValue().toString();
        } else if (e instanceof NullLiteral) {
            return "null";
        } else if (e == BooleanLiteral.TRUE) {
            return "true";
        } else if (e == BooleanLiteral.FALSE) {
            return "false";
        } else if (e instanceof PrefixExpression) {
            return generatePrefixExpression(PrefixExpression.class.cast(e));
        } else if (e instanceof PostfixExpression) {
            return generatePostfixExpression(PostfixExpression.class.cast(e));
        } else if (e instanceof InfixExpression) {
            return generateInfixExpression(InfixExpression.class.cast(e));
        } else if (e instanceof VariableExpression) {
            return generateVariableExpression(VariableExpression.class.cast(e));
        } else {
            throw new RuntimeException("Unknown entity class: " + e.getClass().getName());
        }
    }

    private String generatePrefixExpression(PrefixExpression e) {
        return "NOT YET";
    }

    private String generatePostfixExpression(PostfixExpression e) {
        return "NOT YET";
    }

    private String generateInfixExpression(InfixExpression e) {
        return "NOT YET";
    }

    private String generateVariableExpression(VariableExpression e) {
        return "NOT YET";
    }

    private String generateExpressionList(List<Expression> list) {
        return "NOT YET";
    }

    private String generateIdList(List<Variable> list) {
        List<String> names = new ArrayList<String>();
        for (Variable v : list) {
            names.add(String.format("_v%d", v.getId()));
        }
        return Joiner.on(", ").join(names);
    }

    private void emit(String line, Object... args) {
        int pad = indentPadding * indentLevel;

        if (args.length != 0) {
            line = String.format(line, args);
        }

        // printf does not allow "%0s" as a format specifier, darn it.
        if (pad == 0) {
            writer.println(line);
        } else {
            writer.printf("%" + pad + "s%s\n", "", line);
        }
    }
}
