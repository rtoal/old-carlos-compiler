package edu.lmu.cs.xlg.carlos;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import edu.lmu.cs.xlg.carlos.entities.Entity.AnalysisContext;
import edu.lmu.cs.xlg.carlos.entities.Program;
import edu.lmu.cs.xlg.carlos.syntax.Parser;
import edu.lmu.cs.xlg.translators.CarlosToJavaScriptTranslator;
import edu.lmu.cs.xlg.util.Log;

/**
 * A Carlos compiler, with a main() method to enable running as a standalone application, and
 * utility methods to compile from a stream for invocation by other applications (like testers,
 * debuggers, IDEs, etc).
 */
public class Compiler {

    /**
     *  A custom logger that writes errors and messages from a property file of base name Carlos.
     */
    private Log log = new Log("Carlos", new PrintWriter(System.err, true));

    /**
     * Processes command line arguments and runs the compiler based on the arguments. The command
     * line syntax for running the compiler as an application is:
     * <pre>
     * java Carlos [option] &lt;basefilename&gt;
     * </pre>
     * where &lt;basefilename&gt; is the name of the Carlos source file without the mandatory
     * <code>.carlos</code> extension. Option is:
     * <pre>
     *   -syn: check syntax only, writes to stdout.
     *   -sem: check static semantics only, writes semantic graph to stdout.
     *   -opt: stop after optimizing the semantic graph, writes to stdout.
     *   -js: (the default) translate to JavaScript, writes to .js file.
     * </pre>
     */
    public static void main(String[] args) throws IOException {

        Compiler compiler = new Compiler();
        String option;
        String baseFileName;

        // Resolve command line arguments. If the option argument is missing, use "-js" as a default.
        if (args.length == 1) {
            option = "-js";
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
                Program program = compiler.checkSyntax(reader);
                program.printSyntaxTree("", "", new PrintWriter(System.out, true));
            } else if (option.equals("-sem")) {
                Program program = compiler.checkSemantics(reader);
                program.printEntities(new PrintWriter(System.out, true));
            } else if (option.equals("-opt")) {
                Program program = compiler.produceOptimizedSemanticGraph(reader);
                program.printEntities(new PrintWriter(System.out, true));
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
     * Does the whole front end given Carlos source code from a reader.
     */
    public Program produceOptimizedSemanticGraph(Reader reader) throws IOException {
        Program program = checkSemantics(reader);
        if (log.getErrorCount() > 0) {
            return null;
        }
        log.message("optimizing");
        program.optimize();
        return program;
    }

    /**
     * Compiles a Carlos program from a reader and writes the JavaScript to a writer.
     */
    public void generateJavaScript(Reader reader, PrintWriter writer) throws IOException {
        Program program = produceOptimizedSemanticGraph(reader);
        if (log.getErrorCount() > 0) {
            return;
        }
        log.message("writing");
        new CarlosToJavaScriptTranslator().translateProgram(program, writer);
        writer.close();
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
