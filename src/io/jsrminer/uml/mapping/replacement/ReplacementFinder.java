package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.diff.StringDistance;
import io.jsrminer.uml.diff.UMLOperationDiff;
import io.jsrminer.uml.mapping.PreProcessor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.jsrminer.uml.mapping.replacement.Replacement.ReplacementType;
import static io.jsrminer.uml.mapping.replacement.VariableReplacementWithMethodInvocation.Direction;

public class ReplacementFinder {

    private static final Pattern DOUBLE_QUOTES = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    public Set<Replacement> findReplacementsWithExactMatching(SingleStatement statement1, SingleStatement statement2
            , Map<String, String> parameterToArgumentMap
            , ReplacementInfo replacementInfo
            , PreProcessor preProcessor) {

        final StatementDiff diff = new StatementDiff(statement1, statement2);

        // Intersect variables
        Set<String> variablesToBeAddedAsUnmatched = findCommonVariablesToBeAddedAsUnmatched(statement1, statement2, replacementInfo);
        diff.variables1.addAll(variablesToBeAddedAsUnmatched);
        diff.variables2.addAll(variablesToBeAddedAsUnmatched);

        // replace unmatched variables with the corresponding arguments (Argumentize?)
        // Add parameters to unmatched?
        addArgumentsToVariables(diff.variables1, parameterToArgumentMap);
        addArgumentsToVariables(diff.variables2, parameterToArgumentMap);

        // Find replacements in various rounds

        // 1. Argument replacement with variables
        // Perform replacements of  all the arguments in s1 with variables in s2
        final Map<String, String> variableToArgumentMap = replaceArgumentsWithVariables(
                statement1, diff, replacementInfo);

        // 2. Replace variables with the corresponding arguments in method invocations
        Map<String, List<? extends Invocation>> methodInvocationMap1 = new LinkedHashMap<>(statement1.getMethodInvocationMap());
        Map<String, List<? extends Invocation>> methodInvocationMap2 = new LinkedHashMap<>(statement2.getMethodInvocationMap());

        // The map is just for returning value (TODO improve)
        // Replace the variables which are in method invocation arguments
        Map<Integer, Set<String>> unmatchedFunctionsMap
                = intersectFunctionInvocations(statement1, statement2
                , methodInvocationMap1, methodInvocationMap2
                , parameterToArgumentMap, variableToArgumentMap);

        Set<String> functionInvocations1 = unmatchedFunctionsMap.get(0);
        Set<String> functionInvocations2 = unmatchedFunctionsMap.get(1);

//////        Set<String> types1 = new LinkedHashSet<String>(statement1.getTypes());
////        Set<String> types2 = new LinkedHashSet<String>(statement2.getTypes());
////        removeCommonTypes(types1, types2, statement1.getTypes(), statement2.getTypes());
////        //perform type replacements
////        findReplacements(types1, types2, replacementInfo, ReplacementType.TYPE);

        intersectAndReplaceInfixOperators(statement1, statement2, replacementInfo);

        // Find all variables and invocations
        Map<Integer, Set<String>> variablesAndFunctionInvocationsMap
                = getVariablesAndFunctionInvocations(diff.variables1, diff.variables2,
                methodInvocationMap1, functionInvocations1, functionInvocations2,
                replacementInfo);

        Set<String> variablesAndMethodInvocations1 = variablesAndFunctionInvocationsMap.get(0);
        Set<String> variablesAndMethodInvocations2 = variablesAndFunctionInvocationsMap.get(1);

        if (replacementInfo.getRawEditDistance() > 0) {
            replaceVariablesAndFunctionInvocations(statement1, statement2
                    , parameterToArgumentMap, replacementInfo
                    , diff.variables1, diff.variables2
                    , methodInvocationMap1, methodInvocationMap2
                    , functionInvocations1, functionInvocations2
                    , variablesAndMethodInvocations1, variablesAndMethodInvocations2);
        }

        //3. replace variables with the corresponding arguments in object creations
        intersectAndReplaceObjectCreations(statement1, statement2,
                parameterToArgumentMap, replacementInfo, variableToArgumentMap);

        // Perform literal replacements
        replaceLiterals(statement1, statement2, replacementInfo, diff);
        replaceArrayAccess(replacementInfo, diff, functionInvocations1, functionInvocations2);

        // Replace variables With prefixExpressions and ViceVersa
        findAndPerformBestReplacements(diff.variables1, diff.prefixExpressions2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_PREFIX_EXPRESSION);
        findAndPerformBestReplacements(diff.prefixExpressions1, diff.variables2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_PREFIX_EXPRESSION);

        // Replace stringLiterals1 with variables
        findAndPerformBestReplacements(diff.stringLiterals1, diff.variables2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_STRING_LITERAL);

        // Replace variables with Null Literals
        if (statement1.getNullLiterals().isEmpty() && !statement2.getNullLiterals().isEmpty()) {
            Set<String> nullLiterals2 = new LinkedHashSet<>();
            nullLiterals2.add("null");
            findAndPerformBestReplacements(diff.variables1, nullLiterals2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_NULL_LITERAL);
        }
        // region TODO ternaryOpsExp
//
//        if (statement1.getTernaryOperatorExpressions().isEmpty() && !statement2.getTernaryOperatorExpressions().isEmpty()) {
//            if (!statement1.getNullLiterals().isEmpty()) {
//                Set<String> nullLiterals1 = new LinkedHashSet<String>();
//                nullLiterals1.add("null");
//                Set<String> ternaryExpressions2 = new LinkedHashSet<String>();
//                for (TernaryOperatorExpression ternary : statement2.getTernaryOperatorExpressions()) {
//                    ternaryExpressions2.add(ternary.getExpression());
//                }
//                findReplacements(nullLiterals1, ternaryExpressions2, replacementInfo, ReplacementType.NULL_LITERAL_REPLACED_WITH_CONDITIONAL_EXPRESSION);
//            }
//        } else if (!statement1.getTernaryOperatorExpressions().isEmpty() && statement2.getTernaryOperatorExpressions().isEmpty()) {
//            if (!statement2.getNullLiterals().isEmpty()) {
//                Set<String> nullLiterals2 = new LinkedHashSet<String>();
//                nullLiterals2.add("null");
//                Set<String> ternaryExpressions1 = new LinkedHashSet<String>();
//                for (TernaryOperatorExpression ternary : statement1.getTernaryOperatorExpressions()) {
//                    ternaryExpressions1.add(ternary.getExpression());
//                }
//                findReplacements(ternaryExpressions1, nullLiterals2, replacementInfo, ReplacementType.NULL_LITERAL_REPLACED_WITH_CONDITIONAL_EXPRESSION);
//            }
//        }

//        if (!statement1.getString().endsWith("=true;\n") && !statement1.getString().endsWith("=false;\n")) {
//            findReplacements(booleanLiterals1, variables2, replacementInfo, ReplacementType.BOOLEAN_REPLACED_WITH_VARIABLE);
//        }
//        if (!statement2.getString().endsWith("=true;\n") && !statement2.getString().endsWith("=false;\n")) {
//            findReplacements(arguments1, booleanLiterals2, replacementInfo, ReplacementType.BOOLEAN_REPLACED_WITH_ARGUMENT);
//        }
// endregion

        filterReplacements(statement1, statement2
                , replacementInfo, preProcessor
                , diff, methodInvocationMap1, methodInvocationMap2
                , functionInvocations1, functionInvocations2);

        boolean isEqualWithReplacement = s1.equals(s2) || replacementInfo.argumentizedString1.equals(replacementInfo.argumentizedString2) || differOnlyInCastExpressionOrPrefixOperator(s1, s2, replacementInfo) || oneIsVariableDeclarationTheOtherIsVariableAssignment(s1, s2, replacementInfo) ||
                oneIsVariableDeclarationTheOtherIsReturnStatement(s1, s2) || oneIsVariableDeclarationTheOtherIsReturnStatement(statement1.getString(), statement2.getString()) ||
                (commonConditional(s1, s2, replacementInfo) && containsValidOperatorReplacements(replacementInfo)) ||
                equalAfterArgumentMerge(s1, s2, replacementInfo) ||
                equalAfterNewArgumentAdditions(s1, s2, replacementInfo) ||
                (validStatementForConcatComparison(statement1, statement2) && commonConcat(s1, s2, replacementInfo));


        if (isEqualWithReplacement) {
            List<Replacement> typeReplacements = replacementInfo.getReplacements(ReplacementType.TYPE);
            if (typeReplacements.size() > 0 && invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null) {
                for (Replacement typeReplacement : typeReplacements) {
                    if (invocationCoveringTheEntireStatement1.getMethodName().contains(typeReplacement.getBefore()) && invocationCoveringTheEntireStatement2.getMethodName().contains(typeReplacement.getAfter())) {
                        if (invocationCoveringTheEntireStatement1.identicalExpression(invocationCoveringTheEntireStatement2) && invocationCoveringTheEntireStatement1.equalArguments(invocationCoveringTheEntireStatement2)) {
                            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.getName(),
                                    invocationCoveringTheEntireStatement2.getName(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION_NAME);
                            replacementInfo.addReplacement(replacement);
                        } else {
                            Replacement replacement = new MethodInvocationReplacement(invocationCoveringTheEntireStatement1.actualString(),
                                    invocationCoveringTheEntireStatement2.actualString(), invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, ReplacementType.METHOD_INVOCATION);
                            replacementInfo.addReplacement(replacement);
                        }
                        break;
                    }
                }
            }
            if (variableDeclarationsWithEverythingReplaced(variableDeclarations1, variableDeclarations2, replacementInfo) &&
                    !statement1.getLocationInfo().getCodeElementType().equals(CodeElementType.ENHANCED_FOR_STATEMENT) &&
                    !statement2.getLocationInfo().getCodeElementType().equals(CodeElementType.ENHANCED_FOR_STATEMENT)) {
                return null;
            }
            if (variableAssignmentWithEverythingReplaced(statement1, statement2, replacementInfo)) {
                return null;
            }
            if (classInstanceCreationWithEverythingReplaced(statement1, statement2, replacementInfo, parameterToArgumentMap)) {
                return null;
            }

            // region TODO annynomous class
            //        List<AnonymousClassDeclarationObject> anonymousClassDeclarations1 = statement1.getAnonymousClassDeclarations();
//        List<AnonymousClassDeclarationObject> anonymousClassDeclarations2 = statement2.getAnonymousClassDeclarations();

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
            // endregion

            return replacementInfo.getReplacements();
        }

        // region annonymous
//        if (!anonymousClassDeclarations1.isEmpty() && !anonymousClassDeclarations2.isEmpty()) {
//            for (int i = 0; i < anonymousClassDeclarations1.size(); i++) {
//                for (int j = 0; j < anonymousClassDeclarations2.size(); j++) {
//                    AnonymousClassDeclarationObject anonymousClassDeclaration1 = anonymousClassDeclarations1.get(i);
//                    AnonymousClassDeclarationObject anonymousClassDeclaration2 = anonymousClassDeclarations2.get(j);
//                    String statementWithoutAnonymous1 = statementWithoutAnonymous(statement1, anonymousClassDeclaration1, operation1);
//                    String statementWithoutAnonymous2 = statementWithoutAnonymous(statement2, anonymousClassDeclaration2, operation2);
//                    if (statementWithoutAnonymous1.equals(statementWithoutAnonymous2) ||
//                            identicalAfterVariableAndTypeReplacements(statementWithoutAnonymous1, statementWithoutAnonymous2, replacementInfo.getReplacements()) ||
//                            (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
//                                    (invocationCoveringTheEntireStatement1.identicalWithMergedArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements()) ||
//                                            invocationCoveringTheEntireStatement1.identicalWithDifferentNumberOfArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements(), parameterToArgumentMap)))) {
//                        UMLAnonymousClass anonymousClass1 = findAnonymousClass(anonymousClassDeclaration1, operation1);
//                        UMLAnonymousClass anonymousClass2 = findAnonymousClass(anonymousClassDeclaration2, operation2);
//                        int matchedOperations = 0;
//                        for (UMLOperation operation1 : anonymousClass1.getOperations()) {
//                            for (UMLOperation operation2 : anonymousClass2.getOperations()) {
//                                if (operation1.equals(operation2) || operation1.equalSignature(operation2) || operation1.equalSignatureWithIdenticalNameIgnoringChangedTypes(operation2)) {
//                                    UMLOperationBodyMapper mapper = new UMLOperationBodyMapper(operation1, operation2, classDiff);
//                                    int mappings = mapper.mappingsWithoutBlocks();
//                                    if (mappings > 0) {
//                                        int nonMappedElementsT1 = mapper.nonMappedElementsT1();
//                                        int nonMappedElementsT2 = mapper.nonMappedElementsT2();
//                                        if (mappings > nonMappedElementsT1 && mappings > nonMappedElementsT2) {
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
//                        if (matchedOperations > 0) {
//                            Replacement replacement = new Replacement(anonymousClassDeclaration1.toString(), anonymousClassDeclaration2.toString(), ReplacementType.ANONYMOUS_CLASS_DECLARATION);
//                            replacementInfo.addReplacement(replacement);
//                            return replacementInfo.getReplacements();
//                        }
//                    }
//                }
//            }
//        }
        // endregion

        // region lambda
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
        // endregion

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

    private void filterReplacements(SingleStatement statement1, SingleStatement statement2
            , ReplacementInfo replacementInfo, PreProcessor preProcessor
            , StatementDiff diff, Map<String, List<? extends Invocation>> methodInvocationMap1
            , Map<String, List<? extends Invocation>> methodInvocationMap2
            , Set<String> functionInvocations1, Set<String> functionInvocations2) {
        String s1 = preProcessor.getArgumentizedString(statement1);
        String s2 = preProcessor.getArgumentizedString(statement2);

        LinkedHashSet<Replacement> replacementsToBeRemoved = new LinkedHashSet<>();
        LinkedHashSet<Replacement> replacementsToBeAdded = new LinkedHashSet<>();

        for (Replacement replacement : replacementInfo.getAppliedReplacements()) {
            s1 = ReplacementUtil.performReplacement(s1, s2, replacement.getBefore(), replacement.getAfter());

            //find variable replacements within method invocation replacements
            Set<Replacement> set = replacementsWithinMethodInvocations(replacement.getBefore(), replacement.getAfter(),
                    diff.variables1, functionInvocations2,
                    methodInvocationMap2, Direction.VARIABLE_TO_INVOCATION);

            set.addAll(replacementsWithinMethodInvocations(replacement.getBefore(), replacement.getAfter()
                    , functionInvocations1, diff.variables2
                    , methodInvocationMap1, Direction.INVOCATION_TO_VARIABLE));

            if (!set.isEmpty()) {
                replacementsToBeRemoved.add(replacement);
                replacementsToBeAdded.addAll(set);
            }
            Replacement r = variableReplacementWithinMethodInvocations(replacement.getBefore(), replacement.getAfter()
                    , diff.variables1, diff.variables2);
            if (r != null) {
                replacementsToBeRemoved.add(replacement);
                replacementsToBeAdded.add(r);
            }
            Replacement r2 = variableReplacementWithinMethodInvocations(replacement.getBefore(), replacement.getAfter()
                    , diff.stringLiterals1, diff.variables2);
            if (r2 != null) {
                replacementsToBeRemoved.add(replacement);
                replacementsToBeAdded.add(r2);
            }
        }
        replacementInfo.removeReplacements(replacementsToBeRemoved);
        replacementInfo.addReplacements(replacementsToBeAdded);
    }

    private void replaceArrayAccess(ReplacementInfo replacementInfo, StatementDiff diff, Set<String> functionInvocations1, Set<String> functionInvocations2) {
        // Replace variables With arrayAccess and ViceVersa
        findAndPerformBestReplacements(diff.variables1, diff.arrayAccesses2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_ARRAY_ACCESS);
        findAndPerformBestReplacements(diff.arrayAccesses1, diff.variables2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_ARRAY_ACCESS);

        // Replace functionInvocations With arrayAccess and ViceVersa
        findAndPerformBestReplacements(functionInvocations1, diff.arrayAccesses2, replacementInfo, ReplacementType.ARRAY_ACCESS_REPLACED_WITH_METHOD_INVOCATION);
        findAndPerformBestReplacements(diff.arrayAccesses1, functionInvocations2, replacementInfo, ReplacementType.ARRAY_ACCESS_REPLACED_WITH_METHOD_INVOCATION);
    }

    private void replaceLiterals(SingleStatement statement1, SingleStatement statement2, ReplacementInfo replacementInfo, StatementDiff intersection) {
        findAndPerformBestReplacements(intersection.stringLiterals1, intersection.stringLiterals2
                , replacementInfo, ReplacementType.STRING_LITERAL);
        findAndPerformBestReplacements(intersection.numberLiterals1, intersection.numberLiterals2,
                replacementInfo, ReplacementType.NUMBER_LITERAL);

        if (!expressionsContainInitializerOfVariableDeclaration(intersection.numberLiterals1, statement1)
                && !expressionsContainInitializerOfVariableDeclaration(intersection.variables2, statement2) &&
                !statement1.getText().endsWith("=0;\n")) { // TODO defaultValue??
            findAndPerformBestReplacements(intersection.numberLiterals1, intersection.variables2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_NUMBER_LITERAL);
        }
    }

    private void intersectAndReplaceObjectCreations(SingleStatement statement1, SingleStatement statement2, Map<String, String> parameterToArgumentMap, ReplacementInfo replacementInfo, Map<String, String> variableToArgumentMap) {
        Map<Integer, Set<String>> objectCreationsMap = intersectObjectCreations(statement1, statement2,
                parameterToArgumentMap, variableToArgumentMap);
        Set<String> creations1 = objectCreationsMap.get(0);
        Set<String> creations2 = objectCreationsMap.get(1);
        findAndPerformBestReplacements(creations1, creations2, replacementInfo, ReplacementType.CLASS_INSTANCE_CREATION);
    }

    private void replaceVariablesAndFunctionInvocations(SingleStatement statement1, SingleStatement statement2
            , Map<String, String> parameterToArgumentMap, ReplacementInfo replacementInfo
            , Set<String> unmatchedVariables1, Set<String> unmatchedVariables2
            , Map<String, List<? extends Invocation>> methodInvocationMap1
            , Map<String, List<? extends Invocation>> methodInvocationMap2
            , Set<String> functionInvocations1, Set<String> functionInvocations2
            , Set<String> variablesAndMethodInvocations1, Set<String> variablesAndMethodInvocations2) {

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
                        VariableDeclaration v1 = statement1.getVariableDeclaration(s1);
                        VariableDeclaration v2 = statement2.getVariableDeclaration(s2);
//                            if (inconsistentVariableMappingCount(statement1, statement2, v1, v2) > 1 && operation2.loopWithVariables(v1.getVariableName(), v2.getVariableName()) == null) {
//                                replacement = null;
//                            }
                    } else if (unmatchedVariables1.contains(s1) && functionInvocations2.contains(s2)) {
                        OperationInvocation invokedOperationAfter = (OperationInvocation) methodInvocationMap2.get(s2).get(0);
                        replacement = new VariableReplacementWithMethodInvocation(s1, s2, invokedOperationAfter, VariableReplacementWithMethodInvocation.Direction.VARIABLE_TO_INVOCATION);
                    } else if (functionInvocations1.contains(s1) && functionInvocations2.contains(s2)) {
                        OperationInvocation invokedOperationBefore = (OperationInvocation) methodInvocationMap1.get(s1).get(0);
                        OperationInvocation invokedOperationAfter = (OperationInvocation) methodInvocationMap2.get(s2).get(0);

//                            if (invokedOperationBefore.compatibleExpression(invokedOperationAfter)) {
//                                replacement = new MethodInvocationReplacement(s1, s2, invokedOperationBefore, invokedOperationAfter, ReplacementType.METHOD_INVOCATION);
//                            }
//
                    } else if (functionInvocations1.contains(s1) && unmatchedVariables2.contains(s2)) {
                        OperationInvocation invokedOperationBefore = (OperationInvocation) methodInvocationMap1.get(s1).get(0);
                        replacement = new VariableReplacementWithMethodInvocation(s1, s2, invokedOperationBefore, VariableReplacementWithMethodInvocation.Direction.INVOCATION_TO_VARIABLE);
                    }
                    if (replacement != null) {
                        double distancenormalized = (double) distanceRaw / (double) Math.max(temp.length(), replacementInfo.getArgumentizedString2().length());
                        replacementMap.put(distancenormalized, replacement);
                    }
                    if (distanceRaw == 0 && !replacementInfo.getAppliedReplacements().isEmpty()) {
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

    /**
     * Returns variablesAndMethodInvocationMaps. Modifies parameters
     */
    private Map<Integer, Set<String>> getVariablesAndFunctionInvocations(Set<String> unmatchedVariables1
            , Set<String> unmatchedVariables2
            , Map<String, List<? extends Invocation>> methodInvocationMap1
            , Set<String> functionInvocations1
            , Set<String> functionInvocations2
            , ReplacementInfo replacementInfo) {

        Set<String> variablesAndMethodInvocations1 = new LinkedHashSet<>();
        variablesAndMethodInvocations1.addAll(functionInvocations1);
        variablesAndMethodInvocations1.addAll(unmatchedVariables1);

        Set<String> variablesAndMethodInvocations2 = new LinkedHashSet<>();
        variablesAndMethodInvocations2.addAll(functionInvocations2);
        variablesAndMethodInvocations2.addAll(unmatchedVariables2);

        //apply existing replacements on method invocations
        for (String functionInvocation : functionInvocations1) {
            String temp = new String(functionInvocation);
            for (Replacement replacement : replacementInfo.getAppliedReplacements()) {
                temp = ReplacementUtil.performReplacement(temp, replacement.getBefore(), replacement.getAfter());
            }

            if (!temp.equals(functionInvocation)) {
                variablesAndMethodInvocations1.add(temp);
                methodInvocationMap1.put(temp, methodInvocationMap1.get(functionInvocation));
            }
        }

        //add updated method invocation to the original list of invocations
        functionInvocations1.addAll(variablesAndMethodInvocations1);
        variablesAndMethodInvocations1.addAll(functionInvocations1);
        variablesAndMethodInvocations1.addAll(unmatchedVariables1);

        return Map.of(0, variablesAndMethodInvocations1, 1, variablesAndMethodInvocations2);
    }

    private void intersectAndReplaceInfixOperators(SingleStatement statement1, SingleStatement statement2, ReplacementInfo replacementInfo) {
        //5. Intersection of infixOps
        Set<String> infixOperators1 = new LinkedHashSet<>(statement1.getInfixOperators());
        Set<String> infixOperators2 = new LinkedHashSet<>(statement2.getInfixOperators());
        ReplacementUtil.removeCommonElements(infixOperators1, infixOperators2);
        //perform operator replacements
        findAndPerformBestReplacements(infixOperators1, infixOperators2, replacementInfo, ReplacementType.INFIX_OPERATOR);
    }

    /**
     * Modifies the maps
     *
     * @return The functioninvocations 0, 1 as key respectively
     */
    private Map<Integer, Set<String>> intersectFunctionInvocations(
            SingleStatement statement1,
            SingleStatement statement2,
            Map<String, List<? extends Invocation>> methodInvocationMap1
            , Map<String, List<? extends Invocation>> methodInvocationMap2
            , Map<String, String> parameterToArgumentMap
            , Map<String, String> variableToArgumentMap) {

        Set<String> methodInvocations1 = new LinkedHashSet<>(methodInvocationMap1.keySet());
        Set<String> methodInvocations2 = new LinkedHashSet<>(methodInvocationMap2.keySet());

        // Argumentizations
        addArgumentsToInvocations(methodInvocationMap1, methodInvocations1, parameterToArgumentMap);
        addArgumentsToInvocations(methodInvocationMap2, methodInvocations2, parameterToArgumentMap);
        addArgumentsToInvocations(methodInvocationMap1, methodInvocations1, variableToArgumentMap);

        final OperationInvocation invocationCoveringTheEntireStatement1
                = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement1);
        final OperationInvocation invocationCoveringTheEntireStatement2
                = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement2);

        // / Abstraction
        //remove methodInvocation covering the entire statement
        if (invocationCoveringTheEntireStatement1 != null) {
            for (String methodInvocation1 : methodInvocationMap1.keySet()) {
                for (Invocation call : methodInvocationMap1.get(methodInvocation1)) {
                    if (invocationCoveringTheEntireStatement1.equalsSourceLocation(call)) {
                        methodInvocations1.remove(methodInvocation1);
                    }
                }
            }
        }
        if (invocationCoveringTheEntireStatement2 != null) {
            for (String methodInvocation2 : methodInvocationMap2.keySet()) {
                for (Invocation call : methodInvocationMap2.get(methodInvocation2)) {
                    if (invocationCoveringTheEntireStatement2.equalsSourceLocation(call)) {
                        methodInvocations2.remove(methodInvocation2);
                    }
                }
            }
        }

        // Intersect methods
        Set<String> methodInvocationIntersection = new LinkedHashSet<>(methodInvocations1);
        methodInvocationIntersection.retainAll(methodInvocations2);
        Set<String> methodInvocationsToBeRemovedFromTheIntersection = new LinkedHashSet<>();

        for (String methodInvocation : methodInvocationIntersection) {
            if (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
                    invocationCoveringTheEntireStatement1.equalsInovkedFunctionName(invocationCoveringTheEntireStatement2)) {
                if (!invocationCoveringTheEntireStatement1.getArguments().contains(methodInvocation) &&
                        invocationCoveringTheEntireStatement2.getArguments().contains(methodInvocation)) {
                    methodInvocationsToBeRemovedFromTheIntersection.add(methodInvocation);
                } else if (invocationCoveringTheEntireStatement1.getArguments().contains(methodInvocation) &&
                        !invocationCoveringTheEntireStatement2.getArguments().contains(methodInvocation)) {
                    methodInvocationsToBeRemovedFromTheIntersection.add(methodInvocation);
                }
            }
        }
        methodInvocationIntersection.removeAll(methodInvocationsToBeRemovedFromTheIntersection);
        // remove common methodInvocations from the two sets
        methodInvocations1.removeAll(methodInvocationIntersection);
        methodInvocations2.removeAll(methodInvocationIntersection);

        return Map.of(0, methodInvocations1, 1, methodInvocations2);
    }

    private Map<Integer, Set<String>> intersectObjectCreations(SingleStatement statement1, SingleStatement statement2,
                                                               Map<String, String> parameterToArgumentMap
            , Map<String, String> variableToArgumentMap) {
        Map<String, List<? extends Invocation>> creationMap1 = new LinkedHashMap<>(statement1.getCreationMap());
        Map<String, List<? extends Invocation>> creationMap2 = new LinkedHashMap<>(statement2.getCreationMap());
        Set<String> creations1 = new LinkedHashSet<>(creationMap1.keySet());
        Set<String> creations2 = new LinkedHashSet<>(creationMap2.keySet());

        addArgumentsToInvocations(creationMap1, creations1, parameterToArgumentMap);
        addArgumentsToInvocations(creationMap2, creations2, parameterToArgumentMap);
        addArgumentsToInvocations(creationMap1, creations1, variableToArgumentMap);

        ObjectCreation creationCoveringTheEntireStatement1 = InvocationCoverage.INSTANCE.creationCoveringEntireFragment(statement1);
        ObjectCreation creationCoveringTheEntireStatement2 = InvocationCoverage.INSTANCE.creationCoveringEntireFragment(statement2);
        //remove objectCreation covering the entire statement
        for (String objectCreation1 : creationMap1.keySet()) {
            for (Invocation creation1 : creationMap1.get(objectCreation1)) {
                if (creationCoveringTheEntireStatement1 != null &&
                        creationCoveringTheEntireStatement1.equalsSourceLocation(creation1)) {
                    creations1.remove(objectCreation1);
                }
//                if (((ObjectCreation) creation1).getAnonymousClassDeclaration() != null) {
//                    creations1.remove(objectCreation1);
//                }
            }
        }
        for (String objectCreation2 : creationMap2.keySet()) {
            for (Invocation creation2 : creationMap2.get(objectCreation2)) {
                if (creationCoveringTheEntireStatement2 != null &&
                        creationCoveringTheEntireStatement2.equalsSourceLocation(creation2)) {
                    creations2.remove(objectCreation2);
                }
//                if (((ObjectCreation) creation2).getAnonymousClassDeclaration() != null) {
//                    creations2.remove(objectCreation2);
//                }
            }
        }
        Set<String> creationIntersection = new LinkedHashSet<>(creations1);
        creationIntersection.retainAll(creations2);
        // remove common creations from the two sets
        creations1.removeAll(creationIntersection);
        creations2.removeAll(creationIntersection);

        return Map.of(0, creations1, 0, creations2);
    }

    private Map<String, String> replaceArgumentsWithVariables(SingleStatement statement1,
                                                              StatementDiff intersection,
                                                              ReplacementInfo replacementInfo) {
        final Map<String, String> variableToArgumentMap = new LinkedHashMap<>();
        final Set<String> arguments1 = intersection.arguments1;
        final Set<String> arguments2 = intersection.arguments2;
        final Set<String> unmatchedVariables1 = intersection.variables1;
        final Set<String> unmatchedVariables2 = intersection.variables2;

        if (!argumentsWithIdenticalMethodCalls(arguments1, arguments2, unmatchedVariables1, unmatchedVariables2)) {
            findAndPerformBestReplacements(arguments1, unmatchedVariables2, replacementInfo, ReplacementType.ARGUMENT_REPLACED_WITH_VARIABLE);

            // If the argument replacing the variable is a method call, use subclass of it
            Set<Replacement> replacementsToBeRemoved = new LinkedHashSet<>();
            Set<Replacement> replacementsToBeAdded = new LinkedHashSet<>();

            for (Replacement r : replacementInfo.getAppliedReplacements()) {
                variableToArgumentMap.put(r.getBefore(), r.getAfter());

                // IF the argument which was replaced was an invocation, change it to  a subclass of the replacement
                if (statement1.getMethodInvocationMap().containsKey(r.getBefore())) {
                    Replacement replacement = new VariableReplacementWithMethodInvocation(r.getBefore(),
                            r.getAfter(),
                            statement1.getMethodInvocationMap().get(r.getBefore()).get(0),
                            VariableReplacementWithMethodInvocation.Direction.INVOCATION_TO_VARIABLE);
                    replacementsToBeAdded.add(replacement);
                    replacementsToBeRemoved.add(r);
                }
            }
            replacementInfo.removeReplacements(replacementsToBeRemoved);
            replacementInfo.addReplacements(replacementsToBeAdded);
        }
        return variableToArgumentMap;
    }

    public void addArgumentsToInvocations(Map<String, List<? extends Invocation>> callMap, Set<String> calls,
                                          Map<String, String> parameterToArgumentMap) {
        if (isCallChain(callMap.values())) {
            for (String parameter : parameterToArgumentMap.keySet()) {
                String argument = parameterToArgumentMap.get(parameter);
                if (!parameter.equals(argument)) {
                    Set<String> toBeAdded = new LinkedHashSet<>();
                    for (String call : calls) {
                        String afterReplacement = ReplacementUtil.performArgumentReplacement(call, parameter, argument);
                        if (!call.equals(afterReplacement)) {
                            toBeAdded.add(afterReplacement);
                            List<? extends Invocation> oldCalls = callMap.get(call);
                            List<Invocation> newCalls = new ArrayList<>();
                            for (Invocation oldCall : oldCalls) {
                                Invocation newCall;

                                if (oldCall instanceof ObjectCreation) {
                                    newCall = createObjectCreationWithExpression((ObjectCreation) oldCall, parameter, argument);
                                } else {
                                    newCall = createFunctionInvocationWithExpression((OperationInvocation) oldCall,
                                            parameter, argument);
                                    newCalls.add(newCall);
                                }
                            }
                            callMap.put(afterReplacement, newCalls);
                        }
                    }
                    calls.addAll(toBeAdded);
                }
            }
        } else {
            Set<String> finalNewCalls = new LinkedHashSet<>();
            for (String parameter : parameterToArgumentMap.keySet()) {
                String argument = parameterToArgumentMap.get(parameter);
                if (!parameter.equals(argument)) {
                    Set<String> toBeAdded = new LinkedHashSet<>();
                    for (String call : calls) {
                        String afterReplacement = ReplacementUtil.performArgumentReplacement(call, parameter, argument);
                        if (!call.equals(afterReplacement)) {
                            toBeAdded.add(afterReplacement);
                            List<? extends Invocation> oldCalls = callMap.get(call);
                            List<Invocation> newCalls = new ArrayList<>();
                            for (Invocation oldCall : oldCalls) {
                                Invocation newCall;
                                if (oldCall instanceof ObjectCreation) {
                                    newCall = createObjectCreationWithExpression((ObjectCreation) oldCall, parameter, argument);
                                } else {
                                    newCall = createFunctionInvocationWithExpression((OperationInvocation) oldCall,
                                            parameter, argument);
                                    newCalls.add(newCall);
                                }
                            }
                            callMap.put(afterReplacement, newCalls);
                        }
                    }
                    finalNewCalls.addAll(toBeAdded);
                }
            }
            calls.addAll(finalNewCalls);
        }
    }

    private boolean isCallChain(Collection<List<? extends Invocation>> calls) {
        if (calls.size() > 1) {
            Invocation previous = null;
            Invocation current = null;
            int chainLength = 0;
            for (List<? extends Invocation> list : calls) {
                previous = current;
                current = list.get(0);
                if (current != null && previous != null) {
                    if (previous.getExpression() != null && previous.getExpression().equals(current.actualString())) {
                        chainLength++;
                    } else {
                        return false;
                    }
                }
            }
            if (chainLength == calls.size() - 1) {
                return true;
            }
        }
        return false;
    }

    private void findAndPerformBestReplacements(Set<String> strings1, Set<String> strings2, ReplacementInfo replacementInfo, ReplacementType type) {
        Map.Entry<TreeMap<Double, Set<Replacement>>, TreeMap<Double, Set<Replacement>>> bestReplacements = findBestReplacements(strings1, strings2, type, replacementInfo);
        performReplacementOnArgumentizedString1(bestReplacements.getKey(), bestReplacements.getValue(), replacementInfo);
    }

    /**
     * Returns the best replacements based on lowest edit distance on each round and all the replacements that reduces
     * edit distance on the original string
     *
     * @param strings1
     * @param strings2
     * @param type
     * @return
     */
    private Map.Entry<TreeMap<Double, Set<Replacement>>, TreeMap<Double, Set<Replacement>>> findBestReplacements(
            Set<String> strings1, Set<String> strings2
            , ReplacementType type, ReplacementInfo replacementInfo) /*throws RefactoringMinerTimedOutException*/ {
        TreeMap<Double, Set<Replacement>> globalReplacementMap = new TreeMap<>();
        TreeMap<Double, Set<Replacement>> allReplacementsWithLowerEditDistance = new TreeMap<>();

        boolean isSet2Bigger = strings1.size() <= strings2.size();
        final Set<String> setA = isSet2Bigger ? strings1 : strings2;
        final Set<String> setB = isSet2Bigger ? strings2 : strings1;

        for (String stringA : setA) {

            // Holds replacement map with their normalized edit distance sorted
            // TODO since only the first entry is considered use one
            TreeMap<Double, Replacement> replacementMap = new TreeMap<>();
            for (String stringB : setB) {
//                if (Thread.interrupted()) {
//                    throw new RefactoringMinerTimedOutException();
//                }
//                boolean containsMethodSignatureOfAnonymousClass1 = containsMethodSignatureOfAnonymousClass(s1);
//                boolean containsMethodSignatureOfAnonymousClass2 = containsMethodSignatureOfAnonymousClass(s2);
//
//
//                if (containsMethodSignatureOfAnonymousClass1 != containsMethodSignatureOfAnonymousClass2 &&
//                        operation1.getVariableDeclaration(s1) == null && operation2.getVariableDeclaration(s2) == null) {
//                    continue;
//                }
                String temp = ReplacementUtil.performReplacement(replacementInfo.getArgumentizedString1(), replacementInfo.getArgumentizedString2(), stringA, stringB);
                int editDistanceAfterReplacement = StringDistance.editDistance(temp, replacementInfo.getArgumentizedString2());

                // Check if string edit distance becomes smaller after the replacement
                if (editDistanceAfterReplacement >= 0 && editDistanceAfterReplacement < replacementInfo.getRawEditDistance()) {

                    // Store the normalized distance of this two strings
                    Replacement replacement = new Replacement(stringA, stringB, type);
                    double normalizedDistance = (double) editDistanceAfterReplacement / (double) Math.max(temp.length(), replacementInfo.getArgumentizedString2().length());
                    replacementMap.put(normalizedDistance, replacement);

                    if (allReplacementsWithLowerEditDistance.containsKey(normalizedDistance)) {
                        allReplacementsWithLowerEditDistance.get(normalizedDistance).add(replacement);
                    } else {
                        Set<Replacement> r = new LinkedHashSet<>();
                        r.add(replacement);
                        allReplacementsWithLowerEditDistance.put(normalizedDistance, r);
                    }

                    // If fully matched no need to for further replacement for this two mappings
                    if (editDistanceAfterReplacement == 0) {
                        break;
                    }
                }
            }
            // Check if replacement is found and take the best match one (lowest edit distance)
            if (!replacementMap.isEmpty()) {
                /// Take the best match one in each round (treemap is sorted by distance)
                Double normalizedDistance = replacementMap.firstEntry().getKey();
                Replacement replacement = replacementMap.firstEntry().getValue();
                if (globalReplacementMap.containsKey(normalizedDistance)) {
                    globalReplacementMap.get(normalizedDistance).add(replacement);
                } else {
                    Set<Replacement> r = new LinkedHashSet<>();
                    r.add(replacement);
                    globalReplacementMap.put(normalizedDistance, r);
                }
                if (normalizedDistance == 0) {
                    break;
                }
            }
        }

        return new AbstractMap.SimpleImmutableEntry<>(globalReplacementMap, allReplacementsWithLowerEditDistance);
    }

    private void performReplacementOnArgumentizedString1(TreeMap<Double, Set<Replacement>> bestReplacements, TreeMap<Double, Set<Replacement>> allReplacementsWithLowerEditDistance,
                                                         ReplacementInfo replacementInfo) {
        // Perform replacement
        String strAfterReplacement;
        // CHeck if atleast one replacement found and change the argumentize string
        if (!bestReplacements.isEmpty()) {
            Double normalizedDistance = bestReplacements.firstEntry().getKey();
            if (normalizedDistance == 0) {
                for (Replacement replacement : bestReplacements.firstEntry().getValue()) {

                    replacementInfo.addReplacement(replacement);
                    //replacementInfo.addReplacement(replacement);
                    //replacementInfo.setArgumentizedString1(ReplacementUtil
                    //      .performReplacement(this.getArgumentizedString1(), this.getArgumentizedString2(), replacement.getBefore(), replacement.getAfter()));
                    strAfterReplacement = ReplacementUtil.performReplacement(replacementInfo.getArgumentizedString1(), replacementInfo.getArgumentizedString2(), replacement.getBefore(), replacement.getAfter());
                    replacementInfo.setArgumentizedString1(strAfterReplacement);

                }
            } else {
                // If no identical match found
                Set<String> processedBefores = new LinkedHashSet<>();
                for (Set<Replacement> replacements : bestReplacements.values()) {
                    for (Replacement replacement : replacements) {

                        if (!processedBefores.contains(replacement.getBefore())) {

                            replacementInfo.addReplacement(replacement);
                            strAfterReplacement = ReplacementUtil.performReplacement(replacementInfo.getArgumentizedString1(), replacementInfo.getArgumentizedString2(),
                                    replacement.getBefore(), replacement.getAfter());
                            replacementInfo.setArgumentizedString1(strAfterReplacement);
                            processedBefores.add(replacement.getBefore());

                        } else {
                            //find the next best match for replacement.getAfter() from the replacement cache
                            for (Set<Replacement> replacements2 : allReplacementsWithLowerEditDistance.values()) {
                                for (Replacement replacement2 : replacements2) {
                                    if (replacement2.getAfter().equals(replacement.getAfter()) && !replacement2.equals(replacement)) {

                                        replacementInfo.addReplacement(replacement2);
                                        processedBefores.add(replacement2.getBefore());
                                        strAfterReplacement = ReplacementUtil.performReplacement(replacementInfo.getArgumentizedString1(), replacementInfo.getArgumentizedString2(),
                                                replacement2.getBefore(), replacement2.getAfter());
                                        replacementInfo.setArgumentizedString1(strAfterReplacement);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * TODO for Javascript
     **/
    public static boolean containsMethodSignatureOfAnonymousClass(String s) {
//        String[] lines = s.split("\\n");
//        if (s.contains(" -> ") && lines.length > 1) {
//            return true;
//        }
//        for (String line : lines) {
//            line = VariableReplacementAnalysis.prepareLine(line);
//            if (Visitor.METHOD_SIGNATURE_PATTERN.matcher(line).matches()) {
//                return true;
//            }
//        }
        return false;
    }

    private boolean argumentsWithIdenticalMethodCalls(Set<String> arguments1, Set<String> arguments2,
                                                      Set<String> variables1, Set<String> variables2) {
        int identicalMethodCalls = 0;
        if (arguments1.size() == arguments2.size()) {
            Iterator<String> it1 = arguments1.iterator();
            Iterator<String> it2 = arguments2.iterator();

            while (it1.hasNext() && it2.hasNext()) {
                String arg1 = it1.next();
                String arg2 = it2.next();
                if (arg1.contains("(") && arg2.contains("(") && arg1.contains(")") && arg2.contains(")")) {
                    int indexOfOpeningParenthesis1 = arg1.indexOf("(");
                    int indexOfClosingParenthesis1 = arg1.indexOf(")");
                    boolean openingParenthesisInsideSingleQuotes1 = isInsideSingleQuotes(arg1, indexOfOpeningParenthesis1);
                    boolean openingParenthesisInsideDoubleQuotes1 = isInsideDoubleQuotes(arg1, indexOfOpeningParenthesis1);
                    boolean closingParenthesisInsideSingleQuotes1 = isInsideSingleQuotes(arg1, indexOfClosingParenthesis1);
                    boolean closingParenthesisInsideDoubleQuotes1 = isInsideDoubleQuotes(arg1, indexOfClosingParenthesis1);
                    int indexOfOpeningParenthesis2 = arg2.indexOf("(");
                    int indexOfClosingParenthesis2 = arg2.indexOf(")");
                    boolean openingParenthesisInsideSingleQuotes2 = isInsideSingleQuotes(arg2, indexOfOpeningParenthesis2);
                    boolean openingParenthesisInsideDoubleQuotes2 = isInsideDoubleQuotes(arg2, indexOfOpeningParenthesis2);
                    boolean closingParenthesisInsideSingleQuotes2 = isInsideSingleQuotes(arg2, indexOfClosingParenthesis2);
                    boolean closingParenthesisInsideDoubleQuotes2 = isInsideDoubleQuotes(arg2, indexOfClosingParenthesis2);
                    if (!openingParenthesisInsideSingleQuotes1 && !closingParenthesisInsideSingleQuotes1 &&
                            !openingParenthesisInsideDoubleQuotes1 && !closingParenthesisInsideDoubleQuotes1 &&
                            !openingParenthesisInsideSingleQuotes2 && !closingParenthesisInsideSingleQuotes2 &&
                            !openingParenthesisInsideDoubleQuotes2 && !closingParenthesisInsideDoubleQuotes2) {
                        String s1 = arg1.substring(0, indexOfOpeningParenthesis1);
                        String s2 = arg2.substring(0, indexOfOpeningParenthesis2);
                        if (s1.equals(s2) && s1.length() > 0) {
                            String args1 = arg1.substring(indexOfOpeningParenthesis1 + 1, indexOfClosingParenthesis1);
                            String args2 = arg2.substring(indexOfOpeningParenthesis2 + 1, indexOfClosingParenthesis2);
                            if (variables1.contains(args1) && variables2.contains(args2)) {
                                identicalMethodCalls++;
                            }
                        }
                    }
                }
            }
        }
        return identicalMethodCalls == arguments1.size() && arguments1.size() > 0;
    }

    /**
     * Adds the arguments to the variables whcih are same as the parameter names
     */
    private void addArgumentsToVariables(Set<String> variables, Map<String, String> parameterToArgumentMap) {
        for (String parameter : parameterToArgumentMap.keySet()) {
            String argument = parameterToArgumentMap.get(parameter);
            if (variables.contains(parameter)) {
                variables.add(argument);
                if (argument.contains("(") && argument.contains(")")) {
                    int indexOfOpeningParenthesis = argument.indexOf("(");
                    int indexOfClosingParenthesis = argument.lastIndexOf(")");
                    boolean openingParenthesisInsideSingleQuotes = isInsideSingleQuotes(argument, indexOfOpeningParenthesis);
                    boolean closingParenthesisInsideSingleQuotes = isInsideSingleQuotes(argument, indexOfClosingParenthesis);
                    boolean openingParenthesisInsideDoubleQuotes = isInsideDoubleQuotes(argument, indexOfOpeningParenthesis);
                    boolean closingParenthesisIndideDoubleQuotes = isInsideDoubleQuotes(argument, indexOfClosingParenthesis);
                    if (indexOfOpeningParenthesis < indexOfClosingParenthesis &&
                            !openingParenthesisInsideSingleQuotes && !closingParenthesisInsideSingleQuotes &&
                            !openingParenthesisInsideDoubleQuotes && !closingParenthesisIndideDoubleQuotes) {
                        String arguments = argument.substring(indexOfOpeningParenthesis + 1, indexOfClosingParenthesis);
                        if (!arguments.isEmpty() && !arguments.contains(",") && !arguments.contains("(") && !arguments.contains(")")) {
                            variables.add(arguments);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns unmatched variables of each statements
     *
     * @return
     */
    private /*Map<SingleStatement, Set<String>>*/ Set<String> findCommonVariablesToBeAddedAsUnmatched(SingleStatement statement1, SingleStatement statement2,
                                                                                                      ReplacementInfo replacementInfo) {
        final Set<String> variables1 = new LinkedHashSet<>(statement1.getVariables());
        final Set<String> variables2 = new LinkedHashSet<>(statement2.getVariables());

        // 1. Find common variables
        final Set<String> commonVariables = new LinkedHashSet<>(variables1);
        commonVariables.retainAll(variables2);

        final OperationInvocation invocationCoveringTheEntireStatement1 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement1);
        final OperationInvocation invocationCoveringTheEntireStatement2 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement2);

        // 2. Some of the common variables are not common such as variable with "this" prefixed
        // Also we need to set the inovcations coverting entire statement as uncommon
        Set<String> variablesToBeRemovedFromCommon = new LinkedHashSet<>();
        for (String commonVariable : commonVariables) {
            //  ignore the variables in the intersection that also appear with "this." prefix in the sets of variables
            if (!commonVariable.startsWith("this.") && !commonVariables.contains("this." + commonVariable) &&
                    (variables1.contains("this." + commonVariable) || variables2.contains("this." + commonVariable))) {
                variablesToBeRemovedFromCommon.add(commonVariable);
            }

            // If both statements have same named invocation covering entire statement
            // Check if the arguments contain any of the common variables
            if (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null &&
                    invocationCoveringTheEntireStatement1.equalsInovkedFunctionName(invocationCoveringTheEntireStatement2)) {

                // If both invocation does not contain the common variable as an argument,
                // it should not be in the common variable

                // invocation 2 contains, but not invocation 1
                if (!invocationCoveringTheEntireStatement1.getArguments().contains(commonVariable) &&
                        invocationCoveringTheEntireStatement2.getArguments().contains(commonVariable)) {
                    if (invocationCoveringEntireStatementUsesVariableInArgument(invocationCoveringTheEntireStatement1,
                            commonVariable, replacementInfo.unMatchedStatements1)) {
                        variablesToBeRemovedFromCommon.add(commonVariable);
                    }
                } else if (invocationCoveringTheEntireStatement1.getArguments().contains(commonVariable) &&
                        !invocationCoveringTheEntireStatement2.getArguments().contains(commonVariable)) {
                    // invocation 1 contains, but not invocation 2

                    if (invocationCoveringEntireStatementUsesVariableInArgument(invocationCoveringTheEntireStatement2,
                            commonVariable, replacementInfo.unMatchedStatements2)) {
                        variablesToBeRemovedFromCommon.add(commonVariable);
                    }
                }
            }

            // TODO Purpose?
            if (commonVariable.toUpperCase().equals(commonVariable)
                    && !ReplacementUtil.sameCharsBeforeAfter(statement1.getText(), statement2.getText(), commonVariable)) {
                variablesToBeRemovedFromCommon.add(commonVariable);
            }
        }

        //commonVariables.removeAll(variablesToBeRemovedFromCommon);

        // remove common variables from the two sets
        //variables1.removeAll(commonVariables);
        //variables2.removeAll(commonVariables);
        //return Map.of(statement1, variables1, statement2, variables2);

        return variablesToBeRemovedFromCommon;
    }

    private boolean invocationCoveringEntireStatementUsesVariableInArgument(OperationInvocation invocationCoveringTheEntireStatement, String variable, List<SingleStatement> unMatchedStatements) {
        for (String argument : invocationCoveringTheEntireStatement.getArguments()) {
            String argumentNoWhiteSpace = argument.replaceAll("\\s", "");
            if (argument.contains(variable) && !argument.equals(variable)
                    && !argumentNoWhiteSpace.contains("+" + variable + "+")
                    && !argumentNoWhiteSpace.contains(variable + "+")
                    && !argumentNoWhiteSpace.contains("+" + variable)
                    && !nonMatchedStatementUsesVariableInArgument(unMatchedStatements, variable, argument)) {
                return true;
            }
        }
        return false;
    }

    private boolean nonMatchedStatementUsesVariableInArgument(List<SingleStatement> statements, String variable, String otherArgument) {
        for (SingleStatement statement : statements) {
            OperationInvocation invocation = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement);
            if (invocation != null) {
                for (String argument : invocation.getArguments()) {
                    String argumentNoWhiteSpace = argument.replaceAll("\\s", "");
                    if (!argument.equals(variable)
                            && !argument.equals(otherArgument)
                            && argument.contains(variable)
                            && !argumentNoWhiteSpace.contains("+" + variable + "+")
                            && !argumentNoWhiteSpace.contains(variable + "+")
                            && !argumentNoWhiteSpace.contains("+" + variable)
                    ) {
                        return true;
                    }
                }
            }
        }
        return false;
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

    private int inconsistentVariableMappingCount(SingleStatement statement1, SingleStatement statement2, VariableDeclaration v1, VariableDeclaration v2) {
        int count = 0;
//        if (v1 != null && v2 != null) {
//            for (AbstractCodeMapping mapping : mappings) {
//                List<VariableDeclaration> variableDeclarations1 = mapping.getFragment1().getVariableDeclarations();
//                List<VariableDeclaration> variableDeclarations2 = mapping.getFragment2().getVariableDeclarations();
//                if (variableDeclarations1.contains(v1) &&
//                        variableDeclarations2.size() > 0 &&
//                        !variableDeclarations2.contains(v2)) {
//                    count++;
//                }
//                if (variableDeclarations2.contains(v2) &&
//                        variableDeclarations1.size() > 0 &&
//                        !variableDeclarations1.contains(v1)) {
//                    count++;
//                }
//                if (mapping.isExact()) {
//                    boolean containsMapping = true;
//                    if (statement1 instanceof CompositeStatementObject && statement2 instanceof CompositeStatementObject &&
//                            statement1.getLocationInfo().getCodeElementType().equals(CodeElementType.ENHANCED_FOR_STATEMENT)) {
//                        CompositeStatementObject comp1 = (CompositeStatementObject) statement1;
//                        CompositeStatementObject comp2 = (CompositeStatementObject) statement2;
//                        containsMapping = comp1.contains(mapping.getFragment1()) && comp2.contains(mapping.getFragment2());
//                    }
//                    if (containsMapping && (VariableReplacementAnalysis.bothFragmentsUseVariable(v1, mapping) || VariableReplacementAnalysis.bothFragmentsUseVariable(v2, mapping))) {
//                        count++;
//                    }
//                }
//            }
//        }
        return count;
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

    private static boolean isInsideSingleQuotes(String argument, int indexOfChar) {
        if (indexOfChar > 0 && indexOfChar < argument.length() - 1) {
            return argument.charAt(indexOfChar - 1) == '\'' &&
                    argument.charAt(indexOfChar + 1) == '\'';
        }
        return false;
    }

    private static boolean isInsideDoubleQuotes(String argument, int indexOfChar) {
        Matcher m = DOUBLE_QUOTES.matcher(argument);
        while (m.find()) {
            if (m.group(1) != null) {
                if (indexOfChar > m.start() && indexOfChar < m.end()) {
                    return true;
                }
            }
        }
        return false;
    }

    private ObjectCreation createObjectCreationWithExpression(ObjectCreation objectCreation, String oldExpression, String newExpression) {
        ObjectCreation newObjectCreation = new ObjectCreation();
        newObjectCreation.setType(objectCreation.getType());
        newObjectCreation.setSourceLocation(objectCreation.getSourceLocation());
        updateCall(objectCreation, newObjectCreation, oldExpression, newExpression);
        return newObjectCreation;
    }

    private OperationInvocation createFunctionInvocationWithExpression(OperationInvocation operationInvocation, String oldExpression, String newExpression) {
        OperationInvocation newOperationInvocation = new OperationInvocation();
        newOperationInvocation.setFunctionName(operationInvocation.getFunctionName());
        newOperationInvocation.setSourceLocation(operationInvocation.getSourceLocation());
        updateCall(operationInvocation, newOperationInvocation, oldExpression, newExpression);
//        newOperationInvocation.subExpressions = new ArrayList<String>();
//        for (String argument : this.subExpressions) {
//            newOperationInvocation.subExpressions.add(
//                    ReplacementUtil.performReplacement(argument, oldExpression, newExpression));
//        }
        return newOperationInvocation;
    }

    void updateCall(Invocation thisCall, Invocation newCall, String oldExpression, String newExpression) {
        //newCall.typeArguments = this.typeArguments;
        if (thisCall.getExpression() != null && thisCall.getExpression().equals(oldExpression)) {
            newCall.setExpression(newExpression);
        } else {
            newCall.setExpression(thisCall.getExpression());
        }
        newCall.setArguments(new ArrayList<>());
        for (String argument : thisCall.getArguments()) {
            newCall.getArguments().add(
                    ReplacementUtil.performReplacement(argument, oldExpression, newExpression));
        }
    }

    protected boolean expressionsContainInitializerOfVariableDeclaration(Set<String> expressions, SingleStatement statement) {
        Map<String, VariableDeclaration> variableDeclarations = statement.getVariableDeclarations();

        if (variableDeclarations.size() == 1) {
            VariableDeclaration vd = variableDeclarations.entrySet().iterator().next().getValue();
            Expression initializer = vd.getInitializer();
            return initializer != null && expressions.contains(initializer);
        }
//
//        if (variableDeclarations.size() == 1 && variableDeclarations.values().stream().findFirst().getInitializer() != null) {
//            String initializer = variableDeclarations.get(0).getInitializer().toString();
//            if (expressions.contains(initializer)) {
//                return true;
//            }
//        }
        return false;
    }

    private Set<Replacement> replacementsWithinMethodInvocations(String s1, String s2
            , Set<String> set1, Set<String> set2
            , Map<String, List<? extends Invocation>> methodInvocationMap
            , Direction direction) {
        Set<Replacement> replacements = new LinkedHashSet<>();
        for (String element1 : set1) {
            if (s1.contains(element1) && !s1.equals(element1) && !s1.equals("this." + element1) && !s1.equals("_" + element1)) {
                int startIndex1 = s1.indexOf(element1);
                String substringBeforeIndex1 = s1.substring(0, startIndex1);
                String substringAfterIndex1 = s1.substring(startIndex1 + element1.length(), s1.length());
                for (String element2 : set2) {
                    if (element2.endsWith(substringAfterIndex1) && substringAfterIndex1.length() > 1) {
                        element2 = element2.substring(0, element2.indexOf(substringAfterIndex1));
                    }
                    if (s2.contains(element2) && !s2.equals(element2)) {
                        int startIndex2 = s2.indexOf(element2);
                        String substringBeforeIndex2 = s2.substring(0, startIndex2);
                        String substringAfterIndex2 = s2.substring(startIndex2 + element2.length(), s2.length());
                        List<? extends Invocation> methodInvocationList = null;
                        if (direction.equals(Direction.VARIABLE_TO_INVOCATION))
                            methodInvocationList = methodInvocationMap.get(element2);
                        else if (direction.equals(Direction.INVOCATION_TO_VARIABLE))
                            methodInvocationList = methodInvocationMap.get(element1);
                        if (substringBeforeIndex1.equals(substringBeforeIndex2) && !substringAfterIndex1.isEmpty() && !substringAfterIndex2.isEmpty() && methodInvocationList != null) {
                            Replacement r = new VariableReplacementWithMethodInvocation(element1, element2, (OperationInvocation) methodInvocationList.get(0), direction);
                            replacements.add(r);
                        } else if (substringAfterIndex1.equals(substringAfterIndex2) && !substringBeforeIndex1.isEmpty() && !substringBeforeIndex2.isEmpty() && methodInvocationList != null) {
                            Replacement r = new VariableReplacementWithMethodInvocation(element1, element2, (OperationInvocation) methodInvocationList.get(0), direction);
                            replacements.add(r);
                        }
                    }
                }
            }
        }
        return replacements;
    }

    private Replacement variableReplacementWithinMethodInvocations(String s1, String s2
            , Set<String> variables1, Set<String> variables2) {
        for (String variable1 : variables1) {
            if (s1.contains(variable1) && !s1.equals(variable1) && !s1.equals("this." + variable1) && !s1.equals("_" + variable1)) {
                int startIndex1 = s1.indexOf(variable1);
                String substringBeforeIndex1 = s1.substring(0, startIndex1);
                String substringAfterIndex1 = s1.substring(startIndex1 + variable1.length(), s1.length());

                for (String variable2 : variables2) {
                    if (variable2.endsWith(substringAfterIndex1) && substringAfterIndex1.length() > 1) {
                        variable2 = variable2.substring(0, variable2.indexOf(substringAfterIndex1));
                    }
                    if (s2.contains(variable2) && !s2.equals(variable2)) {
                        int startIndex2 = s2.indexOf(variable2);
                        String substringBeforeIndex2 = s2.substring(0, startIndex2);
                        String substringAfterIndex2 = s2.substring(startIndex2 + variable2.length(), s2.length());
                        if (substringBeforeIndex1.equals(substringBeforeIndex2) && substringAfterIndex1.equals(substringAfterIndex2)) {
                            return new Replacement(variable1, variable2, ReplacementType.VARIABLE_NAME);
                        }
                    }
                }
            }
        }
        return null;
    }
}
