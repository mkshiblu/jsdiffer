package io.jsrminer.uml.diff;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.api.RefactoringMinerTimedOutException;
import io.jsrminer.refactorings.*;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLModel;
import io.jsrminer.uml.mapping.CodeFragmentMapping;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.FunctionUtil;
import io.jsrminer.uml.mapping.LeafCodeFragmentMapping;
import io.jsrminer.uml.mapping.replacement.MergeVariableReplacement;
import io.jsrminer.uml.mapping.replacement.Replacement;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;

import java.util.*;
import java.util.stream.Collectors;

public class UMLModelDiff extends Diff {
    private List<IRefactoring> refactorings = new ArrayList<>();

    public final UMLModel model1;
    public final UMLModel model2;

    private final List<ISourceFile> addedFiles = new ArrayList<>();
    private final List<ISourceFile> removedFiles = new ArrayList<>();

    private final List<ContainerDiff<ISourceFile>> commonFilesDiffList = new ArrayList<>();
    private final List<SourceFileMoveDiff> fileMoveDiffList = new ArrayList<>();
    private final List<SourceFileRenameDiff> fileRenameDiffList = new ArrayList<>();

    private List<ClassDiff> commonClassDiffList = new ArrayList<>();
    private final List<UMLClassMoveDiff> classMoveDiffList = new ArrayList<>();
    private final List<UMLClassRenameDiff> classRenameDiffList = new ArrayList<>();
    private final List<UMLClassMoveDiff> innerClassMoveDiffList = new ArrayList<>();

    public UMLModelDiff(UMLModel model1, UMLModel model2) {
        this.model1 = model1;
        this.model2 = model2;
    }

    public void addRefactoring(IRefactoring ref) {
        refactorings.add(ref);
    }

    public void reportAddedFile(ISourceFile sourceFile) {
        this.addedFiles.add(sourceFile);
    }

    public void reportRemovedFile(ISourceFile sourceFile) {
        this.removedFiles.add(sourceFile);
    }

    public Set<IRefactoring> getClassRefactorings(List<RenameFileRefactoring> renamePackageRefactorings) {
        Set<IRefactoring> refactorings = new LinkedHashSet<>();
        refactorings.addAll(getMoveClassRefactorings());
        refactorings.addAll(getRenameClassRefactorings(renamePackageRefactorings));

        for (var classDiff : commonClassDiffList) {
            refactorings.addAll(classDiff.getAllRefactorings());
        }

        return refactorings;
    }

    public List<IRefactoring> getRefactorings() throws RefactoringMinerTimedOutException {
        Set<IRefactoring> refactorings = new LinkedHashSet<>();

        refactorings.addAll(getMoveFileRefactorings());
        refactorings.addAll(getRenameFileRefactorings());

        List<RenameFileRefactoring> renamePackageRefactorings = new ArrayList<RenameFileRefactoring>();
        for (var r : refactorings) {
            if (r instanceof RenameFileRefactoring) {
                renamePackageRefactorings.add((RenameFileRefactoring) r);
            }
        }

        refactorings.addAll(getClassRefactorings(renamePackageRefactorings));

        //refactorings.addAll(identifyConvertAnonymousClassToTypeRefactorings());
        Map<Replacement, Set<CandidateAttributeRefactoring>> renameMap = new LinkedHashMap<>();
        Map<MergeVariableReplacement, Set<CandidateMergeVariableRefactoring>> mergeMap
                = new LinkedHashMap<>();
        for (var fileDiff : commonFilesDiffList) {
            refactorings.addAll(fileDiff.getAllRefactorings());
            //extractMergePatterns(fileDiff, mergeMap);
            //extractRenamePatterns(fileDiff, renameMap);
        }
        for (var fileDiff : fileMoveDiffList) {
            refactorings.addAll(fileDiff.getAllRefactorings());
//            extractMergePatterns(classDiff, mergeMap);
//            extractRenamePatterns(classDiff, renameMap);
        }

//        for (UMLClassMoveDiff classDiff : innerClassMoveDiffList) {
//            refactorings.addAll(classDiff.getRefactorings());
//            extractMergePatterns(classDiff, mergeMap);
//            extractRenamePatterns(classDiff, renameMap);
//        }

        for (var fileDiff : fileRenameDiffList) {
            refactorings.addAll(fileDiff.getAllRefactorings());
//            extractMergePatterns(fileDiff, mergeMap);
//            extractRenamePatterns(fileDiff, renameMap);
        }

        //Map<RenamePattern, Integer> typeRenamePatternMap = typeRenamePatternMap(refactorings);
//        for (RenamePattern pattern : typeRenamePatternMap.keySet()) {
//            if (typeRenamePatternMap.get(pattern) > 1) {
//                UMLClass removedClass = looksLikeRemovedClass(UMLType.extractTypeObject(pattern.getBefore()));
//                UMLClass addedClass = looksLikeAddedClass(UMLType.extractTypeObject(pattern.getAfter()));
//                if (removedClass != null && addedClass != null) {
//                    UMLClassRenameDiff renameDiff = new UMLClassRenameDiff(removedClass, addedClass, this);
//                    renameDiff.process();
//                    refactorings.addAll(renameDiff.getRefactorings());
//                    extractMergePatterns(renameDiff, mergeMap);
//                    extractRenamePatterns(renameDiff, renameMap);
//                    classRenameDiffList.add(renameDiff);
//                    Refactoring refactoring = null;
//                    if (renameDiff.samePackage())
//                        refactoring = new RenameClassRefactoring(renameDiff.getOriginalClass(), renameDiff.getRenamedClass());
//                    else
//                        refactoring = new MoveAndRenameClassRefactoring(renameDiff.getOriginalClass(), renameDiff.getRenamedClass());
//                    refactorings.add(refactoring);
//                }
//            }
//        }
//        for (MergeVariableReplacement merge : mergeMap.keySet()) {
//            UMLClassBaseDiff diff = null;
//            for (String mergedVariable : merge.getMergedVariables()) {
//                Replacement replacement = new Replacement(mergedVariable, merge.getAfter(), ReplacementType.VARIABLE_NAME);
//                diff = getUMLClassDiffWithAttribute(replacement);
//            }
//            if (diff != null) {
//                Set<UMLAttribute> mergedAttributes = new LinkedHashSet<UMLAttribute>();
//                Set<VariableDeclaration> mergedVariables = new LinkedHashSet<VariableDeclaration>();
//                for (String mergedVariable : merge.getMergedVariables()) {
//                    UMLAttribute a1 = diff.findAttributeInOriginalClass(mergedVariable);
//                    if (a1 != null) {
//                        mergedAttributes.add(a1);
//                        mergedVariables.add(a1.getVariableDeclaration());
//                    }
//                }
//                UMLAttribute a2 = diff.findAttributeInNextClass(merge.getAfter());
//                Set<CandidateMergeVariableRefactoring> set = mergeMap.get(merge);
//                if (mergedVariables.size() > 1 && mergedVariables.size() == merge.getMergedVariables().size() && a2 != null) {
//                    MergeAttributeRefactoring ref = new MergeAttributeRefactoring(mergedAttributes, a2, diff.getOriginalClassName(), diff.getNextClassName(), set);
//                    if (!refactorings.contains(ref)) {
//                        refactorings.add(ref);
//                        Refactoring conflictingRefactoring = attributeRenamed(mergedVariables, a2.getVariableDeclaration(), refactorings);
//                        if (conflictingRefactoring != null) {
//                            refactorings.remove(conflictingRefactoring);
//                        }
//                    }
//                }
//            }
//        }
//        for (Replacement pattern : renameMap.keySet()) {
//            UMLClassBaseDiff diff = getUMLClassDiffWithAttribute(pattern);
//            Set<CandidateAttributeRefactoring> set = renameMap.get(pattern);
//            for (CandidateAttributeRefactoring candidate : set) {
//                if (candidate.getOriginalVariableDeclaration() == null && candidate.getRenamedVariableDeclaration() == null) {
//                    if (diff != null) {
//                        UMLAttribute a1 = diff.findAttributeInOriginalClass(pattern.getBefore());
//                        UMLAttribute a2 = diff.findAttributeInNextClass(pattern.getAfter());
//                        if (!diff.getOriginalClass().containsAttributeWithName(pattern.getAfter()) &&
//                                !diff.getNextClass().containsAttributeWithName(pattern.getBefore()) &&
//                                !attributeMerged(a1, a2, refactorings)) {
//                            UMLAttributeDiff attributeDiff = new UMLAttributeDiff(a1, a2, diff.getOperationBodyMapperList());
//                            Set<Refactoring> attributeDiffRefactorings = attributeDiff.getRefactorings(set);
//                            if (!refactorings.containsAll(attributeDiffRefactorings)) {
//                                refactorings.addAll(attributeDiffRefactorings);
//                                break;//it's not necessary to repeat the same process for all candidates in the set
//                            }
//                        }
//                    }
//                } else if (candidate.getOriginalVariableDeclaration() != null) {
//                    List<UMLClassBaseDiff> diffs1 = getUMLClassDiffWithExistingAttributeAfter(pattern);
//                    List<UMLClassBaseDiff> diffs2 = getUMLClassDiffWithNewAttributeAfter(pattern);
//                    if (!diffs1.isEmpty()) {
//                        UMLClassBaseDiff diff1 = diffs1.get(0);
//                        UMLClassBaseDiff originalClassDiff = null;
//                        if (candidate.getOriginalAttribute() != null) {
//                            originalClassDiff = getUMLClassDiff(candidate.getOriginalAttribute().getClassName());
//                        } else {
//                            originalClassDiff = getUMLClassDiff(candidate.getOperationBefore().getClassName());
//                        }
//                        if (diffs1.size() > 1) {
//                            for (UMLClassBaseDiff classDiff : diffs1) {
//                                if (isSubclassOf(originalClassDiff.nextClass.getName(), classDiff.nextClass.getName())) {
//                                    diff1 = classDiff;
//                                    break;
//                                }
//                            }
//                        }
//                        UMLAttribute a2 = diff1.findAttributeInNextClass(pattern.getAfter());
//                        if (a2 != null) {
//                            if (candidate.getOriginalVariableDeclaration().isAttribute()) {
//                                if (originalClassDiff != null && originalClassDiff.removedAttributes.contains(candidate.getOriginalAttribute())) {
//                                    ReplaceAttributeRefactoring ref = new ReplaceAttributeRefactoring(candidate.getOriginalAttribute(), a2, set);
//                                    if (!refactorings.contains(ref)) {
//                                        refactorings.add(ref);
//                                        break;//it's not necessary to repeat the same process for all candidates in the set
//                                    }
//                                }
//                            } else {
//                                RenameVariableRefactoring ref = new RenameVariableRefactoring(candidate.getOriginalVariableDeclaration(), a2.getVariableDeclaration(), candidate.getOperationBefore(), candidate.getOperationAfter(), candidate.getAttributeReferences());
//                                if (!refactorings.contains(ref)) {
//                                    refactorings.add(ref);
//                                    break;//it's not necessary to repeat the same process for all candidates in the set
//                                }
//                            }
//                        }
//                    } else if (!diffs2.isEmpty()) {
//                        UMLClassBaseDiff diff2 = diffs2.get(0);
//                        UMLClassBaseDiff originalClassDiff = null;
//                        if (candidate.getOriginalAttribute() != null) {
//                            originalClassDiff = getUMLClassDiff(candidate.getOriginalAttribute().getClassName());
//                        } else {
//                            originalClassDiff = getUMLClassDiff(candidate.getOperationBefore().getClassName());
//                        }
//                        if (diffs2.size() > 1) {
//                            for (UMLClassBaseDiff classDiff : diffs2) {
//                                if (isSubclassOf(originalClassDiff.nextClass.getName(), classDiff.nextClass.getName())) {
//                                    diff2 = classDiff;
//                                    break;
//                                }
//                            }
//                        }
//                        UMLAttribute a2 = diff2.findAttributeInNextClass(pattern.getAfter());
//                        if (a2 != null) {
//                            if (candidate.getOriginalVariableDeclaration().isAttribute()) {
//                                if (originalClassDiff != null && originalClassDiff.removedAttributes.contains(candidate.getOriginalAttribute())) {
//                                    MoveAndRenameAttributeRefactoring ref = new MoveAndRenameAttributeRefactoring(candidate.getOriginalAttribute(), a2, set);
//                                    if (!refactorings.contains(ref)) {
//                                        refactorings.add(ref);
//                                        break;//it's not necessary to repeat the same process for all candidates in the set
//                                    }
//                                }
//                            } else {
//                                RenameVariableRefactoring ref = new RenameVariableRefactoring(candidate.getOriginalVariableDeclaration(), a2.getVariableDeclaration(), candidate.getOperationBefore(), candidate.getOperationAfter(), candidate.getAttributeReferences());
//                                if (!refactorings.contains(ref)) {
//                                    refactorings.add(ref);
//                                    break;//it's not necessary to repeat the same process for all candidates in the set
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
        //refactorings.addAll(identifyExtractSuperclassRefactorings());
        //refactorings.addAll(identifyExtractClassRefactorings(commonClassDiffList));
        //refactorings.addAll(identifyExtractClassRefactorings(classMoveDiffList));
        // refactorings.addAll(identifyExtractClassRefactorings(innerClassMoveDiffList));
        //refactorings.addAll(identifyExtractClassRefactorings(classRenameDiffList));
        checkForOperationMovesBetweenCommonFiles();// Only the removed and common classes that are already matchc (has diff)
        checkForOperationMovesIncludingAddedFiles();  // If a method has
        checkForOperationMovesIncludingRemovedFiles(); // If a method has been moved from a deleted file
//        checkForExtractedAndMovedOperations(getOperationBodyMappersInCommonClasses(), getAddedAndExtractedOperationsInCommonClasses());
//        checkForExtractedAndMovedOperations(getOperationBodyMappersInMovedAndRenamedClasses(), getAddedOperationsInMovedAndRenamedClasses());
//        checkForMovedAndInlinedOperations(getOperationBodyMappersInCommonClasses(), getRemovedAndInlinedOperationsInCommonClasses());
//        refactorings.addAll(checkForAttributeMovesBetweenCommonClasses());
//        refactorings.addAll(checkForAttributeMovesIncludingAddedClasses());
//        refactorings.addAll(checkForAttributeMovesIncludingRemovedClasses());

        refactorings.addAll(this.refactorings);

//        for (UMLClassDiff classDiff : commonClassDiffList) {
//            inferMethodSignatureRelatedRefactorings(classDiff, refactorings);
//        }
//        for (UMLClassMoveDiff classDiff : classMoveDiffList) {
//            inferMethodSignatureRelatedRefactorings(classDiff, refactorings);
//        }
//        for (UMLClassMoveDiff classDiff : innerClassMoveDiffList) {
//            inferMethodSignatureRelatedRefactorings(classDiff, refactorings);
//        }
//        for (UMLClassRenameDiff classDiff : classRenameDiffList) {
//            inferMethodSignatureRelatedRefactorings(classDiff, refactorings);
//        }
//        return filterOutDuplicateRefactorings(refactorings);
        return refactorings.stream().collect(Collectors.toList());
    }

    private List<Refactoring> getMoveFileRefactorings() {
        List<Refactoring> refactorings = new ArrayList<>();
        for (var fileMoveDiff : fileMoveDiffList) {
            var originalFile = fileMoveDiff.getOriginalFile();
            String originalName = originalFile.getName();
            var movedFile = fileMoveDiff.getMovedFile();
            String originalPathPrefix = originalFile.getDirectoryPath();
            String movedPathPrefix = movedFile.getDirectoryPath();
            if (originalPathPrefix != null && !originalPathPrefix.equals(movedPathPrefix)) {
                var refactoring = new MoveFileRefactoring(originalFile, movedFile);
                refactorings.add(refactoring);
            }
        }
        return refactorings;
    }

    private List<Refactoring> getMoveClassRefactorings() {
        List<Refactoring> refactorings = new ArrayList<Refactoring>();
        List<MoveSourceFolderRefactoring> moveSourceFolderRefactoringRefactorings = new ArrayList<>();
        for (UMLClassMoveDiff classMoveDiff : classMoveDiffList) {
            var originalClass = classMoveDiff.getOriginalClass();
            String originalName = originalClass.getQualifiedName();
            var movedClass = classMoveDiff.getMovedClass();
            String movedName = movedClass.getQualifiedName();

            String originalPath = originalClass.getSourceLocation().getFilePath();
            String movedPath = movedClass.getSourceLocation().getFilePath();
            String originalPathPrefix = "";
            if (originalPath.contains("/")) {
                originalPathPrefix = originalPath.substring(0, originalPath.lastIndexOf('/'));
            }
            String movedPathPrefix = "";
            if (movedPath.contains("/")) {
                movedPathPrefix = movedPath.substring(0, movedPath.lastIndexOf('/'));
            }

            boolean isFilenameDifferent = !originalPath.equals(movedPath);
            if (!originalName.equals(movedName)
                    || isFilenameDifferent) {
                MoveClassRefactoring refactoring = new MoveClassRefactoring(originalClass, movedClass);
                RenamePattern renamePattern = refactoring.getRenamePattern();
                //check if the the original path is a substring of the moved path and vice versa
                if (renamePattern.getBefore().contains(renamePattern.getAfter()) ||
                        renamePattern.getAfter().contains(renamePattern.getBefore()) ||
                        !originalClass.isTopLevel() || !movedClass.isTopLevel()) {
                    refactorings.add(refactoring);
                }
            } else if (!originalPathPrefix.equals(movedPathPrefix)) {
                MovedClassToAnotherSourceFolder refactoring = new MovedClassToAnotherSourceFolder(originalClass, movedClass, originalPathPrefix, movedPathPrefix);
                RenamePattern renamePattern = refactoring.getRenamePattern();
                boolean foundInMatchingMoveSourceFolderRefactoring = false;
                for (MoveSourceFolderRefactoring moveSourceFolderRefactoring : moveSourceFolderRefactoringRefactorings) {
                    if (moveSourceFolderRefactoring.getPattern().equals(renamePattern)) {
                        moveSourceFolderRefactoring.addMovedClassToAnotherSourceFolder(refactoring);
                        foundInMatchingMoveSourceFolderRefactoring = true;
                        break;
                    }
                }
                if (!foundInMatchingMoveSourceFolderRefactoring) {
                    moveSourceFolderRefactoringRefactorings.add(new MoveSourceFolderRefactoring(refactoring));
                }
            }
        }
        refactorings.addAll(moveSourceFolderRefactoringRefactorings);
        return refactorings;
    }

    private List<Refactoring> getRenameClassRefactorings(List<RenameFileRefactoring> previousRenamePackageRefactorings) {
        List<Refactoring> refactorings = new ArrayList<Refactoring>();
        List<RenameFileRefactoring> newRenamePackageRefactorings = new ArrayList<>();
        for (UMLClassRenameDiff classRenameDiff : classRenameDiffList) {
            if (classRenameDiff.isInSameFile()) {
                RenameClassRefactoring refactoring = new RenameClassRefactoring(classRenameDiff.getOriginalClass(), classRenameDiff.getRenamedClass());
                refactorings.add(refactoring);
            } else {
                MoveAndRenameClassRefactoring refactoring = new MoveAndRenameClassRefactoring(classRenameDiff.getOriginalClass(), classRenameDiff.getRenamedClass());
                RenamePattern renamePattern = refactoring.getRenamePattern();
                boolean foundInMatchingRenamePackageRefactoring = false;
//                //search first in RenamePackage refactorings established from Move Class refactorings
//                for (RenameFileRefactoring renamePackageRefactoring : previousRenamePackageRefactorings) {
//                    if (renamePackageRefactoring.getPattern().equals(renamePattern)) {
//                        renamePackageRefactoring.addMoveClassRefactoring(refactoring);
//                        foundInMatchingRenamePackageRefactoring = true;
//                        break;
//                    }
//                }
//                for (var renamePackageRefactoring : newRenamePackageRefactorings) {
//                    if (renamePackageRefactoring.getPattern().equals(renamePattern)) {
//                        renamePackageRefactoring.addMoveClassRefactoring(refactoring);
//                        foundInMatchingRenamePackageRefactoring = true;
//                        break;
//                    }
//                }
//                if (!foundInMatchingRenamePackageRefactoring) {
//                    newRenamePackageRefactorings.add(new RenamePackageRefactoring(refactoring));
//                }
                refactorings.add(refactoring);
            }
        }
//        for (var renamePackageRefactoring : newRenamePackageRefactorings) {
//            List<PackageLevelRefactoring> moveClassRefactorings = renamePackageRefactoring.getMoveClassRefactorings();
//            if (moveClassRefactorings.size() >= 1 && isSourcePackageDeleted(renamePackageRefactoring)) {
//                refactorings.add(renamePackageRefactoring);
//                previousRenamePackageRefactorings.add(renamePackageRefactoring);
//            }
//        }
        return refactorings;
    }

    private List<Refactoring> getRenameFileRefactorings() {
        List<Refactoring> refactorings = new ArrayList<>();
        for (var classRenameDiff : getFileRenameDiffList()) {
            Refactoring refactoring = null;
            if (classRenameDiff.getRenamedFile().getDirectoryPath().equals(classRenameDiff.getOriginalFile().getDirectoryPath()))
                refactoring = new RenameFileRefactoring(classRenameDiff.getOriginalFile(), classRenameDiff.getRenamedFile());
            else
                refactoring = new MoveAndRenameFileRefactoring(classRenameDiff.getOriginalFile(), classRenameDiff.getRenamedFile());
            refactorings.add(refactoring);
        }
        return refactorings;
    }

//    private boolean isSourcePackageDeleted(RenamePackageRefactoring renamePackageRefactoring) {
//        for (String deletedFolderPath : deletedFolderPaths) {
//            String originalPath = renamePackageRefactoring.getPattern().getBefore();
//            //remove last .
//            String trimmedOriginalPath = originalPath.endsWith(".") ? originalPath.substring(0, originalPath.length() - 1) : originalPath;
//            String convertedPackageToFilePath = trimmedOriginalPath.replaceAll("\\.", "/");
//            if (deletedFolderPath.endsWith(convertedPackageToFilePath)) {
//                return true;
//            }
//        }
//        return false;
//    }


    private void checkForOperationMovesIncludingAddedFiles() throws RefactoringMinerTimedOutException {
        List<FunctionDeclaration> addedOperations = getAddedOperationsInCommonClasses();
        for (ISourceFile addedClass : this.addedFiles) {
            addedOperations.addAll(addedClass.getFunctionDeclarations().stream().map(x -> (FunctionDeclaration) x).collect(Collectors.toList()));
        }
        List<FunctionDeclaration> removedOperations = getRemovedOperationsInCommonClasses();
      /*for(UMLClass removedClass : removedClasses) {
    	  removedOperations.addAll(removedClass.getOperations());
      }*/
        if (removedOperations.size() <= JsConfig.MAXIMUM_NUMBER_OF_COMPARED_METHODS
                || addedOperations.size() <= JsConfig.MAXIMUM_NUMBER_OF_COMPARED_METHODS) {
            checkForOperationMoves(addedOperations, removedOperations);
        }
    }

    public List<ContainerDiff<ISourceFile>> getCommonFilesDiffList() {
        return commonFilesDiffList;
    }

    private void checkForOperationMovesBetweenCommonFiles()
            throws RefactoringMinerTimedOutException {
        List<FunctionDeclaration> addedOperations = getAddedAndExtractedOperationsInCommonClasses();
        List<FunctionDeclaration> removedOperations = getRemovedOperationsInCommonMovedRenamedClasses();
        if (removedOperations.size() <= JsConfig.MAXIMUM_NUMBER_OF_COMPARED_METHODS
                || addedOperations.size() <= JsConfig.MAXIMUM_NUMBER_OF_COMPARED_METHODS) {
            checkForOperationMoves(addedOperations, removedOperations);
        }
    }

    private void checkForOperationMoves(List<FunctionDeclaration> addedOperations
            , List<FunctionDeclaration> removedOperations) throws RefactoringMinerTimedOutException {
        if (addedOperations.size() <= removedOperations.size()) {
            for (Iterator<FunctionDeclaration> addedOperationIterator = addedOperations.iterator(); addedOperationIterator.hasNext(); ) {
                FunctionDeclaration addedOperation = addedOperationIterator.next();
                TreeMap<Integer, List<FunctionBodyMapper>> operationBodyMapperMap = new TreeMap<>();
                for (Iterator<FunctionDeclaration> removedOperationIterator = removedOperations.iterator(); removedOperationIterator.hasNext(); ) {
                    FunctionDeclaration removedOperation = removedOperationIterator.next();
                    FunctionBodyMapper operationBodyMapper = new FunctionBodyMapper(removedOperation, addedOperation, null);

                    int mappings = operationBodyMapper.mappingsWithoutBlocks();
                    if (mappings > 0 && mappedElementsMoreThanNonMappedT1AndT2(mappings, operationBodyMapper)) {
                        int exactMatches = operationBodyMapper.getExactMatches().size();
                        if (operationBodyMapperMap.containsKey(exactMatches)) {
                            List<FunctionBodyMapper> mapperList = operationBodyMapperMap.get(exactMatches);
                            mapperList.add(operationBodyMapper);
                        } else {
                            List<FunctionBodyMapper> mapperList = new ArrayList<>();
                            mapperList.add(operationBodyMapper);
                            operationBodyMapperMap.put(exactMatches, mapperList);
                        }
                    }
                }
                if (!operationBodyMapperMap.isEmpty()) {
                    List<FunctionBodyMapper> firstMappers = firstMappers(operationBodyMapperMap);
                    Collections.sort(firstMappers, new UMLOperationBodyMapperComparator());
                    addedOperationIterator.remove();
                    boolean sameSourceAndTargetClass = sameSourceAndTargetClass(firstMappers);
                    if (sameSourceAndTargetClass) {
                        TreeSet<FunctionBodyMapper> set = null;
                        if (allRenamedOperations(firstMappers)) {
                            set = new TreeSet<>();
                        } else {
                            set = new TreeSet<>(new UMLOperationBodyMapperComparator());
                        }
                        set.addAll(firstMappers);
                        FunctionBodyMapper bestMapper = set.first();
                        firstMappers.clear();
                        firstMappers.add(bestMapper);
                    }
                    for (FunctionBodyMapper firstMapper : firstMappers) {
                        FunctionDeclaration removedOperation = firstMapper.getOperation1();
                        if (sameSourceAndTargetClass) {
                            removedOperations.remove(removedOperation);
                        }

                        Refactoring refactoring = null;
                        boolean isTopLevel = removedOperation.isTopLevel() && addedOperation.isTopLevel();
                        if (!isTopLevel && removedOperation.getParentContainerQualifiedName()
                                .equals(addedOperation.getParentContainerQualifiedName())) {
                            if (FunctionUtil.equalParameterNames(addedOperation, removedOperation)) {
                                //refactoring = new RenameOperationRefactoring(removedOperation, addedOperation);
                            } else {
                                // Methods in the same class with similar body but different signature
                            }
                        } /*else if (removedOperation.isConstructor() == addedOperation.isConstructor() &&
                                isSubclassOf(removedOperation.getClassName(), addedOperation.getClassName()) && addedOperation.compatibleSignature(removedOperation)) {
                            refactoring = new PullUpOperationRefactoring(firstMapper);
                        } else if (removedOperation.isConstructor() == addedOperation.isConstructor() &&
                                isSubclassOf(addedOperation.getClassName(), removedOperation.getClassName()) && addedOperation.compatibleSignature(removedOperation)) {
                            refactoring = new PushDownOperationRefactoring(firstMapper);
                        } */ else if (removedOperation.isConstructor() == addedOperation.isConstructor() &&
                                movedMethodSignature(removedOperation, addedOperation)
                                && !refactoringListContainsAnotherMoveRefactoringWithTheSameOperations(removedOperation, addedOperation)
                        ) {
                            refactoring = new MoveOperationRefactoring(firstMapper);
                        } else if (removedOperation.isConstructor() == addedOperation.isConstructor() &&
                                movedAndRenamedMethodSignature(removedOperation, addedOperation, firstMapper)
                                && !refactoringListContainsAnotherMoveRefactoringWithTheSameOperations(removedOperation, addedOperation)) {
                            refactoring = new MoveOperationRefactoring(firstMapper);
                        }
                        if (refactoring != null) {
                            deleteRemovedOperation(removedOperation);
                            deleteAddedOperation(addedOperation);
                            UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, firstMapper.getMappings());
                            refactorings.addAll(operationSignatureDiff.getRefactorings());
                            refactorings.add(refactoring);
//                            ISourceFile addedClass = getAddedClass(addedOperation.getSourceLocation().getFile());
//                            if (addedClass != null) {
//                                checkForExtractedOperationsWithinMovedMethod(firstMapper, addedClass);
//                            }
                        }

                    }
                }
            }
        } else {
            for (Iterator<FunctionDeclaration> removedOperationIterator = removedOperations.iterator(); removedOperationIterator.hasNext(); ) {
                FunctionDeclaration removedOperation = removedOperationIterator.next();
                TreeMap<Integer, List<FunctionBodyMapper>> operationBodyMapperMap
                        = new TreeMap<Integer, List<FunctionBodyMapper>>();
                for (Iterator<FunctionDeclaration> addedOperationIterator = addedOperations.iterator(); addedOperationIterator.hasNext(); ) {
                    FunctionDeclaration addedOperation = addedOperationIterator.next();

                    FunctionBodyMapper operationBodyMapper = new FunctionBodyMapper(removedOperation, addedOperation, null);
                    int mappings = operationBodyMapper.mappingsWithoutBlocks();
                    if (mappings > 0 && mappedElementsMoreThanNonMappedT1AndT2(mappings, operationBodyMapper)) {
                        int exactMatches = operationBodyMapper.getExactMatches().size();
                        if (operationBodyMapperMap.containsKey(exactMatches)) {
                            List<FunctionBodyMapper> mapperList = operationBodyMapperMap.get(exactMatches);
                            mapperList.add(operationBodyMapper);
                        } else {
                            List<FunctionBodyMapper> mapperList = new ArrayList<>();
                            mapperList.add(operationBodyMapper);
                            operationBodyMapperMap.put(exactMatches, mapperList);
                        }
                    }
                }
                if (!operationBodyMapperMap.isEmpty()) {
                    List<FunctionBodyMapper> firstMappers = firstMappers(operationBodyMapperMap);
                    Collections.sort(firstMappers, new UMLOperationBodyMapperComparator());
                    removedOperationIterator.remove();
                    boolean sameSourceAndTargetClass = sameSourceAndTargetClass(firstMappers);
                    if (sameSourceAndTargetClass) {
                        TreeSet<FunctionBodyMapper> set = null;
                        if (allRenamedOperations(firstMappers)) {
                            set = new TreeSet<>();
                        } else {
                            set = new TreeSet<>(new UMLOperationBodyMapperComparator());
                        }
                        set.addAll(firstMappers);
                        FunctionBodyMapper bestMapper = set.first();
                        firstMappers.clear();
                        firstMappers.add(bestMapper);
                    }
                    for (FunctionBodyMapper firstMapper : firstMappers) {
                        FunctionDeclaration addedOperation = firstMapper.getOperation2();
                        if (sameSourceAndTargetClass) {
                            addedOperations.remove(addedOperation);
                        }

                        Refactoring refactoring = null;
                        boolean isTopLevel = removedOperation.isTopLevel() && addedOperation.isTopLevel();
                        if (!isTopLevel && removedOperation.getParentContainerQualifiedName().equals(addedOperation.getParentContainerQualifiedName())) {
                            if (FunctionUtil.equalParameterNames(addedOperation, removedOperation)) {
                                //refactoring = new RenameOperationRefactoring(removedOperation, addedOperation);
                            } else {
                                // Methods in the same class with similar body but different signature
                            }
                        } //else if (removedOperation.isConstructor() == addedOperation.isConstructor() &&
//                                isSubclassOf(removedOperation.getClassName(), addedOperation.getClassName()) && addedOperation.compatibleSignature(removedOperation)) {
//                            refactoring = new PullUpOperationRefactoring(firstMapper);
//                        } else if (removedOperation.isConstructor() == addedOperation.isConstructor() &&
//                                isSubclassOf(addedOperation.getClassName(), removedOperation.getClassName()) && addedOperation.compatibleSignature(removedOperation)) {
//                            refactoring = new PushDownOperationRefactoring(firstMapper);
                        //    }
                        else if (removedOperation.isConstructor() == addedOperation.isConstructor() &&
                                movedMethodSignature(removedOperation, addedOperation)
                                && !refactoringListContainsAnotherMoveRefactoringWithTheSameOperations(removedOperation, addedOperation)) {
                            refactoring = new MoveOperationRefactoring(firstMapper);
                        } else if (removedOperation.isConstructor() == addedOperation.isConstructor()
                                && movedAndRenamedMethodSignature(removedOperation, addedOperation, firstMapper)
                                && !refactoringListContainsAnotherMoveRefactoringWithTheSameOperations(removedOperation, addedOperation)) {
                            refactoring = new MoveOperationRefactoring(firstMapper);
                        }
                        if (refactoring != null) {
                            deleteRemovedOperation(removedOperation);
                            deleteAddedOperation(addedOperation);
                            UMLOperationDiff operationSignatureDiff = new UMLOperationDiff(removedOperation, addedOperation, firstMapper.getMappings());
                            refactorings.addAll(operationSignatureDiff.getRefactorings());
                            refactorings.add(refactoring);
                        }

                    }
                }
            }
        }

    }

    private boolean movedMethodSignature(FunctionDeclaration removedOperation, FunctionDeclaration addedOperation) {
        if (addedOperation.getName().equals(removedOperation.getName())
            //addedOperation.equalReturnParameter(removedOperation) &&
            //addedOperation.isAbstract() == removedOperation.isAbstract() &&
            //addedOperation.getTypeParameters().equals(removedOperation.getTypeParameters())
        ) {
            if (FunctionUtil.equalParameterNames(addedOperation, removedOperation)) {
                return true;
            } else {

                if (FunctionUtil.equalParameterCount(addedOperation, removedOperation))
                    return true;
//                // ignore parameters of types sourceClass and targetClass
//                List<UMLParameter> oldParameters = new ArrayList<UMLParameter>();
//                Set<String> oldParameterNames = new LinkedHashSet<String>();
//                for (UMLParameter oldParameter : removedOperation.getParameters()) {
//                    if (!looksLikeSameType(oldParameter.getType().getClassType(), addedOperation.getClassName())
//                            && !looksLikeSameType(oldParameter.getType().getClassType(), removedOperation.getClassName())) {
//                        oldParameters.add(oldParameter);
//                        oldParameterNames.add(oldParameter.getName());
//                    }
//                }
//                List<UMLParameter> newParameters = new ArrayList<>();
//                Set<String> newParameterNames = new LinkedHashSet<>();
//                for (UMLParameter newParameter : addedOperation.getParameters()) {
//                    if (!looksLikeSameType(newParameter.getType().getClassType(), addedOperation.getClassName()) &&
//                            !looksLikeSameType(newParameter.getType().getClassType(), removedOperation.getClassName())) {
//                        newParameters.add(newParameter);
//                        newParameterNames.add(newParameter.getName());
//                    }
//                }
//                Set<String> intersection = new LinkedHashSet<>(oldParameterNames);
//                intersection.retainAll(newParameterNames);
//                return oldParameters.equals(newParameters) || oldParameters.containsAll(newParameters) || newParameters.containsAll(oldParameters) || intersection.size() > 0 ||
//                        //removedOperation.isStatic() || addedOperation.isStatic()
//                        ;
            }
        }
        return false;
    }

    private boolean mappedElementsMoreThanNonMappedT1AndT2(int mappings, FunctionBodyMapper operationBodyMapper) {
        int nonMappedElementsT1 = operationBodyMapper.nonMappedElementsT1();
        int nonMappedElementsT2 = operationBodyMapper.nonMappedElementsT2();
        ISourceFile addedFile = getAddedClass(operationBodyMapper.getOperation2().getParentContainerQualifiedName());
        int nonMappedStatementsDeclaringSameVariable = 0;
        for (Iterator<SingleStatement> leafIterator1 = operationBodyMapper.getNonMappedLeavesT1()
                .iterator(); leafIterator1.hasNext(); ) {
            SingleStatement s1 = leafIterator1.next();
            for (SingleStatement s2 : operationBodyMapper.getNonMappedLeavesT2()) {
                if (s1.getVariableDeclarations().size() == 1 && s2.getVariableDeclarations().size() == 1) {
                    VariableDeclaration v1 = s1.getVariableDeclarations().get(0);
                    VariableDeclaration v2 = s2.getVariableDeclarations().get(0);
                    if (v1.getVariableName().equals(v2.getVariableName())
                            && v1.getKind().equals(v2.getKind())) {
                        nonMappedStatementsDeclaringSameVariable++;
                    }
                }
            }
            if (addedFile != null && s1.getVariableDeclarations().size() == 1) {
                VariableDeclaration v1 = s1.getVariableDeclarations().get(0);
//                for (UMLAttribute attribute : addedFile.getAttributes()) {
//                    VariableDeclaration attributeDeclaration = attribute.getVariableDeclaration();
//                    if (attributeDeclaration.getInitializer() != null && v1.getInitializer() != null) {
//                        String attributeInitializer = attributeDeclaration.getInitializer().getText();
//                        String variableInitializer = v1.getInitializer().getText();
//                        if (attributeInitializer.equals(variableInitializer)
//                                && attribute.getType().equals(v1.getType()) &&
//                                (attribute.getName().equals(v1.getVariableName()) ||
//                                        attribute.getName().toLowerCase().contains(v1.getVariableName().toLowerCase()) ||
//                                        v1.getVariableName().toLowerCase().contains(attribute.getName().toLowerCase()))) {
//                            nonMappedStatementsDeclaringSameVariable++;
//                            leafIterator1.remove();
//                            LeafCodeFragmentMapping mapping = new LeafCodeFragmentMapping(v1.getInitializer(), attributeDeclaration.getInitializer(), operationBodyMapper.getOperation1(), operationBodyMapper.getOperation2());
//                            operationBodyMapper.getMappings().add(mapping);
//                            break;
//                        }
//                    }
//                }
            }
        }
        int nonMappedLoopsIteratingOverSameVariable = 0;
        for (BlockStatement c1 : operationBodyMapper.getNonMappedInnerNodesT1()) {
            if (FunctionUtil.isLoop(c1)) {
                for (BlockStatement c2 : operationBodyMapper.getNonMappedInnerNodesT2()) {
                    if (FunctionUtil.isLoop(c2)) {
                        Set<String> intersection = new LinkedHashSet<>(c1.getVariables());
                        intersection.retainAll(c2.getVariables());
                        if (!intersection.isEmpty()) {
                            nonMappedLoopsIteratingOverSameVariable++;
                        }
                    }
                }
            }
        }
        return (mappings > nonMappedElementsT1 - nonMappedStatementsDeclaringSameVariable - nonMappedLoopsIteratingOverSameVariable &&
                mappings > nonMappedElementsT2 - nonMappedStatementsDeclaringSameVariable - nonMappedLoopsIteratingOverSameVariable) ||
                (nonMappedElementsT1 - nonMappedStatementsDeclaringSameVariable - nonMappedLoopsIteratingOverSameVariable == 0 && mappings > Math.floor(nonMappedElementsT2 / 2.0)) ||
                (nonMappedElementsT2 - nonMappedStatementsDeclaringSameVariable - nonMappedLoopsIteratingOverSameVariable == 0 && mappings > Math.floor(nonMappedElementsT1 / 2.0));
    }

    private List<FunctionDeclaration> getAddedAndExtractedOperationsInCommonClasses() {
        List<FunctionDeclaration> addedOperations = new ArrayList<>();
        for (var classDiff : commonFilesDiffList) {
            addedOperations.addAll(classDiff.getAddedOperations());
            for (IRefactoring ref : classDiff.getAllRefactorings()) {
                if (ref instanceof ExtractOperationRefactoring) {
                    ExtractOperationRefactoring extractRef = (ExtractOperationRefactoring) ref;
                    addedOperations.add(extractRef.getExtractedOperation());
                }
            }
        }
        return addedOperations;
    }

    private List<FunctionBodyMapper> firstMappers(TreeMap<Integer, List<FunctionBodyMapper>> operationBodyMapperMap) {
        List<FunctionBodyMapper> firstMappers = new ArrayList<>(operationBodyMapperMap.get(operationBodyMapperMap.lastKey()));
        List<FunctionBodyMapper> extraMappers = operationBodyMapperMap.get(0);
        if (extraMappers != null && operationBodyMapperMap.lastKey() != 0) {
            for (FunctionBodyMapper extraMapper : extraMappers) {
                FunctionDeclaration operation1 = extraMapper.getOperation1();
                FunctionDeclaration operation2 = extraMapper.getOperation2();
                if (FunctionUtil.equalsQualifiedNameAndParameterCount(operation1, operation2))
                //operation1.equalSignature(operation2))
                {
                    List<CodeFragmentMapping> mappings = new ArrayList<>(extraMapper.getMappings());
                    if (mappings.size() == 1) {
                        Set<Replacement> replacements = mappings.get(0).getReplacements();
                        if (replacements.size() == 1) {
                            Replacement replacement = replacements.iterator().next();
                            List<String> parameterNames1 = operation1.getParameterNameList();
                            List<String> parameterNames2 = operation2.getParameterNameList();
                            for (int i = 0; i < parameterNames1.size(); i++) {
                                String parameterName1 = parameterNames1.get(i);
                                String parameterName2 = parameterNames2.get(i);
                                if (replacement.getBefore().equals(parameterName1) &&
                                        replacement.getAfter().equals(parameterName2)) {
                                    firstMappers.add(extraMapper);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return firstMappers;
    }

    private boolean allRenamedOperations(List<FunctionBodyMapper> mappers) {
        for (FunctionBodyMapper mapper : mappers) {
            if (mapper.getOperation1().getName().equals(mapper.getOperation2().getName())) {
                return false;
            }
        }
        return true;
    }

    private boolean sameSourceAndTargetClass(List<FunctionBodyMapper> mappers) {
        if (mappers.size() == 1) {
            return false;
        }
        String sourceClassName = null;
        String targetClassName = null;
        for (FunctionBodyMapper mapper : mappers) {
            String mapperSourceClassName = mapper.getOperation1().getParentContainerQualifiedName();
            if (sourceClassName == null) {
                sourceClassName = mapperSourceClassName;
            } else if (!mapperSourceClassName.equals(sourceClassName)) {
                return false;
            }
            String mapperTargetClassName = mapper.getOperation2().getParentContainerQualifiedName();
            if (targetClassName == null) {
                targetClassName = mapperTargetClassName;
            } else if (!mapperTargetClassName.equals(targetClassName)) {
                return false;
            }
        }
        return true;
    }

    private List<FunctionDeclaration> getRemovedOperationsInCommonMovedRenamedClasses() {
        List<FunctionDeclaration> removedOperations = new ArrayList<>();
        for (var classDiff : commonFilesDiffList) {
            removedOperations.addAll(classDiff.getRemovedOperations());
        }
//        for(UMLClassMoveDiff classDiff : classMoveDiffList) {
//            removedOperations.addAll(classDiff.getRemovedOperations());
//        }
//        for(UMLClassMoveDiff classDiff : innerClassMoveDiffList) {
//            removedOperations.addAll(classDiff.getRemovedOperations());
//        }
//        for(UMLClassRenameDiff classDiff : classRenameDiffList) {
//            removedOperations.addAll(classDiff.getRemovedOperations());
//        }
        return removedOperations;
    }

    public ISourceFile getAddedClass(String filePath) {
        for (ISourceFile sourceFile : this.addedFiles) {
            if (sourceFile.getFilepath().equals(filePath))
                return sourceFile;
        }
        return null;
    }

    private boolean refactoringListContainsAnotherMoveRefactoringWithTheSameOperations(FunctionDeclaration removedOperation, FunctionDeclaration addedOperation) {
        for (IRefactoring refactoring : refactorings) {
            if (refactoring instanceof MoveOperationRefactoring) {
                MoveOperationRefactoring moveRefactoring = (MoveOperationRefactoring) refactoring;
                if (FunctionUtil.isEqual(moveRefactoring.getOriginalOperation(), removedOperation)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean movedAndRenamedMethodSignature(FunctionDeclaration
                                                           removedOperation, FunctionDeclaration addedOperation, FunctionBodyMapper mapper) {
        var removedOperationClassDiff = getUMLClassDiff(removedOperation.getSourceLocation().getFilePath());

        if (removedOperationClassDiff != null
                && containsOperationWithTheSameSignatureInNextClass(removedOperationClassDiff, removedOperation)) {
            return false;
        }
//        if ((removedOperation.isGetter() || removedOperation.isSetter() || addedOperation.isGetter() || addedOperation.isSetter()) &&
//                mapper.mappingsWithoutBlocks() == 1 && mapper.getMappings().size() == 1) {
//            if (!mapper.getMappings().iterator().next().isExact()) {
//                return false;
//            }
//        }
        if ((removedOperation.isConstructor() || addedOperation.isConstructor()) && mapper.mappingsWithoutBlocks() > 0) {
            if (!(ContainerDiff.allMappingsAreExactMatches(mapper) && mapper.nonMappedElementsT1() == 0
                    && mapper.nonMappedElementsT2() == 0)) {
                return false;
            }
        }
        int exactLeafMappings = 0;
        for (CodeFragmentMapping mapping : mapper.getMappings()) {
            if (mapping instanceof LeafCodeFragmentMapping && mapping.isExact() && !mapping.getFragment1().getText().startsWith("return ")) {
                exactLeafMappings++;
            }
        }
        double normalizedEditDistance = mapper.normalizedEditDistance();
        if (exactLeafMappings == 0 && normalizedEditDistance > 0.24) {
            return false;
        }
        if (exactLeafMappings == 1 && normalizedEditDistance > 0.5 && (mapper.nonMappedElementsT1() > 0 || mapper.nonMappedElementsT2() > 0)) {
            return false;
        }
        if (mapper.mappingsWithoutBlocks() == 1) {
            for (CodeFragmentMapping mapping : mapper.getMappings()) {
                String fragment1 = mapping.getFragment1().getText();
                String fragment2 = mapping.getFragment2().getText();
                if (fragment1.startsWith("return true;") || fragment1.startsWith("return false;") || fragment1.startsWith("return this;") || fragment1.startsWith("return null;") || fragment1.startsWith("return;") ||
                        fragment2.startsWith("return true;") || fragment2.startsWith("return false;") || fragment2.startsWith("return this;") || fragment2.startsWith("return null;") || fragment2.startsWith("return;")) {
                    return false;
                }
            }
        }
        return false;
    }

    private ContainerDiff<ISourceFile> getUMLClassDiff(String fileName) {
        for (var fileDiff : this.commonFilesDiffList) {
            if (fileDiff.getContainer1().getFilepath().equals(fileName))
                return fileDiff;
        }
//        for(UMLClassMoveDiff classDiff : classMoveDiffList) {
//            if(classDiff.matches(className))
//                return classDiff;
//        }
//        for(UMLClassMoveDiff classDiff : innerClassMoveDiffList) {
//            if(classDiff.matches(className))
//                return classDiff;
//        }
//        for(UMLClassRenameDiff classDiff : classRenameDiffList) {
//            if(classDiff.matches(className))
//                return classDiff;
//        }
        return null;
    }

    private boolean conflictingMoveOfTopLevelClass(FunctionDeclaration removedClass, FunctionDeclaration addedClass) {
        if (!removedClass.isTopLevel() && !addedClass.isTopLevel()) {
            //check if classMoveDiffList contains already a move for the outer class to a different target
//            for(SourceFileMoveDiff diff : classMoveDiffList) {
//                if((diff.getOriginalClass().getName().startsWith(removedClass.getPackageName()) &&
//                        !diff.getMovedClass().getName().startsWith(addedClass.getPackageName())) ||
//                        (!diff.getOriginalClass().getName().startsWith(removedClass.getPackageName()) &&
//                                diff.getMovedClass().getName().startsWith(addedClass.getPackageName()))) {
//                    return true;
//                }
//            }
        }
        return false;
    }

    public boolean containsOperationWithTheSameSignatureInNextClass(ContainerDiff<ISourceFile> sourceFileDiff, FunctionDeclaration operation) {
        for (IFunctionDeclaration originalOperation : sourceFileDiff.getContainer2().getFunctionDeclarations()) {
            if (FunctionUtil.isExactSignature(originalOperation, operation)) ;
            return true;
        }
        return false;
    }

    private List<FunctionDeclaration> getAddedOperationsInCommonClasses() {
        List<FunctionDeclaration> addedOperations = new ArrayList<>();
        for (var fileDiff : this.commonFilesDiffList) {
            addedOperations.addAll(fileDiff.getAddedOperations());
        }
        return addedOperations;
    }

    private List<FunctionDeclaration> getRemovedOperationsInCommonClasses() {
        List<FunctionDeclaration> removedOperations = new ArrayList<>();
        for (var classDiff : this.commonFilesDiffList) {
            removedOperations.addAll(classDiff.getRemovedOperations());
        }
        return removedOperations;
    }

    private void checkForOperationMovesIncludingRemovedFiles() throws RefactoringMinerTimedOutException {
        List<FunctionDeclaration> addedOperations = getAddedAndExtractedOperationsInCommonClasses();
        List<FunctionDeclaration> removedOperations = getRemovedOperationsInCommonClasses();
        for (ISourceFile removedClass : this.removedFiles) {
            removedOperations.addAll(removedClass.getFunctionDeclarations().stream().map(ifd -> (FunctionDeclaration) ifd).collect(Collectors.toList()));
        }
        if (removedOperations.size() <= JsConfig.MAXIMUM_NUMBER_OF_COMPARED_METHODS || addedOperations.size() <=
                JsConfig.MAXIMUM_NUMBER_OF_COMPARED_METHODS) {
            checkForOperationMoves(addedOperations, removedOperations);
        }
    }

    private void deleteRemovedOperation(FunctionDeclaration operation) {
        var classDiff = getUMLClassDiff(operation.getSourceLocation().getFilePath());
        if (classDiff != null)
            classDiff.getRemovedOperations().remove(operation);
    }

    private void deleteAddedOperation(FunctionDeclaration operation) {
        var classDiff = getUMLClassDiff(operation.getSourceLocation().getFilePath());
        if (classDiff != null)
            classDiff.getAddedOperations().remove(operation);
    }

    public List<ISourceFile> getAddedFiles() {
        return addedFiles;
    }

    public List<ISourceFile> getRemovedFiles() {
        return removedFiles;
    }

    public List<FunctionBodyMapper> findMappersWithMatchingSignature2(IFunctionDeclaration operation2) {
        List<FunctionBodyMapper> mappers = new ArrayList<>();
        for (var classDiff : this.commonFilesDiffList) {
            var mapper = classDiff.findMapperWithMatchingSignature2(operation2);
            if (mapper != null) {
                mappers.add(mapper);
            }
        }
//        for (UMLClassMoveDiff classDiff : classMoveDiffList) {
//            UMLOperationBodyMapper mapper = classDiff.findMapperWithMatchingSignature2(operation2);
//            if (mapper != null) {
//                mappers.add(mapper);
//            }
//        }
//        for (UMLClassMoveDiff classDiff : innerClassMoveDiffList) {
//            UMLOperationBodyMapper mapper = classDiff.findMapperWithMatchingSignature2(operation2);
//            if (mapper != null) {
//                mappers.add(mapper);
//            }
//        }
//        for (UMLClassRenameDiff classDiff : classRenameDiffList) {
//            UMLOperationBodyMapper mapper = classDiff.findMapperWithMatchingSignature2(operation2);
//            if (mapper != null) {
//                mappers.add(mapper);
//            }
//        }
//
        return mappers;
    }

    public boolean commonlyImplementedOperations(IFunctionDeclaration operation1, IFunctionDeclaration operation2, SourceFileDiffer classDiff2) {
//        var classDiff1 = getUMLClassDiff(operation1.getSourceLocation().getFilePath());
//        if(classDiff1 != null) {
//      //      Set<UMLType> commonInterfaces = classDiff1.nextClassCommonInterfaces(classDiff2);
//            for(UMLType commonInterface : commonInterfaces) {
//                UMLClassBaseDiff interfaceDiff = getUMLClassDiff(commonInterface);
//                if(interfaceDiff != null &&
//                        interfaceDiff.containsOperationWithTheSameSignatureInOriginalClass(operation1) &&
//                        interfaceDiff.containsOperationWithTheSameSignatureInNextClass(operation2)) {
//                    return true;
//                }
//            }
//        }
        return false;
    }

    void reportFileMoveDiff(SourceFileMoveDiff sourceFileMoveDiff) {
        this.fileMoveDiffList.add(sourceFileMoveDiff);
    }

    void reportClassMoveDiff(UMLClassMoveDiff classMoveDiff) {
        this.classMoveDiffList.add(classMoveDiff);
    }

    public List<UMLClassMoveDiff> getClassMoveDiffList() {
        return classMoveDiffList;
    }

    public List<SourceFileMoveDiff> getFileMoveDiffList() {
        return this.fileMoveDiffList;
    }

    public List<SourceFileRenameDiff> getFileRenameDiffList() {
        return fileRenameDiffList;
    }

    public List<UMLClassMoveDiff> getInnerClassMoveDiffList() {
        return innerClassMoveDiffList;
    }

    public List<UMLClassRenameDiff> getClassRenameDiffList() {
        return classRenameDiffList;
    }

    public void addInnerClassMoveDiffList(UMLClassMoveDiff classMoveDiff) {
        this.innerClassMoveDiffList.add(classMoveDiff);
    }

    public void addClassRenameDiffList(UMLClassRenameDiff classRenameDiff) {
        this.classRenameDiffList.add(classRenameDiff);
    }

    public void addUMLClassDiff(ClassDiff classDiff) {
        this.commonClassDiffList.add(classDiff);
    }
}
