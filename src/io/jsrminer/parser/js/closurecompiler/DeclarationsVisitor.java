package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.*;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
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
        public FunctionDeclaration visit(FunctionDeclarationTree tree, CodeFragment fragment, IContainer container) {

            final boolean isAnonymous = fragment instanceof ILeafFragment;
            FunctionDeclaration function;

            if (isAnonymous) {
                AnonymousFunctionDeclaration anonymousFunctionDeclaration = new AnonymousFunctionDeclaration();
                function = anonymousFunctionDeclaration;
                ((ILeafFragment) fragment).getAnonymousFunctionDeclarations().add(anonymousFunctionDeclaration);
                anonymousFunctionDeclaration.setText(getTextInSource(tree, false));
            } else {
                function = new FunctionDeclaration();
                container.getFunctionDeclarations().add(function);
                //fragment.getFunctionDeclarations().add(function);
            }

            // Load function info
            AstInfoExtractor.loadFunctionInfo(tree, function, container);
            if (isAnonymous) {
                container.getAnonymousFunctionDeclarations().add((AnonymousFunctionDeclaration) function);
            }

            processFunctionParamaterAndBody(tree, fragment, container, isAnonymous, function);
            return function;
        }
    };

    static void processFunctionParamaterAndBody(FunctionDeclarationTree tree, CodeFragment fragment, IContainer container, boolean isAnonymous, FunctionDeclaration function) {
        // Load parameters
        tree.formalParameterList.parameters.forEach(parameterTree -> {
            UMLParameter parameter = createUmlParameter(parameterTree.asIdentifierExpression(), function);
            function.getParameters().add(parameter);
        });

        // Load functionBody by passing the function as the new container
        if (tree.functionBody != null) {
            switch (tree.functionBody.type) {
                case BLOCK:

                    BlockStatement bodyBlock = new BlockStatement();
                    bodyBlock.setText("{");
                    function.setBody(new FunctionBody(bodyBlock));
                    BlockTree blockTree = tree.functionBody.asBlock();
                    populateBlockStatementData(blockTree, bodyBlock);
                    blockTree.statements.forEach(statementTree -> {
                        Visitor.visitStatement(statementTree, bodyBlock, function);
                    });
                    break;
                case IDENTIFIER_EXPRESSION:
                case UNARY_EXPRESSION:
                    // TODO handle Arrow expression or Identifier
                    //var bodyTree = tree.functionBody.asUnaryExpression();
                    // bodyTree.
                    if (isAnonymous)
                        fragment.getAnonymousFunctionDeclarations().remove(function);
                    else
                        container.getFunctionDeclarations().remove(function);
                    break;
            }
        } else {
            throw new RuntimeException("Null function body not handled for "
                    + function.getQualifiedName() + " at " + tree.location.toString());
        }
    }

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
                addVariableDeclarationToParent(leaf, vd);
            }
            return null;
        }
    };

    public static void addVariableDeclarationToParent(ILeafFragment leaf, VariableDeclaration vd) {
        leaf.getVariableDeclarations().add(vd);
        leaf.getVariables().add(vd.variableName);

        if (vd.getInitializer() != null) {
            copyLeafData(vd.getInitializer(), leaf);
        }
    }

    /**
     * A variable declaration Node
     */
    protected static VariableDeclaration processVariableDeclaration(VariableDeclarationTree tree
            , VariableDeclarationKind kind
            , IContainer container
            , INode scopeNode) {
        VariableDeclaration variableDeclaration;
        switch (tree.lvalue.type) {
            case IDENTIFIER_EXPRESSION:
                variableDeclaration = createVariableDeclarationFromIdentifier(tree.lvalue.asIdentifierExpression()
                        , kind
                        , scopeNode == null ? container : scopeNode);
                break;
            case OBJECT_PATTERN:
                variableDeclaration = createVariableDeclarationFromObjectPattern(tree.lvalue.asObjectPattern()
                        , kind
                        , scopeNode == null ? container : scopeNode);
                break;
            default:
                throw new RuntimeException(tree.location + " Variable declaration type : " + tree.type + " Not handled");
        }


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

    static VariableDeclaration createVariableDeclarationFromVariableName(String variableName
            , VariableDeclarationKind kind
            , SourceRange fieldLocation
            , SourceLocation parentLocation) {
        var variableDeclaration = new VariableDeclaration(variableName, kind);

        var location = createSourceLocation(fieldLocation);
        variableDeclaration.setSourceLocation(location);

        // Set Scope (TODO set body source location
        variableDeclaration.setScope(createVariableScope(location, parentLocation));

        return variableDeclaration;
    }

    static VariableDeclaration createVariableDeclarationFromObjectPattern(ObjectPatternTree tree
            , VariableDeclarationKind kind
            , INode scopeNode) {
        String variableName = getTextInSource(tree.fields.get(0), false);
        var variableDeclaration = new VariableDeclaration(variableName, kind);

        variableDeclaration.setSourceLocation(createSourceLocation(tree));

        // Set Scope (TODO set body source location
        variableDeclaration.setScope(createVariableScope(tree, scopeNode));

        return variableDeclaration;
    }
}
