package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.VariableStatementTree;
import io.jsrminer.sourcetree.*;
import io.rminer.core.api.IContainer;

public class StatementsVisitor {
//
//    public static SingleStatement createVariableDeclarationStatement(VariableStatementTree tree, CodeFragment parent, IContainer container) {
//        function.setSourceLocation(NodeProcessor.createSourceLocation(tree.location));
//
//        // Name
//        String name = tree.name == null ? generateNameForAnonymousContainer(container) : tree.name.value;
//        function.setName(name);
//        function.setQualifiedName(generateQualifiedName(function.getName(), container));
//        function.setFullyQualifiedName(function.getSourceLocation().getFilePath() + "|" + function.getQualifiedName());
//        function.setParentContainerQualifiedName(container.getQualifiedName());
//
//        function.setIsTopLevel(container instanceof ISourceFile);
//        //function.setIsConstructor(function.);
//
//        // Parameter
//
//        // Function Body
//    }

    public static final NodeProcessor<SingleStatement, VariableStatementTree, BlockStatement> variableStatementProcessor
            = new NodeProcessor<>() {
        @Override
        public SingleStatement process(VariableStatementTree tree, BlockStatement parent, IContainer container) {
            if (!(parent instanceof BlockStatement))
                throw new RuntimeException("VDS is not inside of a block");

            String text;
            int depth;
            int indexInParent;
            SourceLocation location;
            CodeElementType type;
            var leaf = new SingleStatement();// AstInfoExtractor.createSingleStatement(text, tree.location, );

            //leaf.getVariableDeclarations().add()
            //return leaf;
            return leaf;
        }
    };
}
