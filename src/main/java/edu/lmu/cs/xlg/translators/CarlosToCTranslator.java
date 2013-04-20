package edu.lmu.cs.xlg.translators;

import java.io.PrintWriter;

import edu.lmu.cs.xlg.carlos.entities.Program;

/**
 * A translator from Carlos semantic graphs to C.
 */
public class CarlosToCTranslator {

    public void translateProgram(Program program, PrintWriter writer) {
        writer.println("// Translation to C is not yet implemented");
    }
}
