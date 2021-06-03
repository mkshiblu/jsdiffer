package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

public class ExceptionVisitor {

    private Visitor visitor;

    BabelNodeVisitor<BlockStatement, BlockStatement> tryStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitTryStatement(node, parent, container);
    };

    BabelNodeVisitor<BlockStatement, BlockStatement> catchClausetVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitCatchClause(node, parent, container);
    };

    BabelNodeVisitor<BlockStatement, SingleStatement> throwStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitThrowStatement(node, parent, container);
    };

    ExceptionVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * interface TryStatement <: Statement {
     * type: "TryStatement";
     * block: BlockStatement;
     * handler: CatchClause | null;
     * finalizer: BlockStatement | null;
     * }
     * A try statement. If handler is null then finalizer must be a BlockStatement.
     */
    public BlockStatement visitTryStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var tryStatement = new TryStatement();
        visitor.getNodeUtil().populateBlockStatementData(node, tryStatement);
        visitor.getNodeUtil().addStatement(tryStatement, parent);

        // Parse try body
        visitor.visitStatement(node.get("block"), tryStatement, container);

        // Parse catch
        var catchBlockNode = node.get("handler");
        if (catchBlockNode != null && catchBlockNode.isDefined()) {
            BlockStatement catchStatement = (BlockStatement) visitor.visitStatement(catchBlockNode, parent, container);
            tryStatement.getCatchClauses().add(catchStatement);
        }

        // Parse finally
        var finallyBlockNode = node.get("finalizer");
        if (finallyBlockNode.isDefined()) {
            BlockStatement finallyStatement = (BlockStatement) visitor.visitStatement(finallyBlockNode, parent, container);
            tryStatement.setFinallyClause(finallyStatement);
        }

        return tryStatement;
    }

    /**
     * interface CatchClause <: Node {
     * type: "CatchClause";
     * param: Pattern | null;
     * body: BlockStatement;
     * }
     * A catch clause following a try block.
     */
    public BlockStatement visitCatchClause(BabelNode node, BlockStatement parent, IContainer container) {
        var composite = visitor.getNodeUtil().createBlockStatementPopulateAndAddToParent(node, parent);

        // Treat exception as variable declaration
        var paramNode = node.get("param");
        var variableDeclaration
                = this.visitor.getNodeUtil().createVariableDeclarationFromIdentifier(paramNode
                , VariableDeclarationKind.VAR
                , composite);

        composite.getOwnVariableDeclarations().add(variableDeclaration);
        Expression exceptionExpression = this.visitor.getNodeUtil().createBaseExpressionWithRMType(paramNode, CodeElementType.CATCH_CLAUSE_EXCEPTION_NAME);
        visitor.getNodeUtil().addExpressionToBlockStatement(exceptionExpression, composite);

        // Parse body
        visitor.visitStatement(node.get("body"), composite, container);

        return composite;
    }

    /**
     * interface ThrowStatement <: Statement {
     * type: "ThrowStatement";
     * argument: Expression;
     * }
     */
    SingleStatement visitThrowStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var leaf = visitor.getNodeUtil().createSingleStatementPopulateAndAddToParent(node, parent);
        visitor.visitExpression(node.get("argument"), leaf, container);
        return leaf;
    }
//    public BlockStatement visitFinallyStatement(BabelNode node, BlockStatement parent, IContainer container) {
//        var composite = createBlockStatementPopulateAndAddToParent(node, parent);
//        visitor.visitStatement(node.block, composite, container);
//        return composite;
//    }
}
