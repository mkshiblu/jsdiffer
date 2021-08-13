package io.jsrminer.parser.js.babel;

import io.jsrminer.parser.js.JavaScriptParser;
import io.jsrminer.uml.UMLModel;
import io.rminerx.core.api.ISourceFile;
import io.rminerx.core.entities.SourceFile;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jgit.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.print.Book;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BabelParser extends JavaScriptParser {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public UMLModel parse(Map<String, String> fileContents) {
        final UMLModel umlModel = new UMLModel();
        try {
            var sourceFileMap = parseAsync(fileContents);
            sourceFileMap.entrySet().forEach((entry) -> {
                umlModel.getSourceFileModels().put(entry.getKey(), entry.getValue());
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return umlModel;
    }

    private Map<String, SourceFile> parseAsync(Map<String, String> fileContents) {
        final var tasks = fileContents.keySet();

        List<CompletableFuture<SourceFile>> futures = tasks.stream()
                .map(filepath -> CompletableFuture.supplyAsync(() -> {
                    String content = fileContents.get(filepath);
                    return parseAndLoadSourceFile(content, filepath, new JBabel());
                }))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toMap(SourceFile::getName, Function.identity()));
    }

    @Override
    public ISourceFile parseSource(String content, @NonNull String filepath) {
        if (filepath == null) {
            throw new NullPointerException("filepath cannot be null");
        }
        JBabel jBabel = new JBabel();
        SourceFile source = parseAndLoadSourceFile(content, filepath, jBabel);
        source.setFilepath(filepath);
        return source;
    }

    /**
     * Parses the code using the jsEngine
     *
     * @return
     */
    private SourceFile parseAndLoadSourceFile(String fileContent, String filepath, JBabel jBabel) {
//        StopWatch watch = new StopWatch();
//        watch.start();
//        log.info("Processing " + filepath + "...");
        // Get AST
        ParseResult result = parseAndMakeAst(filepath, fileContent, jBabel);
        // Traverse AST and load model
        if (result.getProgramAST() == null) {
//            watch.stop();
            throw new RuntimeException("Error parsing " + filepath);
        } else {
            var builder = new Visitor(filepath, fileContent);
            SourceFile file = builder.loadFromAst(result.getProgramAST());
            file.setFilepath(filepath);
//            watch.stop();
//            log.debug("Parse and Load time: " + watch.toString());
            return file;
        }
    }

    private ParseResult parseAndMakeAst(String fileName, String fileContent, JBabel babel) {
        final List<SyntaxMessage> warnings = new LinkedList<>();
        final List<SyntaxMessage> errors = new LinkedList<>();

        BabelNode programAst = null;
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

        private BabelNode programAST;

        private ParseResult(BabelNode programAST, List<SyntaxMessage> errors, List<SyntaxMessage> warnings) {
            this.programAST = programAST;
            this.errors = errors;
            this.warnings = warnings;
        }

        /**
         * Returns the AST, or null if parse error.
         */
        public BabelNode getProgramAST() {
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
