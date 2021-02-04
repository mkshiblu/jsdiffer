package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.IfStatementTree;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.Expression;
import io.rminerx.core.api.IContainer;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

public class ChoiceStatementsVisitor {
    /**
     * An If Condition Statement
     * Has condition, ifClause, elseClause
     */
    public static final INodeVisitor<BlockStatement, IfStatementTree, BlockStatement> ifStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public BlockStatement visit(IfStatementTree tree, BlockStatement parent, IContainer container) {
            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);

            // Parse condition
            Expression conditionExpression = createBaseExpressionWithRMType(tree.condition, CodeElementType.IF_STATEMENT_CONDITION);
            Visitor.visitExpression(tree.condition, conditionExpression, container);
            addExpression(conditionExpression, composite);

            // Parse body of if clause
            Visitor.visitStatement(tree.ifClause, composite, container);

            // Parse else condition
            if (tree.elseClause != null) {
                Visitor.visitStatement(tree.elseClause, composite, container);
            }
            return composite;
        }
    };
}
