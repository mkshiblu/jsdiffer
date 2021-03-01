package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.TokenType;
import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.ObjectLiteralExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.entities.DeclarationContainer;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;
import static io.jsrminer.parser.js.closurecompiler.DeclarationsVisitor.addVariableDeclarationToParent;
import static io.jsrminer.parser.js.closurecompiler.DeclarationsVisitor.createVariableDeclarationFromVariableName;

public class ObjectsVisitor {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
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
            String text = getTextInSource(tree, false);
            anonymousFunctionDeclaration.setText(text);

            BlockStatement blockStatement = new BlockStatement();
            blockStatement.setSourceLocation(AstInfoExtractor.createSourceLocation(tree));
            blockStatement.setText("{");
            blockStatement.setCodeElementType(CodeElementType.BLOCK_STATEMENT);

            anonymousFunctionDeclaration.setBody(new FunctionBody(blockStatement));

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

    private static void processProperty(ParseTree tree, BlockStatement body, IContainer container, ObjectLiteral objectLiteral) {
        var property = tree.asPropertyNameAssignment();

        if (property.name.type == TokenType.IDENTIFIER) {
            var fieldName = property.name.asIdentifier().value;
            var propInitializerTree = property.value;

            if (propInitializerTree != null) {
                switch (propInitializerTree.type) {
                    case FUNCTION_DECLARATION:
                        processObjectFunctionDeclaration(propInitializerTree.asFunctionDeclaration(), body, container, fieldName);
                        break;
                    default:
                        // Else this is an attribute i.e. field declaration
                        processObjectFieldDeclaration(propInitializerTree, body, container, fieldName, tree);
                }
            }
        } else {
            log.warn(tree.toString() + ": Object literal's property name is not an identifier. Skipping...");
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

    static VariableDeclaration processObjectFieldDeclaration(ParseTree propInitializerTree
            , BlockStatement body
            , IContainer container
            , String fieldName
            , ParseTree propertyTree) {

        // Create a single statement
        var leaf = new SingleStatement();
        leaf.setSourceLocation(createSourceLocation(propertyTree.location));
        leaf.setType(CodeElementType.VARIABLE_DECLARATION_STATEMENT);
        leaf.setText(fieldName + " = " + getTextInSource(propInitializerTree, true));
        addStatement(leaf, body);

        var variableDeclaration = createVariableDeclarationFromVariableName(fieldName, null, propertyTree.location, body.getSourceLocation());

        // Process initializer
        Expression expression = createBaseExpressionWithRMType(propInitializerTree, CodeElementType.VARIABLE_DECLARATION_INITIALIZER);
        Visitor.visitExpression(propInitializerTree, expression, container);
        variableDeclaration.setInitializer(expression);

        // Keep track of all the vd
        addVariableDeclarationToParent(leaf, variableDeclaration);

        return variableDeclaration;
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
}
