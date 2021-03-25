package io.jsrminer.uml.diff;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.CandidateAttributeRefactoring;
import io.jsrminer.refactorings.CandidateMergeVariableRefactoring;
import io.jsrminer.refactorings.CandidateSplitVariableRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.MapperRefactoringProcessor;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.FunctionUtil;
import io.jsrminer.uml.mapping.replacement.*;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.IFunctionDeclaration;

import java.util.*;

public class ContainerDiff {

    protected final List<IRefactoring> refactorings = new ArrayList<>();
    protected Set<MethodInvocationReplacement> consistentMethodInvocationRenames;

    protected Map<Replacement, Set<CandidateAttributeRefactoring>> renameMap = new LinkedHashMap<>();
    protected Map<MergeVariableReplacement, Set<CandidateMergeVariableRefactoring>> mergeMap = new LinkedHashMap<MergeVariableReplacement, Set<CandidateMergeVariableRefactoring>>();
    protected Map<SplitVariableReplacement, Set<CandidateSplitVariableRefactoring>> splitMap = new LinkedHashMap<SplitVariableReplacement, Set<CandidateSplitVariableRefactoring>>();

    protected final List<FunctionBodyMapper> bodyMapperList = new ArrayList<>();
    private List<UMLOperationDiff> operationDiffList = new ArrayList<>();

    /**
     * Name map
     */
    private FunctionBodyMapper bodyStatementMapper;
    MapperRefactoringProcessor mapperRefactoringProcessor = new MapperRefactoringProcessor();

    final IContainer container1;
    final IContainer container2;

    protected final List<FunctionDeclaration> addedOperations = new ArrayList<>();
    protected final List<FunctionDeclaration> removedOperations = new ArrayList<>();

    public ContainerDiff(IContainer container1, IContainer container2) {
        this.container1 = container1;
        this.container2 = container2;
    }

    public IContainer getContainer1() {
        return container1;
    }

    public IContainer getContainer2() {
        return container2;
    }

    public void reportAddedOperation(FunctionDeclaration addedOperation) {
        this.addedOperations.add(addedOperation);
    }

    public void reportRemovedOperation(FunctionDeclaration removedOperation) {
        this.removedOperations.add(removedOperation);
    }

    public List<FunctionDeclaration> getAddedOperations() {
        return addedOperations;
    }

    public List<FunctionDeclaration> getRemovedOperations() {
        return removedOperations;
    }

    public List<UMLOperationDiff> getOperationDiffList() {
        return operationDiffList;
    }

    public UMLOperationDiff getOperationDiff(FunctionDeclaration operation1, FunctionDeclaration operation2) {
        for (UMLOperationDiff diff : operationDiffList) {
            if (diff.function1.equals(operation1) && diff.function2.equals(operation2)) {
                return diff;
            }
        }
        return null;
    }

    public List<FunctionBodyMapper> getBodyMapperList() {
        return bodyMapperList;
    }

    public List<IRefactoring> getRefactoringsBeforePostProcessing() {
        return this.refactorings;
    }

    public Set<MethodInvocationReplacement> getConsistentMethodInvocationRenames() {
        return consistentMethodInvocationRenames;
    }

    public void setConsistentMethodInvocationRenames(Set<MethodInvocationReplacement> consistentMethodInvocationRenames) {
        this.consistentMethodInvocationRenames = consistentMethodInvocationRenames;
    }

    public FunctionBodyMapper getBodyStatementMapper() {
        return bodyStatementMapper;
    }

    public void setBodyStatementMapper(FunctionBodyMapper bodyStatementMapper) {
        this.bodyStatementMapper = bodyStatementMapper;
    }

    public void processMapperRefactorings(FunctionBodyMapper mapper, List<IRefactoring> refactorings) {
        mapperRefactoringProcessor.processMapperRefactorings(mapper
                , refactorings, this.renameMap, this.mergeMap, this.splitMap);
    }

    public static boolean allMappingsAreExactMatches(FunctionBodyMapper operationBodyMapper) {
        int mappings = operationBodyMapper.mappingsWithoutBlocks();
        int tryMappings = 0;
        int mappingsWithTypeReplacement = 0;
        for (CodeFragmentMapping mapping : operationBodyMapper.getMappings()) {
            if (mapping.getFragment1().getText().equals("try") && mapping.getFragment2()
                    .getText().equals("try")) {
                tryMappings++;
            }
            if (mapping.containsReplacement(ReplacementType.TYPE)) {
                mappingsWithTypeReplacement++;
            }
        }
        if (mappings == operationBodyMapper.getExactMatches().size() + tryMappings) {
            return true;
        }
        if (mappings == operationBodyMapper.getExactMatches().size() + tryMappings + mappingsWithTypeReplacement && mappings > mappingsWithTypeReplacement) {
            return true;
        }
        return false;
    }

    public FunctionBodyMapper findMapperWithMatchingSignature2(IFunctionDeclaration operation2) {
        for (var mapper : this.bodyMapperList) {
            if (FunctionUtil.equalNameAndParameterCount(mapper.function1, operation2)) {
                return mapper;
            }
        }
        return null;
    }


    /**
     * Similar to Rminer UMLBaseClass.getRefactoring()
     *
     * @return
     */
    public List<IRefactoring> getAllRefactorings() {
        List<IRefactoring> refactorings = new ArrayList<>(this.refactorings);

        for (FunctionBodyMapper mapper : this.bodyMapperList) {
            UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(mapper.getOperation1(), mapper.getOperation2(), mapper.getMappings());
            refactorings.addAll(operationSignatureDiff.getRefactorings());
            processMapperRefactorings(mapper, refactorings);
        }

//        refactorings.addAll(inferAttributeMergesAndSplits(renameMap, refactorings));
//        for (MergeVariableReplacement merge : mergeMap.keySet()) {
//            Set<UMLAttribute> mergedAttributes = new LinkedHashSet<UMLAttribute>();
//            Set<VariableDeclaration> mergedVariables = new LinkedHashSet<VariableDeclaration>();
//            for (String mergedVariable : merge.getMergedVariables()) {
//                UMLAttribute a1 = findAttributeInOriginalClass(mergedVariable);
//                if (a1 != null) {
//                    mergedAttributes.add(a1);
//                    mergedVariables.add(a1.getVariableDeclaration());
//                }
//            }
//            UMLAttribute a2 = findAttributeInNextClass(merge.getAfter());
//            Set<CandidateMergeVariableRefactoring> set = mergeMap.get(merge);
//            for (CandidateMergeVariableRefactoring candidate : set) {
//                if (mergedVariables.size() > 1 && mergedVariables.size() == merge.getMergedVariables().size() && a2 != null) {
//                    MergeAttributeRefactoring ref = new MergeAttributeRefactoring(mergedAttributes, a2, getOriginalClassName(), getNextClassName(), set);
//                    if (!refactorings.contains(ref)) {
//                        refactorings.add(ref);
//                        break;//it's not necessary to repeat the same process for all candidates in the set
//                    }
//                } else {
//                    candidate.setMergedAttributes(mergedAttributes);
//                    candidate.setNewAttribute(a2);
//                    candidateAttributeMerges.add(candidate);
//                }
//            }
//        }
//        for (SplitVariableReplacement split : splitMap.keySet()) {
//            Set<UMLAttribute> splitAttributes = new LinkedHashSet<UMLAttribute>();
//            Set<VariableDeclaration> splitVariables = new LinkedHashSet<VariableDeclaration>();
//            for (String splitVariable : split.getSplitVariables()) {
//                UMLAttribute a2 = findAttributeInNextClass(splitVariable);
//                if (a2 != null) {
//                    splitAttributes.add(a2);
//                    splitVariables.add(a2.getVariableDeclaration());
//                }
//            }
//            UMLAttribute a1 = findAttributeInOriginalClass(split.getBefore());
//            Set<CandidateSplitVariableRefactoring> set = splitMap.get(split);
//            for (CandidateSplitVariableRefactoring candidate : set) {
//                if (splitVariables.size() > 1 && splitVariables.size() == split.getSplitVariables().size() && a1 != null) {
//                    SplitAttributeRefactoring ref = new SplitAttributeRefactoring(a1, splitAttributes, getOriginalClassName(), getNextClassName(), set);
//                    if (!refactorings.contains(ref)) {
//                        refactorings.add(ref);
//                        break;//it's not necessary to repeat the same process for all candidates in the set
//                    }
//                } else {
//                    candidate.setSplitAttributes(splitAttributes);
//                    candidate.setOldAttribute(a1);
//                    candidateAttributeSplits.add(candidate);
//                }
//            }
//        }
//        Set<Replacement> renames = renameMap.keySet();
//        Set<Replacement> allConsistentRenames = new LinkedHashSet<Replacement>();
//        Set<Replacement> allInconsistentRenames = new LinkedHashSet<Replacement>();
//        //  Map<String, Set<String>> aliasedAttributesInOriginalClass = originalClass.aliasedAttributes();
//        // Map<String, Set<String>> aliasedAttributesInNextClass = nextClass.aliasedAttributes();
//
//        ConsistentReplacementDetector.updateRenames(allConsistentRenames, allInconsistentRenames, renames,
//                aliasedAttributesInOriginalClass, aliasedAttributesInNextClass);
//        allConsistentRenames.removeAll(allInconsistentRenames);
//        for (Replacement pattern : allConsistentRenames) {
//            UMLAttribute a1 = findAttributeInOriginalClass(pattern.getBefore());
//            UMLAttribute a2 = findAttributeInNextClass(pattern.getAfter());
//            Set<CandidateAttributeRefactoring> set = renameMap.get(pattern);
//            for (CandidateAttributeRefactoring candidate : set) {
//                if (candidate.getOriginalVariableDeclaration() == null && candidate.getRenamedVariableDeclaration() == null) {
//                    if (a1 != null && a2 != null) {
//                        if ((!originalClass.containsAttributeWithName(pattern.getAfter()) || cyclicRename(renameMap, pattern)) &&
//                                (!nextClass.containsAttributeWithName(pattern.getBefore()) || cyclicRename(renameMap, pattern)) &&
//                                !inconsistentAttributeRename(pattern, aliasedAttributesInOriginalClass, aliasedAttributesInNextClass) &&
//                                !attributeMerged(a1, a2, refactorings) && !attributeSplit(a1, a2, refactorings)) {
//                            UMLAttributeDiff attributeDiff = new UMLAttributeDiff(a1, a2, operationBodyMapperList);
//                            Set<Refactoring> attributeDiffRefactorings = attributeDiff.getRefactorings(set);
//                            if (!refactorings.containsAll(attributeDiffRefactorings)) {
//                                refactorings.addAll(attributeDiffRefactorings);
//                                break;//it's not necessary to repeat the same process for all candidates in the set
//                            }
//                        }
//                    } else {
//                        candidate.setOriginalAttribute(a1);
//                        candidate.setRenamedAttribute(a2);
//                        if (a1 != null)
//                            candidate.setOriginalVariableDeclaration(a1.getVariableDeclaration());
//                        if (a2 != null)
//                            candidate.setRenamedVariableDeclaration(a2.getVariableDeclaration());
//                        candidateAttributeRenames.add(candidate);
//                    }
//                } else if (candidate.getOriginalVariableDeclaration() != null) {
//                    if (a2 != null) {
//                        RenameVariableRefactoring ref = new RenameVariableRefactoring(
//                                candidate.getOriginalVariableDeclaration(), a2.getVariableDeclaration(),
//                                candidate.getOperationBefore(), candidate.getOperationAfter(), candidate.getAttributeReferences());
//                        if (!refactorings.contains(ref)) {
//                            refactorings.add(ref);
//                            if (!candidate.getOriginalVariableDeclaration().getType().equals(a2.getVariableDeclaration().getType()) ||
//                                    !candidate.getOriginalVariableDeclaration().getType().equalsQualified(a2.getVariableDeclaration().getType())) {
//                                ChangeVariableTypeRefactoring refactoring = new ChangeVariableTypeRefactoring(candidate.getOriginalVariableDeclaration(), a2.getVariableDeclaration(),
//                                        candidate.getOperationBefore(), candidate.getOperationAfter(), candidate.getAttributeReferences());
//                                refactoring.addRelatedRefactoring(ref);
//                                refactorings.add(refactoring);
//                            }
//                        }
//                    } else {
//                        //field is declared in a superclass or outer class
//                        candidateAttributeRenames.add(candidate);
//                    }
//                } else if (candidate.getRenamedVariableDeclaration() != null) {
//                    //inline field
//                }
//            }
//        }
        return refactorings;
    }

//    @Override
//    public int compareTo(ContainerDiff o) {
//        return this.container1.getQualifiedName().compareTo(o.container1.getQualifiedName());
//    }
}
