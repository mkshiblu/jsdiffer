package io.jsrminer.parser.js.closurecompiler;

import io.jsrminer.BaseTest;
import io.jsrminer.sourcetree.AnonymousFunctionDeclaration;
import io.jsrminer.sourcetree.SingleStatement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectVisitorTest extends BaseTest {
    static SingleStatement returnObjStatement;
    static AnonymousFunctionDeclaration anonymousFunctionDeclaration;

    @BeforeAll
    public static void setup() {
        AstInfo ast = new AstInfo(StatementsDataProvider.OBJECT_EXPRESSIONS);
        var tree = ast.programTree.sourceElements.get(0);
        var returnObjStatement = ControlFlowStatementsVisitor.returnStatementProcessor.visit(tree.asReturnStatement(), ast.bodyBlock, ast.container);
        var res = ObjectsVisitor.objectLiteralExpression.visit(tree.asObjectLiteralExpression(), returnObjStatement, ast.container);
    }

    @Test
    public void StatementCounts() {
        assertEquals(0, anonymousFunctionDeclaration.getStatements().size());
    }
}
