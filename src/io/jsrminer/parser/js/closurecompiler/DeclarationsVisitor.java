package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.BlockTree;
import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

class DeclarationsVisitor {

    protected static FunctionDeclaration loadMemberFunction(FunctionDeclarationTree tree) {
        FunctionDeclaration function = new FunctionDeclaration();
        function.setSourceLocation(createSourceLocation(tree.location));
        function.setName(tree.name.value);
        return function;
    }


    protected static FunctionDeclaration loadArrowFunctionDeclaration(FunctionDeclarationTree tree) {
        AnonymousFunctionDeclaration function = new AnonymousFunctionDeclaration();
        function.setSourceLocation(createSourceLocation(tree.location));
        function.setName(tree.name.value);
        return function;
    }
//
//    protected static FunctionDeclaration processFunctionDeclaration(FunctionDeclarationTree tree, BlockStatement parent, IContainer container) {
//        FunctionDeclaration function = new FunctionDeclaration();
//
//        AstInfoExtractor.loadFunctionInfo(tree, function, container);
//
//        // Parse body
//        //function.setBody();
//        return function;
//    }
//
//
//    protected static AnonymousFunctionDeclaration processInlineFunctionDeclaration(FunctionDeclarationTree tree, ILeafFragment leaf, IContainer container) {
//        AnonymousFunctionDeclaration function = new AnonymousFunctionDeclaration();
//        function.setSourceLocation(AstInfoExtractor.createSourceLocation(tree.location));
//        function.setName(tree.name.value);
//        return function;
//    }

    public static final NodeProcessor<FunctionDeclaration, FunctionDeclarationTree, CodeFragment> functionDeclarationProcessor
            = new NodeProcessor<>() {
        @Override
        public FunctionDeclaration process(FunctionDeclarationTree tree, CodeFragment parent, IContainer container) {

            final boolean isAnonymous = parent instanceof ILeafFragment;
            FunctionDeclaration function;

            if (isAnonymous) {
                AnonymousFunctionDeclaration anonymousFunctionDeclaration = new AnonymousFunctionDeclaration();
                function = anonymousFunctionDeclaration;
                parent.getAnonymousFunctionDeclarations().add(anonymousFunctionDeclaration);
            } else {
                function = new FunctionDeclaration();
                container.getFunctionDeclarations().add(function);
            }

            // Load function info
            AstInfoExtractor.loadFunctionInfo(tree, function, container);

            // Load parameters
            tree.formalParameterList.parameters.forEach(parameterTree -> {
                UMLParameter parameter = createUmlParameter(parameterTree.asIdentifierExpression(), function);
                function.getParameters().add(parameter);
            });

            // Load functionBody by passing the function as the new container
            if (tree.functionBody != null) {
                BlockTree blockTree = tree.functionBody.asBlock();

                BlockStatement dummyParent = new BlockStatement();
                dummyParent.setText("{");
                AstInfoExtractor.populateLocationAndType(blockTree, dummyParent);
                dummyParent.setDepth(-1);

                Visitor.visitStatement(blockTree, dummyParent, function);
                BlockStatement bodyBlock = (BlockStatement) dummyParent.getStatements().get(0);
                bodyBlock.setParent(null);
                function.setBody(new FunctionBody(bodyBlock));
            } else {
                throw new RuntimeException("Null function body not handled for "
                        + function.getQualifiedName() + " at " + tree.location.toString());
            }


            return function;
        }
    };
}
