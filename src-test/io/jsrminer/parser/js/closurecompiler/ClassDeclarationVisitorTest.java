package io.jsrminer.parser.js.closurecompiler;

import io.jsrminer.BaseTest;
import io.jsrminer.sourcetree.AnonymousClassDeclaration;
import io.jsrminer.sourcetree.Statement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ClassDeclarationVisitorTest extends BaseTest {
    static AnonymousClassDeclaration anonymous;
    static List<Statement> statements;

    @BeforeAll
    public static void setup() {
        AstInfo ast = new AstInfo(StatementsDataProvider.CLASS_DECLARATIONS);
        var tree = ast.programTree.sourceElements.get(0);
        var x = DeclarationsVisitor.classDeclarationProcessor.visit(tree.asClassDeclaration(), ast.bodyBlock, ast.container);

    }

    @Test
    void testStatementCount(){

    }
}


