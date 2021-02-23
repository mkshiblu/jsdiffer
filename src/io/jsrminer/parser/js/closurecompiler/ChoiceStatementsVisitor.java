package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.CaseClauseTree;
import com.google.javascript.jscomp.parsing.parser.trees.DefaultClauseTree;
import com.google.javascript.jscomp.parsing.parser.trees.IfStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.SwitchStatementTree;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.Expression;
import io.jsrminer.sourcetree.SingleStatement;
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
            addExpression(conditionExpression, composite);
            Visitor.visitExpression(tree.condition, conditionExpression, container);

            // Parse body of if clause
            Visitor.visitStatement(tree.ifClause, composite, container);

            // Parse else condition
            if (tree.elseClause != null) {
                Visitor.visitStatement(tree.elseClause, composite, container);
            }
            return composite;
        }
    };

    /**
     * Has expression and caseClauses
     */
    public static final INodeVisitor<BlockStatement, SwitchStatementTree, BlockStatement> switchStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public BlockStatement visit(SwitchStatementTree tree, BlockStatement parent, IContainer container) {
            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);

            // Parse condition
            Expression conditionExpression = createBaseExpressionWithRMType(tree.expression, CodeElementType.SWITCH_STATEMENT_CONDITION);
            addExpression(conditionExpression, composite);
            Visitor.visitExpression(tree.expression, conditionExpression, container);

            // Parse case statements
            tree.caseClauses.forEach(caseTree -> {
                Visitor.visitStatement(caseTree, composite, container);
            });

            return composite;
        }
    };

    /**
     * Has expression (test value) and statements (statements that are executed if the test succeeds)
     */
    public static final INodeVisitor<SingleStatement, CaseClauseTree, BlockStatement> caseStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public SingleStatement visit(CaseClauseTree tree, BlockStatement parent, IContainer container) {
            var leaf = createSingleStatementPopulateAndAddToParent(tree, parent);

            // Load expression data
            Visitor.visitExpression(tree.expression, leaf, container);

            // load the statements
            tree.statements.forEach(statementTree -> Visitor.visitStatement(statementTree, parent, container));
            return leaf;
        }
    };

    /**
     * A default clause for switch statement. Has statements
     */
    public static final INodeVisitor<SingleStatement, DefaultClauseTree, BlockStatement> defaultClauseStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public SingleStatement visit(DefaultClauseTree tree, BlockStatement parent, IContainer container) {
            var leaf = createSingleStatementPopulateAndAddToParent(tree, parent);
            // load the statements
            tree.statements.forEach(statementTree -> Visitor.visitStatement(statementTree, parent, container));
            return leaf;
        }
    };
}
