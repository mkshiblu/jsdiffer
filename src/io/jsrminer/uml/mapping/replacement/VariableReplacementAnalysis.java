package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.*;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.jsrminer.uml.diff.ContainerDiff;
import io.jsrminer.uml.diff.UMLOperationDiff;
import io.jsrminer.uml.diff.UMLParameterDiff;
import io.jsrminer.uml.diff.detection.ConsistentReplacementDetector;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.rminer.core.api.IAnonymousFunctionDeclaration;

import java.util.*;

import static io.jsrminer.uml.mapping.replacement.Replacement.ReplacementType;
import static io.jsrminer.uml.mapping.replacement.VariableReplacementWithMethodInvocation.Direction;
import static java.util.AbstractMap.SimpleEntry;

public class VariableReplacementAnalysis {
    private Set<CodeFragmentMapping> mappings;
    private List<SingleStatement> nonMappedLeavesT1;
    private List<SingleStatement> nonMappedLeavesT2;
    private List<BlockStatement> nonMappedInnerNodesT1;
    private List<BlockStatement> nonMappedInnerNodesT2;
    private FunctionDeclaration operation1;
    private FunctionDeclaration operation2;
    private List<FunctionBodyMapper> childMappers;
    private Set<IRefactoring> refactorings;
    private FunctionDeclaration callSiteOperation;
    private UMLOperationDiff operationDiff;
    private ContainerDiff containerDiff;
    private Set<RenameVariableRefactoring> variableRenames = new LinkedHashSet<>();
    private Set<MergeVariableRefactoring> variableMerges = new LinkedHashSet<MergeVariableRefactoring>();
    private Set<SplitVariableRefactoring> variableSplits = new LinkedHashSet<SplitVariableRefactoring>();
    private Set<CandidateAttributeRefactoring> candidateAttributeRenames = new LinkedHashSet<CandidateAttributeRefactoring>();
    private Set<CandidateMergeVariableRefactoring> candidateAttributeMerges = new LinkedHashSet<CandidateMergeVariableRefactoring>();
    private Set<CandidateSplitVariableRefactoring> candidateAttributeSplits = new LinkedHashSet<CandidateSplitVariableRefactoring>();

    public VariableReplacementAnalysis(FunctionBodyMapper mapper, Set<IRefactoring> refactorings
            , ContainerDiff containerDiff) {
        this.containerDiff = containerDiff;
        this.mappings = mapper.getMappings();
        this.nonMappedLeavesT1 = new ArrayList<>(mapper.getNonMappedLeavesT1());
        this.nonMappedLeavesT2 = new ArrayList<>(mapper.getNonMappedLeavesT2());
        this.nonMappedInnerNodesT1 = new ArrayList<>(mapper.getNonMappedInnerNodesT1());
        this.nonMappedInnerNodesT2 = new ArrayList<>(mapper.getNonMappedInnerNodesT2());
        this.operation1 = mapper.function1;
        this.operation2 = mapper.function2;
        this.childMappers = new ArrayList<FunctionBodyMapper>();
        this.childMappers.addAll(mapper.getChildMappers());
        FunctionBodyMapper parentMapper = mapper.getParentMapper();
        if (parentMapper != null) {
            this.childMappers.addAll(parentMapper.getChildMappers());
        }
        this.refactorings = refactorings;
        this.callSiteOperation = mapper.getCallerFunction();
        this.operationDiff = containerDiff != null ? containerDiff.getOperationDiff(operation1, operation2) : null;

        findVariableSplits();
        findVariableMerges();
        findConsistentVariableRenames();
        //findParametersWrappedInLocalVariables();
        //findAttributeExtractions();
    }

//    private void findAttributeExtractions() {
//        if (containerDiff != null) {
//            for (CodeFragmentMapping mapping : mappings) {
//                for (Replacement replacement : mapping.getReplacements()) {
//                    if (replacement.involvesVariable()) {
//                        // TODO attribute
////                        for (UMLAttribute addedAttribute : classDiff.getAddedAttributes()) {
////                            VariableDeclaration variableDeclaration = addedAttribute.getVariableDeclaration();
////                            if (addedAttribute.getName().equals(replacement.getAfter()) && variableDeclaration.getInitializer() != null &&
////                                    variableDeclaration.getInitializer().getString().equals(replacement.getBefore())) {
////                                ExtractAttributeRefactoring refactoring = new ExtractAttributeRefactoring(addedAttribute, classDiff.getOriginalClass(), classDiff.getNextClass());
////                                refactoring.addReference(mapping);
////                                refactorings.add(refactoring);
////                            }
////                        }
//                    }
//                }
//            }
//        }
//    }

//    private void findParametersWrappedInLocalVariables() {
//        for (SingleStatement statement : nonMappedLeavesT2) {
//            for (VariableDeclaration declaration : statement.getVariableDeclarations()) {
//                Expression initializer = declaration.getInitializer();
//                if (initializer != null) {
//                    for (String key : initializer.getCreationMap().keySet()) {
//                        List<ObjectCreation> creations = initializer.getCreationMap().get(key);
//                        for (ObjectCreation creation : creations) {
//                            for (String argument : creation.getArguments()) {
//                                SimpleEntry<VariableDeclaration, FunctionDeclaration> v2 = getVariableDeclaration2(new Replacement("", argument, ReplacementType.VARIABLE_NAME));
//                                SimpleEntry<VariableDeclaration, FunctionDeclaration> v1 = getVariableDeclaration1(new Replacement(declaration.getVariableName(), "", ReplacementType.VARIABLE_NAME));
//                                if (v2 != null && v1 != null) {
//                                    Set<CodeFragmentMapping> references = VariableReferenceExtractor.findReferences(v1.getKey(), v2.getKey(), mappings);
//                                    RenameVariableRefactoring ref = new RenameVariableRefactoring(v1.getKey(), v2.getKey(), v1.getValue(), v2.getValue(), references);
//                                    if (!existsConflictingExtractVariableRefactoring(ref) && !existsConflictingMergeVariableRefactoring(ref) && !existsConflictingSplitVariableRefactoring(ref)) {
//                                        variableRenames.add(ref);
//                                        if (!v1.getKey().getKind().equals(v2.getKey().getKind())
//                                                || !v1.getKey().getType().equalsQualified(v2.getKey().getType())) {
//                                            ChangeVariableKindRefactoring refactoring = new ChangeVariableKindRefactoring(v1.getKey(), v2.getKey(), v1.getValue(), v2.getValue(), references);
//                                            refactoring.addRelatedRefactoring(ref);
//                                            refactorings.add(refactoring);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    public Set<RenameVariableRefactoring> getVariableRenames() {
        return variableRenames;
    }

    public Set<MergeVariableRefactoring> getVariableMerges() {
        return variableMerges;
    }

    public Set<SplitVariableRefactoring> getVariableSplits() {
        return variableSplits;
    }

    public Set<CandidateAttributeRefactoring> getCandidateAttributeRenames() {
        return candidateAttributeRenames;
    }

    public Set<CandidateMergeVariableRefactoring> getCandidateAttributeMerges() {
        return candidateAttributeMerges;
    }

    public Set<CandidateSplitVariableRefactoring> getCandidateAttributeSplits() {
        return candidateAttributeSplits;
    }

    private void findVariableSplits() {
        Map<SplitVariableReplacement, Set<CodeFragmentMapping>> splitMap = new LinkedHashMap<SplitVariableReplacement, Set<CodeFragmentMapping>>();
        Map<String, Map<VariableReplacementWithMethodInvocation, Set<CodeFragmentMapping>>> variableInvocationExpressionMap = new LinkedHashMap<String, Map<VariableReplacementWithMethodInvocation, Set<CodeFragmentMapping>>>();
        for (CodeFragmentMapping mapping : mappings) {
            for (Replacement replacement : mapping.getReplacements()) {
                if (replacement instanceof SplitVariableReplacement) {
                    SplitVariableReplacement split = (SplitVariableReplacement) replacement;
                    if (splitMap.containsKey(split)) {
                        splitMap.get(split).add(mapping);
                    } else {
                        Set<CodeFragmentMapping> mappings = new LinkedHashSet<CodeFragmentMapping>();
                        mappings.add(mapping);
                        splitMap.put(split, mappings);
                    }
                } else if (replacement instanceof VariableReplacementWithMethodInvocation) {
                    VariableReplacementWithMethodInvocation variableReplacement = (VariableReplacementWithMethodInvocation) replacement;
                    processVariableReplacementWithMethodInvocation(variableReplacement, mapping, variableInvocationExpressionMap, VariableReplacementWithMethodInvocation.Direction.INVOCATION_TO_VARIABLE);
                } else if (replacement.getType().equals(ReplacementType.VARIABLE_NAME)) {
                    for (SingleStatement statement : nonMappedLeavesT1) {
                        VariableDeclaration variableDeclaration = statement.getVariableDeclaration(replacement.getBefore());
                        if (variableDeclaration != null) {
                            Expression initializer = variableDeclaration.getInitializer();
                            if (initializer != null) {
                                OperationInvocation invocation = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(initializer);
                                if (invocation != null) {
                                    VariableReplacementWithMethodInvocation variableReplacement
                                            = new VariableReplacementWithMethodInvocation(initializer.getText(), replacement.getAfter(), invocation, VariableReplacementWithMethodInvocation.Direction.INVOCATION_TO_VARIABLE);
                                    processVariableReplacementWithMethodInvocation(variableReplacement, mapping, variableInvocationExpressionMap, VariableReplacementWithMethodInvocation.Direction.INVOCATION_TO_VARIABLE);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (SingleStatement statement : nonMappedLeavesT1) {
            for (String parameterName : operation2.getParameterNameList()) {
                VariableDeclaration variableDeclaration = statement.getVariableDeclaration(parameterName);
                if (variableDeclaration != null) {
                    Expression initializer = variableDeclaration.getInitializer();
                    if (initializer != null) {
                        OperationInvocation invocation = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(initializer);
                        if (invocation != null) {
                            String expression = invocation.getExpression();
                            if (expression != null) {
                                VariableReplacementWithMethodInvocation variableReplacement = new VariableReplacementWithMethodInvocation(initializer.getText(), parameterName, invocation, Direction.INVOCATION_TO_VARIABLE);
                                processVariableReplacementWithMethodInvocation(variableReplacement, null, variableInvocationExpressionMap, Direction.INVOCATION_TO_VARIABLE);
                            }
                        }
                    }
                }
            }
        }
        for (String key : variableInvocationExpressionMap.keySet()) {
            Map<VariableReplacementWithMethodInvocation, Set<CodeFragmentMapping>> map = variableInvocationExpressionMap.get(key);
            Set<CodeFragmentMapping> mappings = new LinkedHashSet<CodeFragmentMapping>();
            Set<String> splitVariables = new LinkedHashSet<String>();
            for (VariableReplacementWithMethodInvocation replacement : map.keySet()) {
                if (!PrefixSuffixUtils.normalize(key).equals(PrefixSuffixUtils.normalize(replacement.getAfter()))) {
                    splitVariables.add(replacement.getAfter());
                    mappings.addAll(map.get(replacement));
                }
            }
            if (splitVariables.size() > 0) {
                SplitVariableReplacement split = new SplitVariableReplacement(key, splitVariables);
                splitMap.put(split, mappings);
            }
        }
        for (SplitVariableReplacement split : splitMap.keySet()) {
            Set<VariableDeclaration> splitVariables = new LinkedHashSet<VariableDeclaration>();
            Set<FunctionDeclaration> splitVariableOperations = new LinkedHashSet<FunctionDeclaration>();
            for (String variableName : split.getSplitVariables()) {
                SimpleEntry<VariableDeclaration, FunctionDeclaration> declaration = getVariableDeclaration2(split, variableName);
                if (declaration != null) {
                    splitVariables.add(declaration.getKey());
                    splitVariableOperations.add(declaration.getValue());
                }
            }
            SimpleEntry<VariableDeclaration, FunctionDeclaration> oldVariable = getVariableDeclaration1(split);
            if (splitVariables.size() > 1 && splitVariables.size() == split.getSplitVariables().size() && oldVariable != null) {
                FunctionDeclaration operationAfter = splitVariableOperations.iterator().next();
                SplitVariableRefactoring refactoring = new SplitVariableRefactoring(oldVariable.getKey(), splitVariables, oldVariable.getValue(), operationAfter, splitMap.get(split));
                if (!existsConflictingExtractVariableRefactoring(refactoring) && !existsConflictingParameterRenameInOperationDiff(refactoring)) {
                    variableSplits.add(refactoring);
                }
            } else {
                CandidateSplitVariableRefactoring candidate = new CandidateSplitVariableRefactoring(split.getBefore(), split.getSplitVariables(), operation1, operation2, splitMap.get(split));
                candidateAttributeSplits.add(candidate);
            }
        }
    }

    private void processVariableReplacementWithMethodInvocation(
            VariableReplacementWithMethodInvocation variableReplacement, CodeFragmentMapping mapping,
            Map<String, Map<VariableReplacementWithMethodInvocation, Set<CodeFragmentMapping>>> variableInvocationExpressionMap, VariableReplacementWithMethodInvocation.Direction direction) {
        String expression = variableReplacement.getInvokedOperation().getExpression();
        if (expression != null && variableReplacement.getDirection().equals(direction)) {
            if (variableInvocationExpressionMap.containsKey(expression)) {
                Map<VariableReplacementWithMethodInvocation, Set<CodeFragmentMapping>> map = variableInvocationExpressionMap.get(expression);
                if (map.containsKey(variableReplacement)) {
                    if (mapping != null) {
                        map.get(variableReplacement).add(mapping);
                    }
                } else {
                    Set<CodeFragmentMapping> mappings = new LinkedHashSet<CodeFragmentMapping>();
                    if (mapping != null) {
                        mappings.add(mapping);
                    }
                    map.put(variableReplacement, mappings);
                }
            } else {
                Set<CodeFragmentMapping> mappings = new LinkedHashSet<CodeFragmentMapping>();
                if (mapping != null) {
                    mappings.add(mapping);
                }
                Map<VariableReplacementWithMethodInvocation, Set<CodeFragmentMapping>> map = new LinkedHashMap<VariableReplacementWithMethodInvocation, Set<CodeFragmentMapping>>();
                map.put(variableReplacement, mappings);
                variableInvocationExpressionMap.put(expression, map);
            }
        }
    }

    private void findVariableMerges() {
        Map<MergeVariableReplacement, Set<CodeFragmentMapping>> mergeMap = new LinkedHashMap<MergeVariableReplacement, Set<CodeFragmentMapping>>();
        Map<String, Map<VariableReplacementWithMethodInvocation, Set<CodeFragmentMapping>>> variableInvocationExpressionMap = new LinkedHashMap<String, Map<VariableReplacementWithMethodInvocation, Set<CodeFragmentMapping>>>();
        Map<String, Map<Replacement, Set<CodeFragmentMapping>>> variableInvocationVariableMap = new LinkedHashMap<String, Map<Replacement, Set<CodeFragmentMapping>>>();
        for (CodeFragmentMapping mapping : mappings) {
            for (Replacement replacement : mapping.getReplacements()) {
                if (replacement instanceof MergeVariableReplacement) {
                    MergeVariableReplacement merge = (MergeVariableReplacement) replacement;
                    if (mergeMap.containsKey(merge)) {
                        mergeMap.get(merge).add(mapping);
                    } else {
                        Set<CodeFragmentMapping> mappings = new LinkedHashSet<CodeFragmentMapping>();
                        mappings.add(mapping);
                        mergeMap.put(merge, mappings);
                    }
                } else if (replacement instanceof VariableReplacementWithMethodInvocation) {
                    VariableReplacementWithMethodInvocation variableReplacement = (VariableReplacementWithMethodInvocation) replacement;
                    processVariableReplacementWithMethodInvocation(variableReplacement, mapping, variableInvocationExpressionMap, Direction.VARIABLE_TO_INVOCATION);
                } else if (replacement instanceof MethodInvocationReplacement) {
                    MethodInvocationReplacement invocationReplacement = (MethodInvocationReplacement) replacement;
                    OperationInvocation invocationBefore = invocationReplacement.getInvokedOperationBefore();
                    OperationInvocation invocationAfter = invocationReplacement.getInvokedOperationAfter();
                    if (invocationBefore.identicalName(invocationAfter) && invocationBefore.identicalExpression(invocationAfter) && !invocationBefore.equalArguments(invocationAfter)) {
                        Set<String> argumentIntersection = new LinkedHashSet<String>(invocationBefore.getArguments());
                        argumentIntersection.retainAll(invocationAfter.getArguments());
                        Set<String> arguments1WithoutCommon = new LinkedHashSet<String>(invocationBefore.getArguments());
                        arguments1WithoutCommon.removeAll(argumentIntersection);
                        Set<String> arguments2WithoutCommon = new LinkedHashSet<String>(invocationAfter.getArguments());
                        arguments2WithoutCommon.removeAll(argumentIntersection);
                        if (arguments1WithoutCommon.size() > arguments2WithoutCommon.size() && arguments2WithoutCommon.size() == 1) {
                            MergeVariableReplacement merge = new MergeVariableReplacement(arguments1WithoutCommon, arguments2WithoutCommon.iterator().next());
                            if (mergeMap.containsKey(merge)) {
                                mergeMap.get(merge).add(mapping);
                            } else {
                                Set<CodeFragmentMapping> mappings = new LinkedHashSet<CodeFragmentMapping>();
                                mappings.add(mapping);
                                mergeMap.put(merge, mappings);
                            }
                        }
                    }
                } else if (replacement.getType().equals(ReplacementType.VARIABLE_NAME)) {
                    for (SingleStatement statement : nonMappedLeavesT2) {
                        VariableDeclaration variableDeclaration = statement.getVariableDeclaration(replacement.getBefore());
                        if (variableDeclaration != null) {
                            Expression initializer = variableDeclaration.getInitializer();
                            if (initializer != null) {
                                OperationInvocation invocation = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(initializer);
                                if (invocation != null) {
                                    VariableReplacementWithMethodInvocation variableReplacement = new VariableReplacementWithMethodInvocation(replacement.getBefore(), initializer.getText(), invocation, VariableReplacementWithMethodInvocation.Direction.VARIABLE_TO_INVOCATION);
                                    processVariableReplacementWithMethodInvocation(variableReplacement, mapping, variableInvocationExpressionMap, VariableReplacementWithMethodInvocation.Direction.VARIABLE_TO_INVOCATION);
                                }
                            }
                        }
                    }
                    if (replacement.getAfter().contains(".")) {
                        String compositeVariable = replacement.getAfter().substring(0, replacement.getAfter().indexOf("."));
                        if (variableInvocationVariableMap.containsKey(compositeVariable)) {
                            Map<Replacement, Set<CodeFragmentMapping>> map = variableInvocationVariableMap.get(compositeVariable);
                            if (map.containsKey(replacement)) {
                                if (mapping != null) {
                                    map.get(replacement).add(mapping);
                                }
                            } else {
                                Set<CodeFragmentMapping> mappings = new LinkedHashSet<CodeFragmentMapping>();
                                if (mapping != null) {
                                    mappings.add(mapping);
                                }
                                map.put(replacement, mappings);
                            }
                        } else {
                            Set<CodeFragmentMapping> mappings = new LinkedHashSet<CodeFragmentMapping>();
                            if (mapping != null) {
                                mappings.add(mapping);
                            }
                            Map<Replacement, Set<CodeFragmentMapping>> map = new LinkedHashMap<Replacement, Set<CodeFragmentMapping>>();
                            map.put(replacement, mappings);
                            variableInvocationVariableMap.put(compositeVariable, map);
                        }
                    }
                }
            }
        }
        for (SingleStatement statement : nonMappedLeavesT2) {
            for (String parameterName : operation1.getParameterNameList()) {
                VariableDeclaration variableDeclaration = statement.getVariableDeclaration(parameterName);
                if (variableDeclaration != null) {
                    Expression initializer = variableDeclaration.getInitializer();
                    if (initializer != null) {
                        OperationInvocation invocation = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(initializer);
                        if (invocation != null) {
                            VariableReplacementWithMethodInvocation variableReplacement = new VariableReplacementWithMethodInvocation(parameterName, initializer.getText(), invocation, Direction.VARIABLE_TO_INVOCATION);
                            processVariableReplacementWithMethodInvocation(variableReplacement, null, variableInvocationExpressionMap, Direction.VARIABLE_TO_INVOCATION);
                        }
                    }
                }
            }
        }
        for (String key : variableInvocationExpressionMap.keySet()) {
            Map<VariableReplacementWithMethodInvocation, Set<CodeFragmentMapping>> map = variableInvocationExpressionMap.get(key);
            Set<CodeFragmentMapping> mappings = new LinkedHashSet<CodeFragmentMapping>();
            Set<String> mergedVariables = new LinkedHashSet<String>();
            for (VariableReplacementWithMethodInvocation replacement : map.keySet()) {
                if (!PrefixSuffixUtils.normalize(key).equals(PrefixSuffixUtils.normalize(replacement.getBefore()))) {
                    mergedVariables.add(replacement.getBefore());
                    mappings.addAll(map.get(replacement));
                }
            }
            if (mergedVariables.size() > 0) {
                MergeVariableReplacement merge = new MergeVariableReplacement(mergedVariables, key);
                mergeMap.put(merge, mappings);
            }
        }
        for (String key : variableInvocationVariableMap.keySet()) {
            Map<Replacement, Set<CodeFragmentMapping>> map = variableInvocationVariableMap.get(key);
            Set<CodeFragmentMapping> mappings = new LinkedHashSet<CodeFragmentMapping>();
            Set<String> mergedVariables = new LinkedHashSet<String>();
            for (Replacement replacement : map.keySet()) {
                if (!PrefixSuffixUtils.normalize(key).equals(PrefixSuffixUtils.normalize(replacement.getBefore()))) {
                    mergedVariables.add(replacement.getBefore());
                    mappings.addAll(map.get(replacement));
                }
            }
            if (mergedVariables.size() > 0) {
                MergeVariableReplacement merge = new MergeVariableReplacement(mergedVariables, key);
                mergeMap.put(merge, mappings);
            }
        }
        for (MergeVariableReplacement merge : mergeMap.keySet()) {
            Set<VariableDeclaration> mergedVariables = new LinkedHashSet<VariableDeclaration>();
            Set<FunctionDeclaration> mergedVariableOperations = new LinkedHashSet<FunctionDeclaration>();
            for (String variableName : merge.getMergedVariables()) {
                SimpleEntry<VariableDeclaration, FunctionDeclaration> declaration = getVariableDeclaration1(merge, variableName);
                if (declaration != null) {
                    mergedVariables.add(declaration.getKey());
                    mergedVariableOperations.add(declaration.getValue());
                }
            }
            SimpleEntry<VariableDeclaration, FunctionDeclaration> newVariable = getVariableDeclaration2(merge);
            if (mergedVariables.size() > 1 && mergedVariables.size() == merge.getMergedVariables().size() && newVariable != null) {
                FunctionDeclaration operationBefore = mergedVariableOperations.iterator().next();
                MergeVariableRefactoring refactoring = new MergeVariableRefactoring(mergedVariables, newVariable.getKey(), operationBefore, newVariable.getValue(), mergeMap.get(merge));
                if (!existsConflictingInlineVariableRefactoring(refactoring) && !existsConflictingParameterRenameInOperationDiff(refactoring)) {
                    variableMerges.add(refactoring);
                    VariableDeclaration firstMergedVariable = null;
                    boolean allMergedVariablesHaveEqualAnnotations = true;
                    for (VariableDeclaration mergedVariable : mergedVariables) {
                        if (firstMergedVariable == null) {
                            firstMergedVariable = mergedVariable;
                        }
//                        else if (!firstMergedVariable.getAnnotations().equals(mergedVariable.getAnnotations())) {
//                            allMergedVariablesHaveEqualAnnotations = false;
//                            break;
//                        }
                    }
//                    if (allMergedVariablesHaveEqualAnnotations) {
//                        UMLAnnotationListDiff annotationListDiff = new UMLAnnotationListDiff(firstMergedVariable.getAnnotations(), newVariable.getKey().getAnnotations());
//                        for (UMLAnnotation annotation : annotationListDiff.getAddedAnnotations()) {
//                            AddVariableAnnotationRefactoring ref = new AddVariableAnnotationRefactoring(annotation, firstMergedVariable, newVariable.getKey(), operationBefore, newVariable.getValue());
//                            refactorings.add(ref);
//                        }
//                        for (UMLAnnotation annotation : annotationListDiff.getRemovedAnnotations()) {
//                            RemoveVariableAnnotationRefactoring ref = new RemoveVariableAnnotationRefactoring(annotation, firstMergedVariable, newVariable.getKey(), operationBefore, newVariable.getValue());
//                            refactorings.add(ref);
//                        }
//                        for (UMLAnnotationDiff annotationDiff : annotationListDiff.getAnnotationDiffList()) {
//                            ModifyVariableAnnotationRefactoring ref = new ModifyVariableAnnotationRefactoring(annotationDiff.getRemovedAnnotation(), annotationDiff.getAddedAnnotation(), firstMergedVariable, newVariable.getKey(), operationBefore, newVariable.getValue());
//                            refactorings.add(ref);
//                        }
//                    }
                }
            } else {
                CandidateMergeVariableRefactoring candidate = new CandidateMergeVariableRefactoring(merge.getMergedVariables(), merge.getAfter(), operation1, operation2, mergeMap.get(merge));
                candidateAttributeMerges.add(candidate);
            }
        }
    }

    private void findConsistentVariableRenames() {
        Map<Replacement, Set<CodeFragmentMapping>> variableDeclarationReplacementOccurrenceMap = getVariableDeclarationReplacementOccurrenceMap();
        Set<Replacement> allConsistentVariableDeclarationRenames = allConsistentRenames(variableDeclarationReplacementOccurrenceMap);
        for (Replacement replacement : allConsistentVariableDeclarationRenames) {
            VariableDeclarationReplacement vdReplacement = (VariableDeclarationReplacement) replacement;
            Set<CodeFragmentMapping> set = variableDeclarationReplacementOccurrenceMap.get(vdReplacement);
            if ((set.size() > 1 && consistencyCheck(vdReplacement.getVariableDeclaration1(), vdReplacement.getVariableDeclaration2(), set)) ||
                    (set.size() == 1 && replacementInLocalVariableDeclaration(vdReplacement.getVariableNameReplacement(), set))) {
                RenameVariableRefactoring ref = new RenameVariableRefactoring(vdReplacement.getVariableDeclaration1(), vdReplacement.getVariableDeclaration2(), vdReplacement.getOperation1(), vdReplacement.getOperation2(), set);
                if (!existsConflictingExtractVariableRefactoring(ref) && !existsConflictingMergeVariableRefactoring(ref) && !existsConflictingSplitVariableRefactoring(ref)) {
                    variableRenames.add(ref);
                    if (!vdReplacement.getVariableDeclaration1().getKind().equals(vdReplacement.getVariableDeclaration2().getKind()) /*|| !vdReplacement.getVariableDeclaration1().getType().equalsQualified(vdReplacement.getVariableDeclaration2().getType())*/) {
                        ChangeVariableKindRefactoring refactoring = new ChangeVariableKindRefactoring(vdReplacement.getVariableDeclaration1(), vdReplacement.getVariableDeclaration2(), vdReplacement.getOperation1(), vdReplacement.getOperation2(), set);
                        refactoring.addRelatedRefactoring(ref);
                        refactorings.add(refactoring);
                    }
//                    UMLAnnotationListDiff annotationListDiff = new UMLAnnotationListDiff(vdReplacement.getVariableDeclaration1().getAnnotations(), vdReplacement.getVariableDeclaration2().getAnnotations());
//                    for (UMLAnnotation annotation : annotationListDiff.getAddedAnnotations()) {
//                        AddVariableAnnotationRefactoring refactoring = new AddVariableAnnotationRefactoring(annotation, vdReplacement.getVariableDeclaration1(), vdReplacement.getVariableDeclaration2(), vdReplacement.getOperation1(), vdReplacement.getOperation2());
//                        refactorings.add(refactoring);
//                    }
//                    for (UMLAnnotation annotation : annotationListDiff.getRemovedAnnotations()) {
//                        RemoveVariableAnnotationRefactoring refactoring = new RemoveVariableAnnotationRefactoring(annotation, vdReplacement.getVariableDeclaration1(), vdReplacement.getVariableDeclaration2(), vdReplacement.getOperation1(), vdReplacement.getOperation2());
//                        refactorings.add(refactoring);
//                    }
//                    for (UMLAnnotationDiff annotationDiff : annotationListDiff.getAnnotationDiffList()) {
//                        ModifyVariableAnnotationRefactoring refactoring = new ModifyVariableAnnotationRefactoring(annotationDiff.getRemovedAnnotation(), annotationDiff.getAddedAnnotation(), vdReplacement.getVariableDeclaration1(), vdReplacement.getVariableDeclaration2(), vdReplacement.getOperation1(), vdReplacement.getOperation2());
//                        refactorings.add(refactoring);
//                    }
                }
            } else {
                RenameVariableRefactoring ref = new RenameVariableRefactoring(vdReplacement.getVariableDeclaration1(), vdReplacement.getVariableDeclaration2(), vdReplacement.getOperation1(), vdReplacement.getOperation2(), set);
                if (refactorings.contains(ref)) {
                    refactorings.remove(ref);
                }
            }
        }
        Map<Replacement, Set<CodeFragmentMapping>> replacementOccurrenceMap = getReplacementOccurrenceMap(ReplacementType.VARIABLE_NAME);
        Set<Replacement> allConsistentRenames = allConsistentRenames(replacementOccurrenceMap);
        Map<Replacement, Set<CodeFragmentMapping>> finalConsistentRenames = new LinkedHashMap<Replacement, Set<CodeFragmentMapping>>();
        for (Replacement replacement : allConsistentRenames) {
            SimpleEntry<VariableDeclaration, FunctionDeclaration> v1 = getVariableDeclaration1(replacement);
            SimpleEntry<VariableDeclaration, FunctionDeclaration> v2 = getVariableDeclaration2(replacement);
            Set<CodeFragmentMapping> set = replacementOccurrenceMap.get(replacement);
            if ((set.size() > 1 && v1 != null && v2 != null && consistencyCheck(v1.getKey(), v2.getKey(), set)) ||
                    potentialParameterRename(replacement, set) ||
                    v1 == null || v2 == null ||
                    (set.size() == 1 && replacementInLocalVariableDeclaration(replacement, set))) {
                finalConsistentRenames.put(replacement, set);
            }
            if (v1 != null && !v1.getKey().isParameter() && v2 != null && v2.getKey().isParameter() && consistencyCheck(v1.getKey(), v2.getKey(), set) &&
                    !operation1.getParameterNameList().contains(v2.getKey().getVariableName())) {
                finalConsistentRenames.put(replacement, set);
            }
        }
        for (Replacement replacement : finalConsistentRenames.keySet()) {
            SimpleEntry<VariableDeclaration, FunctionDeclaration> v1 = getVariableDeclaration1(replacement);
            SimpleEntry<VariableDeclaration, FunctionDeclaration> v2 = getVariableDeclaration2(replacement);
            if (v1 != null && v2 != null) {
                Set<CodeFragmentMapping> variableReferences = finalConsistentRenames.get(replacement);
                RenameVariableRefactoring ref = new RenameVariableRefactoring(v1.getKey(), v2.getKey(), v1.getValue(), v2.getValue(), variableReferences);
                if (!existsConflictingExtractVariableRefactoring(ref)
                        && !existsConflictingMergeVariableRefactoring(ref)
                        && !existsConflictingSplitVariableRefactoring(ref))
                //                  &&
                //       v1.getKey().isVarargsParameter() == v2.getKey().isVarargsParameter())
                {
                    variableRenames.add(ref);
                    if (!v1.getKey().getKind().equals(v2.getKey().getKind())) {
                        ChangeVariableKindRefactoring refactoring = new ChangeVariableKindRefactoring(v1.getKey(), v2.getKey(), v1.getValue(), v2.getValue(), variableReferences);
                        refactoring.addRelatedRefactoring(ref);
                        refactorings.add(refactoring);
                    }
//                    if (!v1.getKey().getType().equals(v2.getKey().getType()) || !v1.getKey().getType().equalsQualified(v2.getKey().getType())) {
//                        ChangeVariableKindRefactoring refactoring = new ChangeVariableKindRefactoring(v1.getKey(), v2.getKey(), v1.getValue(), v2.getValue(), variableReferences);
//                        refactoring.addRelatedRefactoring(ref);
//                        refactorings.add(refactoring);
//                    }
////                    UMLAnnotationListDiff annotationListDiff = new UMLAnnotationListDiff(v1.getKey().getAnnotations(), v2.getKey().getAnnotations());
////                    for (UMLAnnotation annotation : annotationListDiff.getAddedAnnotations()) {
////                        AddVariableAnnotationRefactoring refactoring = new AddVariableAnnotationRefactoring(annotation, v1.getKey(), v2.getKey(), v1.getValue(), v2.getValue());
////                        refactorings.add(refactoring);
////                    }
////                    for (UMLAnnotation annotation : annotationListDiff.getRemovedAnnotations()) {
////                        RemoveVariableAnnotationRefactoring refactoring = new RemoveVariableAnnotationRefactoring(annotation, v1.getKey(), v2.getKey(), v1.getValue(), v2.getValue());
////                        refactorings.add(refactoring);
////                    }
////                    for (UMLAnnotationDiff annotationDiff : annotationListDiff.getAnnotationDiffList()) {
////                        ModifyVariableAnnotationRefactoring refactoring = new ModifyVariableAnnotationRefactoring(annotationDiff.getRemovedAnnotation(), annotationDiff.getAddedAnnotation(), v1.getKey(), v2.getKey(), v1.getValue(), v2.getValue());
////                        refactorings.add(refactoring);
//                    }
                }
            } else if (!PrefixSuffixUtils.normalize(replacement.getBefore()).equals(PrefixSuffixUtils.normalize(replacement.getAfter())) &&
                    (!operation1.getAllVariables().contains(replacement.getAfter()) || cyclicRename(finalConsistentRenames.keySet(), replacement)) &&
                    (!operation2.getAllVariables().contains(replacement.getBefore()) || cyclicRename(finalConsistentRenames.keySet(), replacement)) &&
                    !fieldAssignmentWithPreviouslyExistingParameter(replacementOccurrenceMap.get(replacement)) &&
                    !fieldAssignmentToPreviouslyExistingAttribute(replacementOccurrenceMap.get(replacement))) {

                CandidateAttributeRefactoring candidate = new CandidateAttributeRefactoring(
                        replacement.getBefore(), replacement.getAfter(), operation1, operation2,
                        replacementOccurrenceMap.get(replacement));
                if (v1 != null)
                    candidate.setOriginalVariableDeclaration(v1.getKey());
                if (v2 != null)
                    candidate.setRenamedVariableDeclaration(v2.getKey());
                this.candidateAttributeRenames.add(candidate);
            }
        }
    }

    private boolean fieldAssignmentToPreviouslyExistingAttribute(Set<CodeFragmentMapping> mappings) {
        if (mappings.size() == 1) {
            CodeFragmentMapping mapping = mappings.iterator().next();
            String fragment1 = mapping.getFragment1().getText();
            String fragment2 = mapping.getFragment2().getText();
            if (fragment1.contains("=") && fragment1.endsWith(";\n") && fragment2.contains("=") && fragment2.endsWith(";\n")) {
                String value1 = fragment1.substring(fragment1.indexOf("=") + 1, fragment1.lastIndexOf(";\n"));
                String value2 = fragment2.substring(fragment2.indexOf("=") + 1, fragment2.lastIndexOf(";\n"));
                String attribute1 = PrefixSuffixUtils.normalize(fragment1.substring(0, fragment1.indexOf("=")));
                String attribute2 = PrefixSuffixUtils.normalize(fragment2.substring(0, fragment2.indexOf("=")));
//                if (value1.equals(attribute1) && containerDiff.getOriginalClass().containsAttributeWithName(attribute1) && containerDiff.getNextClass().containsAttributeWithName(attribute1)) {
//                    return true;
//                }
//                if (value2.equals(attribute2) && containerDiff.getOriginalClass().containsAttributeWithName(attribute2) && containerDiff.getNextClass().containsAttributeWithName(attribute2)) {
//                    return true;
//                }
            }
        }
        return false;
    }

    private boolean fieldAssignmentWithPreviouslyExistingParameter(Set<CodeFragmentMapping> mappings) {
        if (mappings.size() == 1) {
            CodeFragmentMapping mapping = mappings.iterator().next();
            String fragment1 = mapping.fragment1.getText();
            String fragment2 = mapping.fragment2.getText();
            if (fragment1.contains("=") && fragment1.endsWith(String.valueOf(JsConfig.STATEMENT_TERMINATOR_CHAR)) && fragment2.contains("=") && fragment2.endsWith(String.valueOf(JsConfig.STATEMENT_TERMINATOR_CHAR))) {
                String value1 = fragment1.substring(fragment1.indexOf("=") + 1, fragment1.lastIndexOf(JsConfig.STATEMENT_TERMINATOR_CHAR));
                String value2 = fragment2.substring(fragment2.indexOf("=") + 1, fragment2.lastIndexOf(JsConfig.STATEMENT_TERMINATOR_CHAR));

                if (operation1.getParameterNameList().contains(value1)
                        && operation2.getParameterNameList().contains(value1) && operationDiff != null) {
                    for (UMLParameter addedParameter : operationDiff.getAddedParameters().values()) {
                        if (addedParameter.name.equals(value2)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private Map<Replacement, Set<CodeFragmentMapping>> getReplacementOccurrenceMap(ReplacementType type) {
        Map<Replacement, Set<CodeFragmentMapping>> map = new LinkedHashMap<>();
        for (CodeFragmentMapping mapping : mappings) {
            for (Replacement replacement : mapping.getReplacements()) {
                if (replacement.getType().equals(type) && !returnVariableMapping(mapping, replacement) && !mapping.containsReplacement(ReplacementType.CONCATENATION) &&
                        !containsMethodInvocationReplacementWithDifferentExpressionNameAndArguments(mapping.getReplacements()) &&
                        replacementNotInsideMethodSignatureOfAnonymousClass(mapping, replacement)) {
                    if (map.containsKey(replacement)) {
                        map.get(replacement).add(mapping);
                    } else {
                        Set<CodeFragmentMapping> list = new LinkedHashSet<>();
                        list.add(mapping);
                        map.put(replacement, list);
                    }
                } else if (replacement.getType().equals(ReplacementType.VARIABLE_REPLACED_WITH_ARRAY_ACCESS)) {
                    String before = replacement.getBefore().contains("[") ? replacement.getBefore().substring(0, replacement.getBefore().indexOf("[")) : replacement.getBefore();
                    String after = replacement.getAfter().contains("[") ? replacement.getAfter().substring(0, replacement.getAfter().indexOf("[")) : replacement.getAfter();
                    Replacement variableReplacement = new Replacement(before, after, ReplacementType.VARIABLE_NAME);
                    if (!returnVariableMapping(mapping, replacement) &&
                            !containsMethodInvocationReplacementWithDifferentExpressionNameAndArguments(mapping.getReplacements()) &&
                            replacementNotInsideMethodSignatureOfAnonymousClass(mapping, replacement)) {
                        if (map.containsKey(variableReplacement)) {
                            map.get(variableReplacement).add(mapping);
                        } else {
                            Set<CodeFragmentMapping> list = new LinkedHashSet<>();
                            list.add(mapping);
                            map.put(variableReplacement, list);
                        }
                    }
                } else if (replacement.getType().equals(ReplacementType.METHOD_INVOCATION)) {
                    MethodInvocationReplacement methodInvocationReplacement = (MethodInvocationReplacement) replacement;
                    OperationInvocation invocation1 = methodInvocationReplacement.getInvokedOperationBefore();
                    OperationInvocation invocation2 = methodInvocationReplacement.getInvokedOperationAfter();
                    if (invocation1.getFunctionName().equals(invocation2.getFunctionName()) && invocation1.getArguments().size() == invocation2.getArguments().size()) {
                        for (int i = 0; i < invocation1.getArguments().size(); i++) {
                            String argument1 = invocation1.getArguments().get(i);
                            String argument2 = invocation2.getArguments().get(i);
                            if (argument1.contains("[") || argument2.contains("[")) {
                                String before = argument1.contains("[") ? argument1.substring(0, argument1.indexOf("[")) : argument1;
                                String after = argument2.contains("[") ? argument2.substring(0, argument2.indexOf("[")) : argument2;
                                Replacement variableReplacement = new Replacement(before, after, ReplacementType.VARIABLE_NAME);
                                if (!returnVariableMapping(mapping, replacement) &&
                                        !containsMethodInvocationReplacementWithDifferentExpressionNameAndArguments(mapping.getReplacements()) &&
                                        replacementNotInsideMethodSignatureOfAnonymousClass(mapping, replacement)) {
                                    if (map.containsKey(variableReplacement)) {
                                        map.get(variableReplacement).add(mapping);
                                    } else {
                                        Set<CodeFragmentMapping> list = new LinkedHashSet<>();
                                        list.add(mapping);
                                        map.put(variableReplacement, list);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return map;
    }

    private Map<Replacement, Set<CodeFragmentMapping>> getVariableDeclarationReplacementOccurrenceMap() {
        Map<Replacement, Set<CodeFragmentMapping>> map = new LinkedHashMap<>();
        for (CodeFragmentMapping mapping : mappings) {
            for (Replacement replacement : mapping.getReplacements()) {
                if (replacement.getType().equals(ReplacementType.VARIABLE_NAME) && !returnVariableMapping(mapping, replacement) && !mapping.containsReplacement(ReplacementType.CONCATENATION) &&
                        !containsMethodInvocationReplacementWithDifferentExpressionNameAndArguments(mapping.getReplacements()) &&
                        replacementNotInsideMethodSignatureOfAnonymousClass(mapping, replacement)) {
                    AbstractMap.SimpleEntry<VariableDeclaration, FunctionDeclaration> v1 = getVariableDeclaration1(replacement, mapping);
                    AbstractMap.SimpleEntry<VariableDeclaration, FunctionDeclaration> v2 = getVariableDeclaration2(replacement, mapping);
                    if (v1 != null && v2 != null) {
                        VariableDeclarationReplacement r = new VariableDeclarationReplacement(v1.getKey(), v2.getKey(), v1.getValue(), v2.getValue());
                        if (map.containsKey(r)) {
                            map.get(r).add(mapping);
                        } else {
                            Set<CodeFragmentMapping> list = new LinkedHashSet<>();
                            list.add(mapping);
                            map.put(r, list);
                        }
                    }
                }
            }
        }
        if (operationDiff != null) {
            List<UMLParameterDiff> allParameterDiffs = new ArrayList<UMLParameterDiff>();
            for (UMLParameterDiff parameterDiff : operationDiff.getParameterDiffs()) {
                if (parameterDiff.isNameChanged) {
                    allParameterDiffs.add(parameterDiff);
                }
            }
            List<UMLParameterDiff> matchedParameterDiffs = new ArrayList<UMLParameterDiff>();
            for (UMLParameterDiff parameterDiff : allParameterDiffs) {
                for (Replacement replacement : map.keySet()) {
                    VariableDeclarationReplacement vdR = (VariableDeclarationReplacement) replacement;
                    if (parameterDiff.getRemovedParameter().getVariableDeclaration().equals(vdR.getVariableDeclaration1()) &&
                            parameterDiff.getAddedParameter().getVariableDeclaration().equals(vdR.getVariableDeclaration2())) {
                        matchedParameterDiffs.add(parameterDiff);
                        break;
                    }
                }
            }
            Set<VariableDeclarationReplacement> keysToBeRemoved = new LinkedHashSet<VariableDeclarationReplacement>();
            for (UMLParameterDiff parameterDiff : matchedParameterDiffs) {
                for (Replacement replacement : map.keySet()) {
                    VariableDeclarationReplacement vdR = (VariableDeclarationReplacement) replacement;
                    if (parameterDiff.getRemovedParameter().getVariableDeclaration().equals(vdR.getVariableDeclaration1()) &&
                            !parameterDiff.getAddedParameter().getVariableDeclaration().equals(vdR.getVariableDeclaration2())) {
                        keysToBeRemoved.add(vdR);
                    } else if (!parameterDiff.getRemovedParameter().getVariableDeclaration().equals(vdR.getVariableDeclaration1()) &&
                            parameterDiff.getAddedParameter().getVariableDeclaration().equals(vdR.getVariableDeclaration2())) {
                        keysToBeRemoved.add(vdR);
                    }
                }
            }
            for (VariableDeclarationReplacement key : keysToBeRemoved) {
                map.remove(key);
            }
        }
        return map;
    }

    private static boolean returnVariableMapping(CodeFragmentMapping mapping, Replacement replacement) {
        return mapping.fragment1.getText().equals("return " + replacement.getBefore() + JsConfig.STATEMENT_TERMINATOR_CHAR) &&
                mapping.fragment2.getText().equals("return " + replacement.getAfter() + JsConfig.STATEMENT_TERMINATOR_CHAR);
    }

    private boolean containsMethodInvocationReplacementWithDifferentExpressionNameAndArguments(Set<Replacement> replacements) {
        for (Replacement replacement : replacements) {
            if (replacement instanceof MethodInvocationReplacement) {
                MethodInvocationReplacement r = (MethodInvocationReplacement) replacement;
                if (r.differentExpressionNameAndArguments())
                    return true;
            }
        }
        return false;
    }

    private boolean replacementNotInsideMethodSignatureOfAnonymousClass(CodeFragmentMapping mapping, Replacement replacement) {
        CodeFragment fragment1 = mapping.fragment1;
        CodeFragment fragment2 = mapping.fragment2;
        List<IAnonymousFunctionDeclaration> anonymousClassDeclarations1 = fragment1.getAnonymousFunctionDeclarations();
        List<IAnonymousFunctionDeclaration> anonymousClassDeclarations2 = fragment2.getAnonymousFunctionDeclarations();
        if (anonymousClassDeclarations1.size() > 0 && anonymousClassDeclarations2.size() > 0) {
            boolean replacementBeforeNotFoundInMethodSignature = false;
            String[] lines1 = fragment1.getText().split("\\n");
            for (String line : lines1) {
                line = prepareLine(line);
                if (!JsConfig.METHOD_SIGNATURE_PATTERN.matcher(line).matches() &&
                        ReplacementUtil.contains(line, replacement.getBefore())) {
                    replacementBeforeNotFoundInMethodSignature = true;
                    break;
                }
            }
            boolean replacementAfterNotFoundInMethodSignature = false;
            String[] lines2 = fragment2.getText().split("\\n");
            for (String line : lines2) {
                line = prepareLine(line);
                if (!JsConfig.METHOD_SIGNATURE_PATTERN.matcher(line).matches() &&
                        ReplacementUtil.contains(line, replacement.getAfter())) {
                    replacementAfterNotFoundInMethodSignature = true;
                    break;
                }
            }
            return replacementBeforeNotFoundInMethodSignature && replacementAfterNotFoundInMethodSignature;
        }
        return true;
    }

    public static String prepareLine(String line) {
        line = line.trim();
        if (line.startsWith("@Nullable")) {
            line = line.substring(9, line.length());
            line = line.trim();
        }
        if (line.startsWith("@Override")) {
            line = line.substring(9, line.length());
            line = line.trim();
        }
        if (line.contains("throws ")) {
            line = line.substring(0, line.indexOf("throws "));
        }
        return line;
    }

    private static boolean cyclicRename(Set<Replacement> finalConsistentRenames, Replacement replacement) {
        for (Replacement r : finalConsistentRenames) {
            if (replacement.getAfter().equals(r.getBefore()))
                return true;
            if (replacement.getBefore().equals(r.getAfter()))
                return true;
        }
        return false;
    }

    private Set<Replacement> allConsistentRenames(Map<Replacement, Set<CodeFragmentMapping>> replacementOccurrenceMap) {
        Set<Replacement> renames = replacementOccurrenceMap.keySet();
        Set<Replacement> allConsistentRenames = new LinkedHashSet<Replacement>();
        Set<Replacement> allInconsistentRenames = new LinkedHashSet<Replacement>();
        ConsistentReplacementDetector.updateRenames(allConsistentRenames, allInconsistentRenames, renames);
        allConsistentRenames.removeAll(allInconsistentRenames);
        return allConsistentRenames;
    }

    private boolean replacementInLocalVariableDeclaration(Replacement replacement, Set<CodeFragmentMapping> set) {
        VariableDeclaration v1 = null;
        for (CodeFragmentMapping mapping : mappings) {
            if (mapping.getReplacements().contains(replacement)) {
                v1 = mapping.fragment1.findVariableDeclarationIncludingParent(replacement.getBefore());
                break;
            }
        }
        VariableDeclaration v2 = null;
        for (CodeFragmentMapping mapping : mappings) {
            if (mapping.getReplacements().contains(replacement)) {
                v2 = mapping.fragment2.findVariableDeclarationIncludingParent(replacement.getAfter());
                break;
            }
        }
        Set<VariableDeclaration> allVariableDeclarations1 = new LinkedHashSet<VariableDeclaration>();
        Set<VariableDeclaration> allVariableDeclarations2 = new LinkedHashSet<VariableDeclaration>();
        for (CodeFragmentMapping referenceMapping : set) {
            CodeFragment statement1 = referenceMapping.getFragment1();
            CodeFragment statement2 = referenceMapping.getFragment2();
            if (statement1 instanceof BlockStatement && statement2 instanceof BlockStatement &&
                    statement1.getCodeElementType().equals(CodeElementType.ENHANCED_FOR_STATEMENT)) {
                BlockStatement comp1 = (BlockStatement) statement1;
                BlockStatement comp2 = (BlockStatement) statement2;
                allVariableDeclarations1.addAll(comp1.getAllVariableDeclarations());
                allVariableDeclarations2.addAll(comp2.getAllVariableDeclarations());
            } else {
                allVariableDeclarations1.addAll(operation1.getAllVariableDeclarations());
                allVariableDeclarations2.addAll(operation2.getAllVariableDeclarations());
                break;
            }
        }
        return v1 != null && v2 != null &&
                //v1.equalVariableDeclarationType(v2) &&
                !containsVariableDeclarationWithName(allVariableDeclarations1, v2.variableName) &&
                (!containsVariableDeclarationWithName(allVariableDeclarations2, v1.variableName)
                        || operation2.loopWithVariables(v1.getVariableName(), v2.getVariableName()) != null) &&
                consistencyCheck(v1, v2, set);
    }

    private boolean consistencyCheck(VariableDeclaration v1, VariableDeclaration v2, Set<CodeFragmentMapping> set) {
        return !variableAppearsInExtractedMethod(v1, v2) &&
                !variableAppearsInTheInitializerOfTheOtherVariable(v1, v2) &&
                !inconsistentVariableMapping(v1, v2, set);
    }

    private boolean variableAppearsInTheInitializerOfTheOtherVariable(VariableDeclaration v1, VariableDeclaration v2) {
        if (v1.getInitializer() != null) {
            if (v1.getInitializer().getText().equals(v2.variableName)) {
                return true;
            }
            if (v1.getInitializer().getTernaryOperatorExpressions().size() == 1) {
                TernaryOperatorExpression ternary = v1.getInitializer().getTernaryOperatorExpressions().get(0);
                if (ternary.getThenExpression().getVariables().contains(v2.variableName) || ternary.getElseExpression().getVariables().contains(v2.variableName)) {
                    boolean v2InitializerContainsThisReference = false;
                    if (v2.getInitializer() != null && v2.getInitializer().getVariables().contains("this." + v2.variableName)) {
                        v2InitializerContainsThisReference = true;
                    }
                    if (!v2InitializerContainsThisReference) {
                        return true;
                    }
                }
            }
        }
        if (v2.getInitializer() != null) {
            if (v2.getInitializer().getText().equals(v1.variableName)) {
                return true;
            }
            if (v2.getInitializer().getTernaryOperatorExpressions().size() == 1) {
                TernaryOperatorExpression ternary = v2.getInitializer().getTernaryOperatorExpressions().get(0);
                if (ternary.getThenExpression().getVariables().contains(v1.variableName) || ternary.getElseExpression().getVariables().contains(v1.variableName)) {
                    boolean v1InitializerContainsThisReference = false;
                    if (v1.getInitializer() != null && v1.getInitializer().getVariables().contains("this." + v1.variableName)) {
                        v1InitializerContainsThisReference = true;
                    }
                    if (!v1InitializerContainsThisReference) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean inconsistentVariableMapping(VariableDeclaration v1, VariableDeclaration v2, Set<CodeFragmentMapping> set) {
        if (v1 != null && v2 != null) {
            for (CodeFragmentMapping mapping : mappings) {
                List<VariableDeclaration> variableDeclarations1 = mapping.getFragment1().getVariableDeclarations();
                List<VariableDeclaration> variableDeclarations2 = mapping.getFragment2().getVariableDeclarations();
                if (variableDeclarations1.contains(v1)) {
                    if (variableDeclarations2.size() > 0 && !variableDeclarations2.contains(v2)) {
                        return true;
                    } else if (variableDeclarations2.size() == 0 && v1.getInitializer() != null &&
                            mapping.getFragment2().getText().startsWith(v1.getInitializer().getText())) {
                        return true;
                    }
                }
                if (variableDeclarations2.contains(v2)) {
                    if (variableDeclarations1.size() > 0 && !variableDeclarations1.contains(v1)) {
                        return true;
                    } else if (variableDeclarations1.size() == 0 && v2.getInitializer() != null &&
                            mapping.getFragment1().getText().startsWith(v2.getInitializer().getText())) {
                        return true;
                    }
                }
                if (mapping.isExact()) {
                    for (CodeFragmentMapping referenceMapping : set) {
                        CodeFragment statement1 = referenceMapping.getFragment1();
                        CodeFragment statement2 = referenceMapping.getFragment2();
                        boolean containsMapping = true;
                        if (statement1 instanceof BlockStatement && statement2 instanceof BlockStatement &&
                                statement1.getCodeElementType().equals(CodeElementType.ENHANCED_FOR_STATEMENT)) {
                            BlockStatement comp1 = (BlockStatement) statement1;
                            BlockStatement comp2 = (BlockStatement) statement2;
                            containsMapping = comp1.contains(mapping.getFragment1()) && comp2.contains(mapping.getFragment2());
                        }
                        if (containsMapping && (bothFragmentsUseVariable(v1, mapping) || bothFragmentsUseVariable(v2, mapping)) &&
                                operation2.loopWithVariables(v1.getVariableName(), v2.getVariableName()) == null) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean bothFragmentsUseVariable(VariableDeclaration v1, CodeFragmentMapping mapping) {
        return mapping.getFragment1().getVariables().contains(v1.getVariableName()) &&
                mapping.getFragment2().getVariables().contains(v1.getVariableName());
    }

    private static boolean containsVariableDeclarationWithName(Set<VariableDeclaration> variableDeclarations, String variableName) {
        for (VariableDeclaration declaration : variableDeclarations) {
            if (declaration.getVariableName().equals(variableName)) {
                return true;
            }
        }
        return false;
    }

    private SimpleEntry<VariableDeclaration, FunctionDeclaration> getVariableDeclaration1(Replacement replacement) {
        for (CodeFragmentMapping mapping : mappings) {
            if (mapping.getReplacements().contains(replacement)) {
                VariableDeclaration vd = mapping.getFragment1().findVariableDeclarationIncludingParent(replacement.getBefore());
                if (vd != null) {
                    return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, mapping.getOperation1());
                }
            }
        }
        for (UMLParameter parameter : operation1.getParameters()) {
            VariableDeclaration vd = parameter.getVariableDeclaration();
            if (vd != null && vd.getVariableName().equals(replacement.getBefore())) {
                return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, operation1);
            }
        }
        if (callSiteOperation != null) {
            for (UMLParameter parameter : callSiteOperation.getParameters()) {
                VariableDeclaration vd = parameter.getVariableDeclaration();
                if (vd != null && vd.getVariableName().equals(replacement.getBefore())) {
                    return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, callSiteOperation);
                }
            }
        }
        return null;
    }

    private SimpleEntry<VariableDeclaration, FunctionDeclaration> getVariableDeclaration1(MergeVariableReplacement replacement, String variableName) {
        for (CodeFragmentMapping mapping : mappings) {
            Set<String> foundMergedVariables = new LinkedHashSet<String>();
            for (Replacement r : mapping.getReplacements()) {
                if (replacement.getMergedVariables().contains(r.getBefore())) {
                    foundMergedVariables.add(r.getBefore());
                }
            }
            if (mapping.getReplacements().contains(replacement) || foundMergedVariables.equals(replacement.getMergedVariables())) {
                VariableDeclaration vd = mapping.getFragment1().findVariableDeclarationIncludingParent(variableName);
                if (vd != null) {
                    return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, mapping.getOperation1());
                }
            }
        }
        for (UMLParameter parameter : operation1.getParameters()) {
            VariableDeclaration vd = parameter.getVariableDeclaration();
            if (vd != null && vd.getVariableName().equals(variableName)) {
                return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, operation1);
            }
        }
        if (callSiteOperation != null) {
            for (UMLParameter parameter : callSiteOperation.getParameters()) {
                VariableDeclaration vd = parameter.getVariableDeclaration();
                if (vd != null && vd.getVariableName().equals(variableName)) {
                    return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, callSiteOperation);
                }
            }
        }
        return null;
    }

    private SimpleEntry<VariableDeclaration, FunctionDeclaration> getVariableDeclaration2(Replacement replacement) {
        for (CodeFragmentMapping mapping : mappings) {
            if (mapping.getReplacements().contains(replacement)) {
                VariableDeclaration vd = mapping.getFragment2().findVariableDeclarationIncludingParent(replacement.getAfter());
                if (vd != null) {
                    return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, mapping.getOperation2());
                }
            }
        }
        for (UMLParameter parameter : operation2.getParameters()) {
            VariableDeclaration vd = parameter.getVariableDeclaration();
            if (vd != null && vd.getVariableName().equals(replacement.getAfter())) {
                return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, operation2);
            }
        }
        if (callSiteOperation != null) {
            for (UMLParameter parameter : callSiteOperation.getParameters()) {
                VariableDeclaration vd = parameter.getVariableDeclaration();
                if (vd != null && vd.getVariableName().equals(replacement.getAfter())) {
                    return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, callSiteOperation);
                }
            }
        }
        return null;
    }

    private SimpleEntry<VariableDeclaration, FunctionDeclaration> getVariableDeclaration2(SplitVariableReplacement replacement, String variableName) {
        for (CodeFragmentMapping mapping : mappings) {
            if (mapping.getReplacements().contains(replacement)) {
                Set<String> foundSplitVariables = new LinkedHashSet<String>();
                for (Replacement r : mapping.getReplacements()) {
                    if (replacement.getSplitVariables().contains(r.getAfter())) {
                        foundSplitVariables.add(r.getAfter());
                    }
                }
                if (mapping.getReplacements().contains(replacement) || foundSplitVariables.equals(replacement.getSplitVariables())) {
                    VariableDeclaration vd = mapping.getFragment2().findVariableDeclarationIncludingParent(variableName);
                    if (vd != null) {
                        return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, mapping.getOperation2());
                    }
                }
            }
        }
        for (UMLParameter parameter : operation2.getParameters()) {
            VariableDeclaration vd = parameter.getVariableDeclaration();
            if (vd != null && vd.getVariableName().equals(variableName)) {
                return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, operation2);
            }
        }
        if (callSiteOperation != null) {
            for (UMLParameter parameter : callSiteOperation.getParameters()) {
                VariableDeclaration vd = parameter.getVariableDeclaration();
                if (vd != null && vd.getVariableName().equals(variableName)) {
                    return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, callSiteOperation);
                }
            }
        }
        return null;
    }

    private SimpleEntry<VariableDeclaration, FunctionDeclaration> getVariableDeclaration2(MergeVariableReplacement replacement) {
        for (CodeFragmentMapping mapping : mappings) {
            Set<String> foundMergedVariables = new LinkedHashSet<String>();
            for (Replacement r : mapping.getReplacements()) {
                if (replacement.getMergedVariables().contains(r.getBefore())) {
                    foundMergedVariables.add(r.getBefore());
                }
            }
            if (mapping.getReplacements().contains(replacement) || foundMergedVariables.equals(replacement.getMergedVariables())) {
                VariableDeclaration vd = mapping.getFragment2().findVariableDeclarationIncludingParent(replacement.getAfter());
                if (vd != null) {
                    return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, mapping.getOperation2());
                }
            }
        }
        for (UMLParameter parameter : operation2.getParameters()) {
            VariableDeclaration vd = parameter.getVariableDeclaration();
            if (vd != null && vd.getVariableName().equals(replacement.getAfter())) {
                return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, operation2);
            }
        }
        if (callSiteOperation != null) {
            for (UMLParameter parameter : callSiteOperation.getParameters()) {
                VariableDeclaration vd = parameter.getVariableDeclaration();
                if (vd != null && vd.getVariableName().equals(replacement.getAfter())) {
                    return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, callSiteOperation);
                }
            }
        }
        return null;
    }

    private boolean variableAppearsInExtractedMethod(VariableDeclaration v1, VariableDeclaration v2) {
        if (v1 != null) {
            for (FunctionBodyMapper mapper : childMappers) {
                for (CodeFragmentMapping mapping : mapper.getMappings()) {
                    if (mapping.getFragment1().getVariableDeclarations().contains(v1)) {
                        if (v2 != null && v2.getInitializer() != null) {
                            FunctionDeclaration extractedMethod = mapper.function2;
                            Map<String, List<OperationInvocation>> methodInvocationMap = v2.getInitializer().getMethodInvocationMap();
                            for (String key : methodInvocationMap.keySet()) {
                                for (OperationInvocation invocation : methodInvocationMap.get(key)) {
                                    if (invocation.matchesOperation(extractedMethod)) {
                                        return false;
                                    } else {
                                        //check if the extracted method is called in the initializer of a variable used in the initializer of v2
                                        List<String> initializerVariables = v2.getInitializer().getVariables();
                                        for (String variable : initializerVariables) {
                                            for (VariableDeclaration declaration : operation2.getAllVariableDeclarations()) {
                                                if (declaration.getVariableName().equals(variable) && declaration.getInitializer() != null) {
                                                    Map<String, List<OperationInvocation>> methodInvocationMap2 = declaration.getInitializer().getMethodInvocationMap();
                                                    for (String key2 : methodInvocationMap2.keySet()) {
                                                        for (OperationInvocation invocation2 : methodInvocationMap2.get(key2)) {
                                                            if (invocation2.matchesOperation(extractedMethod)) {
                                                                return false;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return true;
                    }
                }
                for (SingleStatement nonMappedStatement : mapper.getNonMappedLeavesT2()) {
                    VariableDeclaration variableDeclaration2 = nonMappedStatement.getVariableDeclaration(v1.getVariableName());
                    if (variableDeclaration2 != null && variableDeclaration2.getKind().equals(v1.getKind())) {
                        for (CodeFragmentMapping mapping : mapper.getMappings()) {
                            if (mapping.getFragment2().equals(nonMappedStatement.getParent())) {
                                if (mapping.getFragment1() instanceof BlockStatement) {
                                    BlockStatement composite1 = (BlockStatement) mapping.getFragment1();
                                    List<SingleStatement> leaves1 = composite1.getAllLeafStatementsIncludingNested();
                                    for (SingleStatement leaf1 : leaves1) {
                                        VariableDeclaration variableDeclaration1 = leaf1.getVariableDeclaration(variableDeclaration2.getVariableName());
                                        if (variableDeclaration1 != null) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean existsConflictingParameterRenameInOperationDiff(MergeVariableRefactoring ref) {
        if (operationDiff != null) {
            for (UMLParameterDiff parameterDiff : operationDiff.getParameterDiffs()) {
                if (ref.getMergedVariables().contains(parameterDiff.getRemovedParameter().getVariableDeclaration()) &&
                        ref.getNewVariable().equals(parameterDiff.getAddedParameter().getVariableDeclaration())) {
                    return true;

                }
            }
        }
        return false;
    }

    private boolean existsConflictingParameterRenameInOperationDiff(SplitVariableRefactoring ref) {
        if (operationDiff != null) {
            for (UMLParameterDiff parameterDiff : operationDiff.getParameterDiffs()) {
                if (ref.getSplitVariables().contains(parameterDiff.getAddedParameter().getVariableDeclaration()) &&
                        ref.getOldVariable().equals(parameterDiff.getRemovedParameter().getVariableDeclaration())) {
                    return true;

                }
            }
        }
        return false;
    }

    private boolean existsConflictingExtractVariableRefactoring(RenameVariableRefactoring ref) {
        for (IRefactoring refactoring : refactorings) {
            if (refactoring instanceof ExtractVariableRefactoring) {
                ExtractVariableRefactoring extractVariableRef = (ExtractVariableRefactoring) refactoring;
                if (extractVariableRef.getVariableDeclaration().equals(ref.getRenamedVariable()) &&
                        extractVariableRef.getOperationAfter().equals(ref.getOperationAfter())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existsConflictingExtractVariableRefactoring(SplitVariableRefactoring ref) {
        for (IRefactoring refactoring : refactorings) {
            if (refactoring instanceof ExtractVariableRefactoring) {
                ExtractVariableRefactoring extractVariableRef = (ExtractVariableRefactoring) refactoring;
                if (ref.getSplitVariables().contains(extractVariableRef.getVariableDeclaration())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existsConflictingInlineVariableRefactoring(MergeVariableRefactoring ref) {
        for (IRefactoring refactoring : refactorings) {
            if (refactoring instanceof InlineVariableRefactoring) {
                InlineVariableRefactoring inlineVariableRef = (InlineVariableRefactoring) refactoring;
                if (ref.getMergedVariables().contains(inlineVariableRef.getVariableDeclaration())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existsConflictingMergeVariableRefactoring(RenameVariableRefactoring ref) {
        for (MergeVariableRefactoring merge : variableMerges) {
            if (merge.getOperationBefore().equals(ref.getOperationBefore()) &&
                    merge.getOperationAfter().equals(ref.getOperationAfter()) &&
                    merge.getMergedVariables().contains(ref.getOriginalVariable()) &&
                    merge.getNewVariable().equals(ref.getRenamedVariable())) {
                return true;
            }
        }
        return false;
    }

    private boolean existsConflictingSplitVariableRefactoring(RenameVariableRefactoring ref) {
        for (SplitVariableRefactoring split : variableSplits) {
            if (split.getOperationBefore().equals(ref.getOperationBefore()) &&
                    split.getOperationAfter().equals(ref.getOperationAfter()) &&
                    split.getSplitVariables().contains(ref.getRenamedVariable()) &&
                    split.getOldVariable().equals(ref.getOriginalVariable())) {
                return true;
            }
        }
        return false;
    }

    private boolean potentialParameterRename(Replacement replacement, Set<CodeFragmentMapping> set) {
        int index1 = operation1.getParameterNameList().indexOf(replacement.getBefore());
        if (index1 == -1 && callSiteOperation != null) {
            index1 = callSiteOperation.getParameterNameList().indexOf(replacement.getBefore());
        }
        int index2 = operation2.getParameterNameList().indexOf(replacement.getAfter());
        if (index2 == -1 && callSiteOperation != null) {
            index2 = callSiteOperation.getParameterNameList().indexOf(replacement.getAfter());
        }
        if (fieldAssignmentToPreviouslyExistingAttribute(set)) {
            return false;
        }
        if (fieldAssignmentWithPreviouslyExistingParameter(set)) {
            return false;
        }
        return index1 >= 0 && index1 == index2;
    }

    private SimpleEntry<VariableDeclaration, FunctionDeclaration> getVariableDeclaration1(Replacement replacement, CodeFragmentMapping mapping) {
        if (mapping.getReplacements().contains(replacement)) {
            VariableDeclaration vd = mapping.getFragment1().findVariableDeclarationIncludingParent(replacement.getBefore());
            if (vd != null) {
                return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, mapping.getOperation1());
            }
        }

        for (UMLParameter parameter : operation1.getParameters()) {
            VariableDeclaration vd = parameter.getVariableDeclaration();
            if (vd != null && vd.getVariableName().equals(replacement.getBefore())) {
                return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, operation1);
            }
        }
        if (callSiteOperation != null) {
            for (UMLParameter parameter : callSiteOperation.getParameters()) {
                VariableDeclaration vd = parameter.getVariableDeclaration();
                if (vd != null && vd.getVariableName().equals(replacement.getBefore())) {
                    return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, callSiteOperation);
                }
            }
        }
        return null;
    }

    private SimpleEntry<VariableDeclaration, FunctionDeclaration> getVariableDeclaration2(Replacement replacement, CodeFragmentMapping mapping) {
        if (mapping.getReplacements().contains(replacement)) {
            VariableDeclaration vd = mapping.getFragment2().findVariableDeclarationIncludingParent(replacement.getAfter());
            if (vd != null) {
                return new SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, mapping.getOperation2());
            }
        }
        for (UMLParameter parameter : operation2.getParameters()) {
            VariableDeclaration vd = parameter.getVariableDeclaration();
            if (vd != null && vd.getVariableName().equals(replacement.getAfter())) {
                return new AbstractMap.SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, operation2);
            }
        }
        if (callSiteOperation != null) {
            for (UMLParameter parameter : callSiteOperation.getParameters()) {
                VariableDeclaration vd = parameter.getVariableDeclaration();
                if (vd != null && vd.getVariableName().equals(replacement.getAfter())) {
                    return new AbstractMap.SimpleEntry<VariableDeclaration, FunctionDeclaration>(vd, callSiteOperation);
                }
            }
        }
        return null;
    }
}
