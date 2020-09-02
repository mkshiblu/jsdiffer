package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.CodeFragment;
import io.jsrminer.sourcetree.Expression;
import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.uml.diff.StringDistance;

import java.util.*;

public class ReplacementFinder {
    private String argumentizedString1;
    private String argumentizedString2;
    private int rawDistance;
    private Set<Replacement> replacements;
    final List<? extends CodeFragment> unMatchedStatements1;
    final List<? extends CodeFragment> unMatchedStatements2;

    public ReplacementFinder(String argumentizedString1, String argumentizedString2,
                             List<? extends CodeFragment> unMatchedStatements1, List<? extends CodeFragment> unMatchedStatements2) {
        setArgumentizedString1(argumentizedString1);
        this.argumentizedString2 = argumentizedString2;
        this.unMatchedStatements1 = unMatchedStatements1;
        this.unMatchedStatements2 = unMatchedStatements2;
        this.replacements = new LinkedHashSet<Replacement>();
    }

    public String getArgumentizedString1() {
        return argumentizedString1;
    }

    public String getArgumentizedString2() {
        return argumentizedString2;
    }

    public void setArgumentizedString1(String string) {
        this.argumentizedString1 = string;
        this.rawDistance = StringDistance.editDistance(this.argumentizedString1, this.argumentizedString2);
    }

    public int getRawDistance() {
        return rawDistance;
    }

    public void addReplacement(Replacement r) {
        this.replacements.add(r);
    }

    public void addReplacements(Set<Replacement> replacementsToBeAdded) {
        this.replacements.addAll(replacementsToBeAdded);
    }

    public void removeReplacements(Set<Replacement> replacementsToBeRemoved) {
        this.replacements.removeAll(replacementsToBeRemoved);
    }

    public Set<Replacement> getReplacements() {
        return replacements;
    }

    public List<Replacement> getReplacements(Replacement.ReplacementType type) {
        List<Replacement> replacements = new ArrayList<>();
        for (Replacement replacement : this.replacements) {
            if (replacement.getType().equals(type)) {
                replacements.add(replacement);
            }
        }
        return replacements;
    }

    public Set<Replacement> findReplacementsWithExactMatching(SingleStatement statement1, SingleStatement statement2, Map<String, String> parameterToArgumentMap) {

        List<VariableDeclaration> variableDeclarations1 = new ArrayList<>(statement1.getVariableDeclarations());
        List<VariableDeclaration> variableDeclarations2 = new ArrayList<>(statement2.getVariableDeclarations());

        final VariableDeclaration variableDeclarationWithArrayInitializer1 = findDeclarationWithArrayInitializer(variableDeclarations1);
        final VariableDeclaration variableDeclarationWithArrayInitializer2 = findDeclarationWithArrayInitializer(variableDeclarations2);

        OperationInvocation invocationCoveringTheEntireStatement1 = statement1.invocationCoveringEntireFragment();
        OperationInvocation invocationCoveringTheEntireStatement2 = statement2.invocationCoveringEntireFragment();
//
//        // Get a copu of variables
//        Set<String> variables1 = new LinkedHashSet<>(statement1.getVariables());
//        Set<String> variables2 = new LinkedHashSet<>(statement2.getVariables());
//        Set<String> variableIntersection = new LinkedHashSet<>(variables1);
//
//        variableIntersection.retainAll(variables2);
//
//
//        // ignore the variables in the intersection that also appear with "this." prefix in the sets of variables
//        // ignore the variables in the intersection that are static fields
//        Set<String> variablesToBeRemovedFromTheIntersection = new LinkedHashSet<String>();
//        for(String variable : variableIntersection) {
//            if(!variable.startsWith("this.") && !variableIntersection.contains("this."+variable) &&
//                    (variables1.contains("this."+variable) || variables2.contains("this."+variable))) {
//                variablesToBeRemovedFromTheIntersection.add(variable);
//            }
//            if(invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
//                    invocationCoveringTheEntireStatement1.identicalName(invocationCoveringTheEntireStatement2)) {
//                if(!invocationCoveringTheEntireStatement1.getArguments().contains(variable) &&
//                        invocationCoveringTheEntireStatement2.getArguments().contains(variable)) {
//                    for(String argument : invocationCoveringTheEntireStatement1.getArguments()) {
//                        String argumentNoWhiteSpace = argument.replaceAll("\\s","");
//                        if(argument.contains(variable) && !argument.equals(variable) && !argumentNoWhiteSpace.contains("+" + variable + "+") &&
//                                !argumentNoWhiteSpace.contains(variable + "+") && !argumentNoWhiteSpace.contains("+" + variable) &&
//                                !nonMatchedStatementUsesVariableInArgument(replacementInfo.statements1, variable, argument)) {
//                            variablesToBeRemovedFromTheIntersection.add(variable);
//                        }
//                    }
//                }
//                else if(invocationCoveringTheEntireStatement1.getArguments().contains(variable) &&
//                        !invocationCoveringTheEntireStatement2.getArguments().contains(variable)) {
//                    for(String argument : invocationCoveringTheEntireStatement2.getArguments()) {
//                        String argumentNoWhiteSpace = argument.replaceAll("\\s","");
//                        if(argument.contains(variable) && !argument.equals(variable) && !argumentNoWhiteSpace.contains("+" + variable + "+") &&
//                                !argumentNoWhiteSpace.contains(variable + "+") && !argumentNoWhiteSpace.contains("+" + variable) &&
//                                !nonMatchedStatementUsesVariableInArgument(replacementInfo.statements2, variable, argument)) {
//                            variablesToBeRemovedFromTheIntersection.add(variable);
//                        }
//                    }
//                }
//            }
//            if(variable.toUpperCase().equals(variable) && !ReplacementUtil.sameCharsBeforeAfter(statement1.getString(), statement2.getString(), variable)) {
//                variablesToBeRemovedFromTheIntersection.add(variable);
//            }
//        }
//
//        variableIntersection.removeAll(variablesToBeRemovedFromTheIntersection);
//
//        // remove common variables from the two sets
//        variables1.removeAll(variableIntersection);
//        variables2.removeAll(variableIntersection);
//
//        // replace variables with the corresponding arguments
//        replaceVariablesWithArguments(variables1, parameterToArgumentMap);
//        replaceVariablesWithArguments(variables2, parameterToArgumentMap);
//
//        Map<String, List<? extends AbstractCall>> methodInvocationMap1 = new LinkedHashMap<String, List<? extends AbstractCall>>(statement1.getMethodInvocationMap());
//        Map<String, List<? extends AbstractCall>> methodInvocationMap2 = new LinkedHashMap<String, List<? extends AbstractCall>>(statement2.getMethodInvocationMap());
//        Set<String> methodInvocations1 = new LinkedHashSet<String>(methodInvocationMap1.keySet());
//        Set<String> methodInvocations2 = new LinkedHashSet<String>(methodInvocationMap2.keySet());
//
//        Map<String, List<? extends AbstractCall>> creationMap1 = new LinkedHashMap<String, List<? extends AbstractCall>>(statement1.getCreationMap());
//        Map<String, List<? extends AbstractCall>> creationMap2 = new LinkedHashMap<String, List<? extends AbstractCall>>(statement2.getCreationMap());
//        Set<String> creations1 = new LinkedHashSet<String>(creationMap1.keySet());
//        Set<String> creations2 = new LinkedHashSet<String>(creationMap2.keySet());
//
//        Set<String> arguments1 = new LinkedHashSet<String>(statement1.getArguments());
//        Set<String> arguments2 = new LinkedHashSet<String>(statement2.getArguments());
//        removeCommonElements(arguments1, arguments2);
//
//        if(!argumentsWithIdenticalMethodCalls(arguments1, arguments2, variables1, variables2)) {
//            findReplacements(arguments1, variables2, replacementInfo, ReplacementType.ARGUMENT_REPLACED_WITH_VARIABLE);
//        }
//
//        Map<String, String> map = new LinkedHashMap<String, String>();
//        Set<Replacement> replacementsToBeRemoved = new LinkedHashSet<Replacement>();
//        Set<Replacement> replacementsToBeAdded = new LinkedHashSet<Replacement>();
//        for(Replacement r : replacementInfo.getReplacements()) {
//            map.put(r.getBefore(), r.getAfter());
//            if(methodInvocationMap1.containsKey(r.getBefore())) {
//                Replacement replacement = new VariableReplacementWithMethodInvocation(r.getBefore(), r.getAfter(), (OperationInvocation)methodInvocationMap1.get(r.getBefore()).get(0), Direction.INVOCATION_TO_VARIABLE);
//                replacementsToBeAdded.add(replacement);
//                replacementsToBeRemoved.add(r);
//            }
//        }
//        replacementInfo.getReplacements().removeAll(replacementsToBeRemoved);
//        replacementInfo.getReplacements().addAll(replacementsToBeAdded);
//
//        // replace variables with the corresponding arguments in method invocations
//        replaceVariablesWithArguments(methodInvocationMap1, methodInvocations1, parameterToArgumentMap);
//        replaceVariablesWithArguments(methodInvocationMap2, methodInvocations2, parameterToArgumentMap);
//
//        replaceVariablesWithArguments(methodInvocationMap1, methodInvocations1, map);
//
//        //remove methodInvocation covering the entire statement
//        if(invocationCoveringTheEntireStatement1 != null) {
//            for(String methodInvocation1 : methodInvocationMap1.keySet()) {
//                for(AbstractCall call : methodInvocationMap1.get(methodInvocation1)) {
//                    if(invocationCoveringTheEntireStatement1.getLocationInfo().equals(call.getLocationInfo())) {
//                        methodInvocations1.remove(methodInvocation1);
//                    }
//                }
//            }
//        }
//        if(invocationCoveringTheEntireStatement2 != null) {
//            for(String methodInvocation2 : methodInvocationMap2.keySet()) {
//                for(AbstractCall call : methodInvocationMap2.get(methodInvocation2)) {
//                    if(invocationCoveringTheEntireStatement2.getLocationInfo().equals(call.getLocationInfo())) {
//                        methodInvocations2.remove(methodInvocation2);
//                    }
//                }
//            }
//        }
//        Set<String> methodInvocationIntersection = new LinkedHashSet<String>(methodInvocations1);
//        methodInvocationIntersection.retainAll(methodInvocations2);
//        Set<String> methodInvocationsToBeRemovedFromTheIntersection = new LinkedHashSet<String>();
//        for(String methodInvocation : methodInvocationIntersection) {
//            if(invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
//                    invocationCoveringTheEntireStatement1.identicalName(invocationCoveringTheEntireStatement2)) {
//                if(!invocationCoveringTheEntireStatement1.getArguments().contains(methodInvocation) &&
//                        invocationCoveringTheEntireStatement2.getArguments().contains(methodInvocation)) {
//                    methodInvocationsToBeRemovedFromTheIntersection.add(methodInvocation);
//                }
//                else if(invocationCoveringTheEntireStatement1.getArguments().contains(methodInvocation) &&
//                        !invocationCoveringTheEntireStatement2.getArguments().contains(methodInvocation)) {
//                    methodInvocationsToBeRemovedFromTheIntersection.add(methodInvocation);
//                }
//            }
//        }
//        methodInvocationIntersection.removeAll(methodInvocationsToBeRemovedFromTheIntersection);
//        // remove common methodInvocations from the two sets
//        methodInvocations1.removeAll(methodInvocationIntersection);
//        methodInvocations2.removeAll(methodInvocationIntersection);
//
//        Set<String> variablesAndMethodInvocations1 = new LinkedHashSet<String>();
//        //variablesAndMethodInvocations1.addAll(methodInvocations1);
//        //variablesAndMethodInvocations1.addAll(variables1);
//
//        Set<String> variablesAndMethodInvocations2 = new LinkedHashSet<String>();
//        variablesAndMethodInvocations2.addAll(methodInvocations2);
//        variablesAndMethodInvocations2.addAll(variables2);
//
//        Set<String> types1 = new LinkedHashSet<String>(statement1.getTypes());
//        Set<String> types2 = new LinkedHashSet<String>(statement2.getTypes());
//        removeCommonTypes(types1, types2, statement1.getTypes(), statement2.getTypes());
//
//        // replace variables with the corresponding arguments in object creations
//        replaceVariablesWithArguments(creationMap1, creations1, parameterToArgumentMap);
//        replaceVariablesWithArguments(creationMap2, creations2, parameterToArgumentMap);
//
//        replaceVariablesWithArguments(creationMap1, creations1, map);
//
//        ObjectCreation creationCoveringTheEntireStatement1 = statement1.creationCoveringEntireFragment();
//        ObjectCreation creationCoveringTheEntireStatement2 = statement2.creationCoveringEntireFragment();
//        //remove objectCreation covering the entire statement
//        for(String objectCreation1 : creationMap1.keySet()) {
//            for(AbstractCall creation1 : creationMap1.get(objectCreation1)) {
//                if(creationCoveringTheEntireStatement1 != null &&
//                        creationCoveringTheEntireStatement1.getLocationInfo().equals(creation1.getLocationInfo())) {
//                    creations1.remove(objectCreation1);
//                }
//                if(((ObjectCreation)creation1).getAnonymousClassDeclaration() != null) {
//                    creations1.remove(objectCreation1);
//                }
//            }
//        }
//        for(String objectCreation2 : creationMap2.keySet()) {
//            for(AbstractCall creation2 : creationMap2.get(objectCreation2)) {
//                if(creationCoveringTheEntireStatement2 != null &&
//                        creationCoveringTheEntireStatement2.getLocationInfo().equals(creation2.getLocationInfo())) {
//                    creations2.remove(objectCreation2);
//                }
//                if(((ObjectCreation)creation2).getAnonymousClassDeclaration() != null) {
//                    creations2.remove(objectCreation2);
//                }
//            }
//        }
//        Set<String> creationIntersection = new LinkedHashSet<String>(creations1);
//        creationIntersection.retainAll(creations2);
//        // remove common creations from the two sets
//        creations1.removeAll(creationIntersection);
//        creations2.removeAll(creationIntersection);
//
//        Set<String> stringLiterals1 = new LinkedHashSet<String>(statement1.getStringLiterals());
//        Set<String> stringLiterals2 = new LinkedHashSet<String>(statement2.getStringLiterals());
//        removeCommonElements(stringLiterals1, stringLiterals2);
//
//        Set<String> numberLiterals1 = new LinkedHashSet<String>(statement1.getNumberLiterals());
//        Set<String> numberLiterals2 = new LinkedHashSet<String>(statement2.getNumberLiterals());
//        removeCommonElements(numberLiterals1, numberLiterals2);
//
//        Set<String> booleanLiterals1 = new LinkedHashSet<String>(statement1.getBooleanLiterals());
//        Set<String> booleanLiterals2 = new LinkedHashSet<String>(statement2.getBooleanLiterals());
//        removeCommonElements(booleanLiterals1, booleanLiterals2);
//
//        Set<String> infixOperators1 = new LinkedHashSet<String>(statement1.getInfixOperators());
//        Set<String> infixOperators2 = new LinkedHashSet<String>(statement2.getInfixOperators());
//        removeCommonElements(infixOperators1, infixOperators2);
//
//        Set<String> arrayAccesses1 = new LinkedHashSet<String>(statement1.getArrayAccesses());
//        Set<String> arrayAccesses2 = new LinkedHashSet<String>(statement2.getArrayAccesses());
//        removeCommonElements(arrayAccesses1, arrayAccesses2);
//
//        Set<String> prefixExpressions1 = new LinkedHashSet<String>(statement1.getPrefixExpressions());
//        Set<String> prefixExpressions2 = new LinkedHashSet<String>(statement2.getPrefixExpressions());
//        removeCommonElements(prefixExpressions1, prefixExpressions2);
//
//        //perform type replacements
//        findReplacements(types1, types2, replacementInfo, ReplacementType.TYPE);
//
//        //perform operator replacements
//        findReplacements(infixOperators1, infixOperators2, replacementInfo, ReplacementType.INFIX_OPERATOR);
//
//        //apply existing replacements on method invocations
//        for(String methodInvocation1 : methodInvocations1) {
//            String temp = new String(methodInvocation1);
//            for(Replacement replacement : replacementInfo.getReplacements()) {
//                temp = ReplacementUtil.performReplacement(temp, replacement.getBefore(), replacement.getAfter());
//            }
//            if(!temp.equals(methodInvocation1)) {
//                variablesAndMethodInvocations1.add(temp);
//                methodInvocationMap1.put(temp, methodInvocationMap1.get(methodInvocation1));
//            }
//        }
//        //add updated method invocation to the original list of invocations
//        methodInvocations1.addAll(variablesAndMethodInvocations1);
//        variablesAndMethodInvocations1.addAll(methodInvocations1);
//        variablesAndMethodInvocations1.addAll(variables1);
//
//        if (replacementInfo.getRawDistance() > 0) {
//            for(String s1 : variablesAndMethodInvocations1) {
//                TreeMap<Double, Replacement> replacementMap = new TreeMap<Double, Replacement>();
//                int minDistance = replacementInfo.getRawDistance();
//                for(String s2 : variablesAndMethodInvocations2) {
//                    if(Thread.interrupted()) {
//                        throw new RefactoringMinerTimedOutException();
//                    }
//                    String temp = ReplacementUtil.performReplacement(replacementInfo.getArgumentizedString1(), replacementInfo.getArgumentizedString2(), s1, s2);
//                    int distanceRaw = StringDistance.editDistance(temp, replacementInfo.getArgumentizedString2(), minDistance);
//                    boolean multipleInstances = ReplacementUtil.countInstances(temp, s2) > 1;
//                    if(distanceRaw == -1 && multipleInstances) {
//                        distanceRaw = StringDistance.editDistance(temp, replacementInfo.getArgumentizedString2());
//                    }
//                    boolean multipleInstanceRule = multipleInstances && Math.abs(s1.length() - s2.length()) == Math.abs(distanceRaw - minDistance) && !s1.equals(s2);
//                    if(distanceRaw >= 0 && (distanceRaw < replacementInfo.getRawDistance() || multipleInstanceRule)) {
//                        minDistance = distanceRaw;
//                        Replacement replacement = null;
//                        if(variables1.contains(s1) && variables2.contains(s2) && variablesStartWithSameCase(s1, s2, parameterToArgumentMap)) {
//                            replacement = new Replacement(s1, s2, ReplacementType.VARIABLE_NAME);
//                            if(s1.startsWith("(") && s2.startsWith("(") && s1.contains(")") && s2.contains(")")) {
//                                String prefix1 = s1.substring(0, s1.indexOf(")")+1);
//                                String prefix2 = s2.substring(0, s2.indexOf(")")+1);
//                                if(prefix1.equals(prefix2)) {
//                                    String suffix1 = s1.substring(prefix1.length(), s1.length());
//                                    String suffix2 = s2.substring(prefix2.length(), s2.length());
//                                    replacement = new Replacement(suffix1, suffix2, ReplacementType.VARIABLE_NAME);
//                                }
//                            }
//                            VariableDeclaration v1 = statement1.searchVariableDeclaration(s1);
//                            VariableDeclaration v2 = statement2.searchVariableDeclaration(s2);
//                            if(inconsistentVariableMappingCount(statement1, statement2, v1, v2) > 1 && operation2.loopWithVariables(v1.getVariableName(), v2.getVariableName()) == null) {
//                                replacement = null;
//                            }
//                        }
//                        else if(variables1.contains(s1) && methodInvocations2.contains(s2)) {
//                            OperationInvocation invokedOperationAfter = (OperationInvocation) methodInvocationMap2.get(s2).get(0);
//                            replacement = new VariableReplacementWithMethodInvocation(s1, s2, invokedOperationAfter, Direction.VARIABLE_TO_INVOCATION);
//                        }
//                        else if(methodInvocations1.contains(s1) && methodInvocations2.contains(s2)) {
//                            OperationInvocation invokedOperationBefore = (OperationInvocation) methodInvocationMap1.get(s1).get(0);
//                            OperationInvocation invokedOperationAfter = (OperationInvocation) methodInvocationMap2.get(s2).get(0);
//                            if(invokedOperationBefore.compatibleExpression(invokedOperationAfter)) {
//                                replacement = new MethodInvocationReplacement(s1, s2, invokedOperationBefore, invokedOperationAfter, ReplacementType.METHOD_INVOCATION);
//                            }
//                        }
//                        else if(methodInvocations1.contains(s1) && variables2.contains(s2)) {
//                            OperationInvocation invokedOperationBefore = (OperationInvocation) methodInvocationMap1.get(s1).get(0);
//                            replacement = new VariableReplacementWithMethodInvocation(s1, s2, invokedOperationBefore, Direction.INVOCATION_TO_VARIABLE);
//                        }
//                        if(replacement != null) {
//                            double distancenormalized = (double)distanceRaw/(double)Math.max(temp.length(), replacementInfo.getArgumentizedString2().length());
//                            replacementMap.put(distancenormalized, replacement);
//                        }
//                        if(distanceRaw == 0 && !replacementInfo.getReplacements().isEmpty()) {
//                            break;
//                        }
//                    }
//                }
//                if(!replacementMap.isEmpty()) {
//                    Replacement replacement = replacementMap.firstEntry().getValue();
//                    replacementInfo.addReplacement(replacement);
//                    replacementInfo.setArgumentizedString1(ReplacementUtil.performReplacement(replacementInfo.getArgumentizedString1(), replacementInfo.getArgumentizedString2(), replacement.getBefore(), replacement.getAfter()));
//                    if(replacementMap.firstEntry().getKey() == 0) {
//                        break;
//                    }
//                }
//            }
//        }
//
//        //perform creation replacements
//        findReplacements(creations1, creations2, replacementInfo, ReplacementType.CLASS_INSTANCE_CREATION);
//
//        //perform literal replacements
//        findReplacements(stringLiterals1, stringLiterals2, replacementInfo, ReplacementType.STRING_LITERAL);
//        findReplacements(numberLiterals1, numberLiterals2, replacementInfo, ReplacementType.NUMBER_LITERAL);
//        if(!statement1.containsInitializerOfVariableDeclaration(numberLiterals1) && !statement2.containsInitializerOfVariableDeclaration(variables2) &&
//                !statement1.getString().endsWith("=0;\n")) {
//            findReplacements(numberLiterals1, variables2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_NUMBER_LITERAL);
//        }
//        findReplacements(variables1, arrayAccesses2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_ARRAY_ACCESS);
//        findReplacements(arrayAccesses1, variables2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_ARRAY_ACCESS);
//
//        findReplacements(methodInvocations1, arrayAccesses2, replacementInfo, ReplacementType.ARRAY_ACCESS_REPLACED_WITH_METHOD_INVOCATION);
//        findReplacements(arrayAccesses1, methodInvocations2, replacementInfo, ReplacementType.ARRAY_ACCESS_REPLACED_WITH_METHOD_INVOCATION);
//
//        findReplacements(variables1, prefixExpressions2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_PREFIX_EXPRESSION);
//        findReplacements(prefixExpressions1, variables2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_PREFIX_EXPRESSION);
//        findReplacements(stringLiterals1, variables2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_STRING_LITERAL);
//        if(statement1.getNullLiterals().isEmpty() && !statement2.getNullLiterals().isEmpty()) {
//            Set<String> nullLiterals2 = new LinkedHashSet<String>();
//            nullLiterals2.add("null");
//            findReplacements(variables1, nullLiterals2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_NULL_LITERAL);
//        }
//
//        if(statement1.getTernaryOperatorExpressions().isEmpty() && !statement2.getTernaryOperatorExpressions().isEmpty()) {
//            if(!statement1.getNullLiterals().isEmpty()) {
//                Set<String> nullLiterals1 = new LinkedHashSet<String>();
//                nullLiterals1.add("null");
//                Set<String> ternaryExpressions2 = new LinkedHashSet<String>();
//                for(TernaryOperatorExpression ternary : statement2.getTernaryOperatorExpressions()) {
//                    ternaryExpressions2.add(ternary.getExpression());
//                }
//                findReplacements(nullLiterals1, ternaryExpressions2, replacementInfo, ReplacementType.NULL_LITERAL_REPLACED_WITH_CONDITIONAL_EXPRESSION);
//            }
//        }
//        else if(!statement1.getTernaryOperatorExpressions().isEmpty() && statement2.getTernaryOperatorExpressions().isEmpty()) {
//            if(!statement2.getNullLiterals().isEmpty()) {
//                Set<String> nullLiterals2 = new LinkedHashSet<String>();
//                nullLiterals2.add("null");
//                Set<String> ternaryExpressions1 = new LinkedHashSet<String>();
//                for(TernaryOperatorExpression ternary : statement1.getTernaryOperatorExpressions()) {
//                    ternaryExpressions1.add(ternary.getExpression());
//                }
//                findReplacements(ternaryExpressions1, nullLiterals2, replacementInfo, ReplacementType.NULL_LITERAL_REPLACED_WITH_CONDITIONAL_EXPRESSION);
//            }
//        }
//        if(!statement1.getString().endsWith("=true;\n") && !statement1.getString().endsWith("=false;\n")) {
//            findReplacements(booleanLiterals1, variables2, replacementInfo, ReplacementType.BOOLEAN_REPLACED_WITH_VARIABLE);
//        }
//        if(!statement2.getString().endsWith("=true;\n") && !statement2.getString().endsWith("=false;\n")) {
//            findReplacements(arguments1, booleanLiterals2, replacementInfo, ReplacementType.BOOLEAN_REPLACED_WITH_ARGUMENT);
//        }
//
//        String s1 = preprocessInput1(statement1, statement2);
//        String s2 = preprocessInput2(statement1, statement2);
//        replacementsToBeRemoved = new LinkedHashSet<Replacement>();
//        replacementsToBeAdded = new LinkedHashSet<Replacement>();
//        for(Replacement replacement : replacementInfo.getReplacements()) {
//            s1 = ReplacementUtil.performReplacement(s1, s2, replacement.getBefore(), replacement.getAfter());
//            //find variable replacements within method invocation replacements
//            Set<Replacement> set = replacementsWithinMethodInvocations(replacement.getBefore(), replacement.getAfter(), variables1, methodInvocations2, methodInvocationMap2, Direction.VARIABLE_TO_INVOCATION);
//            set.addAll(replacementsWithinMethodInvocations(replacement.getBefore(), replacement.getAfter(), methodInvocations1, variables2, methodInvocationMap1, Direction.INVOCATION_TO_VARIABLE));
//            if(!set.isEmpty()) {
//                replacementsToBeRemoved.add(replacement);
//                replacementsToBeAdded.addAll(set);
//            }
//            Replacement r = variableReplacementWithinMethodInvocations(replacement.getBefore(), replacement.getAfter(), variables1, variables2);
//            if(r != null) {
//                replacementsToBeRemoved.add(replacement);
//                replacementsToBeAdded.add(r);
//            }
//            Replacement r2 = variableReplacementWithinMethodInvocations(replacement.getBefore(), replacement.getAfter(), stringLiterals1, variables2);
//            if(r2 != null) {
//                replacementsToBeRemoved.add(replacement);
//                replacementsToBeAdded.add(r2);
//            }
//        }
//        replacementInfo.removeReplacements(replacementsToBeRemoved);
//        replacementInfo.addReplacements(replacementsToBeAdded);
//        boolean isEqualWithReplacement = s1.equals(s2) || replacementInfo.argumentizedString1.equals(replacementInfo.argumentizedString2) || differOnlyInCastExpressionOrPrefixOperator(s1, s2, replacementInfo) || oneIsVariableDeclarationTheOtherIsVariableAssignment(s1, s2, replacementInfo) ||
//                oneIsVariableDeclarationTheOtherIsReturnStatement(s1, s2) || oneIsVariableDeclarationTheOtherIsReturnStatement(statement1.getString(), statement2.getString()) ||
//                (commonConditional(s1, s2, replacementInfo) && containsValidOperatorReplacements(replacementInfo)) ||
//                equalAfterArgumentMerge(s1, s2, replacementInfo) ||
//                equalAfterNewArgumentAdditions(s1, s2, replacementInfo) ||
//                (validStatementForConcatComparison(statement1, statement2) && commonConcat(s1, s2, replacementInfo));
//        List<AnonymousClassDeclarationObject> anonymousClassDeclarations1 = statement1.getAnonymousClassDeclarations();
//        List<AnonymousClassDeclarationObject> anonymousClassDeclarations2 = statement2.getAnonymousClassDeclarations();
//        if(isEqualWithReplacement) {
//            List<Replacement> typeReplacements = replacementInfo.getReplacements(ReplacementType.TYPE);
//            if(typeReplacements.size() > 0 && invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null) {
//                for(Replacement typeReplacement : typeReplacements) {
//                    if(invocationCoveringTheEntireStatement1.getMethodName().contains(typeReplacement.getBefore()) && invocationCoveringTheEntireStatement2.getMethodName().contains(typeReplacement.getAfter())) {
//                        if(invocationCoveringTheEntireStatement1.identicalExpression(invocationCoveringTheEntireStatement2) && invocationCoveringTheEntireStatement1.equalArguments(invocationCoveringTheEntireStatement2)) {
//                            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.getName(),
//                                    invocationCoveringTheEntireStatement2.getName(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_NAME);
//                            replacementInfo.addReplacement(replacement);
//                        }
//                        else {
//                            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
//                                    invocationCoveringTheEntireStatement2.actualString(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION);
//                            replacementInfo.addReplacement(replacement);
//                        }
//                        break;
//                    }
//                }
//            }
//            if(variableDeclarationsWithEverythingReplaced(variableDeclarations1, variableDeclarations2, replacementInfo) &&
//                    !statement1.getLocationInfo().getCodeElementType().equals(CodeElementType.ENHANCED_FOR_STATEMENT) &&
//                    !statement2.getLocationInfo().getCodeElementType().equals(CodeElementType.ENHANCED_FOR_STATEMENT)) {
//                return null;
//            }
//            if(variableAssignmentWithEverythingReplaced(statement1, statement2, replacementInfo)) {
//                return null;
//            }
//            if(classInstanceCreationWithEverythingReplaced(statement1, statement2, replacementInfo, parameterToArgumentMap)) {
//                return null;
//            }
//            if(!anonymousClassDeclarations1.isEmpty() && !anonymousClassDeclarations2.isEmpty()) {
//                Set<Replacement> replacementsInsideAnonymous = new LinkedHashSet<Replacement>();
//                for(Replacement replacement : replacementInfo.getReplacements()) {
//                    if(replacement instanceof MethodInvocationReplacement) {
//                        for(int i=0; i<anonymousClassDeclarations1.size(); i++) {
//                            for(int j=0; j<anonymousClassDeclarations2.size(); j++) {
//                                AnonymousClassDeclarationObject anonymousClassDeclaration1 = anonymousClassDeclarations1.get(i);
//                                AnonymousClassDeclarationObject anonymousClassDeclaration2 = anonymousClassDeclarations2.get(j);
//                                if(anonymousClassDeclaration1.getMethodInvocationMap().containsKey(replacement.getBefore()) &&
//                                        anonymousClassDeclaration2.getMethodInvocationMap().containsKey(replacement.getAfter())) {
//                                    replacementsInsideAnonymous.add(replacement);
//                                    break;
//                                }
//                            }
//                            if(replacementsInsideAnonymous.contains(replacement)) {
//                                break;
//                            }
//                        }
//                    }
//                }
//                for(Replacement replacement : replacementsInsideAnonymous) {
//                    equalAfterNewArgumentAdditions(replacement.getBefore(), replacement.getAfter(), replacementInfo);
//                }
//            }
//            return replacementInfo.getReplacements();
//        }
//        if(!anonymousClassDeclarations1.isEmpty() && !anonymousClassDeclarations2.isEmpty()) {
//            for(int i=0; i<anonymousClassDeclarations1.size(); i++) {
//                for(int j=0; j<anonymousClassDeclarations2.size(); j++) {
//                    AnonymousClassDeclarationObject anonymousClassDeclaration1 = anonymousClassDeclarations1.get(i);
//                    AnonymousClassDeclarationObject anonymousClassDeclaration2 = anonymousClassDeclarations2.get(j);
//                    String statementWithoutAnonymous1 = statementWithoutAnonymous(statement1, anonymousClassDeclaration1, operation1);
//                    String statementWithoutAnonymous2 = statementWithoutAnonymous(statement2, anonymousClassDeclaration2, operation2);
//                    if(statementWithoutAnonymous1.equals(statementWithoutAnonymous2) ||
//                            identicalAfterVariableAndTypeReplacements(statementWithoutAnonymous1, statementWithoutAnonymous2, replacementInfo.getReplacements()) ||
//                            (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
//                                    (invocationCoveringTheEntireStatement1.identicalWithMergedArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements()) ||
//                                            invocationCoveringTheEntireStatement1.identicalWithDifferentNumberOfArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements(), parameterToArgumentMap)))) {
//                        UMLAnonymousClass anonymousClass1 = findAnonymousClass(anonymousClassDeclaration1, operation1);
//                        UMLAnonymousClass anonymousClass2 = findAnonymousClass(anonymousClassDeclaration2, operation2);
//                        int matchedOperations = 0;
//                        for(UMLOperation operation1 : anonymousClass1.getOperations()) {
//                            for(UMLOperation operation2 : anonymousClass2.getOperations()) {
//                                if(operation1.equals(operation2) || operation1.equalSignature(operation2) || operation1.equalSignatureWithIdenticalNameIgnoringChangedTypes(operation2)) {
//                                    UMLOperationBodyMapper mapper = new UMLOperationBodyMapper(operation1, operation2, classDiff);
//                                    int mappings = mapper.mappingsWithoutBlocks();
//                                    if(mappings > 0) {
//                                        int nonMappedElementsT1 = mapper.nonMappedElementsT1();
//                                        int nonMappedElementsT2 = mapper.nonMappedElementsT2();
//                                        if(mappings > nonMappedElementsT1 && mappings > nonMappedElementsT2) {
//                                            this.mappings.addAll(mapper.mappings);
//                                            this.nonMappedInnerNodesT1.addAll(mapper.nonMappedInnerNodesT1);
//                                            this.nonMappedInnerNodesT2.addAll(mapper.nonMappedInnerNodesT2);
//                                            this.nonMappedLeavesT1.addAll(mapper.nonMappedLeavesT1);
//                                            this.nonMappedLeavesT2.addAll(mapper.nonMappedLeavesT2);
//                                            matchedOperations++;
//                                            UMLOperationDiff operationDiff = new UMLOperationDiff(operation1, operation2, mapper.mappings);
//                                            this.refactorings.addAll(mapper.getRefactorings());
//                                            this.refactorings.addAll(operationDiff.getRefactorings());
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        if(matchedOperations > 0) {
//                            Replacement replacement = new Replacement(anonymousClassDeclaration1.toString(), anonymousClassDeclaration2.toString(), ReplacementType.ANONYMOUS_CLASS_DECLARATION);
//                            replacementInfo.addReplacement(replacement);
//                            return replacementInfo.getReplacements();
//                        }
//                    }
//                }
//            }
//        }
//        List<LambdaExpressionObject> lambdas1 = statement1.getLambdas();
//        List<LambdaExpressionObject> lambdas2 = statement2.getLambdas();
//        List<UMLOperationBodyMapper> lambdaMappers = new ArrayList<UMLOperationBodyMapper>();
//        if(!lambdas1.isEmpty() && !lambdas2.isEmpty()) {
//            for(int i=0; i<lambdas1.size(); i++) {
//                for(int j=0; j<lambdas2.size(); j++) {
//                    LambdaExpressionObject lambda1 = lambdas1.get(i);
//                    LambdaExpressionObject lambda2 = lambdas2.get(j);
//                    UMLOperationBodyMapper mapper = new UMLOperationBodyMapper(lambda1, lambda2, this);
//                    int mappings = mapper.mappingsWithoutBlocks();
//                    if(mappings > 0) {
//                        int nonMappedElementsT1 = mapper.nonMappedElementsT1();
//                        int nonMappedElementsT2 = mapper.nonMappedElementsT2();
//                        if(mappings > nonMappedElementsT1 && mappings > nonMappedElementsT2) {
//                            this.mappings.addAll(mapper.mappings);
//                            this.nonMappedInnerNodesT1.addAll(mapper.nonMappedInnerNodesT1);
//                            this.nonMappedInnerNodesT2.addAll(mapper.nonMappedInnerNodesT2);
//                            this.nonMappedLeavesT1.addAll(mapper.nonMappedLeavesT1);
//                            this.nonMappedLeavesT2.addAll(mapper.nonMappedLeavesT2);
//                            this.refactorings.addAll(mapper.getRefactorings());
//                            lambdaMappers.add(mapper);
//                        }
//                    }
//                }
//            }
//        }
//        OperationInvocation assignmentInvocationCoveringTheEntireStatement1 = invocationCoveringTheEntireStatement1 == null ? statement1.assignmentInvocationCoveringEntireStatement() : invocationCoveringTheEntireStatement1;
//        //method invocation is identical
//        if(assignmentInvocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null) {
//            for(String key1 : methodInvocationMap1.keySet()) {
//                for(AbstractCall invocation1 : methodInvocationMap1.get(key1)) {
//                    if(invocation1.identical(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements()) &&
//                            !assignmentInvocationCoveringTheEntireStatement1.getArguments().contains(key1)) {
//                        String expression1 = assignmentInvocationCoveringTheEntireStatement1.getExpression();
//                        if(expression1 == null || !expression1.contains(key1)) {
//                            return replacementInfo.getReplacements();
//                        }
//                    }
//                    else if(invocation1.identicalName(invocationCoveringTheEntireStatement2) && invocation1.equalArguments(invocationCoveringTheEntireStatement2) &&
//                            !assignmentInvocationCoveringTheEntireStatement1.getArguments().contains(key1) && invocationCoveringTheEntireStatement2.getExpression() != null) {
//                        boolean expressionMatched = false;
//                        Set<AbstractCodeFragment> additionallyMatchedStatements2 = new LinkedHashSet<AbstractCodeFragment>();
//                        for(AbstractCodeFragment codeFragment : replacementInfo.statements2) {
//                            VariableDeclaration variableDeclaration = codeFragment.getVariableDeclaration(invocationCoveringTheEntireStatement2.getExpression());
//                            OperationInvocation invocationCoveringEntireCodeFragment = codeFragment.invocationCoveringEntireFragment();
//                            if(variableDeclaration != null && variableDeclaration.getInitializer() != null && invocation1.getExpression() != null && invocation1.getExpression().equals(variableDeclaration.getInitializer().getString())) {
//                                Replacement r = new Replacement(invocation1.getExpression(), variableDeclaration.getVariableName(), ReplacementType.VARIABLE_REPLACED_WITH_EXPRESSION_OF_METHOD_INVOCATION);
//                                replacementInfo.getReplacements().add(r);
//                                additionallyMatchedStatements2.add(codeFragment);
//                                expressionMatched = true;
//                            }
//                            if(invocationCoveringEntireCodeFragment != null && assignmentInvocationCoveringTheEntireStatement1.identicalName(invocationCoveringEntireCodeFragment) &&
//                                    assignmentInvocationCoveringTheEntireStatement1.equalArguments(invocationCoveringEntireCodeFragment)) {
//                                additionallyMatchedStatements2.add(codeFragment);
//                            }
//                        }
//                        if(expressionMatched) {
//                            if(additionallyMatchedStatements2.size() > 0) {
//                                Replacement r = new CompositeReplacement(statement1.getString(), statement2.getString(), new LinkedHashSet<AbstractCodeFragment>(), additionallyMatchedStatements2);
//                                replacementInfo.getReplacements().add(r);
//                            }
//                            return replacementInfo.getReplacements();
//                        }
//                    }
//                }
//            }
//        }
//        //method invocation is identical with a difference in the expression call chain
//        if(invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null) {
//            if(invocationCoveringTheEntireStatement1.identicalWithExpressionCallChainDifference(invocationCoveringTheEntireStatement2)) {
//                List<? extends AbstractCall> invokedOperationsBefore = methodInvocationMap1.get(invocationCoveringTheEntireStatement1.getExpression());
//                List<? extends AbstractCall> invokedOperationsAfter = methodInvocationMap2.get(invocationCoveringTheEntireStatement2.getExpression());
//                if(invokedOperationsBefore != null && invokedOperationsBefore.size() > 0 && invokedOperationsAfter != null && invokedOperationsAfter.size() > 0) {
//                    OperationInvocation invokedOperationBefore = (OperationInvocation)invokedOperationsBefore.get(0);
//                    OperationInvocation invokedOperationAfter = (OperationInvocation)invokedOperationsAfter.get(0);
//                    Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.getExpression(), invocationCoveringTheEntireStatement2.getExpression(), invokedOperationBefore, invokedOperationAfter, ReplacementType.METHOD_INVOCATION_EXPRESSION);
//                    replacementInfo.addReplacement(replacement);
//                    return replacementInfo.getReplacements();
//                }
//                else if(invokedOperationsBefore != null && invokedOperationsBefore.size() > 0) {
//                    OperationInvocation invokedOperationBefore = (OperationInvocation)invokedOperationsBefore.get(0);
//                    Replacement replacement = new VariableReplacementWithMethodInvocation(invocationCoveringTheEntireStatement1.getExpression(), invocationCoveringTheEntireStatement2.getExpression(), invokedOperationBefore, Direction.INVOCATION_TO_VARIABLE);
//                    replacementInfo.addReplacement(replacement);
//                    return replacementInfo.getReplacements();
//                }
//                else if(invokedOperationsAfter != null && invokedOperationsAfter.size() > 0) {
//                    OperationInvocation invokedOperationAfter = (OperationInvocation)invokedOperationsAfter.get(0);
//                    Replacement replacement = new VariableReplacementWithMethodInvocation(invocationCoveringTheEntireStatement1.getExpression(), invocationCoveringTheEntireStatement2.getExpression(), invokedOperationAfter, Direction.VARIABLE_TO_INVOCATION);
//                    replacementInfo.addReplacement(replacement);
//                    return replacementInfo.getReplacements();
//                }
//                if(invocationCoveringTheEntireStatement1.numberOfSubExpressions() == invocationCoveringTheEntireStatement2.numberOfSubExpressions() &&
//                        invocationCoveringTheEntireStatement1.getExpression().contains(".") == invocationCoveringTheEntireStatement2.getExpression().contains(".")) {
//                    return replacementInfo.getReplacements();
//                }
//            }
//        }
//        //method invocation is identical if arguments are replaced
//        if(invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
//                invocationCoveringTheEntireStatement1.identicalExpression(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements()) &&
//                invocationCoveringTheEntireStatement1.identicalName(invocationCoveringTheEntireStatement2) ) {
//            for(String key : methodInvocationMap2.keySet()) {
//                for(AbstractCall invocation2 : methodInvocationMap2.get(key)) {
//                    if(invocationCoveringTheEntireStatement1.identicalOrReplacedArguments(invocation2, replacementInfo.getReplacements())) {
//                        return replacementInfo.getReplacements();
//                    }
//                }
//            }
//        }
//        //method invocation is identical if arguments are wrapped or concatenated
//        if(invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
//                invocationCoveringTheEntireStatement1.identicalExpression(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements()) &&
//                invocationCoveringTheEntireStatement1.identicalName(invocationCoveringTheEntireStatement2) ) {
//            for(String key : methodInvocationMap2.keySet()) {
//                for(AbstractCall invocation2 : methodInvocationMap2.get(key)) {
//                    if(invocationCoveringTheEntireStatement1.identicalOrWrappedArguments(invocation2)) {
//                        Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
//                                invocationCoveringTheEntireStatement2.actualString(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_ARGUMENT_WRAPPED);
//                        replacementInfo.addReplacement(replacement);
//                        return replacementInfo.getReplacements();
//                    }
//                    if(invocationCoveringTheEntireStatement1.identicalOrConcatenatedArguments(invocation2)) {
//                        Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
//                                invocationCoveringTheEntireStatement2.actualString(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_ARGUMENT_CONCATENATED);
//                        replacementInfo.addReplacement(replacement);
//                        return replacementInfo.getReplacements();
//                    }
//                }
//            }
//        }
//        //method invocation has been renamed but the expression and arguments are identical
//        if(invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
//                invocationCoveringTheEntireStatement1.renamedWithIdenticalExpressionAndArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements(), UMLClassBaseDiff.MAX_OPERATION_NAME_DISTANCE)) {
//            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.getName(),
//                    invocationCoveringTheEntireStatement2.getName(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_NAME);
//            replacementInfo.addReplacement(replacement);
//            return replacementInfo.getReplacements();
//        }
//        //method invocation has been renamed but the expressions are null and arguments are identical
//        if(invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
//                invocationCoveringTheEntireStatement1.renamedWithIdenticalArgumentsAndNoExpression(invocationCoveringTheEntireStatement2, UMLClassBaseDiff.MAX_OPERATION_NAME_DISTANCE, lambdaMappers)) {
//            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.getName(),
//                    invocationCoveringTheEntireStatement2.getName(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_NAME);
//            replacementInfo.addReplacement(replacement);
//            return replacementInfo.getReplacements();
//        }
//        //method invocation has been renamed (one name contains the other), one expression is null, but the other is not null, and arguments are identical
//        if(invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
//                invocationCoveringTheEntireStatement1.renamedWithDifferentExpressionAndIdenticalArguments(invocationCoveringTheEntireStatement2)) {
//            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
//                    invocationCoveringTheEntireStatement2.actualString(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_NAME_AND_EXPRESSION);
//            replacementInfo.addReplacement(replacement);
//            return replacementInfo.getReplacements();
//        }
//        //method invocation has been renamed and arguments changed, but the expressions are identical
//        if(invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
//                invocationCoveringTheEntireStatement1.renamedWithIdenticalExpressionAndDifferentNumberOfArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements(), UMLClassBaseDiff.MAX_OPERATION_NAME_DISTANCE, lambdaMappers)) {
//            ReplacementType type = invocationCoveringTheEntireStatement1.getName().equals(invocationCoveringTheEntireStatement2.getName()) ? ReplacementType.METHOD_INVOCATION_ARGUMENT : ReplacementType.METHOD_INVOCATION_NAME_AND_ARGUMENT;
//            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
//                    invocationCoveringTheEntireStatement2.actualString(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, type);
//            replacementInfo.addReplacement(replacement);
//            return replacementInfo.getReplacements();
//        }
//        if(!methodInvocations1.isEmpty() && invocationCoveringTheEntireStatement2 != null) {
//            for(String methodInvocation1 : methodInvocations1) {
//                for(AbstractCall operationInvocation1 : methodInvocationMap1.get(methodInvocation1)) {
//                    if(operationInvocation1.renamedWithIdenticalExpressionAndDifferentNumberOfArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements(), UMLClassBaseDiff.MAX_OPERATION_NAME_DISTANCE, lambdaMappers) &&
//                            !isExpressionOfAnotherMethodInvocation(operationInvocation1, methodInvocationMap1)) {
//                        ReplacementType type = operationInvocation1.getName().equals(invocationCoveringTheEntireStatement2.getName()) ? ReplacementType.METHOD_INVOCATION_ARGUMENT : ReplacementType.METHOD_INVOCATION_NAME_AND_ARGUMENT;
//                        Replacement replacement = new MethodInvocationReplacement(operationInvocation1.actualString(),
//                                invocationCoveringTheEntireStatement2.actualString(), (OperationInvocation)operationInvocation1, invocationCoveringTheEntireStatement2, type);
//                        replacementInfo.addReplacement(replacement);
//                        return replacementInfo.getReplacements();
//                    }
//                }
//            }
//        }
//        //method invocation has only changes in the arguments (different number of arguments)
//        if(invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null) {
//            if(invocationCoveringTheEntireStatement1.identicalWithMergedArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {
//                return replacementInfo.getReplacements();
//            }
//            else if(invocationCoveringTheEntireStatement1.identicalWithDifferentNumberOfArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements(), parameterToArgumentMap)) {
//                Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
//                        invocationCoveringTheEntireStatement2.actualString(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_ARGUMENT);
//                replacementInfo.addReplacement(replacement);
//                return replacementInfo.getReplacements();
//            }
//        }
//        if(!methodInvocations1.isEmpty() && invocationCoveringTheEntireStatement2 != null) {
//            for(String methodInvocation1 : methodInvocations1) {
//                for(AbstractCall operationInvocation1 : methodInvocationMap1.get(methodInvocation1)) {
//                    if(operationInvocation1.identicalWithMergedArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {
//                        return replacementInfo.getReplacements();
//                    }
//                    else if(operationInvocation1.identicalWithDifferentNumberOfArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements(), parameterToArgumentMap)) {
//                        Replacement replacement = new MethodInvocationReplacement(operationInvocation1.actualString(),
//                                invocationCoveringTheEntireStatement2.actualString(), (OperationInvocation)operationInvocation1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_ARGUMENT);
//                        replacementInfo.addReplacement(replacement);
//                        return replacementInfo.getReplacements();
//                    }
//                }
//            }
//        }
//        //check if the argument of the method call in the first statement is returned in the second statement
//        Replacement r;
//        if(invocationCoveringTheEntireStatement1 != null && (r = invocationCoveringTheEntireStatement1.makeReplacementForReturnedArgument(replacementInfo.getArgumentizedString2())) != null) {
//            replacementInfo.addReplacement(r);
//            return replacementInfo.getReplacements();
//        }
//        for(String methodInvocation1 : methodInvocations1) {
//            for(AbstractCall operationInvocation1 : methodInvocationMap1.get(methodInvocation1)) {
//                if(statement1.getString().endsWith(methodInvocation1 + ";\n") && (r = operationInvocation1.makeReplacementForReturnedArgument(replacementInfo.getArgumentizedString2())) != null) {
//                    if(operationInvocation1.makeReplacementForReturnedArgument(statement2.getString()) != null) {
//                        replacementInfo.addReplacement(r);
//                        return replacementInfo.getReplacements();
//                    }
//                }
//            }
//        }
//        //check if the argument of the method call in the second statement is returned in the first statement
//        if(invocationCoveringTheEntireStatement2 != null && (r = invocationCoveringTheEntireStatement2.makeReplacementForWrappedCall(replacementInfo.getArgumentizedString1())) != null) {
//            replacementInfo.addReplacement(r);
//            return replacementInfo.getReplacements();
//        }
//        for(String methodInvocation2 : methodInvocations2) {
//            for(AbstractCall operationInvocation2 : methodInvocationMap2.get(methodInvocation2)) {
//                if(statement2.getString().endsWith(methodInvocation2 + ";\n") && (r = operationInvocation2.makeReplacementForWrappedCall(replacementInfo.getArgumentizedString1())) != null) {
//                    if(operationInvocation2.makeReplacementForWrappedCall(statement1.getString()) != null) {
//                        replacementInfo.addReplacement(r);
//                        return replacementInfo.getReplacements();
//                    }
//                }
//            }
//        }
//        //check if the argument of the method call in the second statement is the right hand side of an assignment in the first statement
//        if(invocationCoveringTheEntireStatement2 != null &&
//                (r = invocationCoveringTheEntireStatement2.makeReplacementForAssignedArgument(replacementInfo.getArgumentizedString1())) != null &&
//                methodInvocationMap1.containsKey(invocationCoveringTheEntireStatement2.getArguments().get(0))) {
//            replacementInfo.addReplacement(r);
//            return replacementInfo.getReplacements();
//        }
//        //check if the method call in the second statement is the expression of the method invocation in the first statement
//        if(invocationCoveringTheEntireStatement2 != null) {
//            for(String key1 : methodInvocationMap1.keySet()) {
//                for(AbstractCall invocation1 : methodInvocationMap1.get(key1)) {
//                    if(statement1.getString().endsWith(key1 + ";\n") &&
//                            methodInvocationMap2.keySet().contains(invocation1.getExpression())) {
//                        Replacement replacement = new MethodInvocationReplacement(invocation1.actualString(),
//                                invocationCoveringTheEntireStatement2.actualString(), (OperationInvocation)invocation1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION);
//                        replacementInfo.addReplacement(replacement);
//                        return replacementInfo.getReplacements();
//                    }
//                }
//            }
//        }
//        //check if the method call in the first statement is the expression of the method invocation in the second statement
//        if(invocationCoveringTheEntireStatement1 != null) {
//            for(String key2 : methodInvocationMap2.keySet()) {
//                for(AbstractCall invocation2 : methodInvocationMap2.get(key2)) {
//                    if(statement2.getString().endsWith(key2 + ";\n") &&
//                            methodInvocationMap1.keySet().contains(invocation2.getExpression())) {
//                        Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
//                                invocation2.actualString(), invocationCoveringTheEntireStatement1, (OperationInvocation)invocation2, ReplacementType.METHOD_INVOCATION);
//                        replacementInfo.addReplacement(replacement);
//                        return replacementInfo.getReplacements();
//                    }
//                }
//            }
//        }
//        //check if the argument of the class instance creation in the first statement is the expression of the method invocation in the second statement
//        if(creationCoveringTheEntireStatement1 != null) {
//            for(String key2 : methodInvocationMap2.keySet()) {
//                for(AbstractCall invocation2 : methodInvocationMap2.get(key2)) {
//                    if(statement2.getString().endsWith(key2 + ";\n") &&
//                            creationCoveringTheEntireStatement1.getArguments().contains(invocation2.getExpression())) {
//                        Replacement replacement = new ClassInstanceCreationWithMethodInvocationReplacement(creationCoveringTheEntireStatement1.getName(),
//                                invocation2.getName(), ReplacementType.CLASS_INSTANCE_CREATION_REPLACED_WITH_METHOD_INVOCATION, creationCoveringTheEntireStatement1, (OperationInvocation)invocation2);
//                        replacementInfo.addReplacement(replacement);
//                        return replacementInfo.getReplacements();
//                    }
//                }
//            }
//        }
//        //builder call chain in the first statement is replaced with class instance creation in the second statement
//        if(invocationCoveringTheEntireStatement1 != null && creationCoveringTheEntireStatement2 != null) {
//            if(invocationCoveringTheEntireStatement1.getName().equals("build")) {
//                int commonArguments = 0;
//                for(String key1 : methodInvocationMap1.keySet()) {
//                    if(invocationCoveringTheEntireStatement1.actualString().startsWith(key1)) {
//                        for(AbstractCall invocation1 : methodInvocationMap1.get(key1)) {
//                            Set<String> argumentIntersection = invocation1.argumentIntersection(creationCoveringTheEntireStatement2);
//                            commonArguments += argumentIntersection.size();
//                        }
//                    }
//                }
//                if(commonArguments > 0) {
//                    Replacement replacement = new MethodInvocationWithClassInstanceCreationReplacement(invocationCoveringTheEntireStatement1.getName(),
//                            creationCoveringTheEntireStatement2.getName(), ReplacementType.BUILDER_REPLACED_WITH_CLASS_INSTANCE_CREATION, invocationCoveringTheEntireStatement1, creationCoveringTheEntireStatement2);
//                    replacementInfo.addReplacement(replacement);
//                    return replacementInfo.getReplacements();
//                }
//            }
//        }
//        //object creation is identical
//        if(creationCoveringTheEntireStatement1 != null && creationCoveringTheEntireStatement2 != null &&
//                creationCoveringTheEntireStatement1.identical(creationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {
//            boolean identicalArrayInitializer = true;
//            if(creationCoveringTheEntireStatement1.isArray() && creationCoveringTheEntireStatement2.isArray()) {
//                identicalArrayInitializer = creationCoveringTheEntireStatement1.identicalArrayInitializer(creationCoveringTheEntireStatement2);
//            }
//            if(identicalArrayInitializer) {
//                return replacementInfo.getReplacements();
//            }
//        }
//        //object creation has only changes in the arguments
//        if(creationCoveringTheEntireStatement1 != null && creationCoveringTheEntireStatement2 != null) {
//            if(creationCoveringTheEntireStatement1.identicalWithMergedArguments(creationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {
//                return replacementInfo.getReplacements();
//            }
//            else if(creationCoveringTheEntireStatement1.identicalWithDifferentNumberOfArguments(creationCoveringTheEntireStatement2, replacementInfo.getReplacements(), parameterToArgumentMap)) {
//                Replacement replacement = new ObjectCreationReplacement(creationCoveringTheEntireStatement1.getName(),
//                        creationCoveringTheEntireStatement2.getName(), creationCoveringTheEntireStatement1, creationCoveringTheEntireStatement2, ReplacementType.CLASS_INSTANCE_CREATION_ARGUMENT);
//                replacementInfo.addReplacement(replacement);
//                return replacementInfo.getReplacements();
//            }
//        }
//        //check if the argument lists are identical after replacements
//        if(creationCoveringTheEntireStatement1 != null && creationCoveringTheEntireStatement2 != null &&
//                creationCoveringTheEntireStatement1.identicalName(creationCoveringTheEntireStatement2) &&
//                creationCoveringTheEntireStatement1.identicalExpression(creationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {
//            if(creationCoveringTheEntireStatement1.isArray() && creationCoveringTheEntireStatement2.isArray() &&
//                    s1.substring(s1.indexOf("[")+1, s1.lastIndexOf("]")).equals(s2.substring(s2.indexOf("[")+1, s2.lastIndexOf("]"))) &&
//                    s1.substring(s1.indexOf("[")+1, s1.lastIndexOf("]")).length() > 0) {
//                return replacementInfo.getReplacements();
//            }
//            if(!creationCoveringTheEntireStatement1.isArray() && !creationCoveringTheEntireStatement2.isArray() &&
//                    s1.substring(s1.indexOf("(")+1, s1.lastIndexOf(")")).equals(s2.substring(s2.indexOf("(")+1, s2.lastIndexOf(")"))) &&
//                    s1.substring(s1.indexOf("(")+1, s1.lastIndexOf(")")).length() > 0) {
//                return replacementInfo.getReplacements();
//            }
//        }
//        //check if array creation is replaced with data structure creation
//        if(creationCoveringTheEntireStatement1 != null && creationCoveringTheEntireStatement2 != null &&
//                variableDeclarations1.size() == 1 && variableDeclarations2.size() == 1) {
//            VariableDeclaration v1 = variableDeclarations1.get(0);
//            VariableDeclaration v2 = variableDeclarations2.get(0);
//            String initializer1 = v1.getInitializer() != null ? v1.getInitializer().getString() : null;
//            String initializer2 = v2.getInitializer() != null ? v2.getInitializer().getString() : null;
//            if(v1.getType().getArrayDimension() == 1 && v2.getType().containsTypeArgument(v1.getType().getClassType()) &&
//                    creationCoveringTheEntireStatement1.isArray() && !creationCoveringTheEntireStatement2.isArray() &&
//                    initializer1 != null && initializer2 != null &&
//                    initializer1.substring(initializer1.indexOf("[")+1, initializer1.lastIndexOf("]")).equals(initializer2.substring(initializer2.indexOf("(")+1, initializer2.lastIndexOf(")")))) {
//                r = new ObjectCreationReplacement(initializer1, initializer2,
//                        creationCoveringTheEntireStatement1, creationCoveringTheEntireStatement2, ReplacementType.ARRAY_CREATION_REPLACED_WITH_DATA_STRUCTURE_CREATION);
//                replacementInfo.addReplacement(r);
//                return replacementInfo.getReplacements();
//            }
//            if(v2.getType().getArrayDimension() == 1 && v1.getType().containsTypeArgument(v2.getType().getClassType()) &&
//                    !creationCoveringTheEntireStatement1.isArray() && creationCoveringTheEntireStatement2.isArray() &&
//                    initializer1 != null && initializer2 != null &&
//                    initializer1.substring(initializer1.indexOf("(")+1, initializer1.lastIndexOf(")")).equals(initializer2.substring(initializer2.indexOf("[")+1, initializer2.lastIndexOf("]")))) {
//                r = new ObjectCreationReplacement(initializer1, initializer2,
//                        creationCoveringTheEntireStatement1, creationCoveringTheEntireStatement2, ReplacementType.ARRAY_CREATION_REPLACED_WITH_DATA_STRUCTURE_CREATION);
//                replacementInfo.addReplacement(r);
//                return replacementInfo.getReplacements();
//            }
//        }
//        if(!creations1.isEmpty() && creationCoveringTheEntireStatement2 != null) {
//            for(String creation1 : creations1) {
//                for(AbstractCall objectCreation1 : creationMap1.get(creation1)) {
//                    if(objectCreation1.identicalWithMergedArguments(creationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {
//                        return replacementInfo.getReplacements();
//                    }
//                    else if(objectCreation1.identicalWithDifferentNumberOfArguments(creationCoveringTheEntireStatement2, replacementInfo.getReplacements(), parameterToArgumentMap)) {
//                        Replacement replacement = new ObjectCreationReplacement(objectCreation1.getName(),
//                                creationCoveringTheEntireStatement2.getName(), (ObjectCreation)objectCreation1, creationCoveringTheEntireStatement2, ReplacementType.CLASS_INSTANCE_CREATION_ARGUMENT);
//                        replacementInfo.addReplacement(replacement);
//                        return replacementInfo.getReplacements();
//                    }
//                    //check if the argument lists are identical after replacements
//                    if(objectCreation1.identicalName(creationCoveringTheEntireStatement2) &&
//                            objectCreation1.identicalExpression(creationCoveringTheEntireStatement2, replacementInfo.getReplacements())) {
//                        if(((ObjectCreation)objectCreation1).isArray() && creationCoveringTheEntireStatement2.isArray() &&
//                                s1.substring(s1.indexOf("[")+1, s1.lastIndexOf("]")).equals(s2.substring(s2.indexOf("[")+1, s2.lastIndexOf("]"))) &&
//                                s1.substring(s1.indexOf("[")+1, s1.lastIndexOf("]")).length() > 0) {
//                            return replacementInfo.getReplacements();
//                        }
//                        if(!((ObjectCreation)objectCreation1).isArray() && !creationCoveringTheEntireStatement2.isArray() &&
//                                s1.substring(s1.indexOf("(")+1, s1.lastIndexOf(")")).equals(s2.substring(s2.indexOf("(")+1, s2.lastIndexOf(")"))) &&
//                                s1.substring(s1.indexOf("(")+1, s1.lastIndexOf(")")).length() > 0) {
//                            return replacementInfo.getReplacements();
//                        }
//                    }
//                }
//            }
//        }
//        if(creationCoveringTheEntireStatement1 != null && (r = creationCoveringTheEntireStatement1.makeReplacementForReturnedArgument(replacementInfo.getArgumentizedString2())) != null) {
//            replacementInfo.addReplacement(r);
//            return replacementInfo.getReplacements();
//        }
//        for(String creation1 : creations1) {
//            for(AbstractCall objectCreation1 : creationMap1.get(creation1)) {
//                if(statement1.getString().endsWith(creation1 + ";\n") && (r = objectCreation1.makeReplacementForReturnedArgument(replacementInfo.getArgumentizedString2())) != null) {
//                    replacementInfo.addReplacement(r);
//                    return replacementInfo.getReplacements();
//                }
//            }
//        }
//        if(variableDeclarationWithArrayInitializer1 != null && invocationCoveringTheEntireStatement2 != null && variableDeclarations2.isEmpty() &&
//                !containsMethodSignatureOfAnonymousClass(statement1.getString()) && !containsMethodSignatureOfAnonymousClass(statement2.getString())) {
//            String args1 = s1.substring(s1.indexOf("{")+1, s1.lastIndexOf("}"));
//            String args2 = s2.substring(s2.indexOf("(")+1, s2.lastIndexOf(")"));
//            if(args1.equals(args2)) {
//                r = new Replacement(args1, args2, ReplacementType.ARRAY_INITIALIZER_REPLACED_WITH_METHOD_INVOCATION_ARGUMENTS);
//                replacementInfo.addReplacement(r);
//                return replacementInfo.getReplacements();
//            }
//        }
//        if(variableDeclarationWithArrayInitializer2 != null && invocationCoveringTheEntireStatement1 != null && variableDeclarations1.isEmpty() &&
//                !containsMethodSignatureOfAnonymousClass(statement1.getString()) && !containsMethodSignatureOfAnonymousClass(statement2.getString())) {
//            String args1 = s1.substring(s1.indexOf("(")+1, s1.lastIndexOf(")"));
//            String args2 = s2.substring(s2.indexOf("{")+1, s2.lastIndexOf("}"));
//            if(args1.equals(args2)) {
//                r = new Replacement(args1, args2, ReplacementType.ARRAY_INITIALIZER_REPLACED_WITH_METHOD_INVOCATION_ARGUMENTS);
//                replacementInfo.addReplacement(r);
//                return replacementInfo.getReplacements();
//            }
//        }
//        List<TernaryOperatorExpression> ternaryOperatorExpressions1 = statement1.getTernaryOperatorExpressions();
//        List<TernaryOperatorExpression> ternaryOperatorExpressions2 = statement2.getTernaryOperatorExpressions();
//        if(ternaryOperatorExpressions1.isEmpty() && ternaryOperatorExpressions2.size() == 1) {
//            TernaryOperatorExpression ternary = ternaryOperatorExpressions2.get(0);
//            for(String creation : creationIntersection) {
//                if((r = ternary.makeReplacementWithTernaryOnTheRight(creation)) != null) {
//                    replacementInfo.addReplacement(r);
//                    return replacementInfo.getReplacements();
//                }
//            }
//            for(String methodInvocation : methodInvocationIntersection) {
//                if((r = ternary.makeReplacementWithTernaryOnTheRight(methodInvocation)) != null) {
//                    replacementInfo.addReplacement(r);
//                    return replacementInfo.getReplacements();
//                }
//            }
//            if(invocationCoveringTheEntireStatement1 != null && (r = ternary.makeReplacementWithTernaryOnTheRight(invocationCoveringTheEntireStatement1.actualString())) != null) {
//                replacementInfo.addReplacement(r);
//                return replacementInfo.getReplacements();
//            }
//            if(creationCoveringTheEntireStatement1 != null && (r = ternary.makeReplacementWithTernaryOnTheRight(creationCoveringTheEntireStatement1.actualString())) != null) {
//                replacementInfo.addReplacement(r);
//                return replacementInfo.getReplacements();
//            }
//            for(String creation2 : creations2) {
//                if((r = ternary.makeReplacementWithTernaryOnTheRight(creation2)) != null) {
//                    for(AbstractCall c2 : creationMap2.get(creation2)) {
//                        for(String creation1 : creations1) {
//                            for(AbstractCall c1 : creationMap1.get(creation1)) {
//                                if(((ObjectCreation)c1).getType().compatibleTypes(((ObjectCreation)c2).getType()) && c1.equalArguments(c2)) {
//                                    replacementInfo.addReplacement(r);
//                                    return replacementInfo.getReplacements();
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if(ternaryOperatorExpressions1.size() == 1 && ternaryOperatorExpressions2.isEmpty()) {
//            TernaryOperatorExpression ternary = ternaryOperatorExpressions1.get(0);
//            for(String creation : creationIntersection) {
//                if((r = ternary.makeReplacementWithTernaryOnTheLeft(creation)) != null) {
//                    replacementInfo.addReplacement(r);
//                    return replacementInfo.getReplacements();
//                }
//            }
//            for(String methodInvocation : methodInvocationIntersection) {
//                if((r = ternary.makeReplacementWithTernaryOnTheLeft(methodInvocation)) != null) {
//                    replacementInfo.addReplacement(r);
//                    return replacementInfo.getReplacements();
//                }
//            }
//            if(invocationCoveringTheEntireStatement2 != null && (r = ternary.makeReplacementWithTernaryOnTheLeft(invocationCoveringTheEntireStatement2.actualString())) != null) {
//                replacementInfo.addReplacement(r);
//                return replacementInfo.getReplacements();
//            }
//            if(creationCoveringTheEntireStatement2 != null && (r = ternary.makeReplacementWithTernaryOnTheLeft(creationCoveringTheEntireStatement2.actualString())) != null) {
//                replacementInfo.addReplacement(r);
//                return replacementInfo.getReplacements();
//            }
//            for(String creation1 : creations1) {
//                if((r = ternary.makeReplacementWithTernaryOnTheLeft(creation1)) != null) {
//                    for(AbstractCall c1 : creationMap1.get(creation1)) {
//                        for(String creation2 : creations2) {
//                            for(AbstractCall c2 : creationMap2.get(creation2)) {
//                                if(((ObjectCreation)c1).getType().compatibleTypes(((ObjectCreation)c2).getType()) && c1.equalArguments(c2)) {
//                                    replacementInfo.addReplacement(r);
//                                    return replacementInfo.getReplacements();
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if(invocationCoveringTheEntireStatement2 != null && statement2.getString().equals(invocationCoveringTheEntireStatement2.actualString() + ";\n") &&
//                invocationCoveringTheEntireStatement2.getArguments().size() == 1 && statement1.getString().endsWith("=" + invocationCoveringTheEntireStatement2.getArguments().get(0) + ";\n") &&
//                invocationCoveringTheEntireStatement2.expressionIsNullOrThis() && invocationCoveringTheEntireStatement2.getName().startsWith("set")) {
//            String prefix1 = statement1.getString().substring(0, statement1.getString().lastIndexOf("="));
//            if(variables1.contains(prefix1)) {
//                String before = prefix1 + "=" + invocationCoveringTheEntireStatement2.getArguments().get(0);
//                String after = invocationCoveringTheEntireStatement2.actualString();
//                r = new Replacement(before, after, ReplacementType.FIELD_ASSIGNMENT_REPLACED_WITH_SETTER_METHOD_INVOCATION);
//                replacementInfo.addReplacement(r);
//                return replacementInfo.getReplacements();
//            }
//        }
        return null;
    }

    private VariableDeclaration findDeclarationWithArrayInitializer(List<VariableDeclaration> variableDeclarations) {
        Expression initializer;
        for (VariableDeclaration declaration : variableDeclarations) {

            if ((initializer = declaration.getInitializer()) != null
                    && initializer.getText().startsWith("{") && initializer.getText().endsWith("}")) {
                return declaration;
            }
        }
        return null;
    }
}
