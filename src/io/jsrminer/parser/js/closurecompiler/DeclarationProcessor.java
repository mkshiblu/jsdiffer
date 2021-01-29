package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;
import io.jsrminer.sourcetree.AnonymousFunctionDeclaration;
import io.jsrminer.sourcetree.CodeFragment;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.rminer.core.api.IContainer;
import io.rminer.core.api.ISourceFile;

class DeclarationProcessor {

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

    protected static FunctionDeclaration loadMemberFunction(FunctionDeclarationTree tree) {
        FunctionDeclaration function = new FunctionDeclaration();
        function.setSourceLocation(NodeProcessor.createSourceLocation(tree.location));
        function.setName(tree.name.value);
        return function;
    }

    protected static AnonymousFunctionDeclaration loadFunctionExpression(FunctionDeclarationTree tree) {
        AnonymousFunctionDeclaration function = new AnonymousFunctionDeclaration();
        function.setSourceLocation(NodeProcessor.createSourceLocation(tree.location));
        function.setName(tree.name.value);
        return function;
    }

    protected static FunctionDeclaration loadArrowFunctionDeclaration(FunctionDeclarationTree tree) {
        AnonymousFunctionDeclaration function = new AnonymousFunctionDeclaration();
        function.setSourceLocation(NodeProcessor.createSourceLocation(tree.location));
        function.setName(tree.name.value);
        return function;
    }

    protected static FunctionDeclaration loadFunctionDeclaration(FunctionDeclarationTree tree, IContainer container) {
        FunctionDeclaration function = new FunctionDeclaration();

        loadFunctionInfo(tree, function, container);

        // Parse body
        //function.setBody();
        return function;
    }

    private static void loadFunctionInfo(FunctionDeclarationTree tree, FunctionDeclaration function, IContainer container) {
        function.setSourceLocation(NodeProcessor.createSourceLocation(tree.location));

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

    public static final NodeProcessor<FunctionDeclaration, FunctionDeclarationTree> functionDeclarationProcessor
            = new NodeProcessor<>() {
        @Override
        public FunctionDeclaration process(FunctionDeclarationTree tree, CodeFragment parent, IContainer container) {
            FunctionDeclaration function = loadFunctionDeclaration(tree, container);
            //parent.getFunctionDeclarations().add(function);
            container.getFunctionDeclarations().add(function);
            return function;
        }
    };
}
