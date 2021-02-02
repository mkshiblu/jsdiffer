package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.Expression;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.SourceLocation;
import io.rminer.core.api.IContainer;
import io.rminer.core.api.ISourceFile;

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

    static CodeElementType getCodeElementType(ParseTree tree){
        //tree.type.toString()
        //return CodeElementType.getFromTitleCase();
        return null;
    }
}
