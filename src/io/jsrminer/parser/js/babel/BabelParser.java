package io.jsrminer.parser.js.babel;

import io.jsrminer.parser.js.JavaScriptParser;
import io.jsrminer.uml.UMLModel;
import io.rminerx.core.api.ISourceFile;
import io.rminerx.core.entities.SourceFile;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jgit.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class BabelParser extends JavaScriptParser {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public UMLModel parse(Map<String, String> fileContents) {
        final UMLModel umlModel = new UMLModel();
        try {

            JBabel jBabel = new JBabel();
            for (String filepath : fileContents.keySet()) {
                final String content = fileContents.get(filepath);

                try {
                    log.info("Processing " + filepath + "...");
                    SourceFile sourceFile = parseAndLoadSourceFile(content, filepath, jBabel);
                    sourceFile.setFilepath(filepath);
                    umlModel.getSourceFileModels().put(filepath, sourceFile);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    //System.out.println("Ignoring file " + filepath + " due to exception" + ex.toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return umlModel;
    }

    @Override
    public ISourceFile parseSource(String content, @NonNull String filepath) {
        if (filepath == null)
            throw new NullPointerException("filepath cannot be null");
        try {
            JBabel jBabel = new JBabel();
            SourceFile source = parseAndLoadSourceFile(content, filepath, jBabel);
            source.setFilepath(filepath);
            return source;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the code using the jsEngine
     *
     * @return
     */
    private SourceFile parseAndLoadSourceFile(String fileContent, String filePath, JBabel jBabel) {
        StopWatch watch = new StopWatch();
        watch.start();

        // Get AST
        ParseResult result = parseAndMakeAst(filePath, fileContent, jBabel);
        // Traverse AST and load model
        if (result.getProgramAST() == null) {
            watch.stop();
            throw new RuntimeException("Error parsing " + filePath);
        } else {
            Visitor builder = new Visitor(filePath, fileContent);
            SourceFile file = builder.loadFromAst(result.getProgramAST());
            watch.stop();
            log.debug("Parse and Load time: " + watch.toString());
            return file;
        }
    }

    public ParseResult parseAndMakeAst(String fileName, String fileContent, JBabel babel) {
        final List<SyntaxMessage> warnings = new LinkedList<>();
        final List<SyntaxMessage> errors = new LinkedList<>();

        JV8 programAst = null;
        try {
            var ast = babel.parse(fileName, fileContent);
            programAst = ast.get("program");

        } catch (Exception e) {
            errors.add(new SyntaxMessage(String.format("%s: %s", e.getClass(), e.getMessage()), -1, -1));
        }

        return new ParseResult(programAst, errors, warnings);
    }

    /**
     * Syntax error message.
     */
    static class SyntaxMessage {
        private final String message;
        private final int line;
        private final int column;

        /**
         * Constructs a new syntax error message object.
         */
        SyntaxMessage(String message, int line, int column) {
            this.message = message;
            this.line = line;
            this.column = column;
        }

        /**
         * Returns the message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns the source location.
         */
        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return line + "," + column + ": " + getMessage();
        }
    }

    /**
     * Result from parser.
     */
    public static class ParseResult {

        private final List<SyntaxMessage> errors;

        private final List<SyntaxMessage> warnings;

        private JV8 programAST;

        private ParseResult(JV8 programAST, List<SyntaxMessage> errors, List<SyntaxMessage> warnings) {
            this.programAST = programAST;
            this.errors = errors;
            this.warnings = warnings;
        }

        /**
         * Returns the AST, or null if parse error.
         */
        public JV8 getProgramAST() {
            return programAST;
        }

        /**
         * Returns the list of parse errors.
         */
        List<SyntaxMessage> getErrors() {
            return errors;
        }

        /**
         * Returns the list of parse warnings.
         */
        List<SyntaxMessage> getWarnings() {
            return warnings;
        }
    }
}
