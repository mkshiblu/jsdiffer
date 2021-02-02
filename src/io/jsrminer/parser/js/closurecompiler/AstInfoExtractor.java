package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ISourceFile;

import java.util.EnumMap;

public class AstInfoExtractor {
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
        var expression = new Expression();
        expression.setSourceLocation(AstInfoExtractor.createSourceLocation(tree));
        return expression;
    }

    static String getTextInSource(ParseTree tree) {
        return tree.location.start.source.contents.substring(tree.location.start.offset, tree.location.end.offset);
    }

    public final static EnumMap<ParseTreeType, CodeElementType> parseTreeTypeCodeElementTypeMap = new EnumMap(ParseTreeType.class) {{
        put(ParseTreeType.EXPRESSION_STATEMENT, CodeElementType.EXPRESSION_STATEMENT);

        put(ParseTreeType.IF_STATEMENT, CodeElementType.IF_STATEMENT);
        put(ParseTreeType.SWITCH_STATEMENT, CodeElementType.SWITCH_STATEMENT);

        put(ParseTreeType.BLOCK, CodeElementType.BLOCK_STATEMENT);
        put(ParseTreeType.FUNCTION_DECLARATION, CodeElementType.FUNCTION_DECLARATION);
        put(ParseTreeType.EMPTY_STATEMENT, CodeElementType.EMPTY_STATEMENT);
        put(ParseTreeType.CALL_EXPRESSION, CodeElementType.FUNCTION_INVOCATION);
        //put(ParseTreeType.NEW_EXPRESSION, CodeElementType.CONSTRUCTOR_INVOCATION);
        //put(ParseTreeType., CodeElementType.SUPER_CONSTRUCTOR_INVOCATION);

        //put(ParseTreeType.NEW_EXPRESSION, CodeElementType.OBJECT_CREATION);

        put(ParseTreeType.TRY_STATEMENT, CodeElementType.TRY_STATEMENT);
        put(ParseTreeType.CATCH, CodeElementType.CATCH_CLAUSE);
        put(ParseTreeType.THROW_STATEMENT, CodeElementType.THROW_STATEMENT);

        //put(ParseTreeType.ARRAY_LITERAL_EXPRESSION, CodeElementType.ARRAY_EXPRESSION);
        put(ParseTreeType.FOR_STATEMENT, CodeElementType.FOR_STATEMENT);
        put(ParseTreeType.FOR_IN_STATEMENT, CodeElementType.ENHANCED_FOR_STATEMENT);
        //put(ParseTreeType.FOR_OF_STATEMENT, CodeElementType.ENHANCED_FOR_STATEMENT);
        put(ParseTreeType.DO_WHILE_STATEMENT, CodeElementType.DO_WHILE_STATEMENT);
        put(ParseTreeType.WHILE_STATEMENT, CodeElementType.WHILE_STATEMENT);

        put(ParseTreeType.CONTINUE_STATEMENT, CodeElementType.CONTINUE_STATEMENT);
        put(ParseTreeType.BREAK_STATEMENT, CodeElementType.BREAK_STATEMENT);

        put(ParseTreeType.LABELLED_STATEMENT, CodeElementType.LABELED_STATEMENT);
        put(ParseTreeType.RETURN_STATEMENT, CodeElementType.RETURN_STATEMENT);

        put(ParseTreeType.VARIABLE_STATEMENT, CodeElementType.VARIABLE_DECLARATION_STATEMENT);
        put(ParseTreeType.VARIABLE_DECLARATION, CodeElementType.VARIABLE_DECLARATION);
    }};

    static CodeElementType getCodeElementType(ParseTree tree) {
        if (parseTreeTypeCodeElementTypeMap.get(tree.type) == null)
            throw new RuntimeException("ParseTreeType " + tree.type + " not mapped to CodeElement yet");

        return parseTreeTypeCodeElementTypeMap.get(tree.type);
    }


    static SingleStatement createSingleStatementAndPopulateCommonData(ParseTree tree, BlockStatement parent) {
        var singleStatement = new SingleStatement();
        populateLeafData(tree, singleStatement, parent);
        singleStatement.setParent(parent);
        parent.addStatement(singleStatement);
        return singleStatement;
    }
//
//    static SingleStatement createSingleStatementWithTextLocationIndexDepthType(ParseTree tree, BlockStatement parent) {
//        String text = getTextInSource(tree);
//        SourceLocation location = createSourceLocation(tree);
//        int depth = parent.getDepth() + 1;
//        int indexInParent = parent.getStatements().size();
//        CodeElementType type = getCodeElementType(tree);
//        return createSingleStatement(text, type, location, indexInParent, depth);
//    }

    /**
     * Populates text, sourceLocation, type, depth, index in parent.
     */
    static <T extends CodeFragment> void populateLeafData(ParseTree tree, T fragment, BlockStatement parent) {
        fragment.setText(getTextInSource(tree));
        fragment.setSourceLocation(createSourceLocation(tree));
        fragment.setDepth(parent.getDepth() + 1);
        fragment.setPositionIndexInParent(parent.getStatements().size());
        fragment.setType(getCodeElementType(tree));

    }
}
