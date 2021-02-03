package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.TokenType;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;

public class TypeChecker {
    public static boolean isIdentifier(ParseTree tree) {
        return tree.type == ParseTreeType.IDENTIFIER_EXPRESSION;
    }

    public static boolean isNewExpression(ParseTree tree) {
        return tree.type == ParseTreeType.NEW_EXPRESSION;
    }

    public static boolean isNewTargetExpression(ParseTree tree) {
        return tree.type == ParseTreeType.NEW_TARGET_EXPRESSION;
    }

    public static boolean isCallExpression(ParseTree tree) {
        return tree.type == ParseTreeType.CALL_EXPRESSION;
    }

    public static boolean isFunctionDeclaration(ParseTree tree) {
        return tree.type == ParseTreeType.FUNCTION_DECLARATION;
    }

    public static boolean isObjectLiteralExpression(ParseTree tree) {
        return tree.type == ParseTreeType.OBJECT_LITERAL_EXPRESSION;
    }

    public static boolean isLiteralExpression(ParseTree tree) {
        return tree.type == ParseTreeType.LITERAL_EXPRESSION;
    }

    public static boolean isStringLiteral(ParseTree tree) {
        return isLiteralExpression(tree)
                && tree.asLiteralExpression().literalToken.type == TokenType.STRING;
    }

//        public static boolean isBooleanLiteral(ParseTree tree) {
//            return isLiteralExpression(tree)
//                    && tree.asLiteralExpression().literalToken.type == TokenType.;
//        }
}

