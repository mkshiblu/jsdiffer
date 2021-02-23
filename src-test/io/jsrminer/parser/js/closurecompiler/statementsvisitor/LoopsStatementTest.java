package io.jsrminer.parser.js.closurecompiler.statementsvisitor;

import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import io.jsrminer.parser.js.closurecompiler.LoopStatementsVisitor;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.SourceLocation;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.rminerx.core.entities.Container;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoopsStatementTest extends StatementsTest {

    static BlockStatement forStatement;
    static BlockStatement forOfStatement;
    static BlockStatement whileStatement;
    static BlockStatement doWhileStatement;

    @BeforeAll
    public static void setup() {
        ProgramTree programTree = StatementsDataProvider.LOOPS.getProgramTree();
        BlockStatement parent = StatementsDataProvider.LOOPS.getDummyBodyBlock();
        Container container = StatementsDataProvider.LOOPS.getContainer();
        forStatement = LoopStatementsVisitor.forStatementProcessor.visit(programTree.sourceElements.get(1).asForStatement(), parent, container);
        forOfStatement = LoopStatementsVisitor.forOfStatementProcessor.visit(programTree.sourceElements.get(2).asForOfStatement(), parent, container);
        whileStatement = LoopStatementsVisitor.whileStatementProcessor.visit(programTree.sourceElements.get(3).asWhileStatement(), parent, container);
        doWhileStatement = LoopStatementsVisitor.doWhileStatementProcessor.visit(programTree.sourceElements.get(4).asDoWhileStatement(), parent, container);
    }

    @Test
    void forStatementInitializerTest() {
        assertEquals(2, forStatement.getVariableDeclarations().size());
    }

    @Test
    void forStatementInitializerVariableScopeTest() {
        VariableDeclaration vd1 = forStatement.getAllVariableDeclarations().get(0);
        VariableDeclaration vd2 = forStatement.getAllVariableDeclarations().get(1);

        SourceLocation scope1 = vd1.getScope();
        assertEquals(2, scope1.startLine);
        assertEquals(9, scope1.startColumn);
        assertEquals(4, scope1.endLine);
        assertEquals(1, scope1.endColumn);
    }

    @Test
    void forStatementConditionTest() {

    }

    @Test
    void forStatementUpdaterTest() {

    }

    @Test
    void forStatementBodyTest() {

    }
}