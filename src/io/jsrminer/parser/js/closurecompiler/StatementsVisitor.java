package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.VariableDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.VariableStatementTree;
import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.IContainer;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

public class StatementsVisitor {

    /**
     * A Variable declaration Statement e.g. let x = "4";
     */
    public static final NodeProcessor<SingleStatement, VariableStatementTree, BlockStatement> variableStatementProcessor
            = new NodeProcessor<>() {
        @Override
        public SingleStatement process(VariableStatementTree tree, BlockStatement parent, IContainer container) {
            if (!(parent instanceof BlockStatement))
                throw new RuntimeException("VDS is not inside of a block");

            String text = getTextInSource(tree);
            SourceLocation location = createSourceLocation(tree);

            int depth = parent.getDepth() + 1;
            int indexInParent = parent.getStatements().size();

            CodeElementType type =  getCodeElementType(tree);
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

        variableDeclaration.setSourceLocation(createSourceLocation(tree));

        // Set Scope (TODO set body source location
        variableDeclaration.setScope(container.getSourceLocation());

        // Process initializer
        if (tree.initializer != null) {
            Expression expression = createBaseExpression(tree.initializer);
            Visitor.visit(tree.initializer, expression, container);
            variableDeclaration.setInitializer(expression);

            // TODO add info to the statement?
        }
        return variableDeclaration;
    }
}
