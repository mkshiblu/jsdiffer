package io.jsrminer.uml;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.CandidateAttributeRefactoring;
import io.jsrminer.refactorings.CandidateMergeVariableRefactoring;
import io.jsrminer.refactorings.CandidateSplitVariableRefactoring;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.*;

import java.util.*;

public class MapperRefactoringProcessor {
//    private final Map<Replacement, Set<CandidateAttributeRefactoring>> renameMap;
//    private final Map<MergeVariableReplacement, Set<CandidateMergeVariableRefactoring>> mergeMap;
//    private final Map<SplitVariableReplacement, Set<CandidateSplitVariableRefactoring>> splitMap;
//
//    public MapperRefactoringProcessor(Map<Replacement, Set<CandidateAttributeRefactoring>> renameMap
//            , Map<MergeVariableReplacement, Set<CandidateMergeVariableRefactoring>> mergeMap,
//                                      Map<SplitVariableReplacement, Set<CandidateSplitVariableRefactoring>> splitMap) {
//        this.renameMap = renameMap;
//        this.mergeMap = mergeMap;
//        this.splitMap = splitMap;
//    }

    public void processMapperRefactorings(FunctionBodyMapper mapper, List<IRefactoring> refactorings
            , Map<Replacement, Set<CandidateAttributeRefactoring>> renameMap
            , Map<MergeVariableReplacement, Set<CandidateMergeVariableRefactoring>> mergeMap
            , Map<SplitVariableReplacement, Set<CandidateSplitVariableRefactoring>> splitMap) {
        for (IRefactoring refactoring : mapper.getRefactoringsByVariableAnalysis()) {
            if (refactorings.contains(refactoring)) {
                //special handling for replacing rename variable refactorings having statement mapping information
                int index = refactorings.indexOf(refactoring);
                refactorings.remove(index);
                refactorings.add(index, refactoring);
            } else {
                refactorings.add(refactoring);
            }
        }
        for (CandidateAttributeRefactoring candidate : mapper.getCandidateAttributeRenames()) {
            if (!multipleExtractedMethodInvocationsWithDifferentAttributesAsArguments(candidate, refactorings)) {
                String before = PrefixSuffixUtils.normalize(candidate.getOriginalVariableName());
                String after = PrefixSuffixUtils.normalize(candidate.getRenamedVariableName());
                if (before.contains(".") && after.contains(".")) {
                    String prefix1 = before.substring(0, before.lastIndexOf(".") + 1);
                    String prefix2 = after.substring(0, after.lastIndexOf(".") + 1);
                    if (prefix1.equals(prefix2)) {
                        before = before.substring(prefix1.length(), before.length());
                        after = after.substring(prefix2.length(), after.length());
                    }
                }
                Replacement renamePattern = new Replacement(before, after, ReplacementType.VARIABLE_NAME);
                if (renameMap.containsKey(renamePattern)) {
                    renameMap.get(renamePattern).add(candidate);
                } else {
                    Set<CandidateAttributeRefactoring> set = new LinkedHashSet<>();
                    set.add(candidate);
                    renameMap.put(renamePattern, set);
                }
            }
        }
        for (CandidateMergeVariableRefactoring candidate : mapper.getCandidateAttributeMerges()) {
            Set<String> before = new LinkedHashSet<String>();
            for (String mergedVariable : candidate.getMergedVariables()) {
                before.add(PrefixSuffixUtils.normalize(mergedVariable));
            }
            String after = PrefixSuffixUtils.normalize(candidate.getNewVariable());
            MergeVariableReplacement merge = new MergeVariableReplacement(before, after);
            processMerge(mergeMap, merge, candidate);
        }

        for (CandidateSplitVariableRefactoring candidate : mapper.getCandidateAttributeSplits()) {
            Set<String> after = new LinkedHashSet<String>();
            for (String splitVariable : candidate.getSplitVariables()) {
                after.add(PrefixSuffixUtils.normalize(splitVariable));
            }
            String before = PrefixSuffixUtils.normalize(candidate.getOldVariable());
            SplitVariableReplacement split = new SplitVariableReplacement(before, after);
            processSplit(splitMap, split, candidate);
        }
    }

    private void processMerge(Map<MergeVariableReplacement, Set<CandidateMergeVariableRefactoring>> mergeMap,
                              MergeVariableReplacement newMerge, CandidateMergeVariableRefactoring candidate) {
        MergeVariableReplacement mergeToBeRemoved = null;
        for (MergeVariableReplacement merge : mergeMap.keySet()) {
            if (merge.subsumes(newMerge)) {
                mergeMap.get(merge).add(candidate);
                return;
            } else if (merge.equal(newMerge)) {
                mergeMap.get(merge).add(candidate);
                return;
            } else if (merge.commonAfter(newMerge)) {
                mergeToBeRemoved = merge;
                Set<String> mergedVariables = new LinkedHashSet<String>();
                mergedVariables.addAll(merge.getMergedVariables());
                mergedVariables.addAll(newMerge.getMergedVariables());
                MergeVariableReplacement replacement = new MergeVariableReplacement(mergedVariables, merge.getAfter());
                Set<CandidateMergeVariableRefactoring> candidates = mergeMap.get(mergeToBeRemoved);
                candidates.add(candidate);
                mergeMap.put(replacement, candidates);
                break;
            } else if (newMerge.subsumes(merge)) {
                mergeToBeRemoved = merge;
                Set<CandidateMergeVariableRefactoring> candidates = mergeMap.get(mergeToBeRemoved);
                candidates.add(candidate);
                mergeMap.put(newMerge, candidates);
                break;
            }
        }
        if (mergeToBeRemoved != null) {
            mergeMap.remove(mergeToBeRemoved);
            return;
        }
        Set<CandidateMergeVariableRefactoring> set = new LinkedHashSet<CandidateMergeVariableRefactoring>();
        set.add(candidate);
        mergeMap.put(newMerge, set);
    }

    private void processSplit(Map<SplitVariableReplacement, Set<CandidateSplitVariableRefactoring>> splitMap,
                              SplitVariableReplacement newSplit, CandidateSplitVariableRefactoring candidate) {
        SplitVariableReplacement splitToBeRemoved = null;
        for (SplitVariableReplacement split : splitMap.keySet()) {
            if (split.subsumes(newSplit)) {
                splitMap.get(split).add(candidate);
                return;
            } else if (split.equal(newSplit)) {
                splitMap.get(split).add(candidate);
                return;
            } else if (split.commonBefore(newSplit)) {
                splitToBeRemoved = split;
                Set<String> splitVariables = new LinkedHashSet<String>();
                splitVariables.addAll(split.getSplitVariables());
                splitVariables.addAll(newSplit.getSplitVariables());
                SplitVariableReplacement replacement = new SplitVariableReplacement(split.getBefore(), splitVariables);
                Set<CandidateSplitVariableRefactoring> candidates = splitMap.get(splitToBeRemoved);
                candidates.add(candidate);
                splitMap.put(replacement, candidates);
                break;
            } else if (newSplit.subsumes(split)) {
                splitToBeRemoved = split;
                Set<CandidateSplitVariableRefactoring> candidates = splitMap.get(splitToBeRemoved);
                candidates.add(candidate);
                splitMap.put(newSplit, candidates);
                break;
            }
        }
        if (splitToBeRemoved != null) {
            splitMap.remove(splitToBeRemoved);
            return;
        }
        Set<CandidateSplitVariableRefactoring> set = new LinkedHashSet<CandidateSplitVariableRefactoring>();
        set.add(candidate);
        splitMap.put(newSplit, set);
    }

    private boolean multipleExtractedMethodInvocationsWithDifferentAttributesAsArguments
            (CandidateAttributeRefactoring candidate, List<IRefactoring> refactorings) {
//        for (IRefactoring refactoring : refactorings) {
//            if (refactoring instanceof ExtractOperationRefactoring) {
//                ExtractOperationRefactoring extractRefactoring = (ExtractOperationRefactoring) refactoring;
//                if (extractRefactoring.getExtractedOperation().equals(candidate.getOperationAfter())) {
//                    List<OperationInvocation> extractedInvocations = extractRefactoring.getExtractedOperationInvocations();
//                    if (extractedInvocations.size() > 1) {
//                        Set<VariableDeclaration> attributesMatchedWithArguments = new LinkedHashSet<VariableDeclaration>();
//                        Set<String> attributeNamesMatchedWithArguments = new LinkedHashSet<String>();
//                        for (OperationInvocation extractedInvocation : extractedInvocations) {
//                            for (String argument : extractedInvocation.getArguments()) {
//                                for (UMLAttribute attribute : originalClass.getAttributes()) {
//                                    if (attribute.getName().equals(argument)) {
//                                        attributesMatchedWithArguments.add(attribute.getVariableDeclaration());
//                                        attributeNamesMatchedWithArguments.add(attribute.getName());
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                        if ((attributeNamesMatchedWithArguments.contains(candidate.getOriginalVariableName()) ||
//                                attributeNamesMatchedWithArguments.contains(candidate.getRenamedVariableName())) &&
//                                attributesMatchedWithArguments.size() > 1) {
//                            return true;
//                        }
//                    }
//                }
//            }
//        }
        return false;
    }
}
