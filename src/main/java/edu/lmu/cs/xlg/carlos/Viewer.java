package edu.lmu.cs.xlg.carlos;

import static javax.swing.KeyStroke.getKeyStroke;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import edu.lmu.cs.xlg.carlos.entities.Entity.AnalysisContext;
import edu.lmu.cs.xlg.carlos.entities.Program;
import edu.lmu.cs.xlg.carlos.syntax.Parser;
import edu.lmu.cs.xlg.squid.Optimizer;
import edu.lmu.cs.xlg.squid.UserSubroutine;
import edu.lmu.cs.xlg.translators.CarlosToJavaScriptTranslator;
import edu.lmu.cs.xlg.translators.CarlosToSquidTranslator;
import edu.lmu.cs.xlg.util.Log;

/**
 * A simple GUI application for viewing the different things the Carlos
 * compiler can do.
 *
 * The application has two panes.  The left is a simple text editor in
 * which one can edit a Carlos program, load one from the file system,
 * and save to the file system.  The right shows a view of the program
 * in response to a user selection action.  The user can choose to see
 * <ul>
 *   <li>The abstract syntax tree
 *   <li>The semantic graph
 *   <li>A translation to JavaScript
 *   <li>A translation to C
 *   <li>A translation to Squid
 * </ul>
 */
@SuppressWarnings("serial")
public class Viewer extends JFrame {

    private JTextArea source = new JTextArea(30, 60);
    private JTextArea view = new JTextArea(30, 80) {
        public void setText(String text) {
            super.setText(text);
            this.setCaretPosition(0);
        }
    };
    private JScrollPane sourcePane = new JScrollPane();
    private JScrollPane viewPane = new JScrollPane();
    private StringWriter errors = new StringWriter();
    private Log log = new Log("Carlos", new PrintWriter(errors));
    private File currentFile;
    private JFileChooser chooser = new JFileChooser(".");

    public Viewer() {
        JSplitPane splitPane = new JSplitPane();
        JMenuBar menuBar = new JMenuBar();

        Action newAction = new AbstractAction("New") {
            {
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
                putValue(Action.ACCELERATOR_KEY, getKeyStroke("control N"));
            }
            public void actionPerformed(ActionEvent e) {newFile();}
        };

        Action openAction = new AbstractAction("Open") {
            {
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
                putValue(Action.ACCELERATOR_KEY, getKeyStroke("control O"));
            }
            public void actionPerformed(ActionEvent e) {openFile();}
        };

        Action saveAction = new AbstractAction("Save") {
            {
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
                putValue(Action.ACCELERATOR_KEY, getKeyStroke("control S"));
            }
            public void actionPerformed(ActionEvent e) {saveFile();}
        };

        Action saveAsAction = new AbstractAction("Save As") {
            public void actionPerformed(ActionEvent e) {saveAsFile();}
        };

        Action quitAction = new AbstractAction("Quit") {
            {
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Q);
                putValue(Action.ACCELERATOR_KEY, getKeyStroke("control Q"));
            }
            public void actionPerformed(ActionEvent e) {quit();}
        };

        Action syntaxAction = new AbstractAction("Syntax") {
            public void actionPerformed(ActionEvent e) {viewSyntaxTree();}
        };

        Action semanticsAction = new AbstractAction("Semantics") {
            public void actionPerformed(ActionEvent e) {viewSemanticGraph();}
        };

        Action quadsAction = new AbstractAction("Squid") {
            public void actionPerformed(ActionEvent e) {viewQuads();}
        };

        Action optimizeAction = new AbstractAction("Optimize") {
            public void actionPerformed(ActionEvent e) {viewOptimizedQuads();}
        };

        Action javaScriptAction = new AbstractAction("JavaScript") {
            public void actionPerformed(ActionEvent e) {viewJavaScript();}
        };

        menuBar.add(new JButton(newAction));
        menuBar.add(new JButton(openAction));
        menuBar.add(new JButton(saveAction));
        menuBar.add(new JButton(saveAsAction));
        menuBar.add(new JButton(syntaxAction));
        menuBar.add(new JButton(semanticsAction));
        menuBar.add(new JButton(quadsAction));
        menuBar.add(new JButton(javaScriptAction));
        setJMenuBar(menuBar);

        source.setFont(new Font("Monospaced", 0, 16));
        sourcePane.setViewportView(source);
        splitPane.setLeftComponent(sourcePane);
        view.setEditable(false);
        view.setFont(new Font("Monospaced", 0, 16));
        viewPane.setViewportView(view);
        splitPane.setRightComponent(viewPane);
        splitPane.setDividerLocation(480);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        setTitle("Carlos Viewer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 712);
    }

    private void newFile() {
        currentFile = null;
        source.setText("");
    }

    private void openFile() {
        chooser.showOpenDialog(this);
        currentFile = chooser.getSelectedFile();
        if (currentFile == null) return;

        StringBuffer buffer = new StringBuffer();
        try {
            BufferedReader in = new BufferedReader(
                new FileReader(currentFile.getCanonicalPath()));
            String line;
            while ((line = in.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            in.close();
        } catch (IOException ignored) {
        }
        source.setText(buffer.toString());
    }

    private void saveFile() {
        if (currentFile == null) {
            view.setText("Nothing to save");
            return;
        }

        try {
            BufferedWriter out = new BufferedWriter(
                new FileWriter(currentFile.getCanonicalPath()));
            out.write(source.getText());
            out.close();
            view.setText("File saved");
        } catch (IOException e) {
            view.setText("File not saved: " + e.getMessage());
        }
    }

    private void saveAsFile() {
        chooser.showSaveDialog(this);
        currentFile = chooser.getSelectedFile();
        if (currentFile != null) {
            saveFile();
        }
    }

    private void quit() {
        System.exit(0);
    }

    private void viewSyntaxTree() {
        viewPane.setViewportView(view);
        Program program = parse();
        if (log.getErrorCount() > 0) {
            view.setText(errors.toString());
        } else {
            StringWriter writer = new StringWriter();
            program.printSyntaxTree("", "", new PrintWriter(writer));
            view.setText(writer.toString());
        }
    }

    private void viewSemanticGraph() {
        viewPane.setViewportView(view);
        Program program = analyze();
        if (log.getErrorCount() > 0) {
            view.setText(errors.toString());
        } else {
            StringWriter writer = new StringWriter();
            program.printEntities(new PrintWriter(writer));
            view.setText(writer.toString());
        }
    }

    private void viewQuads() {
        viewPane.setViewportView(view);
        UserSubroutine main = translate();
        if (log.getErrorCount() > 0) {
            view.setText(errors.toString());
        } else {
            StringWriter writer = new StringWriter();
            main.dump(new PrintWriter(writer));
            view.setText(writer.toString());
        }
    }

    private void viewOptimizedQuads() {
        viewPane.setViewportView(view);
        UserSubroutine main = optimize();
        if (log.getErrorCount() > 0) {
            view.setText(errors.toString());
        } else {
            StringWriter writer = new StringWriter();
            main.dump(new PrintWriter(writer, true));
            view.setText(writer.toString());
        }
    }

    private void viewJavaScript() {
        viewPane.setViewportView(view);
        Program program = analyze();
        if (log.getErrorCount() > 0) return;
        StringWriter writer = new StringWriter();
        new CarlosToJavaScriptTranslator().translateProgram(program, new PrintWriter(writer));
        view.setText(writer.toString());
    }

    private Program parse() {
        log.clearErrors();
        errors.getBuffer().setLength(0);
        Reader reader = new StringReader(source.getText());
        return new Parser(reader).parse(reader, log);
    }

    private Program analyze() {
        Program program = parse();
        if (log.getErrorCount() > 0) return null;
        program.analyze(AnalysisContext.makeGlobalContext(log));
        return program;
    }

    private UserSubroutine translate() {
        Program program = analyze();
        if (log.getErrorCount() > 0) return null;
        return new CarlosToSquidTranslator(4, 8).translateProgram(program);
    }

    private UserSubroutine optimize() {
        UserSubroutine main = translate();
        if (log.getErrorCount() > 0) return null;
        new Optimizer().optimizeSubroutine(main);
        return main;
    }

    /**
     * Runs the application.
     */
    public static void main(String args[]) {
        new Viewer().setVisible(true);
    }
}
