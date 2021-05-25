package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.Invocation;
import io.jsrminer.sourcetree.ObjectCreation;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import java.util.ArrayList;

public class InvocationVisitor {
    private final Visitor visitor;

    InvocationVisitor(Visitor visitor) {
        this.visitor = visitor;
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

//            case MEMBER_EXPRESSION:
//                MemberExpressionTree calleeAsMember = callee.asMemberExpression();
//                name = calleeAsMember.memberName.value;
//                getSubExpression(calleeAsMember.operand, invocation);
//                expressionText = getTextInSource(calleeAsMember.operand, false);
//
//                io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(calleeAsMember.operand, leaf, container);
//                break;
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
//                io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(calleeAsMemberLookupExpression.operand, leaf, container);
//                io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(calleeAsMemberLookupExpression.memberExpression, leaf, container);
//
//                parsedProperly = false;
//                break;
//            case THIS_EXPRESSION:
//                name = "this";
//                break;
            case FUNCTION_EXPRESSION:
                var idNode = callee.get("id");
                if ( idNode!= null && idNode.isDefined()) {
                    name = idNode.getString("name");
                } else {
                    name = visitor.getNodeUtil().generateNameForAnonymousContainer(container);
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
//            case CALL_EXPRESSION:
////                var calleeAsCallExpression = callee.asCallExpression();
////                if (treatCallExpressionOperandAsTheFunctionName) {
////                    name = getTextInSource(calleeAsCallExpression, false);
////                    if (calleeAsCallExpression.operand != null) {
////                        expressionText = getTextInSource(calleeAsCallExpression.operand, false);
////                    }
////                }
//                io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(callee, leaf, container);
//                parsedProperly = false;
//                break;
//            case MISSING_PRIMARY_EXPRESSION:
//            case TEMPLATE_LITERAL_EXPRESSION:
//                io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(callee, leaf, container);
//                parsedProperly = false;
//                break;
            default:
                throw new RuntimeException("Unsupported CallExpression Operand of type " + callee.getType() + " at " + callee.getSourceLocation().toString());
        }
//
//        invocation.setText(getTextInSource(tree, false));
//        invocation.setExpressionText(expressionText);
//        invocation.setSourceLocation(createSourceLocation(tree));
//        invocation.setType(getCodeElementType(tree));
//        invocation.setFunctionName(name);
//
//        var arguments = isNewExpression
//                ? ((NewExpressionTree) tree).arguments
//                : ((CallExpressionTree) tree).arguments;
//
//        if (arguments != null) {
//            arguments.arguments.forEach(argumentTree -> {
//                processArgument(argumentTree, leaf);
//                invocation.getArguments().add(getTextInSource(argumentTree, false));
//                io.jsrminer.parser.js.closurecompiler.Visitor.visitExpression(argumentTree, leaf, container);
//            });
//        }

        return parsedProperly;
    }

    void getSubExpression(BabelNode operand, Invocation invocation) {

    }

    void processArgument(BabelNode argumentNode, ILeafFragment leaf) {

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
//        if (TypeChecker.isIdentifier(argument)
//                || TypeChecker.isStringLiteral(argument)
//                || TypeChecker.isBooleanLiteral(argument)
//                || TypeChecker.isMemberLookupExpression(argument)
//                || TypeChecker.isFunctionDeclaration(argument)
//                || TypeChecker.isObjectLiteralExpression(argument))
//            return;
        leaf.getArguments().add(visitor.getNodeUtil().getTextInSource(argumentNode, false));
    }
}
