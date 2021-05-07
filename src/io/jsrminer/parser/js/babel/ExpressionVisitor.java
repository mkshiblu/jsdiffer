package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.Expression;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.sourcetree.VariableDeclarationKind;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

public class ExpressionVisitor {

    private final Visitor visitor;

    ExpressionVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * interface VariableDeclaration <: Declaration {
     * type: "VariableDeclaration";
     * declarations: [ VariableDeclarator ];
     * kind: "var" | "let" | "const";
     * }
     */
    final BabelNodeVisitor<Expression, ILeafFragment> variableDeclarationProcessor =
            (BabelNode node, ILeafFragment parent, IContainer container) -> {
//                String kindStr = node.get("kind").asString();
//                var kind = VariableDeclarationKind.fromName(kindStr);
//                var declarations = node.get("declarations");
//
//                for (int i = 0; i < declarations.size(); i++) {
//                    processVariableDeclarator(declarations.get(i), kind, parent);
//                }
                return null;
            };
}
