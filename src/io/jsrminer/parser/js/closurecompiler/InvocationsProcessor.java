package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.*;
import io.jsrminer.sourcetree.Invocation;
import io.jsrminer.sourcetree.ObjectCreation;
import io.jsrminer.sourcetree.OperationInvocation;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import java.util.ArrayList;

import static io.jsrminer.parser.js.closurecompiler.AstInfoExtractor.*;

public class InvocationsProcessor {

    /**
     * A New expression e.g. new Person("John");
     * has properties operand, arguments and hasTrailingComma
     */
    public static final INodeVisitor<ObjectCreation, NewExpressionTree, ILeafFragment> newExpression
            = new NodeVisitor<>() {
        @Override
        public ObjectCreation visit(NewExpressionTree tree, ILeafFragment leaf, IContainer container) {
            if (tree.hasTrailingComma) {
                throw new RuntimeException("New Expression Tree with trailing comma found" + tree.location.toString());
            }
            String text = getTextInSource(tree);
            final ObjectCreation creation = new ObjectCreation();
            // Add to the list
            leaf.getCreationMap().computeIfAbsent(text, key -> new ArrayList<>()).add(creation);
            processInvocation(tree, leaf, container, creation);
            return creation;
        }
    };

    /**
     * A Call expression e.g. get("John");
     * has properties operand, arguments
     */
    public static final INodeVisitor<OperationInvocation, CallExpressionTree, ILeafFragment> callExpression
            = new NodeVisitor<>() {
        @Override
        public OperationInvocation visit(CallExpressionTree tree, ILeafFragment leaf, IContainer container) {
            String text = getTextInSource(tree);
            final OperationInvocation invocation = new OperationInvocation();
            addOperationInvocation(text, invocation, leaf);
            processInvocation(tree, leaf, container, invocation);
            return invocation;
        }
    };

    static void addOperationInvocation(String text, OperationInvocation invocation, ILeafFragment leaf) {
        // Add to the list
        leaf.getMethodInvocationMap().computeIfAbsent(text, key -> new ArrayList<>()).add(invocation);
    }

    static void processInvocation(ParseTree tree, ILeafFragment leaf
            , IContainer container, Invocation invocation) {
        String text = getTextInSource(tree);
        String name = null;
        String expressionText = null;

        boolean isNewExpression = tree instanceof NewExpressionTree;
        ParseTree callee = isNewExpression ? ((NewExpressionTree) tree).operand : ((CallExpressionTree) tree).operand;

        switch (callee.type) {
            case IDENTIFIER_EXPRESSION:
                name = callee.asIdentifierExpression().identifierToken.value;
                break;

            case MEMBER_EXPRESSION:
                MemberExpressionTree calleeAsMember = callee.asMemberExpression();
                name = calleeAsMember.memberName.value;
                getSubExpression(calleeAsMember.operand, invocation);
                expressionText = getTextInSource(calleeAsMember.operand);

                Visitor.visitExpression(calleeAsMember.operand, leaf, container);
                break;
            case MEMBER_LOOKUP_EXPRESSION:
                //dispatchListeners[i](event, dispatchInstances[i])
                var calleeAsMemberLookupExpression = callee.asMemberLookupExpression();
                // Take remove text before the last "." if any from name
                int lastDotIndex = text.lastIndexOf(".");
                if (lastDotIndex >= 0) {
                    name = text.substring(lastDotIndex + 1, text.length());
                    expressionText = text.substring(0, lastDotIndex);
                } else {
                    name = text;
                }

                getSubExpression(calleeAsMemberLookupExpression.operand, invocation);
                Visitor.visitExpression(calleeAsMemberLookupExpression.operand, leaf, container);
                break;
            case THIS_EXPRESSION:
                name = "this";
                break;
            case FUNCTION_DECLARATION:
                FunctionDeclarationTree functionDeclarationTree = callee.asFunctionDeclaration();
                if (functionDeclarationTree.name != null) {
                    name = functionDeclarationTree.name.value;
                } else {
                    name = AstInfoExtractor.generateNameForAnonymousContainer(container);
                }
                Visitor.visitExpression(callee, leaf, container);
                break;
            case PAREN_EXPRESSION:
                // Can happen with self invoking function such as (function(p1, p2){ })();
                var calleeAsParenExpression = callee.asParenExpression();

                if (calleeAsParenExpression.expression.type == ParseTreeType.FUNCTION_DECLARATION) {
                    name = leaf.getAnonymousFunctionDeclarations().size() + 1 + "";
                } else {
                    throw new RuntimeException("Paren expression except function is not handled: " + tree.location.toString());
                }
                Visitor.visitExpression(calleeAsParenExpression.expression, leaf, container);
                break;
            default:
                throw new RuntimeException("Unsupported CallExpression Operand of type " + callee.type + " at " + callee.location.toString());
        }

        invocation.setText(getTextInSource(tree));
        invocation.setExpressionText(expressionText);
        invocation.setSourceLocation(createSourceLocation(tree));
        invocation.setType(getCodeElementType(tree));
        invocation.setFunctionName(name);

        var arguments = isNewExpression
                ? ((NewExpressionTree) tree).arguments
                : ((CallExpressionTree) tree).arguments;

        if (arguments != null) {
            arguments.arguments.forEach(argumentTree -> {
                processArgument(argumentTree, leaf);
                invocation.getArguments().add(getTextInSource(argumentTree));
                Visitor.visitExpression(argumentTree, leaf, container);
            });
        }
    }

    static void getSubExpression(ParseTree operand, Invocation invocation) {

    }

    static void processArgument(ParseTree argument, ILeafFragment leaf) {
        if (TypeChecker.isIdentifier(argument)
                || TypeChecker.isStringLiteral(argument)
                || TypeChecker.isBooleanLiteral(argument)
                || TypeChecker.isMemberLookupExpression(argument)
                || TypeChecker.isFunctionDeclaration(argument)
                || TypeChecker.isObjectLiteralExpression(argument))
            return;
        leaf.getArguments().add(AstInfoExtractor.getTextInSource(argument));
    }
}
