package io.jsrminer.parser.js.babel;

import com.google.javascript.jscomp.parsing.parser.trees.BlockTree;
import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.api.INode;

import java.util.ArrayList;
import java.util.List;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;


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

    public void visitFunctionDeclaration(BabelNode node, BlockStatement parent, IContainer container) {
        // TODO can parent be a leaf?
        FunctionDeclaration function = new FunctionDeclaration();
        visitor.getNodeUtil().loadFunctionInfo(node, function, container);
        processFunctionParamaterAndBody(node, parent, container, false, function);

    }

    void processFunctionParamaterAndBody(BabelNode node, CodeFragment fragment, IContainer container, boolean isAnonymous, FunctionDeclaration function) {
        // Load parameters
        var paramterNodes = node.get("params");
//        tree.formalParameterList.parameters.forEach(parameterTree -> {
//            switch (parameterTree.type) {
//                case IDENTIFIER_EXPRESSION:
//                    UMLParameter parameter = createUmlParameter(parameterTree.asIdentifierExpression(), function);
//                    function.getParameters().add(parameter);
//                    break;
//                case OBJECT_PATTERN:
//                    var objectParameter = parameterTree.asObjectPattern();
//                    for (var fieldTree : objectParameter.fields) {
//                        var variableName = fieldTree.asPropertyNameAssignment().name.asIdentifier().value;
//                        var umlParameter = createUmlParameter(variableName, function, createSourceLocation(fieldTree));
//                        function.getParameters().add(umlParameter);
//                    }
//                    break;
//                default:
//                    break;
//            }
//        });

//        // Load functionBody by passing the function as the new container
//        if (tree.functionBody != null) {
//            switch (tree.functionBody.type) {
//                case BLOCK:
//
//                    BlockStatement bodyBlock = new BlockStatement();
//                    bodyBlock.setText("{");
//                    function.setBody(new FunctionBody(bodyBlock));
//                    BlockTree blockTree = tree.functionBody.asBlock();
//                    populateBlockStatementData(blockTree, bodyBlock);
//                    blockTree.statements.forEach(statementTree -> {
//                        io.jsrminer.parser.js.closurecompiler.Visitor.visitStatement(statementTree, bodyBlock, function);
//                    });
//                    break;
//                case IDENTIFIER_EXPRESSION:
//                case UNARY_EXPRESSION:
//                default: // TODO handle Arrow expression or Identifier
//                    //var bodyTree = tree.functionBody.asUnaryExpression();
//                    // bodyTree.
//                    if (isAnonymous)
//                        fragment.getAnonymousFunctionDeclarations().remove(function);
//                    else
//                        container.getFunctionDeclarations().remove(function);
//                    break;
//            }
//        } else {
//            throw new RuntimeException("Null function body not handled for "
//                    + function.getQualifiedName() + " at " + tree.location.toString());
//        }
    }
}