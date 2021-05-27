package io.jsrminer.parser.js.babel;

import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;


public class ExpressionVisitor {

    private final Visitor visitor;

    ExpressionVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * interface AssignmentExpression<: Expression {
     * type: "AssignmentExpression";
     * operator: AssignmentOperator;
     * left: Pattern | Expression;
     * right: Expression;
     * }
     * An assignment operator expression.
     * <p>
     * AssignmentOperator
     * enum AssignmentOperator {
     * "=" | "+=" | "-=" | "*=" | "/=" | "%="
     * | "<<=" | ">>=" | ">>>="
     * | "|=" | "^=" | "&="
     * }
     * An assignment operator token.
     **/
    String visitAssignmentExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        String text = visitor.getNodeUtil().getTextInSource(node, false);
        var operator = node.get("operator").asString();

        // TODO should treated as infix if =?
        if (!"=".equals(operator)) {
            leaf.registerInfixOperator(operator);
            leaf.registerInfixExpression(text);
        }

        visitor.visitExpression(node.get("left"), leaf, container);
        visitor.visitExpression(node.get("right"), leaf, container);

        return text;
    }

    /**
     * interface MemberExpression<: Expression, Pattern {
     * type: "MemberExpression";
     * object: Expression | Super;
     * property: Expression;
     * computed: boolean;
     * optional: boolean | null;
     * }
     * A member expression.If computed is true, the node corresponds to a computed(a[b])
     * member expression and property is an Expression.If computed is false, the node
     * corresponds to a static(a.b) member expression and property is an Identifier.
     * The optional flags indicates that the member expression can be called even if
     * the object is null or undefined.If this is the object value(null / undefined)
     * should be returned.
     **/
    void visitMemberExpression(BabelNode node, ILeafFragment leaf, IContainer container) {

        boolean isComputed = node.get("computed").asBoolean();
        BabelNode propertyNode = node.get("property");

        visitor.visitExpression(node.get("object"), leaf, container);

        if (isComputed) {
            // Array  access?
            // property is expression
            visitor.visitExpression(propertyNode, leaf, container);
        } else {
            // prperty is identifier
            visitor.visitExpression(propertyNode, leaf, container);
        }

//        if (tree.memberName != null) {
//            String variableName = tree.memberName.value;
//            if (tree.operand.type == ParseTreeType.THIS_EXPRESSION) {
//                variableName = "this." + variableName;
//            }
//            leaf.getVariables().add(variableName);
//        }
    }

    /**
     * interface Identifier <: Expression, Pattern {
     * type: "Identifier";
     * name: string;
     * }
     * An identifier. Note that an identifier may be an expression or a destructuring pattern
     */
    void visitIdentifier(BabelNode node, ILeafFragment leaf, IContainer container) {
        String name = node.getString("name");
        leaf.registerVariable(name);
    }

    /**
     * interface UnaryExpression <: Expression {
     * type: "UnaryExpression";
     * operator: UnaryOperator;
     * prefix: boolean;
     * argument: Expression;
     * }
     * <p>
     * E.G. !success, ++i, --i
     */
    void visitUnaryExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        boolean isPrefix = node.get("prefix").asBoolean();
        String operator = node.get("operator").asString();
        String text = this.visitor.getNodeUtil().getTextInSource(node, false);

        if (isPrefix) {
            leaf.registerPrefixExpression(text);
        } else {
            leaf.registerPostfixExpression(text);
        }

        visitor.visitExpression(node.get("argument"), leaf, container);
    }

    /**
     interface BinaryExpression<: Expression {
     type: "BinaryExpression";
     operator: BinaryOperator;
     left: Expression;
     right: Expression;
     }
     */
    public String visitBinaryExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        String text = visitor.getNodeUtil().getTextInSource(node, false);
        var operator = node.getString("operator");

        // TODO should treated as infix if =?
        if (!"=".equals(operator)) {
            leaf.getInfixOperators().add(operator);
            leaf.getInfixExpressions().add(text);
        }

        visitor.visitExpression(node.get("left"), leaf, container);
        visitor.visitExpression(node.get("right"), leaf, container);
        return text;
    }

     /** An ++ or -- after or before and expression
      * interface UpdateExpression <: Expression {
     type: "UpdateExpression";
     operator: UpdateOperator;
     argument: Expression;
     prefix: boolean;
   }*/

    void visitUpdateExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        boolean isPrefix = node.get("prefix").asBoolean();
        String operator = node.get("operator").asString();
        String text = this.visitor.getNodeUtil().getTextInSource(node, false);

        if (isPrefix) {
            leaf.registerPrefixExpression(text);
        } else {
            leaf.registerPostfixExpression(text);
        }

        visitor.visitExpression(node.get("argument"), leaf, container);
    }

    public String visitThisExpression(BabelNode node, ILeafFragment parent, IContainer container) {
        parent.registerVariable("this");
        return this.visitor.getNodeUtil().getTextInSource(node, false);
    }
}
