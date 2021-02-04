package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.*;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.Expression;
import io.rminerx.core.api.IContainer;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

public class LoopStatementsVisitor {

    /**
     * A Standard For loop e.g. for (int i =0; i<5; i++)
     * Has initializer, condition, increment and Body
     */
    public static final INodeVisitor<BlockStatement, ForStatementTree, BlockStatement> forStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public BlockStatement visit(ForStatementTree tree, BlockStatement parent, IContainer container) {
            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);

            // Parse initializer
            if (tree.initializer.type != ParseTreeType.NULL) {
                Expression initializer = createBaseExpressionWithRMType(tree.initializer, CodeElementType.FOR_STATEMENT_INITIALIZER);
                addExpression(initializer, composite);
                Visitor.visitExpression(tree.initializer, initializer, container);
            }
            // Parse condition
            if (tree.condition.type != ParseTreeType.NULL) {
                Expression conditionExpression = createBaseExpressionWithRMType(tree.condition, CodeElementType.FOR_STATEMENT_CONDITION);
                addExpression(conditionExpression, composite);
                Visitor.visitExpression(tree.condition, conditionExpression, container);
            }
            // Parse increment
            if (tree.increment.type != ParseTreeType.NULL) {
                Expression updateExpression = createBaseExpressionWithRMType(tree.condition, CodeElementType.FOR_STATEMENT_UPDATER);
                addExpression(updateExpression, composite);
                Visitor.visitExpression(tree.increment, updateExpression, container);
            }

            // Parse Body
            Visitor.visitStatement(tree.body, composite, container);
            return composite;
        }
    };

    /**
     * The for...in statement iterates over all enumerable properties of an object that are keyed by strings
     * (ignoring ones keyed by Symbols), including inherited enumerable properties.
     * const object = { a: 1, b: 2, c: 3 };
     * for (const property in object)
     * <p>
     * Has initializer, collection and Body
     */
    public static final INodeVisitor<BlockStatement, ForInStatementTree, BlockStatement> forInStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public BlockStatement visit(ForInStatementTree tree, BlockStatement parent, IContainer container) {
            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);

            // Parse initializer
            Expression initializer = createBaseExpressionWithRMType(tree.initializer, CodeElementType.ENHANCED_FOR_STATEMENT_INITIALIZER);
            addExpression(initializer, composite);
            Visitor.visitExpression(tree.initializer, initializer, container);
            initializer.getVariableDeclarations().forEach(vd -> composite.addEnhancedForVariableDeclaration(vd));

            // Parse collection
            Expression collectionExpression = createBaseExpressionWithRMType(tree.collection, CodeElementType.ENHANCED_FOR_STATEMENT_EXPRESSION);
            addExpression(collectionExpression, composite);
            Visitor.visitExpression(tree.collection, collectionExpression, container);

            // Parse Body
            Visitor.visitStatement(tree.body, composite, container);
            return composite;
        }
    };

    /**
     * The for...of statement creates a loop iterating over iterable objects, including: built-in String
     * , Array, array-like objects (e.g., arguments or NodeList), TypedArray, Map, Set, and user-defined iterables.
     * It invokes a custom iteration hook with statements to be executed for the value
     * of each distinct property of the object
     * <p>
     * const array1 = ['a', 'b', 'c'];
     * for (const element of array1) {
     * console.log(element);
     * }
     * <p>
     * Has initializer, collection and Body
     */
    public static final INodeVisitor<BlockStatement, ForOfStatementTree, BlockStatement> forOfStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public BlockStatement visit(ForOfStatementTree tree, BlockStatement parent, IContainer container) {
            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);

            // Parse initializer
            Expression initializer = createBaseExpressionWithRMType(tree.initializer, CodeElementType.ENHANCED_FOR_STATEMENT_INITIALIZER);
            addExpression(initializer, composite);
            Visitor.visitExpression(tree.initializer, initializer, container);
            initializer.getVariableDeclarations().forEach(vd -> composite.addEnhancedForVariableDeclaration(vd));

            // Parse collection
            Expression collectionExpression = createBaseExpressionWithRMType(tree.collection, CodeElementType.ENHANCED_FOR_STATEMENT);
            addExpression(collectionExpression, composite);
            Visitor.visitExpression(tree.collection, collectionExpression, container);

            // Parse Body
            Visitor.visitStatement(tree.body, composite, container);
            return composite;
        }
    };

    /**
     * Has condition and Body
     */
    public static final INodeVisitor<BlockStatement, WhileStatementTree, BlockStatement> whileStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public BlockStatement visit(WhileStatementTree tree, BlockStatement parent, IContainer container) {
            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);

            // Parse condition
            Expression conditionExpression = createBaseExpressionWithRMType(tree.condition, CodeElementType.WHILE_STATEMENT_CONDITION);
            addExpression(conditionExpression, composite);
            Visitor.visitExpression(tree.condition, conditionExpression, container);

            // Parse Body
            Visitor.visitStatement(tree.body, composite, container);
            return composite;
        }
    };

    /**
     * Has Body and condition
     */
    public static final INodeVisitor<BlockStatement, DoWhileStatementTree, BlockStatement> doWhileStatementProcessor
            = new NodeVisitor<>() {
        @Override
        public BlockStatement visit(DoWhileStatementTree tree, BlockStatement parent, IContainer container) {
            var composite = createBlockStatementPopulateAndAddToParent(tree, parent);

            // Parse Body
            Visitor.visitStatement(tree.body, composite, container);

            // Parse condition
            Expression conditionExpression = createBaseExpressionWithRMType(tree.condition, CodeElementType.DO_STATEMENT_CONDITION);
            addExpression(conditionExpression, composite);
            Visitor.visitExpression(tree.condition, conditionExpression, container);

            return composite;
        }
    };
}
