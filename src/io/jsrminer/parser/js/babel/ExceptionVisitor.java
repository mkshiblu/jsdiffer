package io.jsrminer.parser.js.babel;


import com.google.javascript.jscomp.parsing.parser.trees.CatchTree;
import io.jsrminer.parser.js.closurecompiler.DeclarationsVisitor;
import io.jsrminer.parser.js.closurecompiler.INodeVisitor;
import io.jsrminer.parser.js.closurecompiler.NodeVisitor;
import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.IContainer;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

public class ExceptionVisitor {

    private Visitor visitor;

    BabelNodeVisitor<BlockStatement, BlockStatement> tryStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitTryStatement(node, parent, container);
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
        visitor.visitStatement(node.get("body"), tryStatement, container);

        // Parse condition
        var catchBlockNode = node.get("handler");
        if (catchBlockNode != null && catchBlockNode.isDefined()) {
            BlockStatement catchStatement = (BlockStatement) visitor.visitStatement(catchBlockNode, parent, container);
            tryStatement.getCatchClauses().add(catchStatement);
        }

        // Parse condition
        var finallyBlockNode = node.get("finalizer");
        if (finallyBlockNode != null && finallyBlockNode.isDefined()) {
            BlockStatement finallyStatement = (BlockStatement) visitor.visitStatement(finallyBlockNode, parent, container);
            tryStatement.setFinallyClause(finallyStatement);
        }

        return tryStatement;
    }

    /**
     * interface CatchClause <: Node {
     *   type: "CatchClause";
     *   param: Pattern | null;
     *   body: BlockStatement;
     * }
     * A catch clause following a try block.
     */
    public BlockStatement visitCatchClause(BabelNode node, BlockStatement parent, IContainer container) {
        var composite = visitor.getNodeUtil().createBlockStatementPopulateAndAddToParent(node, parent);

        // Treat exception as variable declaration
        var paramNode = node.get("param");
        var variableDeclaration
                = DeclarationsVisitor.createVariableDeclarationFromIdentifier()
                , VariableDeclarationKind.VAR
                , composite);

        composite.getOwnVariableDeclarations().add(variableDeclaration);
        Expression exceptionExpression = this.visitor.getNodeUtil().createBaseExpressionWithRMType(paramNode, CodeElementType.CATCH_CLAUSE_EXCEPTION_NAME);
        visitor.getNodeUtil().addExpressionToBlockStatement(exceptionExpression, composite);

        // Parse body
        visitor.visitStatement(node.get("body"), composite, container);

        return composite;
    }
}
