package io.jsrminer.parser.js.closurecompiler.statementsvisitor;

import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import io.jsrminer.BaseTest;
import io.jsrminer.io.FileUtil;
import io.jsrminer.parser.js.closurecompiler.ClosureCompilerParser;
import io.jsrminer.parser.js.closurecompiler.StatementsVisitor;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.sourcetree.SourceLocation;
import io.jsrminer.sourcetree.VariableDeclarationKind;
import io.rminerx.core.entities.SourceFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VariableDeclarationTest extends BaseTest {

    static ProgramTree programTree;
    static SourceFile container;
    static BlockStatement dummyBodyBlock;
    static SingleStatement letAStatement;
    static SingleStatement vdWithLiteralInitializer;
    static SingleStatement vdWithAnonymousFunctionInitializer;
    static SingleStatement multipleVdsStatements;

    @BeforeAll
    public static void setup() {
        container = new SourceFile();
        container.setFilepath("unnamed.js");
        container.setQualifiedName("unnamed.js");
        var parser = new ClosureCompilerParser();
        var parseResult = parser.parseAndMakeAst("test", FileUtil.readFileContent(getRootResourceDirectory() + "parser/variable_declarations.js"),
                false);
        programTree = parseResult.getProgramAST();
        dummyBodyBlock = new BlockStatement();
        // Set the source location of the block parent to the end of the file
        SourceRange lastElementLocation = programTree.sourceElements.get(programTree.sourceElements.size() - 1).location;
        dummyBodyBlock.setSourceLocation(new SourceLocation(lastElementLocation.start.source.name, 0, 0, lastElementLocation.end.line, lastElementLocation.end.column, 0, lastElementLocation.end.offset));
        dummyBodyBlock.setText("{");

        var letA = programTree.sourceElements.get(0);
        letAStatement = StatementsVisitor.variableStatementProcessor.process(letA.asVariableStatement(), dummyBodyBlock, container);
        vdWithLiteralInitializer = StatementsVisitor.variableStatementProcessor.process(programTree.sourceElements.get(1).asVariableStatement(), dummyBodyBlock, container);
     //   vdWithAnonymousFunctionInitializer = StatementsVisitor.variableStatementProcessor.process(programTree.sourceElements.get(2).asVariableStatement(), dummyBodyBlock, container);
       // multipleVdsStatements = StatementsVisitor.variableStatementProcessor.process(programTree.sourceElements.get(3).asVariableStatement(), dummyBodyBlock, container);
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

    // Test const const c = 34;

    @Test
    public void vdWithLiteralInitializerTextTest() {
        assertEquals("const c = 34;", vdWithLiteralInitializer.getText());
    }

    @Test
    public void vdWithLiteralInitializerLiteralCountTest() {
        assertEquals(1, vdWithLiteralInitializer.getNumberLiterals().size());
    }

    public void vdWithLiteralInitializerDeclarationsCountTest() {
        assertEquals(1, vdWithLiteralInitializer.getVariableDeclarations().size());
    }

    @Test
    public void vdWithLiteralInitializerInitializerTextTest() {
        assertNotNull(vdWithLiteralInitializer.getVariableDeclarations().get(0).getInitializer());
        assertEquals("34", vdWithLiteralInitializer.getVariableDeclarations().get(0).getInitializer().getText());
    }
}
