package io.jsrminer.parser.js.babel;

import io.jsrminer.parser.ErrorReporter;
import io.jsrminer.parser.ParseResult;
import io.jsrminer.parser.SyntaxMessage;
import io.jsrminer.parser.js.JavaScriptParser;
import io.jsrminer.sourcetree.SourceLocation;
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
            var sourceFileMap = internalParse(fileContents);
            sourceFileMap.entrySet().forEach((entry) -> {
                umlModel.getSourceFileModels().put(entry.getKey(), entry.getValue());
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return umlModel;
    }

    private Map<String, SourceFile> internalParse(Map<String, String> fileContents) {
        JBabel babel = new JBabel();
        Map<String, SourceFile> map = new HashMap<>();
        for (Map.Entry<String, String> entry : fileContents.entrySet()) {
            try {
                log.debug("Parsing and loading " + entry.getKey() + "...");
                SourceFile file = parseAndLoadSourceFile(entry.getValue(), entry.getKey(), babel);
                map.put(file.getFilepath(), file);
            } catch (Exception ex) {
                log.error("Ignoring file: " + entry.getKey());
                ex.printStackTrace();
            }
        }
        return map;
    }

    private Map<String, SourceFile> parseParallel(Map<String, String> fileContents) {
        final var tasks = fileContents.entrySet();
        Map<String, SourceFile> map = new HashMap<>();
        try {
            List<CompletableFuture<SourceFile>> futures = tasks.stream()
                    .map(entry -> CompletableFuture.supplyAsync(() -> {
                                return parseAndLoadSourceFile(entry.getValue(), entry.getKey(), new JBabel());
                            }).exceptionally(ex -> {
                                System.out.println(ex);
                                return null;
                            })
                    ).collect(Collectors.toList());

            var resultList = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            map = resultList.stream()
                    .collect(Collectors.toMap(SourceFile::getFilepath, Function.identity()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
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
        ParseResult<BabelNode> result = parseAndMakeAst(filepath, fileContent, jBabel);
        if (result.getProgramAST() == null) {
            throw new RuntimeException("Error parsing " + filepath + System.lineSeparator() + result.getErrors());
        } else {

            final List<SyntaxMessage> warnings = new LinkedList<>();
            final List<SyntaxMessage> errors = new LinkedList<>();

            ErrorReporter errorReporter = new ErrorReporter() {
                @Override
                public void reportError(SourceLocation sourcePosition, String message) {
                    errors.add(new SyntaxMessage(message, sourcePosition.startLine, sourcePosition.startColumn));
                }

                @Override
                public void reportWarning(SourceLocation sourcePosition, String message) {
                    warnings.add(new SyntaxMessage(message, sourcePosition.startLine, sourcePosition.startColumn));
                }
            };

            var builder = new Visitor(filepath, fileContent, errorReporter);
            SourceFile file = builder.loadFromAst(result.getProgramAST());
            result.getErrors().forEach(msg -> log.error(filepath + "(" +
                    msg.getLine() + "-"
                    + msg.getColumn() + "): " + msg.getMessage()));

            result.getWarnings().forEach(msg -> log.warn(filepath + "(" +
                    msg.getLine() + "-"
                    + msg.getColumn() + "): " + msg.getMessage()));
            file.setFilepath(filepath);
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
}
