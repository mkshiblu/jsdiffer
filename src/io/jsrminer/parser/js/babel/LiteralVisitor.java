package io.jsrminer.parser.js.babel;

import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

public class LiteralVisitor {

    private Visitor visitor;

    BabelNodeVisitor<ILeafFragment, String> numericLiteralVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitNumericLiteral(node, parent, container);
    };

    LiteralVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * interface NumericLiteral <: Literal {
     * type: "NumericLiteral";
     * value: number;
     * }
     *
     * @param {*} path
     */
    String visitNumericLiteral(BabelNode node, ILeafFragment leaf, IContainer container) {
        final String value = node.getText();// node.get("value").asString();

        leaf.getNumberLiterals().add(value);
        return value;
    }
}
