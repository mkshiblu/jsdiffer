package io.jsrminer.uml.diff;

import io.jsrminer.api.RefactoringMinerTimedOutException;
import io.jsrminer.sourcetree.ClassDeclaration;
import io.jsrminer.uml.UMLClassMatcher;
import io.jsrminer.uml.UMLModel;
import io.jsrminer.uml.UMLSourceFileMatcher;
import io.rminerx.core.api.ISourceFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UMLModelDiffer {
    UMLModel umlModel1;
    UMLModel umlModel2;

    private final List<ClassDeclaration> addedClasses = new ArrayList<>();
    private final List<ClassDeclaration> removedClasses = new ArrayList<>();

    public UMLModelDiffer(UMLModel umlModel1, UMLModel umlModel2) {
        this.umlModel1 = umlModel1;
        this.umlModel2 = umlModel2;
    }

    public UMLModelDiff diff(Map<String, String> renamedFileHints) {
        final UMLModelDiff modelDiff = new UMLModelDiff(umlModel1, umlModel2);

        // We will process the files first to not mix added or removed classes inside move or renamed files
        reportAddedAndRemovedSourceFiles(modelDiff);
        checkForMovedFiles(renamedFileHints, umlModel2.getRepositoryDirectories(), new UMLSourceFileMatcher.Move(), modelDiff);
        checkForRenamedFiles(renamedFileHints, new UMLSourceFileMatcher.Rename(), modelDiff);

        diffCommonNamedFiles(modelDiff);
        checkForMovedFiles(renamedFileHints, umlModel2.getRepositoryDirectories(), new UMLSourceFileMatcher.RelaxedMove(), modelDiff);
        checkForRenamedFiles(renamedFileHints, new UMLSourceFileMatcher.RelaxedRename(), modelDiff);

        reportAddedAndRemovedClasses(modelDiff);
        checkForMovedClasses(renamedFileHints, umlModel1.getRepositoryDirectories(), new UMLClassMatcher.Move(), modelDiff);
        //modelDiff.checkForRenamedClasses(renamedFileHints, new UMLClassMatcher.Rename());
        //diffCommonNamedClasses(modelDiff);
        //modelDiff.checkForMovedClasses(renamedFileHints, umlModel.repositoryDirectories, new UMLClassMatcher.RelaxedMove());
        //modelDiff.checkForRenamedClasses(renamedFileHints, new UMLClassMatcher.RelaxedRename());

        diffUncommonFiles(modelDiff);
        return modelDiff;
    }

    private void reportAddedAndRemovedSourceFiles(UMLModelDiff modelDiff) {
        for (ISourceFile sourceFile : umlModel1.getSourceFileModels().values()) {
            if (!modelDiff.model2.getSourceFileModels().containsKey(sourceFile.getFilepath()))
                modelDiff.reportRemovedFile(sourceFile);
        }

        for (ISourceFile sourceFile : modelDiff.model2.getSourceFileModels().values()) {
            if (!umlModel1.getSourceFileModels().containsKey(sourceFile.getFilepath()))
                modelDiff.reportAddedFile(sourceFile);
        }
    }

    private void reportAddedAndRemovedClasses(UMLModelDiff modelDiff) {
        var classDeclarations1 = modelDiff.model1.getSourceFileModels().values()
                .stream()
                .map(sourceFile -> sourceFile.getClassDeclarations())
                .flatMap(List::stream)
                .collect(Collectors.toList());

        var classDeclarations2 = modelDiff.model1.getSourceFileModels().values()
                .stream()
                .map(sourceFile -> sourceFile.getClassDeclarations())
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Find removed and added

//        var removedClasses = classDeclarations1.stream()
//                .filter(classDeclaration -> )

    }

    boolean classNameEqual(ClassDeclaration removedClass, ClassDeclaration addedClass) {
        return removedClass.getParentContainerQualifiedName().equals(addedClass.getParentContainerQualifiedName())
                && removedClass.getQualifiedName().equals(addedClass.getQualifiedName())
                && removedClass.getSourceLocation().getFilePath()
                .equals(addedClass.getSourceLocation().getFilePath());
    }

    public void checkForMovedClasses(Map<String, String> renamedFileHints, Set<String> repositoryDirectories, UMLClassMatcher matcher
            , UMLModelDiff modelDiff) throws RefactoringMinerTimedOutException {

        //

        for (Iterator<ClassDeclaration> removedClassIterator = removedClasses.iterator(); removedClassIterator.hasNext(); ) {
            UMLClass removedClass = removedClassIterator.next();
            TreeSet<UMLClassMoveDiff> diffSet = new TreeSet<UMLClassMoveDiff>(new ClassMoveComparator());
            for (Iterator<UMLClass> addedClassIterator = addedClasses.iterator(); addedClassIterator.hasNext(); ) {
                UMLClass addedClass = addedClassIterator.next();
                String removedClassSourceFile = removedClass.getSourceFile();
                String renamedFile = renamedFileHints.get(removedClassSourceFile);
                String removedClassSourceFolder = "";
                if (removedClassSourceFile.contains("/")) {
                    removedClassSourceFolder = removedClassSourceFile.substring(0, removedClassSourceFile.lastIndexOf("/"));
                }
                if (!repositoryDirectories.contains(removedClassSourceFolder)) {
                    deletedFolderPaths.add(removedClassSourceFolder);
                    //add deleted sub-directories
                    String subDirectory = new String(removedClassSourceFolder);
                    while (subDirectory.contains("/")) {
                        subDirectory = subDirectory.substring(0, subDirectory.lastIndexOf("/"));
                        if (!repositoryDirectories.contains(subDirectory)) {
                            deletedFolderPaths.add(subDirectory);
                        }
                    }
                }
                if (matcher.match(removedClass, addedClass, renamedFile)) {
                    if (!conflictingMoveOfTopLevelClass(removedClass, addedClass)) {
                        UMLClassMoveDiff classMoveDiff = new UMLClassMoveDiff(removedClass, addedClass, this);
                        diffSet.add(classMoveDiff);
                    }
                }
            }
            if (!diffSet.isEmpty()) {
                UMLClassMoveDiff minClassMoveDiff = diffSet.first();
                minClassMoveDiff.process();
                classMoveDiffList.add(minClassMoveDiff);
                addedClasses.remove(minClassMoveDiff.getMovedClass());
                removedClassIterator.remove();
            }
        }

        List<UMLClassMoveDiff> allClassMoves = new ArrayList<UMLClassMoveDiff>(this.classMoveDiffList);
        Collections.sort(allClassMoves);

        for (int i = 0; i < allClassMoves.size(); i++) {
            UMLClassMoveDiff classMoveI = allClassMoves.get(i);
            for (int j = i + 1; j < allClassMoves.size(); j++) {
                UMLClassMoveDiff classMoveJ = allClassMoves.get(j);
                if (classMoveI.isInnerClassMove(classMoveJ)) {
                    innerClassMoveDiffList.add(classMoveJ);
                }
            }
        }

        this.classMoveDiffList.removeAll(innerClassMoveDiffList);
    }

    public void checkForMovedFiles(Map<String, String> renamedFileHints
            , Set<String> repositoryDirectories
            , UMLSourceFileMatcher matcher
            , UMLModelDiff modelDiff) {
        LinkedHashSet<String> deletedFolderPaths = new LinkedHashSet<>();

        for (Iterator<ISourceFile> removedFilesIterator = modelDiff.getRemovedFiles().iterator(); removedFilesIterator.hasNext(); ) {
            ISourceFile removedFile = removedFilesIterator.next();
            TreeSet<SourceFileMoveDiff> diffSet = new TreeSet<>((o1, o2) -> {
                double sourceFolderDistance1 = o1.getMovedFile().normalizedSourceFolderDistance(o1.getOriginalFile());
                double sourceFolderDistance2 = o2.getMovedFile().normalizedSourceFolderDistance(o2.getOriginalFile());
                return Double.compare(sourceFolderDistance1, sourceFolderDistance2);
            });

            for (Iterator<ISourceFile> addedFilesIterator = modelDiff.getAddedFiles().iterator(); addedFilesIterator.hasNext(); ) {
                ISourceFile addedFile = addedFilesIterator.next();
                String removedClassSourceFile = removedFile.getFilepath();
                String renamedFile = renamedFileHints.get(removedClassSourceFile);
//                String removedClassSourceFolder = "";
//                if (removedClassSourceFile.contains("/")) {
//                    removedClassSourceFolder = removedClassSourceFile.substring(0, removedClassSourceFile.lastIndexOf("/"));
//                }

//                String removedClassSourceFolder = removedFile.getDirectoryPath();
//
//                if (!repositoryDirectories.contains(removedClassSourceFolder)) {
//                    deletedFolderPaths.add(removedClassSourceFolder);
//
//                    //add deleted sub-directories
//                    String subDirectory = new String(removedClassSourceFolder);
//                    while (subDirectory.contains("/")) {
//                        subDirectory = subDirectory.substring(0, subDirectory.lastIndexOf("/"));
//                        if (!repositoryDirectories.contains(subDirectory)) {
//                            deletedFolderPaths.add(subDirectory);
//                        }
//                    }
//                }


                if (matcher.match(removedFile, addedFile, renamedFile)) {
                    //if (!conflictingMoveOfTopLevelClass(removedFile, addedFile))
                    {
                        SourceFileMoveDiff classMoveDiff = new SourceFileMoveDiff(removedFile, addedFile);
                        diffSet.add(classMoveDiff);
                    }
                }
            }

            if (!diffSet.isEmpty()) {
                SourceFileMoveDiff minClassMoveDiff = diffSet.first();
                //minClassMoveDiff.process();
                SourceFileDiffer sourceFileDiffer = new SourceFileDiffer(minClassMoveDiff.getSource1()
                        , minClassMoveDiff.getSource2(), modelDiff);
                SourceFileDiff sourceDiff = sourceFileDiffer.diff();
                modelDiff.reportClassMoveDiff(minClassMoveDiff);
                modelDiff.getAddedFiles().remove(minClassMoveDiff.getMovedFile());
                removedFilesIterator.remove();
            }
        }

        // For inner class move not applicable for file
//        List<SourceFileMoveDiff> allClassMoves = new ArrayList<>(modelDiff.getClassMoveDiffList());
//        Collections.sort(allClassMoves);
//
//        for (int i = 0; i < allClassMoves.size(); i++) {
//            SourceFileMoveDiff classMoveI = allClassMoves.get(i);
//            for (int j = i + 1; j < allClassMoves.size(); j++) {
//                SourceFileMoveDiff classMoveJ = allClassMoves.get(j);
//                if (classMoveI.isInnerClassMove(classMoveJ)) {
//                    innerClassMoveDiffList.add(classMoveJ);
//                }
//            }
//        }
//        this.classMoveDiffList.removeAll(innerClassMoveDiffList);
    }

    public void checkForRenamedFiles(Map<String, String> renamedFileHints
            , UMLSourceFileMatcher matcher
            , UMLModelDiff modelDiff) {
        for (var removedClassIterator = modelDiff.getRemovedFiles().iterator(); removedClassIterator.hasNext(); ) {
            var removedClass = removedClassIterator.next();
            var diffSet = new TreeSet<>(new FileRenameComparator());

            for (var addedClassIterator = modelDiff.getAddedFiles().iterator(); addedClassIterator.hasNext(); ) {
                var addedClass = addedClassIterator.next();
                String renamedFile = renamedFileHints.get(removedClass.getFilepath());
                if (matcher.match(removedClass, addedClass, renamedFile)) {
//                    if (!conflictingMoveOfTopLevelClass(removedClass, addedClass)
//                            && !innerClassWithTheSameName(removedClass, addedClass))
                    {
                        var classRenameDiff = new SourceFileRenameDiff(removedClass, addedClass);
                        diffSet.add(classRenameDiff);
                    }
                }
            }

            if (!diffSet.isEmpty()) {
                var minClassRenameDiff = diffSet.first();
                SourceFileDiffer sourceFileDiffer = new SourceFileDiffer(minClassRenameDiff.getSource1()
                        , minClassRenameDiff.getSource2(), modelDiff);
                SourceFileDiff sourceDiff = sourceFileDiffer.diff();
                modelDiff.getFileRenameDiffList().add(minClassRenameDiff);
                modelDiff.getAddedFiles().remove(minClassRenameDiff.getRenamedFile());
                removedClassIterator.remove();
            }
        }
    }

    private void diffCommonNamedFiles(UMLModelDiff modelDiff) {
        for (Map.Entry<String, ISourceFile> entry : umlModel1.getSourceFileModels().entrySet()) {
            final String file = entry.getKey();
            final ISourceFile sourceFileModel2 = umlModel2.getSourceFileModel(file);

            // Check if model2 contains the same file
            if (sourceFileModel2 != null) {
                SourceFileDiffer sourceFileDiffer = new SourceFileDiffer(entry.getValue(), sourceFileModel2, modelDiff);
                SourceFileDiff sourceDiff = sourceFileDiffer.diff();

                boolean isEmpty = sourceDiff.getAddedOperations().isEmpty()
                        && sourceDiff.getRemovedOperations().isEmpty()
                        //&& addedAttributes.isEmpty() && removedAttributes.isEmpty() &&
                        // addedEnumConstants.isEmpty() && removedEnumConstants.isEmpty()
                        && sourceDiff.getOperationDiffList().isEmpty()
                        //&& attributeDiffList.isEmpty()
                        && sourceDiff.getBodyStatementMapper() == null
                        && sourceDiff.getOperationBodyMapperList().isEmpty();
                //&& enumConstantDiffList.isEmpty()
                //&& !visibilityChanged && !abstractionChanged;

                if (!isEmpty) {
                    modelDiff.getCommonFilesDiffList().add(sourceDiff);
                }
            }
        }
    }

    private void diffUncommonFiles(UMLModelDiff modelDiff) {
        for (ISourceFile removedFile : modelDiff.getRemovedFiles()) {

            // Check if model2 contains files with similar contents
            List<ISourceFile> addedFiles = modelDiff.getAddedFiles();
//
//            if (umlModel1.getSourceFileModels() != null) {
//                SourceDiffer sourceDiffer = new SourceDiffer(entry.getValue(), sourceFileModel2, modelDiff);
//                SourceFileDiff sourceDiff = sourceDiffer.diff();
//
//                boolean isEmpty = sourceDiff.getAddedOperations().isEmpty()
//                        && sourceDiff.getAddedOperations().isEmpty()
//                        //&& addedAttributes.isEmpty() && removedAttributes.isEmpty() &&
//                        // addedEnumConstants.isEmpty() && removedEnumConstants.isEmpty()
//                        && sourceDiff.getOperationDiffList().isEmpty()
//                        //&& attributeDiffList.isEmpty()
//                        && sourceDiff.getBodyMapperList().isEmpty();
//                //&& enumConstantDiffList.isEmpty()
//                //&& !visibilityChanged && !abstractionChanged;
//
//                if (!isEmpty) {
//                    modelDiff.getCommonFilesDiffList().add(sourceDiff);
//                }
//            }
        }
    }
}
