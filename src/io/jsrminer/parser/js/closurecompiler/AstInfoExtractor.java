package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.*;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.api.INode;
import io.rminerx.core.api.ISourceFile;

import java.util.EnumMap;

public class AstInfoExtractor {
    public final static EnumMap<ParseTreeType, CodeElementType> parseTreeTypeCodeElementTypeMap = new EnumMap(ParseTreeType.class) {{
        put(ParseTreeType.EXPRESSION_STATEMENT, CodeElementType.EXPRESSION_STATEMENT);

        put(ParseTreeType.IF_STATEMENT, CodeElementType.IF_STATEMENT);
        put(ParseTreeType.SWITCH_STATEMENT, CodeElementType.SWITCH_STATEMENT);
        put(ParseTreeType.CASE_CLAUSE, CodeElementType.SWITCH_CASE);

        put(ParseTreeType.BLOCK, CodeElementType.BLOCK_STATEMENT);
        put(ParseTreeType.FUNCTION_DECLARATION, CodeElementType.FUNCTION_DECLARATION);
        put(ParseTreeType.EMPTY_STATEMENT, CodeElementType.EMPTY_STATEMENT);
        put(ParseTreeType.CALL_EXPRESSION, CodeElementType.FUNCTION_INVOCATION);
        put(ParseTreeType.NEW_EXPRESSION, CodeElementType.CONSTRUCTOR_INVOCATION);
        //put(ParseTreeType., CodeElementType.SUPER_CONSTRUCTOR_INVOCATION);

//        put(ParseTreeType.NEW_EXPRESSION, CodeElementType.OBJECT_CREATION);

        put(ParseTreeType.TRY_STATEMENT, CodeElementType.TRY_STATEMENT);
        put(ParseTreeType.CATCH, CodeElementType.CATCH_CLAUSE);
        put(ParseTreeType.FINALLY, CodeElementType.FINALLY_BLOCK);
        put(ParseTreeType.THROW_STATEMENT, CodeElementType.THROW_STATEMENT);

        //put(ParseTreeType.ARRAY_LITERAL_EXPRESSION, CodeElementType.ARRAY_EXPRESSION);
        put(ParseTreeType.FOR_STATEMENT, CodeElementType.FOR_STATEMENT);
        put(ParseTreeType.FOR_IN_STATEMENT, CodeElementType.ENHANCED_FOR_STATEMENT);
        put(ParseTreeType.FOR_OF_STATEMENT, CodeElementType.ENHANCED_FOR_STATEMENT);
        put(ParseTreeType.DO_WHILE_STATEMENT, CodeElementType.DO_WHILE_STATEMENT);
        put(ParseTreeType.WHILE_STATEMENT, CodeElementType.WHILE_STATEMENT);

        put(ParseTreeType.CONTINUE_STATEMENT, CodeElementType.CONTINUE_STATEMENT);
        put(ParseTreeType.BREAK_STATEMENT, CodeElementType.BREAK_STATEMENT);

        put(ParseTreeType.LABELLED_STATEMENT, CodeElementType.LABELED_STATEMENT);
        put(ParseTreeType.RETURN_STATEMENT, CodeElementType.RETURN_STATEMENT);

        put(ParseTreeType.VARIABLE_STATEMENT, CodeElementType.VARIABLE_DECLARATION_STATEMENT);
        put(ParseTreeType.VARIABLE_DECLARATION, CodeElementType.VARIABLE_DECLARATION);

        put(ParseTreeType.LITERAL_EXPRESSION, CodeElementType.LITERAL_EXPRESSION);
    }};

    public static SourceLocation createSourceLocation(SourceRange sourceRange) {
        return new SourceLocation(
                sourceRange.start.source.name
                , sourceRange.start.line
                , sourceRange.start.column
                , sourceRange.end.line
                , sourceRange.end.column
                , sourceRange.start.offset
                , sourceRange.end.offset
        );
    }

    public static SourceLocation createSourceLocation(ParseTree tree) {
        return createSourceLocation(tree.location);
    }


    public static String generateNameForAnonymousContainer(IContainer parentContainer) {
        return parentContainer.getAnonymousFunctionDeclarations().size() + 1 + "";
    }

    public static String generateQualifiedName(String name, IContainer parentContainer) {
        String namespace = null;
        if (!(parentContainer instanceof ISourceFile)) {
            namespace = parentContainer.getQualifiedName();
        }

        return namespace == null ? name : namespace + "." + name;
    }

    static void loadFunctionInfo(FunctionDeclarationTree tree, FunctionDeclaration function, IContainer container) {
        function.setSourceLocation(createSourceLocation(tree.location));

        // Name
        String name = tree.name == null ? generateNameForAnonymousContainer(container) : tree.name.value;
        function.setName(name);
        function.setQualifiedName(generateQualifiedName(function.getName(), container));
        function.setFullyQualifiedName(function.getSourceLocation().getFilePath() + "|" + function.getQualifiedName());
        function.setParentContainerQualifiedName(container.getQualifiedName());

        function.setIsTopLevel(container instanceof ISourceFile);
        //function.setIsConstructor(function.);

        // Parameter

        // Function Body
    }

    static Expression createBaseExpression(ParseTree tree) {
        return createBaseExpressionWithRMType(tree, getCodeElementType(tree));
    }

    static Expression createBaseExpressionWithRMType(ParseTree tree, CodeElementType type) {
        var expression = new Expression();
        ///populateTextLocationAndType(tree, expression);
        populateTextAndLocation(tree, expression);
        expression.setType(type);
        return expression;
    }

    static Expression createExpressionPopulateAndAddToParent(ParseTree tree, BlockStatement parent) {
        var expression = new Expression();
        populateExpressionData(tree, expression);
        addExpression(expression, parent);
        return expression;
    }


    static BlockStatement createBlockStatementPopulateAndAddToParent(ParseTree tree, BlockStatement parent) {
        var blockStatement = new BlockStatement();
        populateBlockStatementData(tree, blockStatement);
        addStatement(blockStatement, parent);
        return blockStatement;
    }

    static SingleStatement createSingleStatementPopulateAndAddToParent(ParseTree tree, BlockStatement parent) {
        var singleStatement = new SingleStatement();
        populateSingleStatementData(tree, singleStatement);
        addStatement(singleStatement, parent);
        return singleStatement;
    }

    static ILeafFragment copyLeafData(ILeafFragment leaf1, ILeafFragment leaf2) {
        leaf1.getVariables().addAll(leaf2.getVariables());
        leaf1.getNullLiterals().addAll(leaf2.getNullLiterals());
        leaf1.getNumberLiterals().addAll(leaf2.getNumberLiterals());
        leaf1.getStringLiterals().addAll(leaf2.getStringLiterals());
        leaf1.getBooleanLiterals().addAll(leaf2.getBooleanLiterals());
        leaf1.getInfixOperators().addAll(leaf2.getInfixOperators());
        leaf1.getPrefixExpressions().addAll(leaf2.getPrefixExpressions());

        leaf1.getPostfixExpressions().addAll(leaf2.getPostfixExpressions());
        leaf1.getTernaryOperatorExpressions().addAll(leaf2.getTernaryOperatorExpressions());
        leaf1.getPrefixExpressions().addAll(leaf2.getPrefixExpressions());
        leaf1.getVariableDeclarations().addAll(leaf2.getVariableDeclarations());
        leaf1.getArguments().addAll(leaf2.getArguments());


        for (var entry : leaf2.getMethodInvocationMap().entrySet()) {
            var invocations1 = leaf1.getMethodInvocationMap().get(entry.getKey());
            if (invocations1 == null) {
                leaf1.getMethodInvocationMap().put(entry.getKey(), entry.getValue());
            } else {
                invocations1.addAll(entry.getValue());
            }
        }

        //leaf1.getMethodInvocationMap().addAll(leaf2.getVariableDeclarations());
        //leaf1.getCreationMap().addAll(leaf2.getVariableDeclarations());

        for (var entry : leaf2.getCreationMap().entrySet()) {
            var creations1 = leaf1.getCreationMap().get(entry.getKey());
            if (creations1 == null) {
                leaf1.getCreationMap().put(entry.getKey(), entry.getValue());
            } else {
                creations1.addAll(entry.getValue());
            }
        }

        return leaf1;
    }

    /**
     *
     */
    static void populateBlockStatementData(ParseTree tree, BlockStatement blockStatement) {
        populateLocationAndType(tree, blockStatement);
        blockStatement.setText(blockStatement.getCodeElementType().keyword);
        if (blockStatement.getText() == null) {
            throw new RuntimeException("Block text was not populated for type " + blockStatement.getCodeElementType().toString());
        }
    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    static void populateSingleStatementData(ParseTree tree, SingleStatement fragment) {
        populateTextLocationAndType(tree, fragment);
    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    static void populateExpressionData(ParseTree tree, Expression fragment) {
        populateTextLocationAndType(tree, fragment);
    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    static <T extends CodeFragment> void populateTextLocationAndType(ParseTree tree, T fragment) {
        fragment.setText(getTextInSource(tree));
        populateLocationAndType(tree, fragment);
    }

    static <T extends CodeFragment> void populateLocationAndType(ParseTree tree, T fragment) {
        fragment.setSourceLocation(createSourceLocation(tree));
        fragment.setType(getCodeElementType(tree));
    }

    static <T extends CodeFragment> void populateTextAndLocation(ParseTree tree, T fragment) {
        fragment.setSourceLocation(createSourceLocation(tree));
        fragment.setText(getTextInSource(tree));
    }

    static void addStatement(Statement statement, BlockStatement parent) {
        statement.setDepth(parent.getDepth() + 1);
        statement.setPositionIndexInParent(parent.getStatements().size());
        parent.addStatement(statement);
        statement.setParent(parent);
    }

    static void addExpression(Expression expression, BlockStatement parent) {
        //an expression has the same index and depth as the composite statement it belong to
        expression.setDepth(parent.getDepth());
        expression.setPositionIndexInParent(parent.getPositionIndexInParent());
        parent.getExpressions().add(expression);
        expression.setOwnerBlock(parent);
    }


    static String getTextInSource(ParseTree tree) {
        return tree.location.start.source.contents.substring(tree.location.start.offset, tree.location.end.offset);
    }

    static CodeElementType getCodeElementType(ParseTree tree) {
        if (parseTreeTypeCodeElementTypeMap.get(tree.type) == null)
            throw new RuntimeException("ParseTreeType " + tree.type + " not mapped to CodeElement yet");

        return parseTreeTypeCodeElementTypeMap.get(tree.type);
    }

    static SourceLocation createVariableScope(ParseTree variable, INode scopeNode) {
        final SourceLocation parentLocation = scopeNode.getSourceLocation();
        return new SourceLocation(parentLocation.getFilePath(),
                variable.getStart().line,
                variable.getStart().column,
                parentLocation.endLine,
                parentLocation.endColumn,
                variable.getStart().offset,
                parentLocation.end
        );
    }

    static UMLParameter createUmlParameter(IdentifierExpressionTree parameterTree, FunctionDeclaration functionDeclaration) {
        String name = parameterTree.identifierToken.value;
        UMLParameter parameter = new UMLParameter(name);
        parameter.setSourceLocation(createSourceLocation(parameterTree));
        parameter.setIndexPositionInParent(functionDeclaration.getParameters().size());
        VariableDeclaration vd = new VariableDeclaration(name, VariableDeclarationKind.VAR);
        vd.setIsParameter(true);
        vd.setScope(createVariableScope(parameterTree, functionDeclaration));
        vd.setSourceLocation(parameter.getSourceLocation());
        parameter.setVariableDeclaration(vd);
        return parameter;
    }

    static BlockStatement createDummyBodyBlock(BlockTree blockTree) {
        BlockStatement dummyParent = new BlockStatement();
        dummyParent.setText("{");
        AstInfoExtractor.populateLocationAndType(blockTree, dummyParent);
        dummyParent.setDepth(-1);
        return dummyParent;
    }
}
