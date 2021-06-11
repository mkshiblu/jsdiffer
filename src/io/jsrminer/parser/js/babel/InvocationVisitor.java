package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.Invocation;
import io.jsrminer.sourcetree.ObjectCreation;
import io.jsrminer.sourcetree.OperationInvocation;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import java.util.ArrayList;

import static io.jsrminer.parser.js.babel.BabelParserConfig.treatCallExpressionOperandAsTheFunctionName;

public class InvocationVisitor {
    private final Visitor visitor;

    BabelNodeVisitor<ILeafFragment, Object> newExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitNewExpression(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, Object> callExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitCallExpression(node, parent, container);
    };

    InvocationVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * interface CallExpression <: Expression {
     * type: "CallExpression";
     * callee: Expression | Super | Import;
     * arguments: [ Expression | SpreadElement ];
     * optional: boolean | null;
     * }
     */
    OperationInvocation visitCallExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        String text = visitor.getNodeUtil().getTextInSource(node, false);
        final var invocation = new OperationInvocation();
        // Add to the list
        leaf.getMethodInvocationMap().computeIfAbsent(text, key -> new ArrayList<>()).add(invocation);
        boolean success = processInvocation(node, leaf, container, invocation);
        if (!success) {
            leaf.getMethodInvocationMap().get(text).remove(invocation);
            if (leaf.getMethodInvocationMap().get(text).size() == 0) {
                leaf.getMethodInvocationMap().remove(text);
            }
        }
        return invocation;
    }

    /**
     * interface NewExpression <: CallExpression {
     * type: "NewExpression";
     * optional: boolean | null;
     * }
     */
    ObjectCreation visitNewExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        String text = visitor.getNodeUtil().getTextInSource(node, false);
        final ObjectCreation creation = new ObjectCreation();
        // Add to the list
        leaf.getCreationMap().computeIfAbsent(text, key -> new ArrayList<>()).add(creation);
        boolean success = processInvocation(node, leaf, container, creation);
        if (!success) {
            leaf.getCreationMap().get(text).remove(creation);
            if (leaf.getCreationMap().get(text).size() == 0) {
                leaf.getCreationMap().remove(text);
            }
        }

        return creation;
    }

    boolean processInvocation(BabelNode node, ILeafFragment leaf
            , IContainer container, Invocation invocation) {
        String text = visitor.getNodeUtil().getTextInSource(node, false);

        String name = null;
        String expressionText = null;
        boolean parsedProperly = true;

        var callee = node.get("callee");
        String calleeText = visitor.getNodeUtil().getTextInSource(callee, false);

        switch (callee.getType()) {
            case IDENTIFIER:
                name = callee.getString("name");
                break;

            case MEMBER_EXPRESSION:
                // computed: a[b], static: a.b
                boolean isComputed = callee.get("computed").asBoolean();
                var objectNode = callee.get("object");
                var propertyNode = callee.get("property");

                if (isComputed) {
                    //x.a[b]
                    // Take remove text before the last "." if any from name
                    //String str = calleeText.replaceAll()
                    int lastDotIndex = calleeText.lastIndexOf(".");
                    if (lastDotIndex >= 0) {
                        name = calleeText.substring(lastDotIndex + 1, calleeText.length());
                        expressionText = calleeText.substring(0, lastDotIndex);
                    } else {
                        name = calleeText;
                    }

                    getSubExpression(objectNode, invocation);
                    visitor.visitExpression(callee, leaf, container);
                    //visitor.visitExpression(objectNode, leaf, container);
                    //visitor.visitExpression(propertyNode, leaf, container);

                    parsedProperly = false;
                } else {
                    // a.b
                    //Property is identifier?
                    name = propertyNode.getString("name");
                    expressionText = objectNode.getText();
                    getSubExpression(objectNode, invocation);
                    visitor.visitExpression(objectNode, leaf, container);
                }

                break;
//            case MEMBER_LOOKUP_EXPRESSION:
//                //dispatchListeners[i](event, dispatchInstances[i])
//                var calleeAsMemberLookupExpression = callee.asMemberLookupExpression();
//                // Take remove text before the last "." if any from name
//                //String str = calleeText.replaceAll()
//                int lastDotIndex = calleeText.lastIndexOf(".");
//                if (lastDotIndex >= 0) {
//                    name = calleeText.substring(lastDotIndex + 1, calleeText.length());
//                    expressionText = calleeText.substring(0, lastDotIndex);
//                } else {
//                    name = calleeText;
//                }
//
//                getSubExpression(calleeAsMemberLookupExpression.operand, invocation);
//                visitor.visitExpression(calleeAsMemberLookupExpression.operand, leaf, container);
//                visitor.visitExpression(calleeAsMemberLookupExpression.memberExpression, leaf, container);
//
//                parsedProperly = false;
//                break;
//            case THIS_EXPRESSION:
//                name = "this";
//                break;
            case FUNCTION_EXPRESSION:
                var idNode = callee.get("id");
                if (idNode != null && idNode.isDefined()) {
                    name = idNode.getString("name");
                } else {
                    name = visitor.getNodeUtil().generateNameForAnonymousFunction(container);
                }
                visitor.visitExpression(callee, leaf, container);
                break;
//            case PAREN_EXPRESSION:
//                // Can happen with self invoking function such as (function(p1, p2){ })();
//                var calleeAsParenExpression = callee.asParenExpression();
//
//                if (calleeAsParenExpression.expression.type == ParseTreeType.FUNCTION_DECLARATION) {
//                    name = leaf.getAnonymousFunctionDeclarations().size() + 1 + "";
//                } else {
//                    parsedProperly = false;
//                    //throw new RuntimeException("Paren expression except function is not handled: " + tree.location.toString());
//                }
//                io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(calleeAsParenExpression.expression, leaf, container);
//                break;
//
            case CALL_EXPRESSION:
                BabelNode operand = null;
                if (treatCallExpressionOperandAsTheFunctionName) {
                    name = visitor.getNodeUtil().getTextInSource(callee, false);
                    operand = callee.get("callee");
                    if (operand.isDefined()) {
                        expressionText = visitor.getNodeUtil().getTextInSource(operand, false);
                    }
                }
                visitor.visitExpression(callee, leaf, container);
                parsedProperly = operand != null && operand.isDefined() && operand.getType() == BabelNodeType.IDENTIFIER;
                break;
            case LOGICAL_EXPRESSION:
                visitor.visitExpression(callee, leaf, container);
                parsedProperly = false;
                break;
//            case TEMPLATE_LITERAL_EXPRESSION:
//                io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(callee, leaf, container);
//                parsedProperly = false;
//                break;
            default:
                throw new RuntimeException("Unsupported CallExpression Operand of type " + callee.getType() + " at " + callee.getSourceLocation().toString());
        }

        invocation.setText(this.visitor.getNodeUtil().getTextInSource(node, false));
        invocation.setExpressionText(expressionText);
        invocation.setSourceLocation(node.getSourceLocation());
        invocation.setType(this.visitor.getNodeUtil().getCodeElementTypeFromBabelNodeType(node.getType()));
        invocation.setFunctionName(name);

        // Parse the arguments
        var argumentsNode = node.get("arguments");
        for (int i = 0; i < argumentsNode.size(); i++) {
            var argumentNode = argumentsNode.get(i);
            registerArgument(argumentNode, leaf);
            invocation.getArguments().add(visitor.getNodeUtil().getTextInSource(argumentNode, false));
            visitor.visitExpression(argumentNode, leaf, container);
        }

        return parsedProperly;
    }

    void getSubExpression(BabelNode operand, Invocation invocation) {

    }

    void registerArgument(BabelNode argumentNode, ILeafFragment leaf) {

        switch (argumentNode.getType()) {
            case IDENTIFIER:
            case STRING_LITERAL:
            case BOOLEAN_LITERAL:
            case MEMBER_EXPRESSION:
            case FUNCTION_DECLARATION:
            case FUNCTION_EXPRESSION:
            case OBJECT_EXPRESSION:
                return;
        }
        leaf.getArguments().add(visitor.getNodeUtil().getTextInSource(argumentNode, false));
    }
}
