package io.jsrminer.sourcetree;

import java.util.LinkedHashSet;
import java.util.Set;

public class OperationInvocation extends Invocation {
    public OperationInvocation() {

    }

    public boolean differentExpressionNameAndArguments(OperationInvocation other) {
        boolean differentExpression = false;
        if (this.expression == null && other.expression != null)
            differentExpression = true;
        if (this.expression != null && other.expression == null)
            differentExpression = true;
        if (this.expression != null && other.expression != null)
            differentExpression = !this.expression.equals(other.expression) &&
                    !this.expression.startsWith(other.expression) && !other.expression.startsWith(this.expression);
        boolean differentName = !this.equalsInovkedFunctionName(other);
        Set<String> argumentIntersection = new LinkedHashSet<String>(this.arguments);
        argumentIntersection.retainAll(other.arguments);
        boolean argumentFoundInExpression = false;
        if (this.expression != null) {
            for (String argument : other.arguments) {
                if (this.expression.contains(argument)) {
                    argumentFoundInExpression = true;
                }
            }
        }
        if (other.expression != null) {
            for (String argument : this.arguments) {
                if (other.expression.contains(argument)) {
                    argumentFoundInExpression = true;
                }
            }
        }
        boolean differentArguments = !this.arguments.equals(other.arguments) &&
                argumentIntersection.isEmpty() && !argumentFoundInExpression;
        return differentExpression && differentName && differentArguments;
    }

    public boolean matchesOperation(FunctionDeclaration operation/*
            , Map<String, UMLType> variableTypeMap, UMLModelDiff modelDiff*/) {
//        List<UMLType> inferredArgumentTypes = new ArrayList<>();
//
//        for (String arg : this.arguments) {
//            int indexOfOpeningParenthesis = arg.indexOf("(");
//            int indexOfOpeningSquareBracket = arg.indexOf("[");
//            boolean openingParenthesisBeforeSquareBracket = false;
//            boolean openingSquareBracketBeforeParenthesis = false;
//            if (indexOfOpeningParenthesis != -1 && indexOfOpeningSquareBracket != -1) {
//                if (indexOfOpeningParenthesis < indexOfOpeningSquareBracket) {
//                    openingParenthesisBeforeSquareBracket = true;
//                } else if (indexOfOpeningSquareBracket < indexOfOpeningParenthesis) {
//                    openingSquareBracketBeforeParenthesis = true;
//                }
//            } else if (indexOfOpeningParenthesis != -1 && indexOfOpeningSquareBracket == -1) {
//                openingParenthesisBeforeSquareBracket = true;
//            } else if (indexOfOpeningParenthesis == -1 && indexOfOpeningSquareBracket != -1) {
//                openingSquareBracketBeforeParenthesis = true;
//            }
//            if (variableTypeMap.containsKey(arg)) {
//                inferredArgumentTypes.add(variableTypeMap.get(arg));
//            } else if (arg.startsWith("\"") && arg.endsWith("\"")) {
//                inferredArgumentTypes.add(UMLType.extractTypeObject("String"));
//            } else if (arg.startsWith("\'") && arg.endsWith("\'")) {
//                inferredArgumentTypes.add(UMLType.extractTypeObject("char"));
//            } else if (arg.endsWith(".class")) {
//                inferredArgumentTypes.add(UMLType.extractTypeObject("Class"));
//            } else if (arg.equals("true")) {
//                inferredArgumentTypes.add(UMLType.extractTypeObject("boolean"));
//            } else if (arg.equals("false")) {
//                inferredArgumentTypes.add(UMLType.extractTypeObject("boolean"));
//            } else if (arg.startsWith("new ") && arg.contains("(") && openingParenthesisBeforeSquareBracket) {
//                String type = arg.substring(4, arg.indexOf("("));
//                inferredArgumentTypes.add(UMLType.extractTypeObject(type));
//            } else if (arg.startsWith("new ") && arg.contains("[") && openingSquareBracketBeforeParenthesis) {
//                String type = arg.substring(4, arg.indexOf("["));
//                for (int i = 0; i < arg.length(); i++) {
//                    if (arg.charAt(i) == '[') {
//                        type = type + "[]";
//                    } else if (arg.charAt(i) == '\n' || arg.charAt(i) == '{') {
//                        break;
//                    }
//                }
//                inferredArgumentTypes.add(UMLType.extractTypeObject(type));
//            } else if (arg.endsWith(".getClassLoader()")) {
//                inferredArgumentTypes.add(UMLType.extractTypeObject("ClassLoader"));
//            } else if (arg.contains("+") && !arg.contains("++") && !UMLOperationBodyMapper.containsMethodSignatureOfAnonymousClass(arg)) {
//                String[] tokens = arg.split(UMLOperationBodyMapper.SPLIT_CONCAT_STRING_PATTERN);
//                if (tokens[0].startsWith("\"") && tokens[0].endsWith("\"")) {
//                    inferredArgumentTypes.add(UMLType.extractTypeObject("String"));
//                } else {
//                    inferredArgumentTypes.add(null);
//                }
//            } else {
//                inferredArgumentTypes.add(null);
//            }
//        }
//        int i = 0;
//        for (UMLParameter parameter : operation.getParametersWithoutReturnType()) {
//            UMLType parameterType = parameter.getType();
//            if (inferredArgumentTypes.size() > i && inferredArgumentTypes.get(i) != null) {
//                if (!parameterType.getClassType().equals(inferredArgumentTypes.get(i).toString()) &&
//                        !parameterType.toString().equals(inferredArgumentTypes.get(i).toString()) &&
//                        !compatibleTypes(parameter, inferredArgumentTypes.get(i), modelDiff)) {
//                    return false;
//                }
//            }
//            i++;
//        }
        return this.getFunctionName().equals(operation.getName())
                /*&& (this.typeArguments == operation.getParameterTypeList().size() || varArgsMatch(operation))*/;
    }

    @Override
    public boolean identicalName(Invocation call) {
        return getFunctionName().equals(((OperationInvocation)call).getFunctionName());
    }
}
