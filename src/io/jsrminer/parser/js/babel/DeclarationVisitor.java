package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.api.INode;
import org.apache.commons.lang3.NotImplementedException;

public class DeclarationVisitor {
    private final Visitor visitor;

    BabelNodeVisitor<ICodeFragment, Object> variableDeclarationVisitor = (BabelNode node, ICodeFragment fragment, IContainer container) -> {
        visitVariableDeclaration(node, fragment, container);
        return null;
    };

    BabelNodeVisitor<BlockStatement, Object> functionDeclarationVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitFunctionDeclaration(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, Object> functionExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitFunctionExpression(node, parent, container);
    };

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
        variableDeclaration.setScope(visitor.getNodeUtil().createVariableScope(variableDeclaration.getSourceLocation(), scopeNode));
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

    /**
     * interface FunctionDeclaration <: Function, Declaration {
     * type: "FunctionDeclaration";
     * id: Identifier;
     * }
     * A function declaration. Note that unlike in the parent interface Function,
     * the id cannot be null, except when this is the child of an ExportDefaultDeclaration.
     *
     * @param node
     * @param parent
     * @param container
     */
    public FunctionDeclaration visitFunctionDeclaration(BabelNode node, BlockStatement parent, IContainer container) {
        FunctionDeclaration function = new FunctionDeclaration();
        container.registerFunctionDeclaration(function);
        visitor.getNodeUtil().loadFunctionDeclarationInfo(node, function, container);
        boolean successFullyParsed = processFunctionParamaterAndBody(node, container, function);
        if (!successFullyParsed) {
            container.getFunctionDeclarations().remove(function);
        }
        return function;
    }

    /**
     * // interface FunctionExpression <: Function, Expression {
     * //     type: "FunctionExpression";
     * //   }
     * //   A function expression.
     * <p>
     * // function [name]([param1[, param2[, ..., paramN]]]) {
     * //     statements
     * //  }
     */
    public AnonymousFunctionDeclaration visitFunctionExpression(BabelNode node, ILeafFragment leafFragment, IContainer container) {
        var anonymousFunctionDeclaration = new AnonymousFunctionDeclaration();
        leafFragment.registerAnonymousFunctionDeclaration(anonymousFunctionDeclaration);
        anonymousFunctionDeclaration.setText(visitor.getNodeUtil().getTextInSource(node, false));
        visitor.getNodeUtil().loadAnonymousFunctionDeclarationInfo(node, anonymousFunctionDeclaration, container);
        container.getAnonymousFunctionDeclarations().add(anonymousFunctionDeclaration);

        boolean isSuccessFullyParsed = processFunctionParamaterAndBody(node, container, anonymousFunctionDeclaration);
        if (!isSuccessFullyParsed) {
            leafFragment.getAnonymousFunctionDeclarations().remove(anonymousFunctionDeclaration);
            container.getAnonymousFunctionDeclarations().remove(anonymousFunctionDeclaration);
        }

        // TODO add unmatched things to leaf?

        return anonymousFunctionDeclaration;
    }

    boolean processFunctionParamaterAndBody(BabelNode node, IContainer container, FunctionDeclaration function) {
        extractFunctionParamters(node, function);

        var functionBodyNode = node.get("body");
        switch (functionBodyNode.getType()) {
            case BLOCK_STATEMENT:
                BlockStatement bodyBlock = new BlockStatement();
                bodyBlock.setText("{");
                function.setBody(new FunctionBody(bodyBlock));
                visitor.getNodeUtil().populateBlockStatementData(functionBodyNode, bodyBlock);

                // Traverse the body statements
                var blockBodyNodes = functionBodyNode.get("body");
                for (int i = 0; i < blockBodyNodes.size(); i++) {
                    visitor.visitStatement(blockBodyNodes.get(i), bodyBlock, function);
                }
                break;

            default:
                throw new NotImplementedException("Body Type: " + functionBodyNode.getSourceLocation());
        }

        return true;
    }

    private void extractFunctionParamters(BabelNode node, FunctionDeclaration function) {
        // Load parameters
        var paramterNodes = node.get("params");
        BabelNode parameterNode;
        for (int i = 0; i < paramterNodes.size(); i++) {
            parameterNode = paramterNodes.get(i);

            switch (parameterNode.getType()) {
                case IDENTIFIER:
                    var umlParamter = visitor.getNodeUtil().createUmlParameter(parameterNode.getString("name"), function, parameterNode.getSourceLocation());
                    function.registerParameter(umlParamter);
                    break;

                default:
                    throw new NotImplementedException("paramter type not handled: " + function.getSourceLocation());
            }
        }
    }
}