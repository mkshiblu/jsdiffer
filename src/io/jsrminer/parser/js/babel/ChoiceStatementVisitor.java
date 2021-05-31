package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.IContainer;


public class ChoiceStatementVisitor {
    private Visitor visitor;

    BabelNodeVisitor<BlockStatement, BlockStatement> ifStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitIfStatement(node, parent, container);
    };

    BabelNodeVisitor<BlockStatement, BlockStatement> switchStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitSwitchStatement(node, parent, container);
    };

    BabelNodeVisitor<BlockStatement, SingleStatement> switchCaseVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitSwitchCase(node, parent, container);
    };

    ChoiceStatementVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * interface IfStatement <: Statement {
     * type: "IfStatement";
     * test: Expression;
     * consequent: Statement;
     * alternate: Statement | null;
     * }
     */
    BlockStatement visitIfStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var composite = this.visitor.getNodeUtil().createBlockStatementPopulateAndAddToParent(node, parent);

        // Parse condition
        final var testNode = node.get("test");
        Expression conditionExpression = visitor.getNodeUtil().createBaseExpressionWithRMType(testNode, CodeElementType.IF_STATEMENT_CONDITION);
        visitor.getNodeUtil().addExpressionToBlockStatement(conditionExpression, composite);
        visitor.visitExpression(testNode, conditionExpression, container);

        // Parse body of if clause
        visitor.visitStatement(node.get("consequent"), composite, container);

        // Parse else condition
        var alternateNode = node.get("alternate");
        if (alternateNode != null && alternateNode.isDefined()) {
            visitor.visitStatement(alternateNode, composite, container);
        }
        return composite;
    }

    /**
     * interface SwitchStatement <: Statement {
     * type: "SwitchStatement";
     * discriminant: Expression;
     * cases: [ SwitchCase ];
     * }
     */
    BlockStatement visitSwitchStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var composite = visitor.getNodeUtil().createBlockStatementPopulateAndAddToParent(node, parent);

        // Parse condition
        var discriminantNode = node.get("discriminant");
        Expression conditionExpression = visitor.getNodeUtil().createBaseExpressionWithRMType(discriminantNode, CodeElementType.SWITCH_STATEMENT_CONDITION);
        visitor.getNodeUtil().addExpressionToBlockStatement(conditionExpression, composite);

        visitor.visitExpression(discriminantNode, conditionExpression, container);

        // Parse case statements
        var caseNodes = node.get("cases");
        for (int i = 0; i < caseNodes.size(); i++) {
            visitor.visitStatement(caseNodes.get(i), composite, container);
        }

        return composite;
    }

    /**
     * interface SwitchCase <: Node {
     * type: "SwitchCase";
     * test: Expression | null;
     * consequent: [ Statement ];
     * }
     * A case (if test is an Expression) or default
     * (if test === null) clause in the body of a switch statement.
     */
    SingleStatement visitSwitchCase(BabelNode node, BlockStatement parent, IContainer container) {
        var leaf = new SingleStatement();//visitor.getNodeUtil().createSingleStatementPopulateAndAddToParent(node, parent);
        visitor.getNodeUtil().addStatement(leaf, parent);

        // Load expression data
        var testNode = node.get("test");

        // Load source location
        // Load type
        leaf.setType(BabelParserConfig.babelNodeToCodeElementTypeMap.get(node.getType()));

        // Load text & type
        var isDefault = !testNode.isDefined();

        // default node
        if (isDefault) {
            leaf.setText("default:");
            leaf.setSourceLocation(new SourceLocation(node.getSourceLocation().getFilePath()
                    , node.getSourceLocation().startLine
                    , node.getSourceLocation().startColumn
                    , node.getSourceLocation().startLine
                    , node.getSourceLocation().startColumn + "default:".length()
                    , node.getSourceLocation().start
                    , node.getSourceLocation().start + "default:".length()));
        } else {
            leaf.setSourceLocation(new SourceLocation(node.getSourceLocation().getFilePath()
                    , node.getSourceLocation().startLine
                    , node.getSourceLocation().startColumn
                    , testNode.getSourceLocation().endLine
                    , testNode.getSourceLocation().endColumn
                    , node.getSourceLocation().start
                    , testNode.getSourceLocation().end));

            // A case node
            var text = testNode.getText();
            if (text.charAt(text.length() - 1) == ';') {
                text = text.substring(0, text.length() - 1);
            }

            leaf.setText("case " + text + ":");

            visitor.visitExpression(testNode, leaf, container);
        }

        // TODO Source location and text and default
        // load the statements
        var consequentNodes = node.get("consequent");
        for (int i = 0; i < consequentNodes.size(); i++) {
            visitor.visitStatement(consequentNodes.get(i), parent, container);
        }

        return leaf;
    }
}
