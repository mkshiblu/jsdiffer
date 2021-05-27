package io.jsrminer.parser.js.babel;


import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeElementType;
import io.rminerx.core.api.IContainer;

public class LoopVisitor {

    private Visitor visitor;

    LoopVisitor(Visitor visitor) {
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
}
//
//    /**
//     * The for...in statement iterates over all enumerable properties of an object that are keyed by strings
//     * (ignoring ones keyed by Symbols), including inherited enumerable properties.
//     * const object = { a: 1, b: 2, c: 3 };
//     * for (const property in object)
//     * <p>
//     * Has initializer, collection and Body
//     */
//    public static final INodeVisitor<BlockStatement, ForInStatementTree, BlockStatement> forInStatementProcessor
//            = new NodeVisitor<>() {
//        @Override
//        public BlockStatement visit(ForInStatementTree tree, BlockStatement parent, IContainer container) {
//            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);
//
//            // Parse initializer
//            Expression initializer = createBaseExpressionWithRMType(tree.initializer, CodeElementType.ENHANCED_FOR_STATEMENT_INITIALIZER);
//            addExpression(initializer, composite);
//            io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(tree.initializer, initializer, container);
//            initializer.getVariableDeclarations().forEach(vd -> composite.addEnhancedForVariableDeclaration(vd));
//
//            // Parse collection
//            Expression collectionExpression = createBaseExpressionWithRMType(tree.collection, CodeElementType.ENHANCED_FOR_STATEMENT_EXPRESSION);
//            addExpression(collectionExpression, composite);
//            io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(tree.collection, collectionExpression, container);
//
//            // Parse Body
//            io.jsrminer.parser.js.closurecompiler.Visitor.visitStatement(tree.body, composite, container);
//            return composite;
//        }
//    };
//
//    /**
//     * The for...of statement creates a loop iterating over iterable objects, including: built-in String
//     * , Array, array-like objects (e.g., arguments or NodeList), TypedArray, Map, Set, and user-defined iterables.
//     * It invokes a custom iteration hook with statements to be executed for the value
//     * of each distinct property of the object
//     * <p>
//     * const array1 = ['a', 'b', 'c'];
//     * for (const element of array1) {
//     * console.log(element);
//     * }
//     * <p>
//     * Has initializer, collection and Body
//     */
//    public static final INodeVisitor<BlockStatement, ForOfStatementTree, BlockStatement> forOfStatementProcessor
//            = new NodeVisitor<>() {
//        @Override
//        public BlockStatement visit(ForOfStatementTree tree, BlockStatement parent, IContainer container) {
//            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);
//
//            // Parse initializer
//            Expression initializer = createBaseExpressionWithRMType(tree.initializer, CodeElementType.ENHANCED_FOR_STATEMENT_INITIALIZER);
//            addExpression(initializer, composite);
//            io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(tree.initializer, initializer, container);
//            initializer.getVariableDeclarations().forEach(vd -> composite.addEnhancedForVariableDeclaration(vd));
//
//            // Parse collection
//            Expression collectionExpression = createBaseExpressionWithRMType(tree.collection, CodeElementType.ENHANCED_FOR_STATEMENT);
//            addExpression(collectionExpression, composite);
//            io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(tree.collection, collectionExpression, container);
//
//            // Parse Body
//            io.jsrminer.parser.js.closurecompiler.Visitor.visitStatement(tree.body, composite, container);
//            return composite;
//        }
//    };
//
//    /**
//     * Has condition and Body
//     */
//    public static final INodeVisitor<BlockStatement, WhileStatementTree, BlockStatement> whileStatementProcessor
//            = new NodeVisitor<>() {
//        @Override
//        public BlockStatement visit(WhileStatementTree tree, BlockStatement parent, IContainer container) {
//            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);
//
//            // Parse condition
//            Expression conditionExpression = createBaseExpressionWithRMType(tree.condition, CodeElementType.WHILE_STATEMENT_CONDITION);
//            addExpression(conditionExpression, composite);
//            io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(tree.condition, conditionExpression, container);
//
//            // Parse Body
//            io.jsrminer.parser.js.closurecompiler.Visitor.visitStatement(tree.body, composite, container);
//            return composite;
//        }
//    };
//
//    /**
//     * Has Body and condition
//     */
//    public static final INodeVisitor<BlockStatement, DoWhileStatementTree, BlockStatement> doWhileStatementProcessor
//            = new NodeVisitor<>() {
//        @Override
//        public BlockStatement visit(DoWhileStatementTree tree, BlockStatement parent, IContainer container) {
//            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);
//
//            // Parse Body
//            io.jsrminer.parser.js.closurecompiler.Visitor.visitStatement(tree.body, composite, container);
//
//            // Parse condition
//            Expression conditionExpression = createBaseExpressionWithRMType(tree.condition, CodeElementType.DO_STATEMENT_CONDITION);
//            addExpression(conditionExpression, composite);
//            Visitor.visitExpression(tree.condition, conditionExpression, container);
//
//            return composite;
//        }
//    };

