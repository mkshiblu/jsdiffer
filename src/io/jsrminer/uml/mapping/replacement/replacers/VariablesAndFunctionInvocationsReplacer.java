package io.jsrminer.uml.mapping.replacement.replacers;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.diff.StringDistance;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.replacement.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class VariablesAndFunctionInvocationsReplacer {

    public void replaceVariablesAndFunctionInvocations(CodeFragment statement1, CodeFragment statement2
            , Map<String, String> parameterToArgumentMap, ReplacementInfo replacementInfo
            , Set<String> unmatchedVariables1, Set<String> unmatchedVariables2
            , Map<String, List<? extends Invocation>> methodInvocationMap1
            , Map<String, List<? extends Invocation>> methodInvocationMap2
            , Set<String> functionInvocations1, Set<String> functionInvocations2
            , Set<String> variablesAndMethodInvocations1, Set<String> variablesAndMethodInvocations2
            , Set<CodeFragmentMapping> bodyMapperMappings
            , FunctionDeclaration function2) {

        // If statements are not matched yet

        for (String s1 : variablesAndMethodInvocations1) {
            TreeMap<Double, Replacement> replacementMap = new TreeMap<>();
            int minDistance = replacementInfo.getRawEditDistance();

            // Try replacing?
            for (String s2 : variablesAndMethodInvocations2) {

                String temp = ReplacementUtil.performReplacement(replacementInfo.getArgumentizedString1(), replacementInfo.getArgumentizedString2(), s1, s2);
                int distanceRaw = StringDistance.editDistance(temp, replacementInfo.getArgumentizedString2(), minDistance);

                boolean multipleInstances = ReplacementUtil.countInstances(temp, s2) > 1;
                if (distanceRaw == -1 && multipleInstances) {
                    distanceRaw = StringDistance.editDistance(temp, replacementInfo.getArgumentizedString2());
                }

                boolean multipleInstanceRule = multipleInstances && Math.abs(s1.length() - s2.length()) == Math.abs(distanceRaw - minDistance) && !s1.equals(s2);
                if (distanceRaw >= 0 && (distanceRaw < replacementInfo.getRawEditDistance() || multipleInstanceRule)) {
                    minDistance = distanceRaw;
                    Replacement replacement = null;
                    if (unmatchedVariables1.contains(s1) && unmatchedVariables2.contains(s2)
                            && variablesStartWithSameCase(s1, s2, parameterToArgumentMap)) {
                        replacement = new Replacement(s1, s2, ReplacementType.VARIABLE_NAME);
                        if (s1.startsWith("(") && s2.startsWith("(") && s1.contains(")") && s2.contains(")")) {
                            String prefix1 = s1.substring(0, s1.indexOf(")") + 1);
                            String prefix2 = s2.substring(0, s2.indexOf(")") + 1);
                            if (prefix1.equals(prefix2)) {
                                String suffix1 = s1.substring(prefix1.length(), s1.length());
                                String suffix2 = s2.substring(prefix2.length(), s2.length());
                                replacement = new Replacement(suffix1, suffix2, ReplacementType.VARIABLE_NAME);
                            }
                        }
                        VariableDeclaration v1 = statement1.findVariableDeclarationIncludingParent(s1);
                        VariableDeclaration v2 = statement2.findVariableDeclarationIncludingParent(s2);
                        if (inconsistentVariableMappingCount(statement1, statement2, v1, v2, bodyMapperMappings) > 1
                                && function2.loopWithVariables(v1.variableName, v2.variableName) == null) {
                            replacement = null;
                        }

                    } else if (unmatchedVariables1.contains(s1) && functionInvocations2.contains(s2)) {
                        OperationInvocation invokedOperationAfter = (OperationInvocation) methodInvocationMap2.get(s2).get(0);
                        replacement = new VariableReplacementWithMethodInvocation(s1, s2, invokedOperationAfter, VariableReplacementWithMethodInvocation.Direction.VARIABLE_TO_INVOCATION);
                    } else if (functionInvocations1.contains(s1) && functionInvocations2.contains(s2)) {
                        OperationInvocation invokedOperationBefore = (OperationInvocation) methodInvocationMap1.get(s1).get(0);
                        OperationInvocation invokedOperationAfter = (OperationInvocation) methodInvocationMap2.get(s2).get(0);

//                        if (invokedOperationBefore.compatibleExpression(invokedOperationAfter)) {
//                            replacement = new MethodInvocationReplacement(s1, s2, invokedOperationBefore, invokedOperationAfter, ReplacementType.METHOD_INVOCATION);
//                        }

                    } else if (functionInvocations1.contains(s1) && unmatchedVariables2.contains(s2)) {
                        OperationInvocation invokedOperationBefore = (OperationInvocation) methodInvocationMap1.get(s1).get(0);
                        replacement = new VariableReplacementWithMethodInvocation(s1, s2, invokedOperationBefore, VariableReplacementWithMethodInvocation.Direction.INVOCATION_TO_VARIABLE);
                    }
                    if (replacement != null) {
                        double distancenormalized = (double) distanceRaw / (double) Math.max(temp.length(), replacementInfo.getArgumentizedString2().length());
                        replacementMap.put(distancenormalized, replacement);
                    }
                    if (distanceRaw == 0 && !replacementInfo.getReplacements().isEmpty()) {
                        break;
                    }
                }
            }
            if (!replacementMap.isEmpty()) {
                Replacement replacement = replacementMap.firstEntry().getValue();
                replacementInfo.addReplacement(replacement);
                replacementInfo.setArgumentizedString1(ReplacementUtil.performReplacement(replacementInfo.getArgumentizedString1(), replacementInfo.getArgumentizedString2(), replacement.getBefore(), replacement.getAfter()));
                if (replacementMap.firstEntry().getKey() == 0) {
                    break;
                }
            }
        }
    }

    private boolean variablesStartWithSameCase(String s1, String s2, Map<String, String> parameterToArgumentMap) {
        if (parameterToArgumentMap.values().contains(s2)) {
            return true;
        }
        if (s1.length() > 0 && s2.length() > 0) {
            if (Character.isUpperCase(s1.charAt(0)) && Character.isUpperCase(s2.charAt(0)))
                return true;
            if (Character.isLowerCase(s1.charAt(0)) && Character.isLowerCase(s2.charAt(0)))
                return true;
            if (s1.charAt(0) == '_' && s2.charAt(0) == '_')
                return true;
            if (s1.charAt(0) == '(' || s2.charAt(0) == '(')
                return true;
        }
        return false;
    }

    private int inconsistentVariableMappingCount(CodeFragment statement1, CodeFragment statement2
            , VariableDeclaration v1, VariableDeclaration v2, Set<CodeFragmentMapping> bodyMapperMappings) {
        int count = 0;
        if (v1 != null && v2 != null) {
            for (CodeFragmentMapping mapping : bodyMapperMappings) {
                List<VariableDeclaration> variableDeclarations1 = mapping.fragment1.getVariableDeclarations();
                List<VariableDeclaration> variableDeclarations2 = mapping.fragment2.getVariableDeclarations();
                if (variableDeclarations1.contains(v1) &&
                        variableDeclarations2.size() > 0 &&
                        !variableDeclarations2.contains(v2)) {
                    count++;
                }
                if (variableDeclarations2.contains(v2) &&
                        variableDeclarations1.size() > 0 &&
                        !variableDeclarations1.contains(v1)) {
                    count++;
                }
                if (mapping.isExact()) {
                    boolean containsMapping = true;
                    if (statement1 instanceof BlockStatement
                            && statement2 instanceof BlockStatement &&
                            statement1.getCodeElementType().equals(CodeElementType.ENHANCED_FOR_STATEMENT)) {
                        BlockStatement comp1 = (BlockStatement) statement1;
                        BlockStatement comp2 = (BlockStatement) statement2;
                        containsMapping = comp1.containsFragment(mapping.fragment1) && comp2.containsFragment(mapping.fragment2);
                    }

                    // TODO revisit
                    if (containsMapping && (
                            VariableReplacementAnalysis.bothFragmentsUseVariable(v1, mapping)
                                    || VariableReplacementAnalysis.bothFragmentsUseVariable(v2, mapping))) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
