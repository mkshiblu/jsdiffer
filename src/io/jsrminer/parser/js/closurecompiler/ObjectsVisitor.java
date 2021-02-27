package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.ObjectLiteralExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.entities.DeclarationContainer;
import org.apache.commons.lang3.NotImplementedException;

import java.util.LinkedHashMap;

public class ObjectsVisitor {

    /**
     * An object literal expression e.g.
     * {
     * name: "John",
     * m1: function (){
     * return "4343";
     * }
     * };
     * <p>
     * Has propertyNameAndValues and HasTrailingCommas
     */
    public static final NodeVisitor<AnonymousFunctionDeclaration, ObjectLiteralExpressionTree, ILeafFragment> objectLiteralExpression
            = new NodeVisitor<>() {
        @Override
        public AnonymousFunctionDeclaration visit(ObjectLiteralExpressionTree tree, ILeafFragment leaf, IContainer container) {
            var anonymousFunctionDeclaration = new AnonymousFunctionDeclaration();

            String name = AstInfoExtractor.generateNameForAnonymousContainer(container);
            AstInfoExtractor.populateContainerNamesAndLocation(anonymousFunctionDeclaration,
                    name, tree.location, container);

            BlockStatement blockStatement = new BlockStatement();
            blockStatement.setSourceLocation(AstInfoExtractor.createSourceLocation(tree));
            blockStatement.setText("{");
            blockStatement.setCodeElementType(CodeElementType.BLOCK_STATEMENT);

            leaf.getAnonymousFunctionDeclarations().add(anonymousFunctionDeclaration);

            //AstInfoExtractor.createDummyBodyBlock()
            final ObjectLiteral literal = new ObjectLiteral();
            tree.propertyNameAndValues.forEach(property -> {
                //Visitor.visitStatement(property, blockStatement, literal);
                processProperty(property, blockStatement, anonymousFunctionDeclaration, literal);
            });

            return anonymousFunctionDeclaration;
        }
    };

    private static void processProperty(ParseTree tree, BlockStatement parent, IContainer container, ObjectLiteral objectLiteral) {
        var property = tree.asPropertyNameAssignment();
        var fieldName = property.name.asIdentifier().value;
        var propInitializerTree = property.value;

        if (propInitializerTree != null) {
            switch (propInitializerTree.type) {
                case FUNCTION_DECLARATION:
                    processObjectFunctionDeclaration(propInitializerTree.asFunctionDeclaration(), parent, container, fieldName);
                    break;
                case VARIABLE_DECLARATION:
                    Visitor.visitStatement(propInitializerTree, parent, container);
                    break;
                default:
                    throw new NotImplementedException();
            }
        }
    }

    static FunctionDeclaration processObjectFunctionDeclaration(FunctionDeclarationTree tree
            , CodeFragment fragment
            , IContainer container
            , String propertyNameAsFunctionName) {
        var function = new FunctionDeclaration();
        container.getFunctionDeclarations().add(function);

        // Load function info
        AstInfoExtractor.populateContainerNamesAndLocation(function, propertyNameAsFunctionName, tree.location, container);

        DeclarationsVisitor.processFunctionParamaterAndBody(tree, fragment, container,
                false, function);
        return function;
    }

    static class ObjectLiteral extends DeclarationContainer {
        private final LinkedHashMap<String, Expression> properties = new LinkedHashMap<>();

        private String name;

        public LinkedHashMap<String, Expression> getProperties() {
            return properties;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getFullyQualifiedName() {
            throw new NotImplementedException();
        }
    }

    static void convertObjectLiteralToAnonymousFunction() {

    }
}
