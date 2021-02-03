package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.BlockTree;
import com.google.javascript.jscomp.parsing.parser.trees.ExpressionStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.VariableDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.VariableStatementTree;
import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.IContainer;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

public class StatementsVisitor {

    /**
     * A Block Statement
     */
    public static final INodeProcessor<BlockStatement, BlockTree, BlockStatement> blockStatementProcessor
            = new NodeProcessor<>() {
        @Override
        public BlockStatement process(BlockTree tree, BlockStatement parent, IContainer container) {
            var blockStatement = createBlockStatementPopulateAndAddToParent(tree, parent);

            // Parse statements
            tree.statements.forEach(statementTree -> {
                Visitor.visitStatement(statementTree, blockStatement, container);
            });

            return blockStatement;
        }
    };

    /**
     * An expression statement such as x = "4";
     */
    public static final NodeProcessor<SingleStatement, ExpressionStatementTree, BlockStatement> expressionStatementProcessor
            = new NodeProcessor<>() {
        @Override
        public SingleStatement process(ExpressionStatementTree tree, BlockStatement parent, IContainer container) {
            var leaf = createSingleStatementPopulateAndAddToParent(tree, parent);
            Visitor.visitExpression(tree.expression, leaf, container);
            return leaf;
        }
    };

    /**
     * A Variable declaration Statement e.g. let x = "4";
     */
    public static final NodeProcessor<SingleStatement, VariableStatementTree, BlockStatement> variableStatementProcessor
            = new NodeProcessor<>() {
        @Override
        public SingleStatement process(VariableStatementTree tree, BlockStatement parent, IContainer container) {
            var leaf = createSingleStatementPopulateAndAddToParent(tree, parent);

            VariableDeclarationKind kind = VariableDeclarationKind.fromName(tree.declarations.declarationType.toString());
            for (var declarationTree : tree.declarations.declarations) {
                VariableDeclaration vd = processVariableDeclaration(declarationTree, kind, container);
                leaf.getVariableDeclarations().add(vd);
                leaf.getVariables().add(vd.variableName);

                if (vd.getInitializer() != null) {
                    copyLeafData(leaf, vd.getInitializer());
                }
            }

            return leaf;
        }
    };

    /**
     * A variable declaration Node
     */
    protected static VariableDeclaration processVariableDeclaration(VariableDeclarationTree tree
            , VariableDeclarationKind kind
            , IContainer container) {
        String variableName = tree.lvalue.asIdentifierExpression().identifierToken.value;
        var variableDeclaration = new VariableDeclaration(variableName, kind);

        variableDeclaration.setSourceLocation(createSourceLocation(tree));

        // Set Scope (TODO set body source location
        variableDeclaration.setScope(container.getSourceLocation());

        // Process initializer
        if (tree.initializer != null) {
            Expression expression = createBaseExpressionWithoutSettingOwner(tree.initializer);
            Visitor.visitExpression(tree.initializer, expression, container);
            variableDeclaration.setInitializer(expression);
        }
        return variableDeclaration;
    }
}
