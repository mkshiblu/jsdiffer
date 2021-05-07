package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.api.INode;

public class DeclarationVisitor {
    private final Visitor visitor;

    DeclarationVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * A return statement. Has expression
     */
    public static final BabelNodeVisitor<SingleStatement, BlockStatement> returnStatementProcessor
            = new BabelNodeVisitor<>() {
        @Override
        public SingleStatement visit(BabelNode tree, BlockStatement parent, IContainer container) {
            return null;
        }
    };

    /**
     * interface VariableDeclaration <: Declaration {
     * type: "VariableDeclaration";
     * declarations: [ VariableDeclarator ];
     * kind: "var" | "let" | "const";
     * }
     */
    final BabelNodeVisitor<VariableDeclaration, ICodeFragment> variableDeclarationProcessor =
            (BabelNode node, ICodeFragment fragment, IContainer container) -> {

                String kindStr = node.get("kind").asString();
                var kind = VariableDeclarationKind.fromName(kindStr);
                var declarations = node.get("declarations");

                ILeafFragment leaf = fragment instanceof BlockStatement
                        ? new SingleStatement() // TODO Populate
                        : (ILeafFragment) fragment;

                for (int i = 0; i < declarations.size(); i++) {
                    processVariableDeclarator(declarations.get(i), kind, leaf, container);
                }
                return null;
            };

    /**
     * interface VariableDeclarator <: Node {
     * type: "VariableDeclarator";
     * id: Pattern;
     * init: Expression | null;
     * }
     *
     * @param {declaratorPath} path
     */
    VariableDeclaration processVariableDeclarator(BabelNode node, VariableDeclarationKind kind, ILeafFragment leaf, IContainer container) {
        String variableName = node.get("id").get("name").asString();
        VariableDeclaration variableDeclaration = createVariableDeclaration(node, variableName, kind, leaf.getParent());

        if (node.get("init") != null) {
            Expression expression = new Expression();
            visitor.visitExpression(node.get("init"), expression, container);
            variableDeclaration.setInitializer(expression);
        }

        return variableDeclaration;
    }

    VariableDeclaration createVariableDeclaration(BabelNode node, String variableName
            , VariableDeclarationKind kind
            , INode scopeNode) {
        var variableDeclaration = new VariableDeclaration(variableName, kind);
        variableDeclaration.setSourceLocation(node.getSourceLocation());

        // Set Scope (TODO set body source location
        variableDeclaration.setScope(createVariableScope(variableDeclaration.getSourceLocation(), scopeNode));

        return variableDeclaration;
    }

    SourceLocation createVariableScope(SourceLocation variableLocation, INode scopeNode) {
        final SourceLocation parentLocation = scopeNode.getSourceLocation();
        return new SourceLocation(parentLocation.getFilePath(),
                variableLocation.startLine,
                variableLocation.startColumn,
                parentLocation.endLine,
                parentLocation.endColumn,
                variableLocation.start,
                parentLocation.end
        );
    }
}