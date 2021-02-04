package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ObjectLiteralExpressionTree;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.Expression;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.entities.DeclarationContainer;

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
    public static final NodeVisitor<ObjectLiteral, ObjectLiteralExpressionTree, ILeafFragment> objectLiteralExpression
            = new NodeVisitor<>() {
        @Override
        public ObjectLiteral visit(ObjectLiteralExpressionTree tree, ILeafFragment leaf, IContainer container) {
            if (tree.hasTrailingComma) {
                throw new RuntimeException("Object expressions with trailing comma not handled");
            }

            BlockStatement blockStatement = new BlockStatement();
            blockStatement.setSourceLocation(AstInfoExtractor.createSourceLocation(tree));
            blockStatement.setText("{");
            blockStatement.setCodeElementType(CodeElementType.BLOCK_STATEMENT);

            final ObjectLiteral literal = new ObjectLiteral();
            tree.propertyNameAndValues.forEach(property -> {
            //    Visitor.visitStatement(property, blockStatement, literal);
            });

            return literal;
        }
    };

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
    }
}
