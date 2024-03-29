package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.api.INode;
import io.rminerx.core.api.ISourceFile;
import io.rminerx.core.entities.DeclarationContainer;

public class BabelNodeUtil {

    private final String fileName;
    private final String fileContent;

    BabelNodeUtil(String fileName, String fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    SingleStatement createSingleStatementPopulateAndAddToParent(BabelNode node, BlockStatement parent) {
        var singleStatement = new SingleStatement();
        populateSingleStatementData(node, singleStatement);
        addStatement(singleStatement, parent);
        return singleStatement;
    }

    BlockStatement createBlockStatementPopulateAndAddToParent(BabelNode node, BlockStatement parent) {
        var blockStatement = new BlockStatement();
        populateBlockStatementData(node, blockStatement);
        addStatement(blockStatement, parent);
        return blockStatement;
    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    void populateSingleStatementData(BabelNode node, SingleStatement fragment) {
        populateTextLocationAndType(node, fragment);
    }

    Expression createBaseExpressionWithRMType(BabelNode tree, CodeElementType type) {
        var expression = new Expression();
        ///populateTextLocationAndType(tree, expression);
        populateTextAndLocation(tree, expression);
        expression.setType(type);
        return expression;
    }


    ILeafFragment copyLeafData(ILeafFragment source, ILeafFragment target) {
        target.getVariables().addAll(source.getVariables());
        target.getNullLiterals().addAll(source.getNullLiterals());
        target.getNumberLiterals().addAll(source.getNumberLiterals());
        target.getStringLiterals().addAll(source.getStringLiterals());
        target.getBooleanLiterals().addAll(source.getBooleanLiterals());
        target.getInfixOperators().addAll(source.getInfixOperators());
        target.getPrefixExpressions().addAll(source.getPrefixExpressions());

        target.getPostfixExpressions().addAll(source.getPostfixExpressions());
        target.getTernaryOperatorExpressions().addAll(source.getTernaryOperatorExpressions());
        target.getPrefixExpressions().addAll(source.getPrefixExpressions());
        target.getVariableDeclarations().addAll(source.getVariableDeclarations());
        target.getArguments().addAll(source.getArguments());


        for (var entry : source.getMethodInvocationMap().entrySet()) {
            var invocations1 = target.getMethodInvocationMap().get(entry.getKey());
            if (invocations1 == null) {
                target.getMethodInvocationMap().put(entry.getKey(), entry.getValue());
            } else {
                invocations1.addAll(entry.getValue());
            }
        }

        //leaf1.getMethodInvocationMap().addAll(leaf2.getVariableDeclarations());
        //leaf1.getCreationMap().addAll(leaf2.getVariableDeclarations());

        for (var entry : source.getCreationMap().entrySet()) {
            var creations1 = target.getCreationMap().get(entry.getKey());
            if (creations1 == null) {
                target.getCreationMap().put(entry.getKey(), entry.getValue());
            } else {
                creations1.addAll(entry.getValue());
            }
        }

        // Copy anonymous classes
        target.getAnonymousFunctionDeclarations().addAll(source.getAnonymousFunctionDeclarations());

        return target;
    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    void populateExpressionData(BabelNode node, Expression fragment) {
        populateTextLocationAndType(node, fragment);
    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    <T extends CodeFragment> void populateTextLocationAndType(BabelNode node, T fragment) {
        fragment.setText(getTextInSource(node, fragment instanceof SingleStatement));
        populateLocationAndType(node, fragment);
    }

    <T extends CodeFragment> void populateLocationAndType(BabelNode node, T fragment) {
        fragment.setSourceLocation(node.getSourceLocation());
        fragment.setType(getCodeElementTypeFromBabelNodeType(node.getType()));
    }

    <T extends CodeFragment> void populateTextAndLocation(BabelNode node, T fragment) {
        fragment.setSourceLocation(node.getSourceLocation());
        fragment.setText(getTextInSource(node, fragment instanceof SingleStatement));
    }

    void addStatement(Statement statement, BlockStatement parent) {
        statement.setDepth(parent.getDepth() + 1);
        statement.setPositionIndexInParent(parent.getStatements().size());
        parent.addStatement(statement);
        statement.setParent(parent);
    }

    void addExpressionToBlockStatement(Expression expression, BlockStatement parent) {
        //an expression has the same index and depth as the composite statement it belong to
        expression.setDepth(parent.getDepth());
        expression.setPositionIndexInParent(parent.getPositionIndexInParent());
        parent.getExpressions().add(expression);
        expression.setOwnerBlock(parent);
    }

    CodeElementType getCodeElementTypeFromBabelNodeType(BabelNodeType babelNodeType) {
        var type = BabelParserConfig.babelNodeToCodeElementTypeMap.get(babelNodeType);
        if (type == null) {
            throw new RuntimeException("Code Element Cannot be Found for babel type " + babelNodeType);
        }
        return type;
    }

    String getTextInSource(BabelNode node, boolean isStatement) {
        final String text = node.toString();//formatCodeFunction.apply(node);
        // Remove or add semicolon based on statement or expression
        // No semicolon for any expression
        var endsWithSemicolon = text.charAt(text.length() - 1) == ';';
        if (isStatement && !endsWithSemicolon) {
            return text + ';';
        }

        // An expression ends with semicolon, remove the semicolon
        else if (!isStatement && endsWithSemicolon) {
            return text.substring(0, text.length() - 1);
        }
        return text;
    }

    void loadAnonymousFunctionDeclarationInfo(BabelNode node, AnonymousFunctionDeclaration function, IContainer container) {
        String name = generateNameForAnonymousFunction(container);
        populateContainerNamesAndLocation(function, name, node.getSourceLocation(), container);

        // Parse optional name if defined
        var idNode = node.get("id");
        if (idNode != null && idNode.isDefined()) {
            var nameNode = idNode.get("name");
            if (nameNode != null) {
                var optionalName = nameNode.asString();
                function.setOptionalName(optionalName);
            }
        }
    }


    void loadFunctionDeclarationInfo(BabelNode node, FunctionDeclaration function, IContainer container) {
        String name = null;
        var idNode = node.get("id");
        if (idNode != null && idNode.isDefined()) {
            var nameNode = idNode.get("name");
            if (nameNode != null) {
                 name = nameNode.asString();
            }
        }
        if (name == null) {
            name = generateNameForAnonymousFunction(container);
        }
        populateContainerNamesAndLocation(function, name, node.getSourceLocation(), container);
        //function.setIsConstructor(function.);
    }
    
    void loadClassDeclarationInfo(BabelNode node, ClassDeclaration classDeclaration, IContainer container) {
        var nameNode = node.get("id").get("name");

        String name;
        if (nameNode != null) {
            name = nameNode.asString();
        } else {
            name = generateNameForAnonymousClassDeclaration(container);
        }
        classDeclaration.setIsTopLevel(container.getContainerType() == IContainer.ContainerType.File);
        populateContainerNamesAndLocation(classDeclaration, name, node.getSourceLocation(), container);
        //function.setIsConstructor(function.);
    }

    void populateContainerNamesAndLocation(DeclarationContainer function, String  name, SourceLocation location, IContainer container) {
        function.setSourceLocation(location);
        function.setName(name);
        function.setQualifiedName(generateQualifiedName(function.getName(), container));
        function.setFullyQualifiedName(function.getSourceLocation().getFilePath() + "|" + function.getQualifiedName());
        function.setParentContainerQualifiedName(container.getQualifiedName());
        function.setIsTopLevel(container instanceof ISourceFile);
    }

    String generateNameForAnonymousFunction(IContainer parentContainer) {
        return parentContainer.getAnonymousFunctionDeclarations().size() + 1 + "";
    }

    String generateNameForAnonymousClassDeclaration(IContainer parentContainer) {
        return parentContainer.getAnonymousClassDeclarations().size() + 1 + "";
    }

    String generateQualifiedName(String name, IContainer parentContainer) {
        String namespace = null;
        if (!(parentContainer instanceof ISourceFile)) {
            namespace = parentContainer.getQualifiedName();
        }

        return namespace == null ? name : namespace + "." + name;
    }

    UMLParameter createUmlParameter(String name, FunctionDeclaration functionDeclaration
            , SourceLocation location) {
        UMLParameter parameter = new UMLParameter(name);
        parameter.setSourceLocation(location);
        parameter.setIndexPositionInParent(functionDeclaration.getParameters().size());
        VariableDeclaration vd = new VariableDeclaration(name, null/*, VariableDeclarationKind.VAR*/);
        vd.setIsParameter(true);
        vd.setScope(createVariableScope(location, functionDeclaration.getSourceLocation()));
        vd.setSourceLocation(parameter.getSourceLocation());
        parameter.setVariableDeclaration(vd);
        return parameter;
    }

    SourceLocation createVariableScope(SourceLocation variableLocation, SourceLocation parentLocation) {
        return new SourceLocation(parentLocation.getFilePath(),
                variableLocation.startLine,
                variableLocation.startColumn,
                parentLocation.endLine,
                parentLocation.endColumn,
                variableLocation.start,
                parentLocation.end
        );
    }

    void populateBlockStatementData(BabelNode node, BlockStatement blockStatement) {
        populateLocationAndType(node, blockStatement);
        blockStatement.setText(blockStatement.getCodeElementType().keyword);
        if (blockStatement.getText() == null) {
            throw new RuntimeException("Block text was not populated for type " + blockStatement.getCodeElementType().toString());
        }
    }

    VariableDeclaration createVariableDeclarationFromIdentifier(BabelNode node
            , VariableDeclarationKind kind
            , INode scopeNode) {
        String variableName = node.getString("name");
        var variableDeclaration = new VariableDeclaration(variableName, kind);

        variableDeclaration.setSourceLocation(node.getSourceLocation());

        // Set Scope (TODO set body source location
        variableDeclaration.setScope(createVariableScope(variableDeclaration.getSourceLocation(), scopeNode.getSourceLocation()));

        return variableDeclaration;
    }
}
