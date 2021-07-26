package io.jsrminer.refactorings;

import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenameAttributeRefactoringTest {
    static String code1 = """
                class ImportParserPlugin {
                    constructor(options) {
                		this.options = options;
                	}
                	apply(parser) {
                	}
                }
                class CommentCompilationWarning extends WebpackError {
                constructor(message, module, loc) {
                    super(message);

                    this.name = "CommentCompilationWarning";

                    this.module = module;
                    this.loc = loc;

                    Error.captureStackTrace(this, this.constructor);
                }
            }
            module.exports = ImportParserPlugin;
            """;

    static String code2 = """
                const WebpackError = require("./WebpackError");

                class CommentCompilationWarning extends WebpackError {
                static y = 1;

                	constructor(message, module, loc) {
                		super(message);

                		this.name = "CommentCompilationWarning";

                		this.module = module;
                		this.loc = loc;

                		Error.captureStackTrace(this, this.constructor);
                	}
                }

                module.exports = CommentCompilationWarning;
            """;

    static List<IRefactoring> refactorings;
    static MoveClassRefactoring moveClassRefactoring;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenCodeSnippets("snippet1.js"
                , code1, "snippet1.js", code2);
        moveClassRefactoring = (MoveClassRefactoring) refactorings.stream()
                .filter(r -> r.getRefactoringType().equals(RefactoringType.MOVE_CLASS))
                .findFirst().orElse(null);
    }

    @Test
    public void testRefactoringType() {
        assertEquals(RefactoringType.MOVE_CLASS, moveClassRefactoring.getRefactoringType());
    }

}
