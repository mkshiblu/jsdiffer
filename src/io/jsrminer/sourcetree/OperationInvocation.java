package io.jsrminer.sourcetree;

import io.jsrminer.uml.diff.StringDistance;
import io.jsrminer.uml.mapping.replacement.PrefixSuffixUtils;

import java.util.*;

public class OperationInvocation extends Invocation {
    private List<String> subExpressions = new ArrayList<>();

    public OperationInvocation() {

    }
    public double normalizedNameDistance(Invocation call) {
        String s1 = getFunctionName().toLowerCase();
        String s2 = ((OperationInvocation) call).getFunctionName().toLowerCase();
        int distance = StringDistance.editDistance(s1, s2);
        double normalized = (double) distance / (double) Math.max(s1.length(), s2.length());
        return normalized;
    }

    public boolean differentExpressionNameAndArguments(OperationInvocation other) {
        boolean differentExpression = false;
        if (this.expressionText == null && other.expressionText != null)
            differentExpression = true;
        if (this.expressionText != null && other.expressionText == null)
            differentExpression = true;
        if (this.expressionText != null && other.expressionText != null)
            differentExpression = !this.expressionText.equals(other.expressionText) &&
                    !this.expressionText.startsWith(other.expressionText) && !other.expressionText.startsWith(this.expressionText);
        boolean differentName = !this.equalsInovkedFunctionName(other);
        Set<String> argumentIntersection = new LinkedHashSet<String>(this.arguments);
        argumentIntersection.retainAll(other.arguments);
        boolean argumentFoundInExpression = false;
        if (this.expressionText != null) {
            for (String argument : other.arguments) {
                if (this.expressionText.contains(argument)) {
                    argumentFoundInExpression = true;
                }
            }
        }
        if (other.expressionText != null) {
            for (String argument : this.arguments) {
                if (other.expressionText.contains(argument)) {
                    argumentFoundInExpression = true;
                }
            }
        }
        boolean differentArguments = !this.arguments.equals(other.arguments) &&
                argumentIntersection.isEmpty() && !argumentFoundInExpression;
        return differentExpression && differentName && differentArguments;
    }

//    public boolean identicalWithExpressionCallChainDifference(OperationInvocation other) {
//        Set<String> subExpressionIntersection = subExpressionIntersection(other);
//        return identicalName(other) &&
//                equalArguments(other) &&
//                subExpressionIntersection.size() > 0 &&
//                (subExpressionIntersection.size() == this.subExpressions().size() ||
//                        subExpressionIntersection.size() == other.subExpressions().size());
//    }
//
//    private Set<String> subExpressionIntersection(OperationInvocation other) {
//        Set<String> subExpressions1 = this.subExpressions();
//        Set<String> subExpressions2 = other.subExpressions();
//        Set<String> intersection = new LinkedHashSet<String>(subExpressions1);
//        intersection.retainAll(subExpressions2);
//        if (subExpressions1.size() == subExpressions2.size()) {
//            Iterator<String> it1 = subExpressions1.iterator();
//            Iterator<String> it2 = subExpressions2.iterator();
//            while (it1.hasNext()) {
//                String subExpression1 = it1.next();
//                String subExpression2 = it2.next();
//                if (!intersection.contains(subExpression1) && differInThisDot(subExpression1, subExpression2)) {
//                    intersection.add(subExpression1);
//                }
//            }
//        }
//        return intersection;
//    }
//


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
        return getFunctionName().equals(((OperationInvocation) call).getFunctionName());
    }

//    public List<String> getSubExpressions() {
//        return subExpressions;
//    }

    public int numberOfSubExpressions() {
        return subExpressions.size();
    }

    public boolean identicalWithExpressionCallChainDifference(OperationInvocation other) {
        Set<String> subExpressionIntersection = subExpressionIntersection(other);
        return identicalName(other) &&
                equalArguments(other) &&
                subExpressionIntersection.size() > 0 &&
                (subExpressionIntersection.size() == this.subExpressions().size() ||
                        subExpressionIntersection.size() == other.subExpressions().size());
    }

    private Set<String> subExpressionIntersection(OperationInvocation other) {
        Set<String> subExpressions1 = this.subExpressions();
        Set<String> subExpressions2 = other.subExpressions();
        Set<String> intersection = new LinkedHashSet<String>(subExpressions1);
        intersection.retainAll(subExpressions2);
        if (subExpressions1.size() == subExpressions2.size()) {
            Iterator<String> it1 = subExpressions1.iterator();
            Iterator<String> it2 = subExpressions2.iterator();
            while (it1.hasNext()) {
                String subExpression1 = it1.next();
                String subExpression2 = it2.next();
                if (!intersection.contains(subExpression1)
                        && differInThisDot(subExpression1, subExpression2)) {
                    intersection.add(subExpression1);
                }
            }
        }
        return intersection;
    }

    private Set<String> subExpressions() {
        Set<String> subExpressions = new LinkedHashSet<>(this.subExpressions);
        String thisExpression = this.expressionText;
        if (thisExpression != null) {
            if (thisExpression.contains(".")) {
                int indexOfDot = thisExpression.indexOf(".");
                String subString = thisExpression.substring(0, indexOfDot);
                if (!subExpressions.contains(subString) && !dotInsideArguments(indexOfDot, thisExpression)) {
                    subExpressions.add(subString);
                }
            } else if (!subExpressions.contains(thisExpression)) {
                subExpressions.add(thisExpression);
            }
        }
        return subExpressions;
    }

    private static boolean dotInsideArguments(int indexOfDot, String thisExpression) {
        boolean openingParenthesisFound = false;
        for (int i = indexOfDot; i >= 0; i--) {
            if (thisExpression.charAt(i) == '(') {
                openingParenthesisFound = true;
                break;
            }
        }
        boolean closingParenthesisFound = false;
        for (int i = indexOfDot; i < thisExpression.length(); i++) {
            if (thisExpression.charAt(i) == ')') {
                closingParenthesisFound = true;
                break;
            }
        }
        return openingParenthesisFound && closingParenthesisFound;
    }

    private static boolean differInThisDot(String subExpression1, String subExpression2) {
        if (subExpression1.length() < subExpression2.length()) {
            String modified = subExpression1;
            String previousCommonPrefix = "";
            String commonPrefix = null;
            while ((commonPrefix = PrefixSuffixUtils.longestCommonPrefix(modified, subExpression2)).length() > previousCommonPrefix.length()) {
                modified = commonPrefix + "this." + modified.substring(commonPrefix.length(), modified.length());
                if (modified.equals(subExpression2)) {
                    return true;
                }
                previousCommonPrefix = commonPrefix;
            }
        } else if (subExpression1.length() > subExpression2.length()) {
            String modified = subExpression2;
            String previousCommonPrefix = "";
            String commonPrefix = null;
            while ((commonPrefix = PrefixSuffixUtils.longestCommonPrefix(modified, subExpression1)).length() > previousCommonPrefix.length()) {
                modified = commonPrefix + "this." + modified.substring(commonPrefix.length(), modified.length());
                if (modified.equals(subExpression1)) {
                    return true;
                }
                previousCommonPrefix = commonPrefix;
            }
        }
        return false;
    }
}
