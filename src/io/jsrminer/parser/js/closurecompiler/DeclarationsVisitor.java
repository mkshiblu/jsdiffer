package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;
import io.jsrminer.sourcetree.AnonymousFunctionDeclaration;
import io.jsrminer.sourcetree.CodeFragment;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.rminerx.core.api.IContainer;

class DeclarationsVisitor {

    protected static FunctionDeclaration loadMemberFunction(FunctionDeclarationTree tree) {
        FunctionDeclaration function = new FunctionDeclaration();
        function.setSourceLocation(AstInfoExtractor.createSourceLocation(tree.location));
        function.setName(tree.name.value);
        return function;
    }

    protected static AnonymousFunctionDeclaration loadFunctionExpression(FunctionDeclarationTree tree) {
        AnonymousFunctionDeclaration function = new AnonymousFunctionDeclaration();
        function.setSourceLocation(AstInfoExtractor.createSourceLocation(tree.location));
        function.setName(tree.name.value);
        return function;
    }

    protected static FunctionDeclaration loadArrowFunctionDeclaration(FunctionDeclarationTree tree) {
        AnonymousFunctionDeclaration function = new AnonymousFunctionDeclaration();
        function.setSourceLocation(AstInfoExtractor.createSourceLocation(tree.location));
        function.setName(tree.name.value);
        return function;
    }

    protected static FunctionDeclaration createFunctionDeclaration(FunctionDeclarationTree tree, IContainer container) {
        FunctionDeclaration function = new FunctionDeclaration();

        AstInfoExtractor.loadFunctionInfo(tree, function, container);

        // Parse body
        //function.setBody();
        return function;
    }

    public static final NodeProcessor<FunctionDeclaration, FunctionDeclarationTree, CodeFragment> functionDeclarationProcessor
            = new NodeProcessor<>() {
        @Override
        public FunctionDeclaration process(FunctionDeclarationTree tree, CodeFragment parent, IContainer container) {
            FunctionDeclaration function = createFunctionDeclaration(tree, container);
            //parent.getFunctionDeclarations().add(function);
            container.getFunctionDeclarations().add(function);
            return function;
        }
    };
}
