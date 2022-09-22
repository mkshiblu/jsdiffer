package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.Expression;
import io.jsrminer.sourcetree.ObjectCreation;
import io.jsrminer.sourcetree.TernaryOperatorExpression;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import java.util.ArrayList;

public class ExpressionVisitor {

    private final Visitor visitor;
    BabelNodeVisitor<ILeafFragment, Object> assignmentExpressionVisitor = (BabelNode node, ILeafFragment fragment, IContainer container) -> {
        return visitAssignmentExpression(node, fragment, container);
    };

    BabelNodeVisitor<ILeafFragment, Object> updateExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitUpdateExpression(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, Object> unaryExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitUnaryExpression(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, Object> yieldExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitYieldExpression(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, Object> identifierVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitIdentifier(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, Object> thisExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitThisExpression(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, Object> memberExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        visitMemberExpression(node, parent, container);
        return null;
    };

    BabelNodeVisitor<ILeafFragment, Object> binaryExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitBinaryExpression(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, String> logicalExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitLogicalExpression(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, Object> arrayExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitArrayExpression(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, String> conditionalExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitConditionalExpression(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, String> sequenceExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitSequenceExpression(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, Void> typeCastExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitTypeCastExpression(node, parent, container);
    };


    BabelNodeVisitor<ILeafFragment, Void> spreadElementVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitSpreadElement(node, parent, container);
    };

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
        boolean parsedAsDeclaration = false;
        String text = visitor.getNodeUtil().getTextInSource(node, false);
        var operator = node.get("operator").asString();

        // TODO should treated as infix if =?
        if (!"=".equals(operator)) {
            leaf.registerInfixOperator(operator);
            leaf.registerInfixExpression(text);
        }

        var leftNode = node.get("left");
        var rightNode = node.get("right");

        if ((rightNode.getType() == BabelNodeType.CLASS_EXPRESSION
                || rightNode.getType() == BabelNodeType.FUNCTION_EXPRESSION) &&
                leftNode.getText().equals("module.exports")) {

            var id = rightNode.get("id");
            if (id != null) {
                var declarationVisitor = visitor.geDeclarationVisitor();
                if(rightNode.getType() == BabelNodeType.CLASS_EXPRESSION) {
                    declarationVisitor.visitClassDeclaration(rightNode, leaf.getParent(), container);
                }else if(rightNode.getType() == BabelNodeType.FUNCTION_EXPRESSION) {
                    declarationVisitor.visitFunctionDeclaration(rightNode, leaf.getParent(), container);
                }
                parsedAsDeclaration = true;
            }
        }

        if (!parsedAsDeclaration){
            visitor.visitExpression(leftNode, leaf, container);
            visitor.visitExpression(rightNode, leaf, container);
        }
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
            leaf.getArrayAccesses().add(node.getText());
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
    String visitIdentifier(BabelNode node, ILeafFragment leaf, IContainer container) {
        String name = node.getString("name");
        leaf.registerVariable(name);
        return name;
    }

    /**
     * interface YieldExpression <: Expression {
     * type: "YieldExpression";
     * argument: Expression | null;
     * delegate: boolean;
     * }
     */
    String visitYieldExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        String text = this.visitor.getNodeUtil().getTextInSource(node, false);
        var argumentNode = node.get("argument");
        if (argumentNode.isDefined())
            visitor.visitExpression(argumentNode, leaf, container);
        return text;
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
    String visitUnaryExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        boolean isPrefix = node.get("prefix").asBoolean();
        String operator = node.get("operator").asString();
        String text = this.visitor.getNodeUtil().getTextInSource(node, false);

        if (isPrefix) {
            leaf.registerPrefixExpression(text);
        } else {
            leaf.registerPostfixExpression(text);
        }

        visitor.visitExpression(node.get("argument"), leaf, container);

        return text;
    }

    /**
     * interface BinaryExpression<: Expression {
     * type: "BinaryExpression";
     * operator: BinaryOperator;
     * left: Expression;
     * right: Expression;
     * }
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

    /**
     * An ++ or -- after or before and expression
     * interface UpdateExpression <: Expression {
     * type: "UpdateExpression";
     * operator: UpdateOperator;
     * argument: Expression;
     * prefix: boolean;
     * }
     */

    String visitUpdateExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        boolean isPrefix = node.get("prefix").asBoolean();
        String operator = node.get("operator").asString();
        String text = this.visitor.getNodeUtil().getTextInSource(node, false);

        if (isPrefix) {
            leaf.registerPrefixExpression(text);
        } else {
            leaf.registerPostfixExpression(text);
        }

        visitor.visitExpression(node.get("argument"), leaf, container);

        return text;
    }

    String visitThisExpression(BabelNode node, ILeafFragment parent, IContainer container) {
        parent.registerVariable("this");
        return this.visitor.getNodeUtil().getTextInSource(node, false);
    }

    /**
     * LogicalExpression
     * interface LogicalExpression <: Expression {
     * type: "LogicalExpression";
     * operator: LogicalOperator;
     * left: Expression;
     * right: Expression;
     * }
     * A logical operator expression.
     * <p>
     * LogicalOperator
     * enum LogicalOperator {
     * "||" | "&&" | "??"
     * }
     * A logical operator token.
     */
    String visitLogicalExpression(BabelNode node, ILeafFragment parent, IContainer container) {
        var operator = node.getString("operator");
        parent.getInfixOperators().add(operator);
        parent.getInfixExpressions().add(node.getText());
        visitor.visitExpression(node.get("left"), parent, container);
        visitor.visitExpression(node.get("right"), parent, container);
        return node.getText();
    }

    /**
     * interface ConditionalExpression <: Expression {
     * type: "ConditionalExpression";
     * test: Expression;
     * alternate: Expression;
     * consequent: Expression;
     * }
     * A conditional expression, i.e., a ternary ?/: expression.
     */
    String visitConditionalExpression(BabelNode node, ILeafFragment parent, IContainer container) {
        String text = visitor.getNodeUtil().getTextInSource(node, false);
        var testNode = node.get("test");
        var alternateNode = node.get("alternate");
        var consequentNode = node.get("consequent");

        Expression expression = visitor.getNodeUtil().createBaseExpressionWithRMType(testNode, CodeElementType.TERNARY_OPERATOR_CONDITION);
        Expression thenExpression = visitor.getNodeUtil().createBaseExpressionWithRMType(consequentNode, CodeElementType.TERNARY_OPERATOR_THEN_EXPRESSION);
        Expression elseExpression = visitor.getNodeUtil().createBaseExpressionWithRMType(alternateNode, CodeElementType.TERNARY_OPERATOR_ELSE_EXPRESSION);

        TernaryOperatorExpression ternaryOperatorExpression
                = new TernaryOperatorExpression(text, expression, thenExpression, elseExpression);
        parent.getTernaryOperatorExpressions().add(ternaryOperatorExpression);

        visitor.visitExpression(testNode, expression, container);
        visitor.visitExpression(consequentNode, thenExpression, container);
        visitor.visitExpression(alternateNode, elseExpression, container);

        return text;
    }

    /**
     * interface ArrayExpression <: Expression {
     * type: "ArrayExpression";
     * elements: [ Expression | SpreadElement | null ];
     * }
     */
    String visitArrayExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        var elementsNode = node.get("elements");
        var isEmptyArrayCreation = elementsNode.isDefined() && elementsNode.size() == 0;

        if (!isEmptyArrayCreation) {
            String text = node.getText();//visitor.getNodeUtil().getTextInSource(node, false);
            leaf.getArrayAccesses().add(text);

            for (int i = 0; i < elementsNode.size(); i++) {
                visitor.visitExpression(elementsNode.get(i), leaf, container);
            }
        } else {
            // Empty array creation
            ObjectCreation creation = new ObjectCreation();
            creation.setSourceLocation(node.getSourceLocation());
            creation.setText(node.getText());
            creation.setType(CodeElementType.ARRAY_EXPRESSION);
            creation.setFunctionName("");
            leaf.getCreationMap().computeIfAbsent(creation.getText(), key -> new ArrayList<>()).add(creation);
        }

        return node.getText();
    }

    /**
     * interface SequenceExpression <: Expression {
     * type: "SequenceExpression";
     * expressions: [ Expression ];
     * }
     * A sequence expression, i.e., a comma-separated sequence of expressions.
     */
    String visitSequenceExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        var expressions = node.get("expressions");

        for (int i = 0; i < expressions.size(); i++) {
            visitor.visitExpression(expressions.get(i), leaf, container);
        }

        return null;
    }

    Void visitTypeCastExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        visitor.visitExpression(node.get("expression"), leaf, container);
        return null;
    }

    Void visitSpreadElement(BabelNode node, ILeafFragment leaf, IContainer container) {
        visitor.visitExpression(node.get("argument"), leaf, container);
        return null;
    }
}
