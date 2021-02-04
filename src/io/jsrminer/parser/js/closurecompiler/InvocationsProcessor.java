package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.CallExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.NewExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
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
    public static final INodeProcessor<Void, NewExpressionTree, ILeafFragment> newExpression
            = new NodeProcessor<>() {
        @Override
        public Void process(NewExpressionTree tree, ILeafFragment leaf, IContainer container) {
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
                case MEMBER_EXPRESSION:
                    Visitor.visitExpression(callee, leaf, container);
                    break;
                default:
                    throw new RuntimeException("Unsupported NewExpression Operand of type " + callee.type + " at " + callee.location.toString());
            }

            final ObjectCreation creation = new ObjectCreation();
            creation.setText(getTextInSource(tree));
            creation.setSourceLocation(createSourceLocation(tree));
            creation.setType(getCodeElementType(tree));
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
    public static final INodeProcessor<Void, CallExpressionTree, ILeafFragment> callExpression
            = new NodeProcessor<>() {
        @Override
        public Void process(CallExpressionTree tree, ILeafFragment leaf, IContainer container) {
            String name = null;
            ParseTree callee = tree.operand;
            //TODO expression text
            switch (callee.type) {
                case IDENTIFIER_EXPRESSION:
                    name = callee.asIdentifierExpression().identifierToken.value;
                    break;
                case FUNCTION_DECLARATION:
                case MEMBER_EXPRESSION:
                    Visitor.visitExpression(callee, leaf, container);
                    break;
                default:
                    throw new RuntimeException("Unsupported NewExpression Operand of type " + callee.type + " at " + callee.location.toString());
            }

            final OperationInvocation invocation = new OperationInvocation();
            invocation.setText(getTextInSource(tree));
            invocation.setSourceLocation(createSourceLocation(tree));
            invocation.setType(getCodeElementType(tree));
            invocation.setFunctionName(name);
            //creation.setExpressionText();

            // Add to the list
            leaf.getMethodInvocationMap().computeIfAbsent(invocation.getText(), key -> new ArrayList<>()).add(invocation);

            if (tree.arguments != null) {
                tree.arguments.arguments.forEach(argumentTree -> {
                    processArgument(argumentTree, leaf);
                    invocation.getArguments().add(getTextInSource(argumentTree));
                    Visitor.visitExpression(argumentTree, leaf, container);
                });
            }
            return null;
        }
    };

    static void processArgument(ParseTree argument, ILeafFragment leaf) {
        if (TypeChecker.isIdentifier(argument)
                || TypeChecker.isCallExpression(argument)
                || TypeChecker.isNewExpression(argument)
                || TypeChecker.isStringLiteral(argument)
                || TypeChecker.isFunctionDeclaration(argument))
            return;

//            if(argument instanceof SuperMethodInvocation ||
//                    argument instanceof Name ||
//                    argument instanceof StringLiteral ||
//                    argument instanceof BooleanLiteral ||
//                    (argument instanceof FieldAccess && ((FieldAccess)argument).getExpression() instanceof ThisExpression) ||
//                    (argument instanceof ArrayAccess && invalidArrayAccess((ArrayAccess)argument)) ||
//                    (argument instanceof InfixExpression && invalidInfix((InfixExpression)argument)))
//                return;
//        if (leaf && (t.isCallExpression(argumentPath.node) || t.isNewExpression(argumentPath.node)
//                || t.isIdentifier(argumentPath.node))
//                || t.isMemberExpression(argumentPath.node)
//                || t.isLiteral(argumentPath.node)
//                || t.isObjectExpression(argumentPath.node)
//                || t.isFunction(argumentPath.node)
//                || t.isClass(argumentPath.node)) {
//            return;
//        }
        leaf.getIdentifierArguments().add(AstInfoExtractor.getTextInSource(argument));
    }
}
