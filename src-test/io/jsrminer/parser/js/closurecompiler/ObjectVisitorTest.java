package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ReturnStatementTree;
import io.jsrminer.BaseTest;
import io.jsrminer.sourcetree.AnonymousFunctionDeclaration;
import io.jsrminer.sourcetree.SingleStatement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectVisitorTest extends BaseTest {
    static AnonymousFunctionDeclaration objectAsFunction;
    static SingleStatement vdStatement;

    @BeforeAll
    public static void setup() {
        AstInfo ast = new AstInfo(StatementsDataProvider.OBJECT_EXPRESSIONS);
        var tree = ast.programTree.sourceElements.get(0);
        var returnObjStatement = ControlFlowStatementsVisitor.returnStatementProcessor.visit(tree.asReturnStatement(), ast.bodyBlock, ast.container);
        objectAsFunction = ObjectsVisitor.objectLiteralExpression.visit(((ReturnStatementTree) tree).expression.asObjectLiteralExpression()
                , returnObjStatement, ast.container);
        vdStatement = (SingleStatement) objectAsFunction.getBody().blockStatement.getStatements().get(0);
    }

    @Test
    public void statementsCount() {
        assertEquals(1, objectAsFunction.getBody().blockStatement.getStatements().size());
    }

    @Test
    public void functionDeclarationsCount() {
        assertEquals(1, objectAsFunction.getFunctionDeclarations().size());
    }

    @Test
    public void variableDeclarationsCount() {
        assertEquals(1, vdStatement.getVariableDeclarations().size());
    }

    @Test
    public void variableDeclarationInitializerTest() {
        assertEquals("Smith", vdStatement.getVariableDeclarations().get(0).getInitializer().stringLiterals.get(0));
    }

    @Test
    public void fieldNameTest() {
        assertEquals("name", vdStatement.getVariableDeclarations().get(0).getVariableName());
    }
}
