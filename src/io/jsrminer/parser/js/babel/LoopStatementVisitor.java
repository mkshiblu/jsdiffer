package io.jsrminer.parser.js.babel;


import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeElementType;
import io.rminerx.core.api.IContainer;

public class LoopStatementVisitor {

    private Visitor visitor;
    BabelNodeVisitor<BlockStatement, BlockStatement> forStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitForStatement(node, parent, container);
    };

    BabelNodeVisitor<BlockStatement, BlockStatement> whileStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitWhileStatement(node, parent, container);
    };
    BabelNodeVisitor<BlockStatement, BlockStatement> doWhileStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitDoWhileStatement(node, parent, container);
    };
    BabelNodeVisitor<BlockStatement, BlockStatement> forInStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitForInStatement(node, parent, container);
    };
    BabelNodeVisitor<BlockStatement, BlockStatement> forOfStatementVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitForOfStatement(node, parent, container);
    };

    LoopStatementVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * interface ForStatement<: Statement {
     * type: "ForStatement";
     * init: VariableDeclaration | Expression | null;
     * test: Expression | null;
     * update: Expression | null;
     * body: Statement;
     * }
     */
    public BlockStatement visitForStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var composite = visitor.getNodeUtil().createBlockStatementPopulateAndAddToParent(node, parent);

        // Parse initializer
        var initNode = node.get("init");
        if (initNode != null && initNode.isDefined()) {
            var initializer = visitor.getNodeUtil().createBaseExpressionWithRMType(initNode, CodeElementType.FOR_STATEMENT_INITIALIZER);
            visitor.getNodeUtil().addExpressionToBlockStatement(initializer, composite);
            visitor.visitExpression(initNode, initializer, container);
        }
        // Parse condition
        var testNode = node.get("test");
        if (testNode != null && testNode.isDefined()) {
            var conditionExpression = visitor.getNodeUtil().createBaseExpressionWithRMType(testNode, CodeElementType.FOR_STATEMENT_CONDITION);
            this.visitor.getNodeUtil().addExpressionToBlockStatement(conditionExpression, composite);
            visitor.visitExpression(testNode, conditionExpression, container);
        }
        // Parse increment
        var updateNode = node.get("update");
        if (updateNode != null && updateNode.isDefined()) {
            var updateExpression = visitor.getNodeUtil().createBaseExpressionWithRMType(updateNode, CodeElementType.FOR_STATEMENT_UPDATER);
            this.visitor.getNodeUtil().addExpressionToBlockStatement(updateExpression, composite);
            visitor.visitExpression(updateNode, updateExpression, container);
        }

        // Parse Body
        visitor.visitStatement(node.get("body"), composite, container);
        return composite;
    }

    /**
     * ForInStatement
     * interface ForInStatement <: Statement {
     * type: "ForInStatement";
     * left: VariableDeclaration |  Expression;
     * right: Expression;
     * body: Statement;
     * }
     * A for/in statement.
     */
    public BlockStatement visitForInStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var composite = visitor.getNodeUtil().createBlockStatementPopulateAndAddToParent(node, parent);

        // Parse initializer
        var leftNode = node.get("left");
        var initializer = visitor.getNodeUtil().createBaseExpressionWithRMType(leftNode, CodeElementType.ENHANCED_FOR_STATEMENT_INITIALIZER);
        visitor.getNodeUtil().addExpressionToBlockStatement(initializer, composite);
        visitor.visitExpression(leftNode, initializer, container);
        initializer.getVariableDeclarations().forEach(vd -> composite.addEnhancedForVariableDeclaration(vd));

        // Parse collection
        final var rightNode = node.get("right");
        var collectionExpression = visitor.getNodeUtil().createBaseExpressionWithRMType(rightNode, CodeElementType.ENHANCED_FOR_STATEMENT_EXPRESSION);
        visitor.getNodeUtil().addExpressionToBlockStatement(collectionExpression, composite);
        visitor.visitExpression(rightNode, collectionExpression, container);

        // Parse Body
        visitor.visitStatement(node.get("body"), composite, container);
        return composite;
    }

    /**
     * interface ForOfStatement <: ForInStatement {
     * type: "ForOfStatement";
     * await: boolean;
     * }
     * ForInStatement
     * interface ForInStatement <: Statement {
     * type: "ForInStatement";
     * left: VariableDeclaration |  Expression;
     * right: Expression;
     * body: Statement;
     * }
     * A for/in statement.
     */
    public BlockStatement visitForOfStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var composite = visitor.getNodeUtil().createBlockStatementPopulateAndAddToParent(node, parent);

        // Parse initializer
        var leftNode = node.get("left");
        var initializer = visitor.getNodeUtil().createBaseExpressionWithRMType(leftNode, CodeElementType.ENHANCED_FOR_STATEMENT_INITIALIZER);
        visitor.getNodeUtil().addExpressionToBlockStatement(initializer, composite);
        visitor.visitExpression(leftNode, initializer, container);
        initializer.getVariableDeclarations().forEach(vd -> composite.addEnhancedForVariableDeclaration(vd));

        // Parse collection
        final var rightNode = node.get("right");
        var collectionExpression = visitor.getNodeUtil().createBaseExpressionWithRMType(rightNode, CodeElementType.ENHANCED_FOR_STATEMENT_EXPRESSION);
        visitor.getNodeUtil().addExpressionToBlockStatement(collectionExpression, composite);
        visitor.visitExpression(rightNode, collectionExpression, container);

        // Parse Body
        visitor.visitStatement(node.get("body"), composite, container);
        return composite;
    }

    /**
     * interface WhileStatement <: Statement {
     * type: "WhileStatement";
     * test: Expression;
     * body: Statement;
     * }
     * A while statement.
     */
    public BlockStatement visitWhileStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var composite = visitor.getNodeUtil().createBlockStatementPopulateAndAddToParent(node, parent);

        // Parse condition
        var testNode = node.get("test");
        var conditionExpression = visitor.getNodeUtil().createBaseExpressionWithRMType(testNode, CodeElementType.WHILE_STATEMENT_CONDITION);
        this.visitor.getNodeUtil().addExpressionToBlockStatement(conditionExpression, composite);
        visitor.visitExpression(testNode, conditionExpression, container);

        // Parse Body
        visitor.visitStatement(node.get("body"), composite, container);
        return composite;
    }

    /**
     * DoWhileStatement
     * interface DoWhileStatement <: Statement {
     * type: "DoWhileStatement";
     * body: Statement;
     * test: Expression;
     * }
     * A do/while statement.
     */
    public BlockStatement visitDoWhileStatement(BabelNode node, BlockStatement parent, IContainer container) {
        var composite = visitor.getNodeUtil().createBlockStatementPopulateAndAddToParent(node, parent);

        // Parse Body
        visitor.visitStatement(node.get("body"), composite, container);

        // Parse condition
        var testNode = node.get("test");
        var conditionExpression = visitor.getNodeUtil().createBaseExpressionWithRMType(testNode, CodeElementType.DO_STATEMENT_CONDITION);
        this.visitor.getNodeUtil().addExpressionToBlockStatement(conditionExpression, composite);
        visitor.visitExpression(testNode, conditionExpression, container);

        return composite;
    }
}