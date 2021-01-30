package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.VariableDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import io.jsrminer.sourcetree.*;
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

    /**
     * A variable declaration Node
     */
    protected static VariableDeclaration createVariableDeclaration(VariableDeclarationTree tree, VariableDeclarationKind kind, IContainer container) {
        String variableName = "#4";

        var variableDeclaration = new VariableDeclaration(variableName, kind);
        return variableDeclaration;
    }

    static SingleStatement createSingleStatement(String text
            , SourceLocation sourceLocation
            , int positionIndexInParent
            , int depth) {
        var singleStatement = new SingleStatement();
        singleStatement.setText(text);
        singleStatement.setSourceLocation(sourceLocation);
        singleStatement.setPositionIndexInParent(positionIndexInParent);
        singleStatement.setDepth(depth);
        return singleStatement;
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
}
