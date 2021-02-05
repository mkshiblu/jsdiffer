package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.*;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.api.INode;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

class DeclarationsVisitor {

    public static final NodeVisitor<FunctionDeclaration, FunctionDeclarationTree, CodeFragment> functionDeclarationProcessor
            = new NodeVisitor<>() {
        @Override
        public FunctionDeclaration visit(FunctionDeclarationTree tree, CodeFragment parent, IContainer container) {

            final boolean isAnonymous = parent instanceof ILeafFragment;
            FunctionDeclaration function;

            if (isAnonymous) {
                AnonymousFunctionDeclaration anonymousFunctionDeclaration = new AnonymousFunctionDeclaration();
                function = anonymousFunctionDeclaration;
                parent.getAnonymousFunctionDeclarations().add(anonymousFunctionDeclaration);
            } else {
                function = new FunctionDeclaration();
                container.getFunctionDeclarations().add(function);
            }

            // Load function info
            AstInfoExtractor.loadFunctionInfo(tree, function, container);

            // Load parameters
            tree.formalParameterList.parameters.forEach(parameterTree -> {
                UMLParameter parameter = createUmlParameter(parameterTree.asIdentifierExpression(), function);
                function.getParameters().add(parameter);
            });

            // Load functionBody by passing the function as the new container
            if (tree.functionBody != null) {
                BlockTree blockTree = tree.functionBody.asBlock();

                BlockStatement dummyParent = createDummyBodyBlock(blockTree);
                Visitor.visitStatement(blockTree, dummyParent, function);
                BlockStatement bodyBlock = (BlockStatement) dummyParent.getStatements().get(0);
                bodyBlock.setParent(null);
                function.setBody(new FunctionBody(bodyBlock));
            } else {
                throw new RuntimeException("Null function body not handled for "
                        + function.getQualifiedName() + " at " + tree.location.toString());
            }


            return function;
        }
    };

    /**
     * One or multiple variable declarations with a single kind such as let x, y = 5
     * Contains declarationType and VariableDeclarationTree declarations
     */
    public static final NodeVisitor<Void, VariableDeclarationListTree, ILeafFragment> variableDeclarationsList
            = new NodeVisitor<>() {
        @Override
        public Void visit(VariableDeclarationListTree tree, ILeafFragment leaf, IContainer container) {
            VariableDeclarationKind kind = VariableDeclarationKind.fromName(tree.declarationType.toString());
            for (var declarationTree : tree.declarations) {
                VariableDeclaration vd = processVariableDeclaration(declarationTree, kind, container, leaf.getParent());
                leaf.getVariableDeclarations().add(vd);
                leaf.getVariables().add(vd.variableName);

                if (vd.getInitializer() != null) {
                    copyLeafData(leaf, vd.getInitializer());
                }
            }
            return null;
        }
    };

    /**
     * A variable declaration Node
     */
    protected static VariableDeclaration processVariableDeclaration(VariableDeclarationTree tree
            , VariableDeclarationKind kind
            , IContainer container
            , INode scopeNode) {

        var variableDeclaration = createVariableDeclarationFromIdentifier(tree.lvalue.asIdentifierExpression()
                , kind
                , scopeNode == null ? container : scopeNode);
        // Process initializer
        if (tree.initializer != null) {
            Expression expression = createBaseExpressionWithRMType(tree.initializer, CodeElementType.VARIABLE_DECLARATION_INITIALIZER);
            Visitor.visitExpression(tree.initializer, expression, container);
            variableDeclaration.setInitializer(expression);
        }

        return variableDeclaration;
    }

    static VariableDeclaration createVariableDeclarationFromIdentifier(IdentifierExpressionTree tree
            , VariableDeclarationKind kind
            , INode scopeNode) {
        String variableName = tree.identifierToken.value;
        var variableDeclaration = new VariableDeclaration(variableName, kind);

        variableDeclaration.setSourceLocation(createSourceLocation(tree));

        // Set Scope (TODO set body source location
        variableDeclaration.setScope(createVariableScope(tree, scopeNode));

        return variableDeclaration;
    }
}
