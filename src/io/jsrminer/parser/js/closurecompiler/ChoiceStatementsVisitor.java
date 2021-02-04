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
    public static final INodeProcessor<BlockStatement, IfStatementTree, BlockStatement> ifStatementProcessor
            = new NodeProcessor<>() {
        @Override
        public BlockStatement process(IfStatementTree tree, BlockStatement parent, IContainer container) {
            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);

            // Parse condition
            Expression conditionExpression = createBaseExpressionWithCustomType(tree.condition, CodeElementType.IF_STATEMENT_CONDITION);
            Visitor.visitExpression(tree.condition, conditionExpression, container);
            addExpression(conditionExpression, composite);

            // Parse body of if clause
            Visitor.visitStatement(tree.ifClause, composite, container);

            // Parse else condition
            if (tree.elseClause != null) {
                Visitor.visitStatement(tree.elseClause, composite, container);
            }
//            AbstractExpression abstractExpression = new AbstractExpression(cu, filePath, ifStatement.getExpression(), CodeElementType.IF_STATEMENT_CONDITION);
//            child.addExpression(abstractExpression);
//            processStatement(cu, filePath, child, ifStatement.getThenStatement());
//            if(ifStatement.getElseStatement() != null) {
//                processStatement(cu, filePath, child, ifStatement.getElseStatement());
//            }
//
            return composite;
        }
    };
}
