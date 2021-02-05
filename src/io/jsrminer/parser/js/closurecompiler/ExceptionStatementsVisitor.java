package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.CatchTree;
import com.google.javascript.jscomp.parsing.parser.trees.FinallyTree;
import com.google.javascript.jscomp.parsing.parser.trees.TryStatementTree;
import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.IContainer;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

public class ExceptionStatementsVisitor {
    /**
     * Has body, catchBlock and and finallyBlock
     */
    public static final INodeVisitor<TryStatement, TryStatementTree, BlockStatement> tryStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public TryStatement visit(TryStatementTree tree, BlockStatement parent, IContainer container) {
            var tryStatement = new TryStatement();
            populateBlockStatementData(tree, tryStatement);
            addStatement(tryStatement, parent);

            // Parse try body
            Visitor.visitStatement(tree.body, tryStatement, container);

            // Parse condition
            if (tree.catchBlock != null) {
                //var catchBlockStatement = createBlockStatementPopulateAndAddToParent(tree.catchBlock, )
                BlockStatement catchStatement = (BlockStatement) Visitor.visitStatement(tree.catchBlock, parent, container);
                tryStatement.getCatchClauses().add(catchStatement);
            }

            // Parse condition
            if (tree.finallyBlock != null) {
                BlockStatement finallyStatement = (BlockStatement) Visitor.visitStatement(tree.finallyBlock, parent, container);
                tryStatement.setFinallyClause(finallyStatement);
            }

            return tryStatement;
        }
    };

    /**
     * Has  ParseTree exception and ParseTree catchBody;
     */
    public static final INodeVisitor<BlockStatement, CatchTree, BlockStatement> catchStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public BlockStatement visit(CatchTree tree, BlockStatement parent, IContainer container) {
            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);


            // Treat exception as variable declaration
            var variableDeclaration
                    = DeclarationsVisitor.createVariableDeclarationFromIdentifier(tree.exception.asIdentifierExpression()
                    , VariableDeclarationKind.VAR
                    , composite);

            // TODO process initializer
            composite.getOwnVariableDeclarations().add(variableDeclaration);
            Expression exceptionExpression = createBaseExpressionWithRMType(tree.exception, CodeElementType.CATCH_CLAUSE_EXCEPTION_NAME);
            addExpression(exceptionExpression, composite);

            // Parse body
            Visitor.visitStatement(tree.catchBody, composite, container);

            return composite;
        }
    };

    /**
     * Has block
     */
    public static final INodeVisitor<BlockStatement, FinallyTree, BlockStatement> finallyStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public BlockStatement visit(FinallyTree tree, BlockStatement parent, IContainer container) {
            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);
            Visitor.visitStatement(tree.block, composite, container);
            return composite;
        }
    };
}
