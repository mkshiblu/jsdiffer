package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import io.jsrminer.BaseTest;
import io.jsrminer.io.FileUtil;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.sourcetree.VariableDeclarationKind;
import io.rminerx.core.entities.SourceFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatementsVisitor_VariableDeclarationTest extends BaseTest {

    static ProgramTree programTree;
    static SourceFile container;
    static BlockStatement dummyBodyBlock;
    static SingleStatement letAStatement;

    @BeforeAll
    public static void setup() {
        container = new SourceFile();
        container.setFilepath("unnamed.js");
        container.setQualifiedName("unnamed.js");
        var parser = new ClosureCompilerParser();
        var parseResult = parser.parseAndMakeAst("test", FileUtil.readFileContent(getRootResourceDirectory() + "closure_model.js"),
                false);
        programTree = parseResult.getProgramAST();
        dummyBodyBlock = new BlockStatement();
        // Set the source location of the block parent to the end of the file
        SourceRange lastElementLocation = programTree.sourceElements.get(programTree.sourceElements.size() - 1).location;
        dummyBodyBlock.setText("{");

        var letA = programTree.sourceElements.get(0);
        letAStatement = StatementsVisitor.variableStatementProcessor.process(letA.asVariableStatement(), dummyBodyBlock, container);
    }

    @Test
    public void letATextTest() {
        assertEquals("let a;", letAStatement.getText());
    }

    @Test
    public void letAVariablesCountTest() {
        assertEquals(1, letAStatement.getVariables().size());
    }

    @Test
    public void letAVariableNameTest() {
        assertEquals("a", letAStatement.getVariables().get(0));
    }

    @Test
    public void letAVariableDeclarationsCountTest() {
        assertEquals(1, letAStatement.getVariableDeclarations().size());
    }

    @Test
    public void letAVariableDeclarationsKindTest() {
        assertEquals(VariableDeclarationKind.LET, letAStatement.getVariableDeclarations().get(0).getKind());
    }

    @Test
    public void letAVariableDeclarationNameTest() {
        assertEquals("a", letAStatement.getVariableDeclarations().get(0).variableName);
    }
}
