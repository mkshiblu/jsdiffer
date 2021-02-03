package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.IfStatementTree;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.Expression;
import io.rminerx.core.api.IContainer;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

public class ChoiceStatementsVisitor {
    /**
     * An If Condition Statement
     */
    public static final INodeProcessor<BlockStatement, IfStatementTree, BlockStatement> ifStatementProcessor
            = new NodeProcessor<>() {
        @Override
        public BlockStatement process(IfStatementTree tree, BlockStatement parent, IContainer container) {
            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);

//            IfStatement ifStatement = (IfStatement)statement;
//            CompositeStatementObject child = new CompositeStatementObject(cu, filePath, ifStatement, parent.getDepth()+1, CodeElementType.IF_STATEMENT);
//            parent.addStatement(child);

            // Process expressions
            Expression expression = createBaseExpressionWithoutSettingOwner(tree.condition);
            Visitor.visitExpression(tree.condition, expression, container);

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
