package io.jsrminer.parser.js.babel;

import com.google.javascript.jscomp.jarjar.org.apache.tools.ant.taskdefs.compilers.Jvc;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import io.jsrminer.sourcetree.*;

public class BabelNodeUtil {

    static SingleStatement createSingleStatementPopulateAndAddToParent(BabelNode node, BlockStatement parent) {
        var singleStatement = new SingleStatement();
        populateSingleStatementData(node, singleStatement);
        addStatement(singleStatement, parent);
        return singleStatement;
    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    static void populateSingleStatementData(BabelNode node, SingleStatement fragment) {
        populateTextLocationAndType(node, fragment);
    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    static void populateExpressionData(BabelNode node, Expression fragment) {
        populateTextLocationAndType(node, fragment);
    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    static <T extends CodeFragment> void populateTextLocationAndType(BabelNode node, T fragment) {
        fragment.setText(getTextInSource(node, fragment instanceof SingleStatement));
        populateLocationAndType(node, fragment);
    }

    static <T extends CodeFragment> void populateLocationAndType(BabelNode node, T fragment) {
        fragment.setSourceLocation(node.getSourceLocation());
        fragment.setType(getCodeElementTypeFromBabelNodeType(node.get("type").asString()));
    }

    static <T extends CodeFragment> void populateTextAndLocation(BabelNode node, T fragment) {
        fragment.setSourceLocation(node.getSourceLocation());
        fragment.setText(getTextInSource(node, fragment instanceof SingleStatement));
    }

    static void addStatement(Statement statement, BlockStatement parent) {
        statement.setDepth(parent.getDepth() + 1);
        statement.setPositionIndexInParent(parent.getStatements().size());
        parent.addStatement(statement);
        statement.setParent(parent);
    }

    static void addExpression(Expression expression, BlockStatement parent) {
        //an expression has the same index and depth as the composite statement it belong to
        expression.setDepth(parent.getDepth());
        expression.setPositionIndexInParent(parent.getPositionIndexInParent());
        parent.getExpressions().add(expression);
        expression.setOwnerBlock(parent);
    }

    static CodeElementType getCodeElementTypeFromBabelNodeType(String babelNodeType) {
        return BabelParserConfig.babelNodeToCodeElementTypeMap.get(babelNodeType);
    }

    static String getTextInSource(BabelNode node, boolean isStatement) {
        return null;
    }
}
