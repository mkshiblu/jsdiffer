package io.jsrminer.parser.js.babel;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import io.jsrminer.sourcetree.*;
import io.jsrminer.util.Lazy;
import io.rminerx.core.api.ILeafFragment;

import java.util.function.Function;

public class BabelNodeUtil {

    private final String fileName;
    private final String fileContent;

    BabelNodeUtil(String fileName, String fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    SingleStatement createSingleStatementPopulateAndAddToParent(BabelNode node, BlockStatement parent) {
        var singleStatement = new SingleStatement();
        populateSingleStatementData(node, singleStatement);
        addStatement(singleStatement, parent);
        return singleStatement;
    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    void populateSingleStatementData(BabelNode node, SingleStatement fragment) {
        populateTextLocationAndType(node, fragment);
    }

    Expression createBaseExpressionWithRMType(BabelNode tree, CodeElementType type) {
        var expression = new Expression();
        ///populateTextLocationAndType(tree, expression);
        populateTextAndLocation(tree, expression);
        expression.setType(type);
        return expression;
    }


    ILeafFragment copyLeafData(ILeafFragment source, ILeafFragment target) {
        target.getVariables().addAll(source.getVariables());
        target.getNullLiterals().addAll(source.getNullLiterals());
        target.getNumberLiterals().addAll(source.getNumberLiterals());
        target.getStringLiterals().addAll(source.getStringLiterals());
        target.getBooleanLiterals().addAll(source.getBooleanLiterals());
        target.getInfixOperators().addAll(source.getInfixOperators());
        target.getPrefixExpressions().addAll(source.getPrefixExpressions());

        target.getPostfixExpressions().addAll(source.getPostfixExpressions());
        target.getTernaryOperatorExpressions().addAll(source.getTernaryOperatorExpressions());
        target.getPrefixExpressions().addAll(source.getPrefixExpressions());
        target.getVariableDeclarations().addAll(source.getVariableDeclarations());
        target.getArguments().addAll(source.getArguments());


        for (var entry : source.getMethodInvocationMap().entrySet()) {
            var invocations1 = target.getMethodInvocationMap().get(entry.getKey());
            if (invocations1 == null) {
                target.getMethodInvocationMap().put(entry.getKey(), entry.getValue());
            } else {
                invocations1.addAll(entry.getValue());
            }
        }

        //leaf1.getMethodInvocationMap().addAll(leaf2.getVariableDeclarations());
        //leaf1.getCreationMap().addAll(leaf2.getVariableDeclarations());

        for (var entry : source.getCreationMap().entrySet()) {
            var creations1 = target.getCreationMap().get(entry.getKey());
            if (creations1 == null) {
                target.getCreationMap().put(entry.getKey(), entry.getValue());
            } else {
                creations1.addAll(entry.getValue());
            }
        }

        // Copy anonymous classes
        target.getAnonymousFunctionDeclarations().addAll(source.getAnonymousFunctionDeclarations());

        return target;
    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    void populateExpressionData(BabelNode node, Expression fragment) {
        populateTextLocationAndType(node, fragment);
    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    <T extends CodeFragment> void populateTextLocationAndType(BabelNode node, T fragment) {
        fragment.setText(getTextInSource(node, fragment instanceof SingleStatement));
        populateLocationAndType(node, fragment);
    }

    <T extends CodeFragment> void populateLocationAndType(BabelNode node, T fragment) {
        fragment.setSourceLocation(node.getSourceLocation());
        fragment.setType(getCodeElementTypeFromBabelNodeType(node.get("type").asString()));
    }

    <T extends CodeFragment> void populateTextAndLocation(BabelNode node, T fragment) {
        fragment.setSourceLocation(node.getSourceLocation());
        fragment.setText(getTextInSource(node, fragment instanceof SingleStatement));
    }

    void addStatement(Statement statement, BlockStatement parent) {
        statement.setDepth(parent.getDepth() + 1);
        statement.setPositionIndexInParent(parent.getStatements().size());
        parent.addStatement(statement);
        statement.setParent(parent);
    }

    void addExpression(Expression expression, BlockStatement parent) {
        //an expression has the same index and depth as the composite statement it belong to
        expression.setDepth(parent.getDepth());
        expression.setPositionIndexInParent(parent.getPositionIndexInParent());
        parent.getExpressions().add(expression);
        expression.setOwnerBlock(parent);
    }

    CodeElementType getCodeElementTypeFromBabelNodeType(String babelNodeType) {
        return BabelParserConfig.babelNodeToCodeElementTypeMap.get(babelNodeType);
    }

    String getTextInSource(BabelNode node, boolean isStatement) {
        final String text = node.toString();//formatCodeFunction.apply(node);
        // Remove or add semicolon based on statement or expression
        // No semicolon for any expression
        var endsWithSemicolon = text.charAt(text.length() - 1) == ';';
        if (isStatement && !endsWithSemicolon) {
            return text + ';';
        }

        // An expression ends with semicolon, remove the semicolon
        else if (!isStatement && endsWithSemicolon) {
            return text.substring(0, text.length() - 1);
        }
        return text;
    }
}
