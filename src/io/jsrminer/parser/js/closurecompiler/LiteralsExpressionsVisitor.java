package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ArrayLiteralExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.LiteralExpressionTree;
import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.ObjectCreation;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import java.util.ArrayList;
import java.util.List;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.getTextInSource;

public class LiteralsExpressionsVisitor {

    public static final NodeVisitor<String, LiteralExpressionTree, ILeafFragment> literalExpressionProcessor
            = new NodeVisitor<>() {
        @Override
        public String visit(LiteralExpressionTree tree, ILeafFragment fragment, IContainer container) {

            List<String> literals;
            String value = tree.literalToken.toString();
            switch (tree.literalToken.type) {
                case NUMBER:
                    literals = fragment.getNumberLiterals();
                    break;
                case STRING:
                case REGULAR_EXPRESSION:
                    literals = fragment.getStringLiterals();
                    value = value.substring(1, value.length() - 1);
                    break;
                case NULL:
                    literals = fragment.getNullLiterals();
                    break;
                case TRUE:
                case FALSE:
                    literals = fragment.getBooleanLiterals();
                    break;
                default:
                    throw new RuntimeException("Literal type: " + tree.literalToken.type + " not handled");
            }

            literals.add(value);
            return tree.literalToken.toString();
        }
    };

    /**
     * Has elements and optionally hasTrailing commas
     */
    public static final NodeVisitor<Void, ArrayLiteralExpressionTree, ILeafFragment> arrayLiteralExpression
            = new NodeVisitor<>() {
        @Override
        public Void visit(ArrayLiteralExpressionTree tree, ILeafFragment leaf, IContainer container) {

            if (tree.elements.size() > 0) {
                String text = getTextInSource(tree, false);
                leaf.getArrayAccesses().add(text);
                tree.elements.forEach(element -> {
                    Visitor.visitExpression(element, leaf, container);
                });
            } else {
                // Empty array creation
                ObjectCreation creation = new ObjectCreation();
                creation.setSourceLocation(AstInfoExtractor.createSourceLocation(tree));
                creation.setText(getTextInSource(tree, false));
                creation.setType(CodeElementType.ARRAY_EXPRESSION);
                creation.setFunctionName("");
                leaf.getCreationMap().computeIfAbsent(creation.getText(), key -> new ArrayList<>()).add(creation);
            }

            return null;
        }
    };
}
