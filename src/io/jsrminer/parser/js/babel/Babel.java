package io.jsrminer.parser.js.babel;

import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import io.jsrminer.parser.JsonFileLoader;
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

public class Babel extends JavaScriptParser {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public Babel() {
        try (final JBabel jsEngine = new JBabel()) {
            jsEngine.createParseFunction();
        }
    }

    void initJsEngine(){
        try (final JBabel jsEngine = new JBabel()) {
            jsEngine.createParseFunction();
        }
    }

    @Override
    public UMLModel parse(Map<String, String> fileContents) {
        final UMLModel umlModel = new UMLModel();
        try {
            for (String filepath : fileContents.keySet()) {
                final String content = fileContents.get(filepath);

                try {
                    log.info("Processing " + filepath + "...");
                    SourceFile sourceFile = parseAndLoadSourceFile(content, filepath);
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

            SourceFile source = parseAndLoadSourceFile(content, filepath);
            source.setFilepath(filepath);
            return source;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    public ISourceFile parseSource(String content, @NonNull String filepath) {
//        if (filepath == null)
//            throw new NullPointerException("filepath cannot be null");
//
//        try (final JavaScriptEngine jsEngine = new JavaScriptEngine()) {
//            jsEngine.createParseFunction();
//
//            SourceFile source = parse(content, jsEngine, filepath);
//            source.setFilepath(filepath);
//            return source;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * Parses the code using the jsEngine
     *
     * @return
     */
    private SourceFile parse(String fileContent, JBabel jsEngine, String filePath) {
        final String blockJson = processScript(fileContent, jsEngine);
        StopWatch watch = new StopWatch();
        watch.start();
        SourceFile file = new JsonFileLoader(filePath).parseSourceFile(blockJson);
        watch.stop();
        log.debug("Model loading time from json: " + watch.toString());
        return file;
    }

    /**
     * Parses the code using the jsEngine
     *
     * @return
     */
    private SourceFile parseAndLoadSourceFile(String fileContent, String filePath) {
        SourceFile file = new SourceFile(filePath);

        StopWatch watch = new StopWatch();
        watch.start();

        // Get AST
        ParseResult result = parseAndMakeAst(filePath, fileContent);
        // Traverse AST and load model
        if (result.getProgramAST() == null) {
            throw new RuntimeException("Error parsing " + filePath);
        } else {
            modelLoader.loadFromAst(result.getProgramAST(), file);
        }

        watch.stop();
        log.debug("Parse and Load time: " + watch.toString());
        return file;
    }

    public ParseResult parseAndMakeAst(String fileName, String fileContent) {
        final List<SyntaxMessage> warnings = new LinkedList<>();
        final List<SyntaxMessage> errors = new LinkedList<>();

        ProgramTree programAST = null;

        try {
//            programAST = parser.parseProgram();
        return (String) jsEngine.executeFunction("parse", script, true);
        } catch (Exception e) {
            errors.add(new SyntaxMessage(String.format("%s: %s", e.getClass(), e.getMessage()), -1, -1));
        }

        return new ParseResult(programAST, errors, warnings);
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

        private ProgramTree programAST;

        private ParseResult(ProgramTree programAST, List<SyntaxMessage> errors, List<SyntaxMessage> warnings) {
            this.programAST = programAST;
            this.errors = errors;
            this.warnings = warnings;
        }

        /**
         * Returns the AST, or null if parse error.
         */
        public ProgramTree getProgramAST() {
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
