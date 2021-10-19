package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.*;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.api.INode;

import java.util.ArrayList;
import java.util.List;

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
            switch (parameterTree.type) {
                case IDENTIFIER_EXPRESSION:
                    UMLParameter parameter = createUmlParameter(parameterTree.asIdentifierExpression(), function);
                    function.getParameters().add(parameter);
                    break;
                case OBJECT_PATTERN:
                    var objectParameter = parameterTree.asObjectPattern();
                    for (var fieldTree : objectParameter.fields) {
                        var variableName = fieldTree.asPropertyNameAssignment().name.asIdentifier().value;
                        var umlParameter = createUmlParameter(variableName, function, createSourceLocation(fieldTree));
                        function.getParameters().add(umlParameter);
                    }
                    break;
                default:
                    break;
            }
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
                default: // TODO handle Arrow expression or Identifier
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
                var vds = processVariableDeclaration(declarationTree, kind, container, leaf.getParent());
                vds.forEach(vd -> addVariableDeclarationToParent(leaf, vd));
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
    protected static List<VariableDeclaration> processVariableDeclaration(VariableDeclarationTree tree
            , VariableDeclarationKind kind
            , IContainer container
            , INode scopeNode) {
        List<VariableDeclaration> variableDeclarations = new ArrayList<>();
        VariableDeclaration variableDeclaration = null;

        switch (tree.lvalue.type) {
            case IDENTIFIER_EXPRESSION:
                variableDeclaration = createVariableDeclarationFromIdentifier(tree.lvalue.asIdentifierExpression()
                        , kind
                        , scopeNode == null ? container : scopeNode);
                variableDeclarations.add(variableDeclaration);
                break;
            case OBJECT_PATTERN:
                variableDeclaration = createVariableDeclarationFromObjectPattern(tree.lvalue.asObjectPattern()
                        , kind
                        , scopeNode == null ? container : scopeNode);
                variableDeclarations.add(variableDeclaration);
                break;
            case ARRAY_PATTERN:
                var arrayPatternVds = createVariableDeclarationsFromArrayPattern(tree.lvalue.asArrayPattern()
                        , kind
                        , scopeNode == null ? container : scopeNode);
                variableDeclarations.addAll(arrayPatternVds);
                break;
            default:
                throw new RuntimeException(tree.location + " Variable declaration type : " + tree.type + " Not handled");
        }

        // Process initializer
        if (tree.initializer != null) {
            Expression expression = createBaseExpressionWithRMType(tree.initializer, CodeElementType.VARIABLE_DECLARATION_INITIALIZER);
            Visitor.visitExpression(tree.initializer, expression, container);
            variableDeclarations.forEach(vd -> vd.setInitializer(expression));
        }

        return variableDeclarations;
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

    // An array pattern such as let [x, y] = [];
    static List<VariableDeclaration> createVariableDeclarationsFromArrayPattern(ArrayPatternTree tree
            , VariableDeclarationKind kind
            , INode scopeNode) {

        List<VariableDeclaration> variableDeclarations = new ArrayList<>();

        tree.elements.forEach(element -> {
            switch (element.type) {
                case IDENTIFIER_EXPRESSION:
                    String variableName = getTextInSource(element, false);
                    var variableDeclaration = new VariableDeclaration(variableName, kind);

                    variableDeclaration.setSourceLocation(createSourceLocation(tree));

                    // Set Scope (TODO set body source location
                    variableDeclaration.setScope(createVariableScope(tree, scopeNode));
                    variableDeclarations.add(variableDeclaration);
                    break;
                default:
                    break;
            }
        });

        return variableDeclarations;
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

    public static final NodeVisitor<ClassDeclaration, ClassDeclarationTree, CodeFragment> classDeclarationProcessor
            = new NodeVisitor<>() {
        @Override
        public ClassDeclaration visit(ClassDeclarationTree tree, CodeFragment fragment, IContainer container) {

            final boolean isAnonymous = fragment instanceof ILeafFragment;
            ClassDeclaration classDeclaration;

            if (isAnonymous) {
                var anonymous = new AnonymousClassDeclaration();
                classDeclaration = anonymous;
                ((ILeafFragment) fragment).getAnonymousClassDeclarations().add(anonymous);
                anonymous.setText(getTextInSource(tree, false));
            } else {
                classDeclaration = new ClassDeclaration();
                container.getClassDeclarations().add(classDeclaration);
            }

            // Load function info
            AstInfoExtractor.loadClassInfo(tree, classDeclaration, container);
            if (isAnonymous) {
                container.getAnonymousClassDeclarations().add((AnonymousClassDeclaration) classDeclaration);
            }

            processClassBody(tree, fragment, container, isAnonymous, classDeclaration);
            return classDeclaration;
        }
    };

    private static void processClassBody(ClassDeclarationTree tree, CodeFragment fragment, IContainer container, boolean isAnonymous, ClassDeclaration classDeclaration) {

        for (var element : tree.elements) {

            switch (element.type) {
                case FUNCTION_DECLARATION:

                    break;
                default:
                    break;
            }
        }
    }
}

