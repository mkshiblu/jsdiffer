package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.api.RefactoringMinerTimedOutException;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.jsrminer.uml.diff.*;
import io.jsrminer.uml.mapping.Argumentizer;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.replacers.BooleanReplacer;
import io.jsrminer.uml.mapping.replacement.replacers.TernaryExpressionReplacer;
import io.jsrminer.uml.mapping.replacement.replacers.VariablesAndFunctionInvocationsReplacer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.jsrminer.sourcetree.JsConfig.ENABLE_SEARCH_FOR_ANONYMOUS_SIGNATURE_DURING_REPLACEMENT;
import static io.jsrminer.uml.mapping.replacement.VariableReplacementWithMethodInvocation.Direction;

public class ReplacementFinder2 {

    private static final Pattern DOUBLE_QUOTES = Pattern.compile("\"([^\"]*)\"|(\\S+)");
    private static final Pattern SPLIT_CONDITIONAL_PATTERN = Pattern.compile("(\\|\\|)|(&&)|(\\?)|(:)");

    final FunctionDeclaration function1;
    final FunctionDeclaration function2;
    final FunctionBodyMapper bodyMapper;
    final ContainerDiff sourceFileDiff;

    public ReplacementFinder2(FunctionBodyMapper bodyMapper, ContainerDiff sourceFileDiff) {
        this.bodyMapper = bodyMapper;
        this.function1 = bodyMapper.function1;
        this.function2 = bodyMapper.function2;
        this.sourceFileDiff = sourceFileDiff;
    }

    private ReplacementInfo createReplacementInfo(CodeFragment statement1
            , CodeFragment statement2
            , Set<? extends CodeFragment> statements1
            , Set<? extends CodeFragment> statements2) {
        List<? extends CodeFragment> unmatchedStatments1 = new ArrayList<>(statements1);
        unmatchedStatments1.remove(statement1);
        List<? extends CodeFragment> unmatchedStatements2 = new ArrayList<>(statements2);
        unmatchedStatements2.remove(statement2);
        return new ReplacementInfo(
                this.bodyMapper.createArgumentizedString(statement1, statement2),
                this.bodyMapper.createArgumentizedString(statement2, statement1),
                unmatchedStatments1, unmatchedStatements2);
    }

    public Set<Replacement> findReplacementsWithExactMatching(CodeFragment statement1
            , CodeFragment statement2
            , Map<String, String> parameterToArgumentMap
            , Set<? extends CodeFragment> statements1
            , Set<? extends CodeFragment> statements2
            , Argumentizer argumentizer) {

        // Since anonymous functions or class can be potentially huge, we need to separately match them
        if (statement1.getAnonymousFunctionDeclarations().size() > 0 || statement2.getAnonymousFunctionDeclarations().size() > 0) {
            AnonymousFunctionReplacementFinder anonymousReplacer = new AnonymousFunctionReplacementFinder(parameterToArgumentMap, this.bodyMapper);
            return anonymousReplacer.replaceInAnonymousFunctions(statement1, statement2, function1, function2);
        }

        ReplacementInfo replacementInfo = createReplacementInfo(statement1, statement2, statements1, statements2);
        final CodeFragmentDiff diff = new CodeFragmentDiff(statement1, statement2);

        // Intersect variables
        final Set<String> variables1 = new LinkedHashSet<>(statement1.getVariables());
        final Set<String> variables2 = new LinkedHashSet<>(statement2.getVariables());
        Set<String> unmatchedCommonVariables = findCommonVariablesToBeAddedAsUnmatched(statement1, statement2, replacementInfo, variables1, variables2);
        diff.variables1.addAll(unmatchedCommonVariables);
        diff.variables2.addAll(unmatchedCommonVariables);

        // replace unmatched variables with the corresponding arguments (Argumentize?)
        // Add parameters to unmatched?
        addArgumentsToVariables(diff.variables1, parameterToArgumentMap);
        addArgumentsToVariables(diff.variables2, parameterToArgumentMap);

        // Find replacements in various rounds

        // 1. Argument replacement with variables
        // Perform replacements of  all the arguments in s1 with variables in s2 (no need call for anonymous)
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
        Set<String> methodInvocationIntersection = unmatchedFunctionsMap.get(2);

        //3. Similar to java type replacements, do kind replacmentss (i.e. let, var)
        intersectAndReplaceVariableDeclarationsKind(statement1, statement2, replacementInfo);

        // 4. Replace infixes
        intersectAndReplaceInfixOperators(statement1, statement2, replacementInfo);

        // Find all variables and invocations
        Map<Integer, Set<String>> variablesAndFunctionInvocationsMap
                = getVariablesAndFunctionInvocations(diff.variables1, diff.variables2,
                methodInvocationMap1, functionInvocations1, functionInvocations2,
                replacementInfo);

        Set<String> variablesAndMethodInvocations1 = variablesAndFunctionInvocationsMap.get(0);
        Set<String> variablesAndMethodInvocations2 = variablesAndFunctionInvocationsMap.get(1);

        // 5. Replace variables and functioninvocations
        if (replacementInfo.getRawEditDistance() > 0) {
            new VariablesAndFunctionInvocationsReplacer()
                    .replaceVariablesAndFunctionInvocations(statement1, statement2
                    , parameterToArgumentMap, replacementInfo
                    , diff.variables1, diff.variables2
                    , methodInvocationMap1, methodInvocationMap2
                    , functionInvocations1, functionInvocations2
                    , variablesAndMethodInvocations1, variablesAndMethodInvocations2
                    , bodyMapper.getMappings()
                    , function2);
        }
        //3. replace variables with the corresponding arguments in object creations
        Map<String, List<? extends Invocation>> creationMap1 = new LinkedHashMap<>(statement1.getCreationMap());
        Map<String, List<? extends Invocation>> creationMap2 = new LinkedHashMap<>(statement2.getCreationMap());
        Map<Integer, Set<String>> creationsMapResult = intersectAndReplaceObjectCreations(statement1, statement2,
                parameterToArgumentMap, replacementInfo
                , variableToArgumentMap
                , creationMap1,
                creationMap2);

        Set<String> creations1 = creationsMapResult.get(0);
        Set<String> creations2 = creationsMapResult.get(1);
        Set<String> creationIntersection = creationsMapResult.get(2);

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
        // Replace ternary expressions
        TernaryExpressionReplacer.apply(statement1, statement2, replacementInfo, this);

        // Replace booleans
        BooleanReplacer.apply(statement1, statement2, replacementInfo, diff, this);

        String[] argumentizedStrings = filterReplacements(statement1, statement2
                , replacementInfo, argumentizer
                , diff, methodInvocationMap1, methodInvocationMap2
                , functionInvocations1, functionInvocations2);

        String s1 = argumentizedStrings[0];
        String s2 = argumentizedStrings[1];

        List<VariableDeclaration> variableDeclarations1 = new ArrayList<>(statement1.getVariableDeclarations());
        List<VariableDeclaration> variableDeclarations2 = new ArrayList<>(statement2.getVariableDeclarations());

        boolean isEqualWithReplacement = s1.equals(s2)
                || replacementInfo.getArgumentizedString1().equals(replacementInfo.getArgumentizedString2())
                || differOnlyInCastExpressionOrPrefixOperator(s1, s2, replacementInfo)
                || oneIsVariableDeclarationTheOtherIsVariableAssignment(s1, s2, replacementInfo)
                || identicalVariableDeclarationsWithDifferentNames(s1, s2, variableDeclarations1, variableDeclarations2, replacementInfo)
                || oneIsVariableDeclarationTheOtherIsReturnStatement(s1, s2)
                || oneIsVariableDeclarationTheOtherIsReturnStatement(statement1.getText(), statement2.getText())
                || (commonConditional(s1, s2, replacementInfo) && containsValidOperatorReplacements(replacementInfo))
                || equalAfterArgumentMerge(s1, s2, replacementInfo)
                || equalAfterNewArgumentAdditions(s1, s2, replacementInfo)
                /*|| (validStatementForConcatComparison(statement1, statement2) && commonConcat(s1, s2, replacementInfo))*/;

        if (isEqualWithReplacement) {
            if (variableDeclarationsWithEverythingReplaced(variableDeclarations1, variableDeclarations2, replacementInfo)
                    && !statement1.getCodeElementType().equals(CodeElementType.ENHANCED_FOR_STATEMENT)
                    && !statement2.getCodeElementType().equals(CodeElementType.ENHANCED_FOR_STATEMENT)) {
                return null;
            }
            if (variableAssignmentWithEverythingReplaced(statement1, statement2, replacementInfo)) {
                return null;
            }
            if (classInstanceCreationWithEverythingReplaced(statement1, statement2, replacementInfo, parameterToArgumentMap)) {
                return null;
            }

            // TODO anonymous
            //checkMethodInvocatioRepalcementInsideAnonymousFunctionDeclarations(statement1, statement2, replacementInfo);
            return replacementInfo.getReplacements();
        }


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

        // If statements cannot be matched with 1 to 1 AST replacement, apply heuristics
        boolean isMatchedByHeuristics = applyHeuristics(statement1, statement2
                , replacementInfo
                , methodInvocationMap1
                , methodInvocationMap2
                , parameterToArgumentMap
                , functionInvocations1
                , functionInvocations2
                , s1
                , s2
                , creations1
                , creations2
                , creationMap1
                , creationMap2
                , creationIntersection
                , methodInvocationIntersection
                , variables1
        );

        return (isEqualWithReplacement || isMatchedByHeuristics) ? replacementInfo.getReplacements() : null;
    }

    // private void checkMethodInvocatioRepalcementInsideAnonymousFunctionDeclarations(CodeFragment statement1, CodeFragment statement2, ReplacementInfo replacementInfo) {
//        List<IAnonymousFunctionDeclaration> anonymousFunction1 = statement1.getAnonymousFunctionDeclarations();
//        List<IAnonymousFunctionDeclaration> anonymousFunction2 = statement2.getAnonymousFunctionDeclarations();
//
//        if (!anonymousFunction1.isEmpty() && !anonymousFunction2.isEmpty()) {
//            Set<Replacement> replacementsInsideAnonymous = new LinkedHashSet<>();
//            for (Replacement replacement : replacementInfo.getReplacements()) {
//                if (replacement instanceof MethodInvocationReplacement) {
//                    for (int i = 0; i < anonymousFunction1.size(); i++) {
//                        for (int j = 0; j < anonymousFunction2.size(); j++) {
//                            IAnonymousFunctionDeclaration anonymousClassDeclaration1 = anonymousFunction1.get(i);
//                            IAnonymousFunctionDeclaration anonymousClassDeclaration2 = anonymousFunction2.get(j);
//
//                            if (anonymousClassDeclaration1.getMethodInvocationMap().containsKey(replacement.getBefore()) &&
//                                    anonymousClassDeclaration2.getMethodInvocationMap().containsKey(replacement.getAfter())) {
//                                replacementsInsideAnonymous.add(replacement);
//                                break;
//                            }
//                        }
//                        if (replacementsInsideAnonymous.contains(replacement)) {
//                            break;
//                        }
//                    }
//                }
//            }
//            for (Replacement replacement : replacementsInsideAnonymous) {
//                equalAfterNewArgumentAdditions(replacement.getBefore(), replacement.getAfter(), replacementInfo);
//            }
//        }
    //}

    private boolean applyHeuristics(CodeFragment statement1, CodeFragment statement2
            , ReplacementInfo replacementInfo
            , Map<String, List<? extends Invocation>> methodInvocationMap1
            , Map<String, List<? extends Invocation>> methodInvocationMap2
            , Map<String, String> parameterToArgumentMap
            , Set<String> methodInvocations1
            , Set<String> methodInvocations2
            , String s1, String s2
            , Set<String> creations1
            , Set<String> creations2
            , Map<String, List<? extends Invocation>> creationMap1
            , Map<String, List<? extends Invocation>> creationMap2
            , Set<String> methodInvocationIntersection
            , Set<String> creationIntersection
            , Set<String> variables1) {

        ReplacementHeuristic heuristic = new ReplacementHeuristic(statement1, statement2, replacementInfo);

        final OperationInvocation invocationCoveringTheEntireStatement1 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement1);
        final OperationInvocation invocationCoveringTheEntireStatement2 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement2);

        OperationInvocation assignmentInvocationCoveringTheEntireStatement1 =
                invocationCoveringTheEntireStatement1 == null
                        ? InvocationCoverage.INSTANCE.assignmentInvocationCoveringEntireStatement(statement1)
                        : invocationCoveringTheEntireStatement1;

        //method invocation is identical
        boolean heuristicsMatched = ReplacementHeuristic.isIdenticalMethodInvocation(assignmentInvocationCoveringTheEntireStatement1,
                methodInvocationMap1, replacementInfo, statement1, statement2);
        if (heuristicsMatched)
            return true;

//        //method invocation is identical with a difference in the expression call chain
        heuristicsMatched = ReplacementHeuristic.functionInvocationIsIdenticalWithADifferenceInExpression(statement1, statement2, replacementInfo
                , methodInvocationMap1, methodInvocationMap2);

        if (heuristicsMatched)
            return true;

//        //method invocation is identical if arguments are replaced
        heuristicsMatched = ReplacementHeuristic.invocationIsIdenticalIfArgumentsAreReplaced(statement1, statement2, replacementInfo, invocationCoveringTheEntireStatement1,
                invocationCoveringTheEntireStatement2, methodInvocationMap2);
        if (heuristicsMatched)
            return true;

        //method invocation is identical if arguments are wrapped or concatenated
        heuristicsMatched = heuristic.invasionsIdenticalIfArgumentsAreWrappedOrConcatenated(invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, methodInvocationMap2);

        if (heuristicsMatched)
            return true;

//        //method invocation has been renamed but the expression and arguments are identical
        heuristicsMatched = heuristic.invocationRenamedButIdenticalExpressionAndArguments(invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2);
        if (heuristicsMatched)
            return true;

//        //method invocation has been renamed but the expressions are null and arguments are identical (inLamdba)?
        //heuristicsMatched = heuristic.invocationRenamedButNoExpressionAndIdenticalArguments(invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, lamndaMappers);
//        if (heuristicsMatched)
//            return true;
//

        //method invocation has been renamed (one name contains the other), one expression is null, but the other is not null, and arguments are identical
        heuristicsMatched = heuristic.invocationRenamedOneExpressionIsNullAndidenticalArguments(invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2);
        if (heuristicsMatched)
            return true;


        //method invocation has been renamed and arguments changed, but the expressions are identical
//        heuristicsMatched = heuristic.invocationRenmadeArgumentsChangedButIdenticalExpressions(invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2, lamdaMappers);
//        if (heuristicsMatched)
//            return true;

//
//        if(!methodInvocations1.isEmpty() && invocationCoveringTheEntireStatement2 != null) {
//            for(String methodInvocation1 : methodInvocations1) {
//                for(Invocation operationInvocation1 : methodInvocationMap1.get(methodInvocation1)) {
//                    if(operationInvocation1.renamedWithIdenticalExpressionAndDifferentNumberOfArguments(invocationCoveringTheEntireStatement2, replacementInfo.getReplacements()
//                            , ContainerDiffer.MAX_OPERATION_NAME_DISTANCE, lambdaMappers) &&
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
        heuristicsMatched = heuristic.invocationHasDifferentArgumentsCount(
                invocationCoveringTheEntireStatement1
                , invocationCoveringTheEntireStatement2
                , parameterToArgumentMap
                , methodInvocationMap1
                , methodInvocations1);
        if (heuristicsMatched)
            return true;

//        //check if the argument of the method call in the first statement is returned in the second statement
        heuristicsMatched = heuristic.argumentIsReturnedInSecondStatement(invocationCoveringTheEntireStatement1
                , methodInvocationMap1
                , methodInvocations1);

        if (heuristicsMatched)
            return true;

        //check if the argument of the method call in the second statement is returned in the first statement
        heuristicsMatched = heuristic.argumentIsReturnedInFirstStatment(invocationCoveringTheEntireStatement2, methodInvocationMap2, methodInvocations2);
        if (heuristicsMatched)
            return true;

//        //check if the argument of the method call in the second statement is the right hand side of an assignment in the first statement
        heuristicsMatched = heuristic.argumentIsOnRightHandSideInSecondStatement(invocationCoveringTheEntireStatement2
                , methodInvocationMap1);
        if (heuristicsMatched)
            return true;

//        //check if the method call in the second statement is the expression of the method invocation in the first statement
        heuristicsMatched = heuristic.invocatioInSecondIsExpressionOfInvocationInFirst(invocationCoveringTheEntireStatement2
                , methodInvocationMap1, methodInvocationMap2);

        if (heuristicsMatched)
            return true;

//        //check if the method call in the first statement is the expression of the method invocation in the second statement
        heuristicsMatched = heuristic.invocationInFirstIsExpressionOfInvocationInSecond(invocationCoveringTheEntireStatement1, methodInvocationMap1, methodInvocationMap2);
        if (heuristicsMatched)
            return true;

//        //check if the argument of the class instance creation in the first statement is the expression of the method invocation in the second statement
        ObjectCreation creationCoveringTheEntireStatement1 = InvocationCoverage.INSTANCE.creationCoveringEntireFragment(statement1);
        heuristicsMatched = heuristic.argumentOfCreationIsExpressionOfInvocationInSecond(creationCoveringTheEntireStatement1, methodInvocationMap2);
        if (heuristicsMatched)
            return true;

//        //builder call chain in the first statement is replaced with class instance creation in the second statement
        ObjectCreation creationCoveringTheEntireStatement2 = InvocationCoverage.INSTANCE.creationCoveringEntireFragment(statement2);
        heuristicsMatched = heuristic.builderCallChainReplacedWithClassInstanceCreation(invocationCoveringTheEntireStatement1
                , creationCoveringTheEntireStatement2, methodInvocationMap1);
        if (heuristicsMatched)
            return true;

//        //object creation is identical
//        heuristicsMatched = heuristic.identicalObjectCreations(creationCoveringTheEntireStatement1
//                , creationCoveringTheEntireStatement2, methodInvocationMap1);
//        if (heuristicsMatched)
//            return true;


//        //object creation has only changes in the arguments
        heuristicsMatched = heuristic.objectCreationIsDifferentOnlyByArguments(creationCoveringTheEntireStatement1
                , creationCoveringTheEntireStatement2, parameterToArgumentMap);
        if (heuristicsMatched)
            return true;

//        //check if the argument lists are identical after replacements
        heuristicsMatched = heuristic.creationArgumentsAreIdenticalAfterReplacements(creationCoveringTheEntireStatement1
                , creationCoveringTheEntireStatement2,
                s1, s2);
        if (heuristicsMatched)
            return true;

        //check if array creation is replaced with data structure creation
        boolean isCreationReplacedWithArrayDeclaration = ReplacementHeuristic.isObjectCreationReplacedWithArrayDeclaration(statement1, statement2, replacementInfo);
        if (isCreationReplacedWithArrayDeclaration) {
            return true;
        }

        heuristicsMatched = heuristic.addtionalCreationHeuristics(creations1, creationMap1
                , creationCoveringTheEntireStatement1, creationCoveringTheEntireStatement2
                , s1, s2
                , parameterToArgumentMap);

        if (heuristicsMatched)
            return true;


        heuristicsMatched = heuristic.variableDeclarationsHeuristics(s1, s2
                , invocationCoveringTheEntireStatement1, invocationCoveringTheEntireStatement2);
        if (heuristicsMatched)
            return true;

        heuristicsMatched = heuristic.ternaryExpressionHeuristics(invocationCoveringTheEntireStatement1
                , invocationCoveringTheEntireStatement2
                , creationCoveringTheEntireStatement1
                , creationCoveringTheEntireStatement2
                , creations1
                , creations2
                , creationIntersection
                , creationMap1
                , creationMap2
                , methodInvocationIntersection);
        if (heuristicsMatched)
            return true;


        heuristicsMatched = heuristic.fieldAssignentReplacedWithSetter(invocationCoveringTheEntireStatement2, variables1);

        return heuristicsMatched;
    }

    private String[] filterReplacements(CodeFragment statement1, CodeFragment statement2
            , ReplacementInfo replacementInfo, Argumentizer argumentizer
            , CodeFragmentDiff diff, Map<String, List<? extends Invocation>> methodInvocationMap1
            , Map<String, List<? extends Invocation>> methodInvocationMap2
            , Set<String> functionInvocations1, Set<String> functionInvocations2) {

        String s1 = argumentizer.getArgumentizedString(statement1);
        String s2 = argumentizer.getArgumentizedString(statement2);

        LinkedHashSet<Replacement> replacementsToBeRemoved = new LinkedHashSet<>();
        LinkedHashSet<Replacement> replacementsToBeAdded = new LinkedHashSet<>();

        for (Replacement replacement : replacementInfo.getReplacements()) {
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

        return new String[]{s1, s2};
    }

    private void replaceArrayAccess(ReplacementInfo replacementInfo, CodeFragmentDiff diff, Set<String> functionInvocations1, Set<String> functionInvocations2) {
        // Replace variables With arrayAccess and ViceVersa
        findAndPerformBestReplacements(diff.variables1, diff.arrayAccesses2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_ARRAY_ACCESS);
        findAndPerformBestReplacements(diff.arrayAccesses1, diff.variables2, replacementInfo, ReplacementType.VARIABLE_REPLACED_WITH_ARRAY_ACCESS);

        // Replace functionInvocations With arrayAccess and ViceVersa
        findAndPerformBestReplacements(functionInvocations1, diff.arrayAccesses2, replacementInfo, ReplacementType.ARRAY_ACCESS_REPLACED_WITH_METHOD_INVOCATION);
        findAndPerformBestReplacements(diff.arrayAccesses1, functionInvocations2, replacementInfo, ReplacementType.ARRAY_ACCESS_REPLACED_WITH_METHOD_INVOCATION);
    }

    private void replaceLiterals(CodeFragment statement1, CodeFragment statement2, ReplacementInfo replacementInfo, CodeFragmentDiff intersection) {
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

    private Map<Integer, Set<String>> intersectAndReplaceObjectCreations(CodeFragment statement1, CodeFragment statement2, Map<String, String> parameterToArgumentMap
            , ReplacementInfo replacementInfo
            , Map<String, String> variableToArgumentMap,
                                                                         Map<String, List<? extends Invocation>> creationMap1, Map<String, List<? extends Invocation>> creationMap2) {
        Map<Integer, Set<String>> objectCreationsMap = intersectObjectCreations(statement1, statement2,
                parameterToArgumentMap, variableToArgumentMap, creationMap1, creationMap2);
        Set<String> creations1 = objectCreationsMap.get(0);
        Set<String> creations2 = objectCreationsMap.get(1);
        Set<String> creationIntersection = objectCreationsMap.get(2);
        findAndPerformBestReplacements(creations1, creations2, replacementInfo, ReplacementType.CLASS_INSTANCE_CREATION);
        return Map.of(0, creations1, 1, creations2, 2, creationIntersection);
    }


    private void intersectAndReplaceVariableDeclarationsKind(CodeFragment statement1, CodeFragment statement2, ReplacementInfo replacementInfo) {

//////        Set<String> types1 = new LinkedHashSet<String>(statement1.getTypes());
////        Set<String> types2 = new LinkedHashSet<String>(statement2.getTypes());
////        removeCommonTypes(types1, types2, statement1.getTypes(), statement2.getTypes());
////        //perform type replacements
////        findReplacements(types1, types2, replacementInfo, ReplacementType.TYPE);
        // Intersect
        Map<CodeFragment, Set<String>> unmatchedKinds = intersectVariableDeclarationsKind(statement1, statement2);
        Set<String> kinds1 = unmatchedKinds.get(statement1);
        Set<String> kinds2 = unmatchedKinds.get(statement2);

        //perform kind replacements
        findAndPerformBestReplacements(kinds1, kinds2, replacementInfo, ReplacementType.KIND);
    }

    private Map<CodeFragment, Set<String>> intersectVariableDeclarationsKind(CodeFragment statement1, CodeFragment statement2) {
        List<String> kindList1 = statement1.getVariableDeclarations()
                .stream()
                .filter(vd -> vd.getKind() != null)
                .map(vd -> vd.getKind().keywordName)
                .collect(Collectors.toList());

        List<String> kindsList2 = statement2.getVariableDeclarations()
                .stream()
                .filter(vd -> vd.getKind() != null)
                .map(vd -> vd.getKind().keywordName)
                .collect(Collectors.toList());

        Set<String> kinds1 = new LinkedHashSet<>(kindList1);
        Set<String> kinds2 = new LinkedHashSet<>(kindsList2);

        // If number of kinds are same, then keep the unequal kinds of same position
        // as unmatched and therefore available for replacement
        if (kindList1.size() == kindsList2.size()) {
            Set<String> unequalKindsInSamePositionIndex = new LinkedHashSet<>();
            for (int i = 0; i < kindList1.size(); i++) {
                String kind1 = kindList1.get(i);
                String kind2 = kindsList2.get(i);
                if (!(kind1 != null && kind1.equals(kind2))) {
                    unequalKindsInSamePositionIndex.add(kind1);
                    unequalKindsInSamePositionIndex.add(kind2);
                }
            }

            Set<String> intersection = new LinkedHashSet<>(kinds1);
            intersection.retainAll(kinds2);
            intersection.removeAll(unequalKindsInSamePositionIndex);
            kinds1.removeAll(intersection);
            kinds2.removeAll(intersection);
        } else {
            ReplacementUtil.removeCommonElements(kinds1, kinds2);
        }

        return Map.of(statement1, kinds1, statement2, kinds2);
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
        //variablesAndMethodInvocations1.addAll(functionInvocations1);
        // variablesAndMethodInvocations1.addAll(unmatchedVariables1);

        Set<String> variablesAndMethodInvocations2 = new LinkedHashSet<>();
        variablesAndMethodInvocations2.addAll(functionInvocations2);
        variablesAndMethodInvocations2.addAll(unmatchedVariables2);

        //apply existing replacements on method invocations
        for (String functionInvocation : functionInvocations1) {
            String temp = new String(functionInvocation);
            for (Replacement replacement : replacementInfo.getReplacements()) {
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

    private void intersectAndReplaceInfixOperators(CodeFragment statement1, CodeFragment statement2, ReplacementInfo replacementInfo) {
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
            CodeFragment statement1,
            CodeFragment statement2,
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

        return Map.of(0, methodInvocations1, 1, methodInvocations2, 2, methodInvocationIntersection);
    }

    private Map<Integer, Set<String>> intersectObjectCreations(CodeFragment statement1, CodeFragment statement2,
                                                               Map<String, String> parameterToArgumentMap
            , Map<String, String> variableToArgumentMap
            , Map<String, List<? extends Invocation>> creationMap1
            , Map<String, List<? extends Invocation>> creationMap2) {
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

        return Map.of(0, creations1, 1, creations2, 2, creationIntersection);
    }

    private Map<String, String> replaceArgumentsWithVariables(CodeFragment statement1,
                                                              CodeFragmentDiff intersection,
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

            for (Replacement r : replacementInfo.getReplacements()) {
                variableToArgumentMap.put(r.getBefore(), r.getAfter());

                // IF the argument which was replaced was an invocation, change it to  a subclass of the replacement
                if (statement1.getMethodInvocationMap().containsKey(r.getBefore())) {
                    Replacement replacement = new VariableReplacementWithMethodInvocation(r.getBefore(),
                            r.getAfter(),
                            statement1.getMethodInvocationMap().get(r.getBefore()).get(0),
                            Direction.INVOCATION_TO_VARIABLE);
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
                                }
                                newCalls.add(newCall);
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
                                }
                                newCalls.add(newCall);
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
                    if (previous.getExpressionText() != null && previous.getExpressionText().equals(current.actualString())) {
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

    public void findAndPerformBestReplacements(Set<String> strings1, Set<String> strings2, ReplacementInfo replacementInfo, ReplacementType type) {
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
                if (Thread.interrupted()) {
                    throw new RefactoringMinerTimedOutException();
                }

                if (ENABLE_SEARCH_FOR_ANONYMOUS_SIGNATURE_DURING_REPLACEMENT) {
                    boolean containsMethodSignatureOfAnonymousClass1 = containsMethodSignatureOfAnonymousClass(stringA);
                    boolean containsMethodSignatureOfAnonymousClass2 = containsMethodSignatureOfAnonymousClass(stringB);
                    if (containsMethodSignatureOfAnonymousClass1 != containsMethodSignatureOfAnonymousClass2 &&
                            this.function1.getVariableDeclaration(stringA) == null
                            && this.function2.getVariableDeclaration(stringB) == null) {
                        continue;
                    }
                }

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
        String[] lines = s.split("\\n");
        if (s.contains(" -> ") && lines.length > 1) {
            return true;
        }
        for (String line : lines) {
            line = VariableReplacementAnalysis.prepareLine(line);
            if (JsConfig.METHOD_SIGNATURE_PATTERN.matcher(line).matches()) {
                return true;
            }
        }
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
     * Returns unmatched variables from both statements
     *
     * @return
     */
    private Set<String> findCommonVariablesToBeAddedAsUnmatched(CodeFragment statement1, CodeFragment statement2,
                                                                ReplacementInfo replacementInfo,
                                                                Set<String> variables1
            , Set<String> variables2) {
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
                    if (invocationCoveringEntireStatementUsesVariableInArgument
                            (invocationCoveringTheEntireStatement1,
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

    private boolean invocationCoveringEntireStatementUsesVariableInArgument(OperationInvocation invocationCoveringTheEntireStatement
            , String variable, List<? extends CodeFragment> unMatchedStatements) {
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

    private boolean nonMatchedStatementUsesVariableInArgument(List<? extends CodeFragment> statements, String variable, String otherArgument) {
        for (CodeFragment statement : statements) {
            OperationInvocation invocation = InvocationCoverage.INSTANCE
                    .getInvocationCoveringEntireFragment(statement);
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

    public static VariableDeclaration findDeclarationWithArrayInitializer(List<VariableDeclaration> variableDeclarations) {
        Expression initializer;
        for (VariableDeclaration declaration : variableDeclarations) {
            if ((initializer = declaration.getInitializer()) != null
                    && initializer.getText().startsWith("{") && initializer.getText().endsWith("}")) {
                return declaration;
            }
        }
        return null;
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
        newObjectCreation.setType(objectCreation.getCodeElementType());
        newObjectCreation.setSourceLocation(objectCreation.getSourceLocation());
        updateCall(objectCreation, newObjectCreation, oldExpression, newExpression);
        return newObjectCreation;
    }

    private OperationInvocation createFunctionInvocationWithExpression(OperationInvocation operationInvocation, String oldExpression, String newExpression) {
        OperationInvocation newOperationInvocation = new OperationInvocation();
        newOperationInvocation.setFunctionName(operationInvocation.getName());
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
        if (thisCall.getExpressionText() != null && thisCall.getExpressionText().equals(oldExpression)) {
            newCall.setExpressionText(newExpression);
        } else {
            newCall.setExpressionText(thisCall.getExpressionText());
        }

        for (String argument : thisCall.getArguments()) {
            newCall.getArguments().add(
                    ReplacementUtil.performReplacement(argument, oldExpression, newExpression));
        }
    }

    protected boolean expressionsContainInitializerOfVariableDeclaration(Set<String> expressions, CodeFragment statement) {
        List<VariableDeclaration> variableDeclarations = statement.getVariableDeclarations();

        if (variableDeclarations.size() == 1) {
            VariableDeclaration vd = variableDeclarations.get(0);
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

    private boolean equalAfterNewArgumentAdditions(String s1, String s2, ReplacementInfo replacementInfo) {
        UMLOperationDiff operationDiff = sourceFileDiff == null ? null : this.sourceFileDiff.getOperationDiff(function1, function2);
        if (operationDiff == null && function1 != null) {
            operationDiff = new UMLOperationDiff(function1, function2);
        }
        String commonPrefix = PrefixSuffixUtils.longestCommonPrefix(s1, s2);
        String commonSuffix = PrefixSuffixUtils.longestCommonSuffix(s1, s2);
        if (!commonPrefix.isEmpty() && !commonSuffix.isEmpty() && !commonPrefix.equals("return ")) {
            int beginIndexS1 = s1.indexOf(commonPrefix) + commonPrefix.length();
            int endIndexS1 = s1.lastIndexOf(commonSuffix);
            String diff1 = beginIndexS1 > endIndexS1 ? "" : s1.substring(beginIndexS1, endIndexS1);
            int beginIndexS2 = s2.indexOf(commonPrefix) + commonPrefix.length();
            int endIndexS2 = s2.lastIndexOf(commonSuffix);
            String diff2 = beginIndexS2 > endIndexS2 ? "" : s2.substring(beginIndexS2, endIndexS2);
            if (beginIndexS1 > endIndexS1) {
                diff2 = diff2 + commonSuffix.substring(0, beginIndexS1 - endIndexS1);
                if (diff2.charAt(diff2.length() - 1) == ',') {
                    diff2 = diff2.substring(0, diff2.length() - 1);
                }
            }
            String characterAfterCommonPrefix = s1.equals(commonPrefix) ? "" : Character.toString(s1.charAt(commonPrefix.length()));
            if (commonPrefix.contains(",") && commonPrefix.lastIndexOf(",") < commonPrefix.length() - 1 &&
                    !characterAfterCommonPrefix.equals(",") && !characterAfterCommonPrefix.equals(")")) {
                String prepend = commonPrefix.substring(commonPrefix.lastIndexOf(",") + 1, commonPrefix.length());
                diff1 = prepend + diff1;
                diff2 = prepend + diff2;
            }
            //if there is a variable replacement diff1 should be empty, otherwise diff1 should include a single variable
            if (diff1.isEmpty() || (function1 != null &&
                    (function1.getParameterNameList().contains(diff1) && !function2.getParameterNameList().contains(diff1) && !containsMethodSignatureOfAnonymousClass(diff2)))
//                    ||
//                    (containerDiff.getOriginalClass().containsAttributeWithName(diff1)
//                            && !classDiff.getNextClass().containsAttributeWithName(diff1)
//                            && !containsMethodSignatureOfAnonymousClass(diff2))
            ) {

                List<UMLParameter> matchingAddedParameters = new ArrayList<UMLParameter>();
                for (UMLParameter addedParameter : operationDiff.getAddedParameters().values()) {
                    if (diff2.contains(addedParameter.name)) {
                        matchingAddedParameters.add(addedParameter);
                    }
                }
                if (matchingAddedParameters.size() > 0) {
                    Replacement matchingReplacement = null;
                    for (Replacement replacement : replacementInfo.getReplacements()) {
                        if (replacement.getType().equals(ReplacementType.VARIABLE_NAME)) {
                            for (UMLParameterDiff parameterDiff : operationDiff.getParameterDiffs()) {
                                if (parameterDiff.isNameChanged &&
                                        replacement.getBefore().equals(parameterDiff.getRemovedParameter().name) &&
                                        replacement.getAfter().equals(parameterDiff.getAddedParameter().name)) {
                                    matchingReplacement = replacement;
                                    break;
                                }
                            }
                        }
                        if (matchingReplacement != null) {
                            break;
                        }
                    }
                    if (matchingReplacement != null) {
                        Set<String> splitVariables = new LinkedHashSet<String>();
                        splitVariables.add(matchingReplacement.getAfter());
                        StringBuilder concat = new StringBuilder();
                        int counter = 0;
                        for (UMLParameter addedParameter : matchingAddedParameters) {
                            splitVariables.add(addedParameter.name);
                            concat.append(addedParameter.name);
                            if (counter < matchingAddedParameters.size() - 1) {
                                concat.append(",");
                            }
                            counter++;
                        }
                        SplitVariableReplacement split = new SplitVariableReplacement(matchingReplacement.getBefore(), splitVariables);
                        if (!split.getSplitVariables().contains(split.getBefore()) && concat.toString().equals(diff2)) {
                            replacementInfo.getReplacements().remove(matchingReplacement);
                            replacementInfo.getReplacements().add(split);
                            return true;
                        }
                    } else if (diff1.isEmpty() && replacementInfo.getReplacements().isEmpty()) {
                        Set<String> addedVariables = new LinkedHashSet<String>();
                        StringBuilder concat = new StringBuilder();
                        int counter = 0;
                        for (UMLParameter addedParameter : matchingAddedParameters) {
                            addedVariables.add(addedParameter.name);
                            concat.append(addedParameter.name);
                            if (counter < matchingAddedParameters.size() - 1) {
                                concat.append(",");
                            }
                            counter++;
                        }
                        if (concat.toString().equals(diff2)) {
                            AddVariableReplacement r = new AddVariableReplacement(addedVariables);
                            replacementInfo.getReplacements().add(r);
                            return true;
                        }
                    }
                    if (function1.getParameterNameList().contains(diff1)) {
                        Set<String> splitVariables = new LinkedHashSet<String>();
                        StringBuilder concat = new StringBuilder();
                        int counter = 0;
                        for (UMLParameter addedParameter : matchingAddedParameters) {
                            splitVariables.add(addedParameter.name);
                            concat.append(addedParameter.name);
                            if (counter < matchingAddedParameters.size() - 1) {
                                concat.append(",");
                            }
                            counter++;
                        }
                        SplitVariableReplacement split = new SplitVariableReplacement(diff1, splitVariables);
                        if (!split.getSplitVariables().contains(split.getBefore()) && concat.toString().equals(diff2)) {
                            replacementInfo.getReplacements().add(split);
                            return true;
                        }
                    }
                }
                if (sourceFileDiff != null) {
//                    List<UMLAttribute> matchingAttributes = new ArrayList<UMLAttribute>();
//                    for(UMLAttribute attribute : containerDiff.getNextClass().getAttributes()) {
//                        if(diff2.contains(attribute.getName())) {
//                            matchingAttributes.add(attribute);
//                        }
//                    }
//                    if(matchingAttributes.size() > 0) {
//                        Replacement matchingReplacement = null;
//                        for(Replacement replacement : replacementInfo.getReplacements()) {
//                            if(replacement.getType().equals(ReplacementType.VARIABLE_NAME)) {
//                                if(classDiff.getOriginalClass().containsAttributeWithName(replacement.getBefore()) &&
//                                        classDiff.getNextClass().containsAttributeWithName(replacement.getAfter())) {
//                                    matchingReplacement = replacement;
//                                    break;
//                                }
//                            }
//                        }
//                        if(matchingReplacement != null) {
//                            Set<String> splitVariables = new LinkedHashSet<String>();
//                            splitVariables.add(matchingReplacement.getAfter());
//                            StringBuilder concat = new StringBuilder();
//                            int counter = 0;
//                            for(UMLAttribute attribute : matchingAttributes) {
//                                splitVariables.add(attribute.getName());
//                                concat.append(attribute.getName());
//                                if(counter < matchingAttributes.size()-1) {
//                                    concat.append(",");
//                                }
//                                counter++;
//                            }
//                            SplitVariableReplacement split = new SplitVariableReplacement(matchingReplacement.getBefore(), splitVariables);
//                            if(!split.getSplitVariables().contains(split.getBefore()) && concat.toString().equals(diff2)) {
//                                replacementInfo.getReplacements().remove(matchingReplacement);
//                                replacementInfo.getReplacements().add(split);
//                                return true;
//                            }
//                        }
//                        else if(diff1.isEmpty() && replacementInfo.getReplacements().isEmpty()) {
//                            Set<String> addedVariables = new LinkedHashSet<String>();
//                            StringBuilder concat = new StringBuilder();
//                            int counter = 0;
//                            for(UMLAttribute attribute : matchingAttributes) {
//                                addedVariables.add(attribute.getName());
//                                concat.append(attribute.getName());
//                                if(counter < matchingAttributes.size()-1) {
//                                    concat.append(",");
//                                }
//                                counter++;
//                            }
//                            if(concat.toString().equals(diff2)) {
//                                AddVariableReplacement r = new AddVariableReplacement(addedVariables);
//                                replacementInfo.getReplacements().add(r);
//                                return true;
//                            }
//                        }
//                        if(classDiff.getOriginalClass().containsAttributeWithName(diff1)) {
//                            Set<String> splitVariables = new LinkedHashSet<String>();
//                            StringBuilder concat = new StringBuilder();
//                            int counter = 0;
//                            for(UMLAttribute attribute : matchingAttributes) {
//                                splitVariables.add(attribute.getName());
//                                concat.append(attribute.getName());
//                                if(counter < matchingAttributes.size()-1) {
//                                    concat.append(",");
//                                }
//                                counter++;
//                            }
//                            SplitVariableReplacement split = new SplitVariableReplacement(diff1, splitVariables);
//                            if(!split.getSplitVariables().contains(split.getBefore()) && concat.toString().equals(diff2)) {
//                                replacementInfo.getReplacements().add(split);
//                                return true;
//                            }
//                        }
//                    }
                }
                List<VariableDeclaration> matchingVariableDeclarations = new ArrayList<VariableDeclaration>();
                for (VariableDeclaration declaration : function2.getAllVariableDeclarations()) {
                    if (diff2.contains(declaration.getVariableName())) {
                        matchingVariableDeclarations.add(declaration);
                    }
                }
                if (matchingVariableDeclarations.size() > 0) {
                    Replacement matchingReplacement = null;
                    for (Replacement replacement : replacementInfo.getReplacements()) {
                        if (replacement.getType().equals(ReplacementType.VARIABLE_NAME)) {
                            int indexOf1 = s1.indexOf(replacement.getAfter());
                            int indexOf2 = s2.indexOf(replacement.getAfter());
                            int characterIndex1 = indexOf1 + replacement.getAfter().length();
                            int characterIndex2 = indexOf2 + replacement.getAfter().length();
                            boolean isVariableDeclarationReplacement =
                                    characterIndex1 < s1.length() && s1.charAt(characterIndex1) == '=' &&
                                            characterIndex2 < s2.length() && s2.charAt(characterIndex2) == '=';
                            if (!isVariableDeclarationReplacement &&
                                    function1.getVariableDeclaration(replacement.getBefore()) != null &&
                                    function2.getVariableDeclaration(replacement.getAfter()) != null) {
                                matchingReplacement = replacement;
                                break;
                            }
                        }
                    }
                    if (matchingReplacement != null) {
                        Set<String> splitVariables = new LinkedHashSet<String>();
                        splitVariables.add(matchingReplacement.getAfter());
                        StringBuilder concat = new StringBuilder();
                        int counter = 0;
                        for (VariableDeclaration declaration : matchingVariableDeclarations) {
                            splitVariables.add(declaration.getVariableName());
                            concat.append(declaration.getVariableName());
                            if (counter < matchingVariableDeclarations.size() - 1) {
                                concat.append(",");
                            }
                            counter++;
                        }
                        SplitVariableReplacement split = new SplitVariableReplacement(matchingReplacement.getBefore(), splitVariables);
                        if (!split.getSplitVariables().contains(split.getBefore()) && concat.toString().equals(diff2)) {
                            replacementInfo.getReplacements().remove(matchingReplacement);
                            replacementInfo.getReplacements().add(split);
                            return true;
                        }
                    } else if (diff1.isEmpty() && replacementInfo.getReplacements().isEmpty()) {
                        Set<String> addedVariables = new LinkedHashSet<String>();
                        StringBuilder concat = new StringBuilder();
                        int counter = 0;
                        for (VariableDeclaration declaration : matchingVariableDeclarations) {
                            addedVariables.add(declaration.getVariableName());
                            concat.append(declaration.getVariableName());
                            if (counter < matchingVariableDeclarations.size() - 1) {
                                concat.append(",");
                            }
                            counter++;
                        }
                        if (concat.toString().equals(diff2)) {
                            AddVariableReplacement r = new AddVariableReplacement(addedVariables);
                            replacementInfo.getReplacements().add(r);
                            return true;
                        }
                    }
                    if (function1.getVariableDeclaration(diff1) != null) {
                        Set<String> splitVariables = new LinkedHashSet<String>();
                        StringBuilder concat = new StringBuilder();
                        int counter = 0;
                        for (VariableDeclaration declaration : matchingVariableDeclarations) {
                            splitVariables.add(declaration.getVariableName());
                            concat.append(declaration.getVariableName());
                            if (counter < matchingVariableDeclarations.size() - 1) {
                                concat.append(",");
                            }
                            counter++;
                        }
                        SplitVariableReplacement split = new SplitVariableReplacement(diff1, splitVariables);
                        if (!split.getSplitVariables().contains(split.getBefore()) && concat.toString().equals(diff2)) {
                            replacementInfo.getReplacements().add(split);
                            return true;
                        }
                    }
                }
            }
        }
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

    private boolean differOnlyInCastExpressionOrPrefixOperator(String s1, String s2, ReplacementInfo info) {
        String commonPrefix = PrefixSuffixUtils.longestCommonPrefix(s1, s2);
        String commonSuffix = PrefixSuffixUtils.longestCommonSuffix(s1, s2);

        if (!commonPrefix.isEmpty() && !commonSuffix.isEmpty()) {
            int beginIndexS1 = s1.indexOf(commonPrefix) + commonPrefix.length();
            int endIndexS1 = s1.lastIndexOf(commonSuffix);
            String diff1 = beginIndexS1 > endIndexS1 ? "" : s1.substring(beginIndexS1, endIndexS1);
            int beginIndexS2 = s2.indexOf(commonPrefix) + commonPrefix.length();
            int endIndexS2 = s2.lastIndexOf(commonSuffix);
            String diff2 = beginIndexS2 > endIndexS2 ? "" : s2.substring(beginIndexS2, endIndexS2);
            if (cast(diff1, diff2)) {
                return true;
            }
            if (cast(diff2, diff1)) {
                return true;
            }
            if (diff1.isEmpty() && (diff2.equals("!") || diff2.equals("~"))) {
                Replacement r = new Replacement(s1, s2, ReplacementType.INVERT_CONDITIONAL);
                info.addReplacement(r);
                return true;
            }
            if (diff2.isEmpty() && (diff1.equals("!") || diff1.equals("~"))) {
                Replacement r = new Replacement(s1, s2, ReplacementType.INVERT_CONDITIONAL);
                info.addReplacement(r);
                return true;
            }
        }
        return false;
    }

    private boolean cast(String diff1, String diff2) {
        return (diff1.isEmpty() && diff2.startsWith("(") && diff2.endsWith(")")) || diff2.equals("(" + diff1 + ")");
    }

    private boolean containsValidOperatorReplacements(ReplacementInfo replacementInfo) {
        List<Replacement> operatorReplacements = replacementInfo.getReplacementsOfType(ReplacementType.INFIX_OPERATOR);
        for (Replacement replacement : operatorReplacements) {
            if (replacement.getBefore().equals("==") && !replacement.getAfter().equals("!="))
                return false;
            if (replacement.getBefore().equals("!=") && !replacement.getAfter().equals("=="))
                return false;
            if (replacement.getBefore().equals("&&") && !replacement.getAfter().equals("||"))
                return false;
            if (replacement.getBefore().equals("||") && !replacement.getAfter().equals("&&"))
                return false;
        }
        return true;
    }

    private boolean oneIsVariableDeclarationTheOtherIsVariableAssignment(String s1, String s2, ReplacementInfo replacementInfo) {
        String commonSuffix = PrefixSuffixUtils.longestCommonSuffix(s1, s2);
        if (s1.contains("=") && s2.contains("=") && (s1.equals(commonSuffix) || s2.equals(commonSuffix))) {
            if (replacementInfo.getReplacements().size() == 2) {
                StringBuilder sb = new StringBuilder();
                int counter = 0;
                for (Replacement r : replacementInfo.getReplacements()) {
                    sb.append(r.getAfter());
                    if (counter == 0) {
                        sb.append("=");
                    } else if (counter == 1) {
                        sb.append(";\n");
                    }
                    counter++;
                }
                if (commonSuffix.equals(sb.toString())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean oneIsVariableDeclarationTheOtherIsReturnStatement(String s1, String s2) {
        String commonSuffix = PrefixSuffixUtils.longestCommonSuffix(s1, s2);
        commonSuffix = commonSuffix.trim();

        if (!commonSuffix.equals("null;\n") && !commonSuffix.equals("true;\n") && !commonSuffix.equals("false;\n") && !commonSuffix.equals("0;\n")) {
            if (s1.startsWith("return ") && s1.substring("return ".length()).equals(commonSuffix) &&
                    s2.contains("=") && s2.substring(s2.indexOf("=") + 2).equals(commonSuffix)) {
                return true;
            }
            if (s2.startsWith("return ") && s2.substring("return ".length()).equals(commonSuffix) &&
                    s1.contains("=") && s1.substring(s1.indexOf("=") + 2).equals(commonSuffix)) {
                return true;
            }
        }
        return false;
    }

    private boolean commonConditional(String s1, String s2, ReplacementInfo info) {
        if (!containsMethodSignatureOfAnonymousClass(s1) && !containsMethodSignatureOfAnonymousClass(s2)) {
            if ((s1.contains("||") || s1.contains("&&") || s2.contains("||") || s2.contains("&&"))) {
                String conditional1 = prepareConditional(s1);
                String conditional2 = prepareConditional(s2);
                String[] subConditions1 = SPLIT_CONDITIONAL_PATTERN.split(conditional1);
                String[] subConditions2 = SPLIT_CONDITIONAL_PATTERN.split(conditional2);
                List<String> subConditionsAsList1 = new ArrayList<String>();
                for (String s : subConditions1) {
                    subConditionsAsList1.add(s.trim());
                }
                List<String> subConditionsAsList2 = new ArrayList<String>();
                for (String s : subConditions2) {
                    subConditionsAsList2.add(s.trim());
                }
                Set<String> intersection = new LinkedHashSet<String>(subConditionsAsList1);
                intersection.retainAll(subConditionsAsList2);
                int matches = 0;
                if (!intersection.isEmpty()) {
                    for (String element : intersection) {
                        boolean replacementFound = false;
                        for (Replacement r : info.getReplacements()) {
                            if (element.equals(r.getAfter()) || element.equals("(" + r.getAfter()) || element.equals(r.getAfter() + ")")) {
                                replacementFound = true;
                                break;
                            }
                            if (r.getType().equals(ReplacementType.INFIX_OPERATOR) && element.contains(r.getAfter())) {
                                replacementFound = true;
                                break;
                            }
                            if (ReplacementUtil.contains(element, r.getAfter()) && element.startsWith(r.getAfter()) &&
                                    (element.endsWith(" != null") || element.endsWith(" == null"))) {
                                replacementFound = true;
                                break;
                            }
                        }
                        if (!replacementFound) {
                            matches++;
                        }
                    }
                }
                if (matches > 0) {
                    Replacement r = new IntersectionReplacement(s1, s2, intersection, ReplacementType.CONDITIONAL);
                    info.addReplacement(r);
                }
                boolean invertConditionalFound = false;
                for (String subCondition1 : subConditionsAsList1) {
                    for (String subCondition2 : subConditionsAsList2) {
                        if (subCondition1.equals("!" + subCondition2)) {
                            Replacement r = new Replacement(subCondition1, subCondition2, ReplacementType.INVERT_CONDITIONAL);
                            info.addReplacement(r);
                            invertConditionalFound = true;
                        }
                        if (subCondition2.equals("!" + subCondition1)) {
                            Replacement r = new Replacement(subCondition1, subCondition2, ReplacementType.INVERT_CONDITIONAL);
                            info.addReplacement(r);
                            invertConditionalFound = true;
                        }
                    }
                }
                if (invertConditionalFound || matches > 0) {
                    return true;
                }
            }
            if (s1.contains(" >= ") && s2.contains(" <= ")) {
                Replacement r = invertConditionalDirection(s1, s2, " >= ", " <= ");
                if (r != null) {
                    info.addReplacement(r);
                    return true;
                }
            }
            if (s1.contains(" <= ") && s2.contains(" >= ")) {
                Replacement r = invertConditionalDirection(s1, s2, " <= ", " >= ");
                if (r != null) {
                    info.addReplacement(r);
                    return true;
                }
            }
            if (s1.contains(" > ") && s2.contains(" < ")) {
                Replacement r = invertConditionalDirection(s1, s2, " > ", " < ");
                if (r != null) {
                    info.addReplacement(r);
                    return true;
                }
            }
            if (s1.contains(" < ") && s2.contains(" > ")) {
                Replacement r = invertConditionalDirection(s1, s2, " < ", " > ");
                if (r != null) {
                    info.addReplacement(r);
                    return true;
                }
            }
        }
        return false;
    }

    private Replacement invertConditionalDirection(String s1, String s2, String operator1, String operator2) {
        int indexS1 = s1.indexOf(operator1);
        int indexS2 = s2.indexOf(operator2);
        //s1 goes right, s2 goes left
        int i = indexS1 + operator1.length();
        int j = indexS2 - 1;
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        while (i < s1.length() && j >= 0) {
            sb1.append(s1.charAt(i));
            sb2.insert(0, s2.charAt(j));
            if (sb1.toString().equals(sb2.toString())) {
                String subCondition1 = operator1 + sb1.toString();
                String subCondition2 = sb2.toString() + operator2;
                Replacement r = new Replacement(subCondition1, subCondition2, ReplacementType.INVERT_CONDITIONAL);
                return r;
            }
            i++;
            j--;
        }
        //s1 goes left, s2 goes right
        i = indexS1 - 1;
        j = indexS2 + operator2.length();
        sb1 = new StringBuilder();
        sb2 = new StringBuilder();
        while (i >= 0 && j < s2.length()) {
            sb1.insert(0, s1.charAt(i));
            sb2.append(s2.charAt(j));
            if (sb1.toString().equals(sb2.toString())) {
                String subCondition1 = sb1.toString() + operator1;
                String subCondition2 = operator2 + sb2.toString();
                Replacement r = new Replacement(subCondition1, subCondition2, ReplacementType.INVERT_CONDITIONAL);
                return r;
            }
            i--;
            j++;
        }
        return null;
    }

    private String prepareConditional(String s) {
        String conditional = s;
        if (s.startsWith("if(") && s.endsWith(")")) {
            conditional = s.substring(3, s.length() - 1);
        }
        if (s.startsWith("while(") && s.endsWith(")")) {
            conditional = s.substring(6, s.length() - 1);
        }
        if (s.startsWith("return ") && s.endsWith(";\n")) {
            conditional = s.substring(7, s.length() - 2);
        }
        int indexOfEquals = s.indexOf("=");
        if (indexOfEquals > -1 && s.charAt(indexOfEquals + 1) != '=' && s.charAt(indexOfEquals - 1) != '!' && s.endsWith(";\n")) {
            conditional = s.substring(indexOfEquals + 1, s.length() - 2);
        }
        return conditional;
    }

    private boolean equalAfterArgumentMerge(String s1, String s2, ReplacementInfo replacementInfo) {
        Map<String, Set<Replacement>> commonVariableReplacementMap = new LinkedHashMap<String, Set<Replacement>>();
        for (Replacement replacement : replacementInfo.getReplacements()) {
            if (replacement.getType().equals(ReplacementType.VARIABLE_NAME)) {
                String key = replacement.getAfter();
                if (commonVariableReplacementMap.containsKey(key)) {
                    commonVariableReplacementMap.get(key).add(replacement);
                    int index = s1.indexOf(key);
                    if (index != -1) {
                        if (s1.charAt(index + key.length()) == ',') {
                            s1 = s1.substring(0, index) + s1.substring(index + key.length() + 1, s1.length());
                        } else if (index > 0 && s1.charAt(index - 1) == ',') {
                            s1 = s1.substring(0, index - 1) + s1.substring(index + key.length(), s1.length());
                        }
                    }
                } else {
                    Set<Replacement> replacements = new LinkedHashSet<Replacement>();
                    replacements.add(replacement);
                    commonVariableReplacementMap.put(key, replacements);
                }
            }
        }
        if (s1.equals(s2)) {
            for (String key : commonVariableReplacementMap.keySet()) {
                Set<Replacement> replacements = commonVariableReplacementMap.get(key);
                if (replacements.size() > 1) {
                    replacementInfo.getReplacements().removeAll(replacements);
                    Set<String> mergedVariables = new LinkedHashSet<String>();
                    for (Replacement replacement : replacements) {
                        mergedVariables.add(replacement.getBefore());
                    }
                    MergeVariableReplacement merge = new MergeVariableReplacement(mergedVariables, key);
                    replacementInfo.getReplacements().add(merge);
                }
            }
            return true;
        }
        return false;
    }

    private boolean variableDeclarationsWithEverythingReplaced(List<VariableDeclaration> variableDeclarations1,
                                                               List<VariableDeclaration> variableDeclarations2,
                                                               ReplacementInfo replacementInfo) {
        if (variableDeclarations1.size() == 1 && variableDeclarations2.size() == 1) {
            boolean typeReplacement = false,
                    variableRename = false,
                    methodInvocationReplacement = false,
                    nullInitializer = false,
                    zeroArgumentClassInstantiation = false,
                    classInstantiationArgumentReplacement = false;

            //UMLType type1 = variableDeclarations1.get(0).getType();
            //UMLType type2 = variableDeclarations2.get(0).getType();
            Expression initializer1 = variableDeclarations1.get(0).getInitializer();
            Expression initializer2 = variableDeclarations2.get(0).getInitializer();

            if (initializer1 == null && initializer2 == null) {
                nullInitializer = true;
            } else if (initializer1 != null && initializer2 != null) {
                nullInitializer = initializer1.getText().equals("null")
                        && initializer2.getText().equals("null");
                if (initializer1.getCreationMap().size() == 1 && initializer2.getCreationMap().size() == 1) {
                    ObjectCreation creation1 = initializer1.getCreationMap().values().iterator().next().get(0);
                    ObjectCreation creation2 = initializer2.getCreationMap().values().iterator().next().get(0);
                    if (creation1.getArguments().size() == 0 && creation2.getArguments().size() == 0) {
                        zeroArgumentClassInstantiation = true;
                    } else if (creation1.getArguments().size() == 1 && creation2.getArguments().size() == 1) {
                        String argument1 = creation1.getArguments().get(0);
                        String argument2 = creation2.getArguments().get(0);
                        for (Replacement replacement : replacementInfo.getReplacements()) {
                            if (replacement.getBefore().equals(argument1) && replacement.getAfter().equals(argument2)) {
                                classInstantiationArgumentReplacement = true;
                                break;
                            }
                        }
                    }
                }
            }
            for (Replacement replacement : replacementInfo.getReplacements()) {
                if (replacement.getType().equals(ReplacementType.TYPE))
                    typeReplacement = true;
                else if (replacement.getType().equals(ReplacementType.VARIABLE_NAME) &&
                        variableDeclarations1.get(0).variableName.equals(replacement.getBefore()) &&
                        variableDeclarations2.get(0).variableName.equals(replacement.getAfter()))
                    variableRename = true;
                else if (replacement instanceof MethodInvocationReplacement) {
                    MethodInvocationReplacement invocationReplacement = (MethodInvocationReplacement) replacement;
                    if (initializer1 != null && invocationReplacement.getInvokedOperationBefore().actualString()
                            .equals(initializer1.getText()) &&
                            initializer2 != null && invocationReplacement.getInvokedOperationAfter()
                            .actualString().equals(initializer2.getText())) {
                        methodInvocationReplacement = true;
                    }
                    if (initializer1 != null && initializer1.getText().equals(replacement.getBefore()) &&
                            initializer2 != null && initializer2.getText().equals(replacement.getAfter())) {
                        methodInvocationReplacement = true;
                    }
                } else if (replacement.getType().equals(ReplacementType.CLASS_INSTANCE_CREATION)) {
                    if (initializer1 != null && initializer1.getText().equals(replacement.getBefore()) &&
                            initializer2 != null && initializer2.getText().equals(replacement.getAfter())) {
                        methodInvocationReplacement = true;
                    }
                }
            }
            if (/*typeReplacement && !type1.compatibleTypes(type2)
                    &&*/ variableRename
                    && (methodInvocationReplacement
                    || nullInitializer
                    || zeroArgumentClassInstantiation
                    || classInstantiationArgumentReplacement)) {
                return true;
            }
        }
        return false;
    }

    private boolean variableAssignmentWithEverythingReplaced(CodeFragment statement1, CodeFragment statement2,
                                                             ReplacementInfo replacementInfo) {
        String string1 = statement1.getText();
        String string2 = statement2.getText();

        if (!string1.endsWith("\n") && !string2.endsWith("\n")) {
            return false;
        }

        if (containsMethodSignatureOfAnonymousClass(string1)) {
            string1 = string1.substring(0, string1.indexOf("\n"));
        }
        if (containsMethodSignatureOfAnonymousClass(string2)) {
            string2 = string2.substring(0, string2.indexOf("\n"));
        }
        if (string1.contains("=") && string1.endsWith(";\n") && string2.contains("=") && string2.endsWith(";\n")) {
            boolean typeReplacement = false, compatibleTypes = false, variableRename = false,
                    classInstanceCreationReplacement = false;
            String variableName1 = string1.substring(0, string1.indexOf("="));
            String variableName2 = string2.substring(0, string2.indexOf("="));
            String assignment1 = string1.substring(string1.indexOf("=") + 1, string1.lastIndexOf(";\n"));
            String assignment2 = string2.substring(string2.indexOf("=") + 1, string2.lastIndexOf(";\n"));
//            UMLType type1 = null, type2 = null;
//            Map<String, List<ObjectCreation>> creationMap1 = statement1.getCreationMap();
//            for (String creation1 : creationMap1.keySet()) {
//                if (creation1.equals(assignment1)) {
//                    type1 = creationMap1.get(creation1).get(0).getType();
//                }
//            }
//            Map<String, List<ObjectCreation>> creationMap2 = statement2.getCreationMap();
//            for (String creation2 : creationMap2.keySet()) {
//                if (creation2.equals(assignment2)) {
//                    type2 = creationMap2.get(creation2).get(0).getType();
//                }
//            }
//            if (type1 != null && type2 != null) {
//                compatibleTypes = type1.compatibleTypes(type2);
//            }
            OperationInvocation inv1 = null, inv2 = null;
            Map<String, List<OperationInvocation>> methodInvocationMap1 = statement1.getMethodInvocationMap();
            for (String invocation1 : methodInvocationMap1.keySet()) {
                if (invocation1.equals(assignment1)) {
                    inv1 = methodInvocationMap1.get(invocation1).get(0);
                }
            }
            Map<String, List<OperationInvocation>> methodInvocationMap2 = statement2.getMethodInvocationMap();
            for (String invocation2 : methodInvocationMap2.keySet()) {
                if (invocation2.equals(assignment2)) {
                    inv2 = methodInvocationMap2.get(invocation2).get(0);
                }
            }
            for (Replacement replacement : replacementInfo.getReplacements()) {
                if (replacement.getType().equals(ReplacementType.TYPE)) {
                    typeReplacement = true;
                    if (string1.contains("new " + replacement.getBefore() + "(")
                            && string2.contains("new " + replacement.getAfter() + "("))
                        classInstanceCreationReplacement = true;
                } else if (replacement.getType().equals(ReplacementType.VARIABLE_NAME)
                        && (variableName1.equals(replacement.getBefore())
                        || variableName1.endsWith(" " + replacement.getBefore()))
                        && (variableName2.equals(replacement.getAfter())
                        || variableName2.endsWith(" " + replacement.getAfter())))
                    variableRename = true;
                else if (replacement.getType().equals(ReplacementType.CLASS_INSTANCE_CREATION) &&
                        assignment1.equals(replacement.getBefore()) &&
                        assignment2.equals(replacement.getAfter()))
                    classInstanceCreationReplacement = true;
            }
            if (typeReplacement && !compatibleTypes && variableRename && classInstanceCreationReplacement) {
                return true;
            }
            if (variableRename && inv1 != null && inv2 != null && inv1.differentExpressionNameAndArguments(inv2)) {
                if (inv1.getArguments().size() > inv2.getArguments().size()) {
                    for (String argument : inv1.getArguments()) {
                        List<OperationInvocation> argumentInvocations = methodInvocationMap1.get(argument);
                        if (argumentInvocations != null) {
                            for (OperationInvocation argumentInvocation : argumentInvocations) {
                                if (!argumentInvocation.differentExpressionNameAndArguments(inv2)) {
                                    return false;
                                }
                            }
                        }
                    }
                } else if (inv1.getArguments().size() < inv2.getArguments().size()) {
                    for (String argument : inv2.getArguments()) {
                        List<OperationInvocation> argumentInvocations = methodInvocationMap2.get(argument);
                        if (argumentInvocations != null) {
                            for (OperationInvocation argumentInvocation : argumentInvocations) {
                                if (!inv1.differentExpressionNameAndArguments(argumentInvocation)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean classInstanceCreationWithEverythingReplaced(CodeFragment statement1, CodeFragment statement2,
                                                                ReplacementInfo replacementInfo, Map<String, String> parameterToArgumentMap) {
        String string1 = statement1.getText();
        String string2 = statement2.getText();

        if (!string1.endsWith("\n") || !string2.endsWith("\n")) {
            return false;
        }

        if (containsMethodSignatureOfAnonymousClass(string1)) {
            string1 = string1.substring(0, string1.indexOf("\n"));
        }
        if (containsMethodSignatureOfAnonymousClass(string2)) {
            string2 = string2.substring(0, string2.indexOf("\n"));
        }
        if (string1.contains("=") && string1.endsWith(";\n") && string2.startsWith("return ") && string2.endsWith(";\n")) {
            boolean typeReplacement = false, compatibleTypes = false, classInstanceCreationReplacement = false;
            String assignment1 = string1.substring(string1.indexOf("=") + 1, string1.lastIndexOf(";\n"));
            String assignment2 = string2.substring(7, string2.lastIndexOf(";\n"));

//            UMLType type1 = null, type2 = null;
            ObjectCreation objectCreation1 = null, objectCreation2 = null;
            Map<String, String> argumentToParameterMap = new LinkedHashMap<String, String>();
            Map<String, List<ObjectCreation>> creationMap1 = statement1.getCreationMap();
            for (String creation1 : creationMap1.keySet()) {
                if (creation1.equals(assignment1)) {
                    objectCreation1 = creationMap1.get(creation1).get(0);
                    //                  type1 = objectCreation1.getType();
                }
            }
            Map<String, List<ObjectCreation>> creationMap2 = statement2.getCreationMap();
            for (String creation2 : creationMap2.keySet()) {
                if (creation2.equals(assignment2)) {
                    objectCreation2 = creationMap2.get(creation2).get(0);
                    //                type2 = objectCreation2.getType();
                    for (String argument : objectCreation2.getArguments()) {
                        if (parameterToArgumentMap.containsKey(argument)) {
                            argumentToParameterMap.put(parameterToArgumentMap.get(argument), argument);
                        }
                    }
                }
            }
            int minArguments = 0;
            //if (type1 != null && type2 != null) {
            //  compatibleTypes = type1.compatibleTypes(type2);
            minArguments = Math.min(objectCreation1.getArguments().size(), objectCreation2.getArguments().size());
            // }

            int replacedArguments = 0;
            for (Replacement replacement : replacementInfo.getReplacements()) {
                if (replacement.getType().equals(ReplacementType.TYPE)) {
                    typeReplacement = true;
                    if (string1.contains("new " + replacement.getBefore() + "(") &&
                            string2.contains("new " + replacement.getAfter() + "("))
                        classInstanceCreationReplacement = true;
                } else if (objectCreation1 != null && objectCreation2 != null &&
                        objectCreation1.getArguments().contains(replacement.getBefore()) &&
                        (objectCreation2.getArguments().contains(replacement.getAfter())
                                || objectCreation2.getArguments().contains(argumentToParameterMap.get(replacement.getAfter())))) {
                    replacedArguments++;
                } else if (replacement.getType().equals(ReplacementType.CLASS_INSTANCE_CREATION) &&
                        assignment1.equals(replacement.getBefore()) &&
                        assignment2.equals(replacement.getAfter()))
                    classInstanceCreationReplacement = true;
            }
            if (typeReplacement && !compatibleTypes && replacedArguments == minArguments && classInstanceCreationReplacement) {
                return true;
            }
        } else if (string1.startsWith("return ") && string1.endsWith(";\n") && string2.contains("=") && string2.endsWith(";\n")) {
            boolean typeReplacement = false, compatibleTypes = false, classInstanceCreationReplacement = false;
            String assignment1 = string1.substring(7, string1.lastIndexOf(";\n"));
            String assignment2 = string2.substring(string2.indexOf("=") + 1, string2.lastIndexOf(";\n"));
            //UMLType type1 = null, type2 = null;
            ObjectCreation objectCreation1 = null, objectCreation2 = null;
            Map<String, String> argumentToParameterMap = new LinkedHashMap<String, String>();
            Map<String, List<ObjectCreation>> creationMap1 = statement1.getCreationMap();
            for (String creation1 : creationMap1.keySet()) {
                if (creation1.equals(assignment1)) {
                    objectCreation1 = creationMap1.get(creation1).get(0);
                    //  type1 = objectCreation1.getType();
                }
            }
            Map<String, List<ObjectCreation>> creationMap2 = statement2.getCreationMap();
            for (String creation2 : creationMap2.keySet()) {
                if (creation2.equals(assignment2)) {
                    objectCreation2 = creationMap2.get(creation2).get(0);
                    //type2 = objectCreation2.getType();
                    for (String argument : objectCreation2.getArguments()) {
                        if (parameterToArgumentMap.containsKey(argument)) {
                            argumentToParameterMap.put(parameterToArgumentMap.get(argument), argument);
                        }
                    }
                }
            }
            int minArguments = 0;
            //if (type1 != null && type2 != null) {
            // compatibleTypes = type1.compatibleTypes(type2);
            minArguments = Math.min(objectCreation1.getArguments().size(), objectCreation2.getArguments().size());
            //}
            int replacedArguments = 0;
            for (Replacement replacement : replacementInfo.getReplacements()) {
                if (replacement.getType().equals(ReplacementType.TYPE)) {
                    typeReplacement = true;
                    if (string1.contains("new " + replacement.getBefore() + "(")
                            && string2.contains("new " + replacement.getAfter() + "("))
                        classInstanceCreationReplacement = true;
                } else if (objectCreation1 != null && objectCreation2 != null &&
                        objectCreation1.getArguments().contains(replacement.getBefore()) &&
                        (objectCreation2.getArguments().contains(replacement.getAfter())
                                || objectCreation2.getArguments()
                                .contains(argumentToParameterMap.get(replacement.getAfter())))) {
                    replacedArguments++;
                } else if (replacement.getType().equals(ReplacementType.CLASS_INSTANCE_CREATION) &&
                        assignment1.equals(replacement.getBefore()) &&
                        assignment2.equals(replacement.getAfter()))
                    classInstanceCreationReplacement = true;
            }
            if (typeReplacement && !compatibleTypes &&
                    replacedArguments == minArguments && classInstanceCreationReplacement) {
                return true;
            }
        }
        return false;
    }

    private boolean identicalVariableDeclarationsWithDifferentNames(String s1, String s2, List<VariableDeclaration> variableDeclarations1, List<VariableDeclaration> variableDeclarations2, ReplacementInfo replacementInfo) {
        if (variableDeclarations1.size() == variableDeclarations2.size() && variableDeclarations1.size() == 1) {
            VariableDeclaration declaration1 = variableDeclarations1.get(0);
            VariableDeclaration declaration2 = variableDeclarations2.get(0);
            if (!declaration1.getVariableName().equals(declaration2.getVariableName())) {
                String commonSuffix = PrefixSuffixUtils.longestCommonSuffix(s1, s2);

                String kind1 = declaration1.getKind() == null ? "" : declaration1.getKind().keywordName;
                String kind2 = declaration2.getKind() == null ? "" : declaration2.getKind().keywordName;

                // IF not global variable, then append space
                String composedString1 = kind1 + (kind1.isEmpty() ? "" : " ") + declaration1.getVariableName() + commonSuffix;
                String composedString2 = kind2 + (kind2.isEmpty() ? "" : " ") + declaration2.getVariableName() + commonSuffix;

                if (s1.equals(composedString1) && s2.equals(composedString2)) {
                    Replacement replacement = new Replacement(declaration1.getVariableName(), declaration2.getVariableName(), ReplacementType.VARIABLE_NAME);
                    replacementInfo.addReplacement(replacement);
                    return true;
                }
            }
        }
        return false;
    }
}
