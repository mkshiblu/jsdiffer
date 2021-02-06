package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.*;
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
    public static final INodeVisitor<Void, NewExpressionTree, ILeafFragment> newExpression
            = new NodeVisitor<>() {
        @Override
        public Void visit(NewExpressionTree tree, ILeafFragment leaf, IContainer container) {
            if (tree.hasTrailingComma) {
                throw new RuntimeException("New Expression Tree with trailing comma found" + tree.location.toString());
            }

            String name = null;
            ParseTree callee = tree.operand;
            //TODO expression text
            switch (callee.type) {
                case IDENTIFIER_EXPRESSION:
                    name = callee.asIdentifierExpression().identifierToken.value;
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
//                case MEMBER_EXPRESSION:
//                    Visitor.visitExpression(callee, leaf, container);
//                    break;
                default:
                    throw new RuntimeException("Unsupported NewExpression Operand of type " + callee.type + " at " + callee.location.toString());
            }

            final ObjectCreation creation = new ObjectCreation();
            creation.setText(getTextInSource(tree));
            creation.setSourceLocation(createSourceLocation(tree));
            creation.setType(getCodeElementType(tree));

            //String qualifiedName = generateQualifiedName(name, container);
            creation.setFunctionName(name);
            //creation.setExpressionText();

            // Add to the list
            leaf.getCreationMap().computeIfAbsent(creation.getText(), key -> new ArrayList<>()).add(creation);

            // Visit arguments
            if (tree.arguments != null) {
                tree.arguments.arguments.forEach(argumentTree -> {
                    processArgument(argumentTree, leaf);
                    creation.getArguments().add(getTextInSource(argumentTree));
                    Visitor.visitExpression(argumentTree, leaf, container);
                });
            }

            return null;
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
            String name = null;
            String expressionText = null;
            ParseTree callee = tree.operand;

            final OperationInvocation invocation = new OperationInvocation();
            addOperationInvocation(text, invocation, leaf);

            //TODO expression text
            switch (callee.type) {
                case IDENTIFIER_EXPRESSION:
                    name = callee.asIdentifierExpression().identifierToken.value;
                    break;

                case MEMBER_EXPRESSION:
                    MemberExpressionTree calleeAsMember = callee.asMemberExpression();
                    name = calleeAsMember.memberName.value;
                    processCalleeExpression(calleeAsMember.operand, invocation);
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

                    processCalleeExpression(calleeAsMemberLookupExpression.operand, invocation);
                    Visitor.visitExpression(calleeAsMemberLookupExpression.operand, leaf, container);
                    break;
//                case CALL_EXPRESSION:
//                    var calleeAsCallExpression = callee.asCallExpression();
//
//                    break;
                case PAREN_EXPRESSION:
                    // Can happen with self invoking function such as (function(p1, p2){ })();
                    var calleeAsParenExpression = callee.asParenExpression();
                    Visitor.visitExpression(calleeAsParenExpression.expression, leaf, container);
//                    name = ""; // IT could be the name of the anonymous function
                    break;


                default:
                    throw new RuntimeException("Unsupported CallExpression Operand of type " + callee.type + " at " + callee.location.toString());
            }

            invocation.setText(getTextInSource(tree));
            invocation.setExpressionText(expressionText);
            invocation.setSourceLocation(createSourceLocation(tree));
            invocation.setType(getCodeElementType(tree));
            invocation.setFunctionName(name);

            if (tree.arguments != null) {
                tree.arguments.arguments.forEach(argumentTree -> {
                    processArgument(argumentTree, leaf);
                    invocation.getArguments().add(getTextInSource(argumentTree));
                    Visitor.visitExpression(argumentTree, leaf, container);
                });
            }
            return invocation;
        }
    };

    static void addOperationInvocation(String text, OperationInvocation invocation, ILeafFragment leaf) {
        // Add to the list
        leaf.getMethodInvocationMap().computeIfAbsent(text, key -> new ArrayList<>()).add(invocation);
    }

    static OperationInvocation processCalleeExpression(ParseTree operand, OperationInvocation invocation) {


        return null;
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
