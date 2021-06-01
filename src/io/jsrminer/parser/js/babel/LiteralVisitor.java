package io.jsrminer.parser.js.babel;

import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

public class LiteralVisitor {

    private Visitor visitor;

    BabelNodeVisitor<ILeafFragment, String> numericLiteralVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitNumericLiteral(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, String> stringLiteralVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitStringLiteral(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, String> booleanLiteralVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitBooleanLiteral(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, String> nullLiteralVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitNullLiteral(node, parent, container);
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

    /**
     * interface StringLiteral <: Literal {
     * type: "StringLiteral";
     * value: string;
     * }
     */
    String visitStringLiteral(BabelNode node, ILeafFragment leaf, IContainer container) {
        final String value = node.getString("value");
        leaf.getStringLiterals().add(value);
        return value;
    }

    /**
     interface NullLiteral <: Literal {
     type: "NullLiteral";
     }
     */
    String visitNullLiteral(BabelNode node, ILeafFragment leaf, IContainer container) {
        final String value = node.getText();
        leaf.getNullLiterals().add(value);
        return value;
    }

    /**
     * interface BooleanLiteral <: Literal {
     *   type: "BooleanLiteral";
     *   value: boolean;
     * }
     */
    String visitBooleanLiteral(BabelNode node, ILeafFragment leaf, IContainer container) {
        final String value = node.getText();
        leaf.getStringLiterals().add(value);
        return value;
    }
}
