package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.api.INode;

import java.util.ArrayList;
import java.util.List;


public class DeclarationVisitor {
    private final Visitor visitor;

    DeclarationVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

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

        BabelNode initNode = node.get("init");

        if (initNode != null && initNode.isDefined()) {
            Expression expression = visitor.getNodeUtil().createBaseExpressionWithRMType(initNode, CodeElementType.VARIABLE_DECLARATION_INITIALIZER);
            visitor.visitExpression(initNode, expression, container);

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

    /**
     * interface VariableDeclaration <: Declaration {
     * type: "VariableDeclaration";
     * declarations: [ VariableDeclarator ];
     * kind: "var" | "let" | "const";
     * }
     */
    void visitVariableDeclaration(BabelNode node, ICodeFragment fragment, IContainer container) {
        String kindStr = node.get("kind").asString();
        var kind = VariableDeclarationKind.fromName(kindStr);
        var declarations = node.get("declarations");
        var isStatement = fragment instanceof BlockStatement;
        ILeafFragment leaf = isStatement
                ? visitor.getNodeUtil().createSingleStatementPopulateAndAddToParent(node, (BlockStatement) fragment)
                : (Expression) fragment;

        for (int i = 0; i < declarations.size(); i++) {
            VariableDeclaration variableDeclaration = processVariableDeclarator(declarations.get(i), kind, leaf, container);
            addVariableDeclarationToParent(leaf, variableDeclaration);
        }
    }

    void addVariableDeclarationToParent(ILeafFragment leaf, VariableDeclaration vd) {
        leaf.getVariableDeclarations().add(vd);
        leaf.getVariables().add(vd.variableName);

        if (vd.getInitializer() != null) {
            visitor.getNodeUtil().copyLeafData(vd.getInitializer(), leaf);
        }
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