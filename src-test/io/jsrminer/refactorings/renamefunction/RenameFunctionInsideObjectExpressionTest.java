package io.jsrminer.refactorings.renamefunction;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.RenameOperationRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenameFunctionInsideObjectExpressionTest extends BaseTest {
    static RenameOperationRefactoring renameOperationRefactoring;
    static List<IRefactoring> refactorings;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenDirectories(
                getRootResourceDirectory() + "renamefunction\\v1",
                getRootResourceDirectory() + "renamefunction\\v2");
        renameOperationRefactoring = (RenameOperationRefactoring) refactorings.get(0);
    }

    @Test
    void testRefactoringCount() {
        assertEquals(1, refactorings.size());
    }

    @Test
    void testOriginalOperationName(){
        assertEquals("svgEllipsisToThreeEllipsis", renameOperationRefactoring.getOriginalOperation().getName());
    }
    @Test
    void  testOriginalOperationQualifiedName(){
        assertEquals("1.parse.svgEllipsisToThreeEllipsis", renameOperationRefactoring.getOriginalOperation().getQualifiedName());
    }

    @Test
    void testMapperMappingsCount() {
        assertEquals(26, renameOperationRefactoring.getBodyMapper().getMappings().size());
    }

    @Test
    void  testRenameOperationName(){
        assertEquals("parseArcCommand", renameOperationRefactoring.getRenamedOperation().getName());
    }

    @Test
    void  testRenameOperationQualifiedName(){
        assertEquals("1.parse.parseArcCommand", renameOperationRefactoring.getRenamedOperation().getQualifiedName());
    }
}
