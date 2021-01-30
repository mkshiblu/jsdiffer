package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.VariableDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.VariableStatementTree;
import io.jsrminer.sourcetree.*;
import io.rminer.core.api.IContainer;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.createBaseExpression;
import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.createSourceLocation;

public class StatementsVisitor {

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

            VariableDeclarationKind kind = VariableDeclarationKind.fromName(tree.declarations.declarationType.toString());
            for (var declarationTree : tree.declarations.declarations) {
                VariableDeclaration vd = processVariableDeclaration(declarationTree, kind, container);
                leaf.getVariableDeclarations().add(vd);
                leaf.getVariables().add(vd.variableName);
            }
            //leaf.getVariableDeclarations().add()
            //return leaf;
            return leaf;
        }
    };


    /**
     * A variable declaration Node
     */
    protected static VariableDeclaration processVariableDeclaration(VariableDeclarationTree tree
            , VariableDeclarationKind kind
            , IContainer container) {
        String variableName = tree.lvalue.asIdentifierExpression().identifierToken.value;
        var variableDeclaration = new VariableDeclaration(variableName, kind);

        // Text
        String text = tree.toString();
        variableDeclaration.setText(text);
        variableDeclaration.setSourceLocation(createSourceLocation(tree));

        // Set Scope (TODO set body source location
        variableDeclaration.setScope(container.getSourceLocation());

        // Process initializer

        //if(ParseTree)
        Expression expression = createBaseExpression(tree.initializer);
        Visitor.visit(tree.initializer, expression, container);
        variableDeclaration.setInitializer(expression);
        return variableDeclaration;
    }
}
