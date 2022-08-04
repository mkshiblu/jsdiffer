package io.jsrminer.refactorings;

import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenameFunctionInsideAnonymousClassTest {
    static String code1 = """
               module.exports =
               class Project extends Model {
                    m() {
                        let x = 1;
                    }

                   getAtomProjectFilePath () {
                        return this.projectFilePath;
                   }
               };
            """;

    static String code2 = """
               module.exports =
               class Project extends Model {
                    m() {
                        let x = 1;
                    }

                   getProjectFilePath () {
                        return this.projectFilePath;
                   }
               };
            """;
    static RenameOperationRefactoring renameOperationRefactoring;
    static List<IRefactoring> refactorings;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenCodeSnippets("snippet.js"
                , code1, "snippet.js", code2);
        renameOperationRefactoring = (RenameOperationRefactoring) refactorings.stream()
                .filter(r -> r.getRefactoringType().equals(RefactoringType.RENAME_METHOD))
                .findFirst().orElse(null);
    }

    @Test
    void testRefactoringCount() {
        assertEquals(1, refactorings.size());
    }

    @Test
    void testOriginalOperationName() {
        assertEquals("resumeBootstrapInternal", renameOperationRefactoring.getOriginalOperation().getName());
    }

    @Test
    void testOriginalOperationQualifiedName() {
        assertEquals("bootstrap.resumeBootstrapInternal", renameOperationRefactoring.getOriginalOperation().getQualifiedName());
    }

    @Test
    void testRenameOperationName() {
        assertEquals("doBootstrap", renameOperationRefactoring.getRenamedOperation().getName());
    }

    @Test
    void testRenameOperationQualifiedName() {
        assertEquals("bootstrap.doBootstrap", renameOperationRefactoring.getRenamedOperation().getQualifiedName());
    }

    @Test
    void testMapperMappingsCount() {
        assertEquals(7, renameOperationRefactoring.getBodyMapper().getMappings().size());
    }

    @Test
    void testOriginalOperationLocation() {
        assertEquals(110, renameOperationRefactoring.getOriginalOperation().getSourceLocation().start);
        assertEquals(1364, renameOperationRefactoring.getOriginalOperation().getSourceLocation().end);
    }

    void testRenamedOperationLocation() {
        assertEquals(110, renameOperationRefactoring.getRenamedOperation().getSourceLocation().start);
        assertEquals(1562, renameOperationRefactoring.getRenamedOperation().getSourceLocation().end);
    }
}
