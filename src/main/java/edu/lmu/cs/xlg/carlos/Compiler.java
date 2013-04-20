package edu.lmu.cs.xlg.carlos;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import edu.lmu.cs.xlg.carlos.entities.Entity.AnalysisContext;
import edu.lmu.cs.xlg.carlos.entities.Program;
import edu.lmu.cs.xlg.carlos.syntax.Parser;
import edu.lmu.cs.xlg.squid.UserSubroutine;
import edu.lmu.cs.xlg.translators.CarlosToJavaScriptTranslator;
import edu.lmu.cs.xlg.translators.CarlosToSquidTranslator;
import edu.lmu.cs.xlg.util.Log;

/**
 * A Carlos compiler, with a main() method to enable running as a standalone application, and
 * utility methods to compile from a stream for invocation by other applications (like testers,
 * debuggers, IDEs, etc).
 */
public class Compiler {

    // All output uses a custom logger reading from a property file of base name Carlos.
    private Log log = new Log("Carlos", new PrintWriter(System.out, true));

    /**
     * Processes command line arguments and runs the compiler based on the arguments. The command
     * line syntax for running the compiler as an application is:
     * <pre>
     * java Carlos [option] &lt;basefilename&gt;
     * </pre>
     * where &lt;basefilename&gt; is the name of the Carlos source file without the mandatory
     * <code>.carlos</code> extension. Option is:
     * <pre>
     *   -syn: check syntax only
     *   -sem: check static semantics only
     *   -c: translate to C
     *   -js: translate to JavaScript
     *   -q: translate to Squid
     * </pre>
     */
    public static void main(String[] args) throws IOException {

        Compiler compiler = new Compiler();
        String option;
        String baseFileName;

        // Resolve command line arguments. If the option argument is missing, use "-exe" as a default.
        if (args.length == 1) {
            option = "-exe";
            baseFileName = args[0];
        } else if (args.length == 2) {
            option = args[0];
            baseFileName = args[1];
        } else {
            compiler.log.message("usage");
            return;
        }

        // Do as much compilation as the options request.
        Reader reader = new FileReader(baseFileName);
        try {
            if (option.equals("-syn")) {
                compiler.checkSyntax(reader);
            } else if (option.equals("-sem")) {
                compiler.checkSemantics(reader);
            } else if (option.equals("-q")) {
                UserSubroutine main = compiler.generateQuads(reader);
                main.dump(new PrintWriter(System.out, true));
            } else if (option.equals("-js")) {
                compiler.generateJavaScript(reader, new PrintWriter(new FileWriter(baseFileName + ".js")));
            } else {
                compiler.log.message("usage");
            }
        } catch (Exception e) {
            compiler.log.exception(e);
        }
    }

    /**
     * Checks the syntax of a Carlos program from a reader.
     */
    public Program checkSyntax(Reader reader) throws IOException {
        log.clearErrors();
        Parser parser = new Parser(reader);
        try {
            log.message("checking_syntax");
            return parser.parse(reader, log);
        } finally {
            reader.close();
        }
    }

    /**
     * Checks the syntax and static semantics given Carlos source code from a reader.
     */
    public Program checkSemantics(Reader reader) throws IOException {
        Program program = checkSyntax(reader);
        if (log.getErrorCount() > 0) {
            return null;
        }
        return checkSemantics(program);
    }

    /**
     * Checks the semantics of a program object.
     */
    public Program checkSemantics(Program program) throws IOException {
        log.message("checking_semantics");
        program.analyze(AnalysisContext.makeGlobalContext(log));
        return program;
    }

    /**
     * Compiles a Carlos program into a Squid object.
     */
    public UserSubroutine generateQuads(Reader reader) throws IOException {
        Program program = checkSemantics(reader);
        if (log.getErrorCount() > 0) {
            return null;
        }
        return new CarlosToSquidTranslator(4, 8).translateProgram(program);
    }

    /**
     * Compiles a Carlos program from a .carlos file into an assembly language program, writing
     * the output to a .asm file.
     */
    public void generateJavaScript(Reader reader, PrintWriter writer) throws IOException {
        Program program = checkSemantics(reader);
        if (log.getErrorCount() > 0) {
            return;
        }
        new CarlosToJavaScriptTranslator().translateProgram(program, writer);
    }

    /**
     * Returns the number of errors logged so far.
     */
    public int getErrorCount() {
        return log.getErrorCount();
    }

    /**
     * Tells the compiler whether or not it should suppress log messages.
     */
    public void setQuiet(boolean quiet) {
        log.setQuiet(quiet);
    }
}
