package io.jsrminer.uml.diff;

import io.jsrminer.api.RefactoringMinerTimedOutException;
import io.jsrminer.sourcetree.ClassDeclaration;
import io.jsrminer.uml.UMLClassMatcher;
import io.jsrminer.uml.UMLModel;
import io.jsrminer.uml.UMLSourceFileMatcher;
import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ISourceFile;

import java.util.*;
import java.util.stream.Collectors;

public class UMLModelDiffer {
    UMLModel umlModel1;
    UMLModel umlModel2;

    private final List<IClassDeclaration> addedClasses = new ArrayList<>();
    private final List<IClassDeclaration> removedClasses = new ArrayList<>();
    private final Set<String> deletedFolderPaths = new LinkedHashSet<>();

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

        var classDeclarations2 = modelDiff.model2.getSourceFileModels().values()
                .stream()
                .map(sourceFile -> sourceFile.getClassDeclarations())
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Find removed and added classes
        classDeclarations1.forEach(classDeclaration1 -> {
            boolean found = false;
            for (var classDeclaration2 : classDeclarations2) {
                if (classNameEqual(classDeclaration1, classDeclaration2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                removedClasses.add(classDeclaration1);
            }
        });

        classDeclarations2.forEach(classDeclaration2 -> {
            boolean found = false;
            for (var classDeclaration1 : classDeclarations1) {
                if (classNameEqual(classDeclaration2, classDeclaration1)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                addedClasses.add(classDeclaration2);
            }
        });
    }

    boolean classNameEqual(IClassDeclaration removedClass, IClassDeclaration addedClass) {
        return removedClass.getParentContainerQualifiedName().equals(addedClass.getParentContainerQualifiedName())
                && removedClass.getQualifiedName().equals(addedClass.getQualifiedName())
                && removedClass.getSourceLocation().getFilePath()
                .equals(addedClass.getSourceLocation().getFilePath());
    }

    public void checkForMovedClasses(Map<String, String> renamedFileHints, Set<String> repositoryDirectories, UMLClassMatcher matcher
            , UMLModelDiff modelDiff) throws RefactoringMinerTimedOutException {

        //
        for (var removedClassIterator = removedClasses.iterator(); removedClassIterator.hasNext(); ) {
            var removedClass = removedClassIterator.next();
            TreeSet<UMLClassMoveDiff> diffSet = new TreeSet<>(new ClassMoveComparator());

            for (var addedClassIterator = addedClasses.iterator(); addedClassIterator.hasNext(); ) {
                var addedClass = addedClassIterator.next();
                String removedClassSourceFile = removedClass.getSourceLocation().getFilePath();
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
                    if (!conflictingMoveOfTopLevelClass(modelDiff, removedClass, addedClass)) {
                        UMLClassMoveDiff classMoveDiff = new UMLClassMoveDiff(removedClass, addedClass);
                        diffSet.add(classMoveDiff);
                    }
                }
            }
            if (!diffSet.isEmpty()) {
                UMLClassMoveDiff minClassMoveDiff = diffSet.first();
                minClassMoveDiff.process();
                modelDiff.reportClassMoveDiff(minClassMoveDiff);
                addedClasses.remove(minClassMoveDiff.getNextClass());
                removedClassIterator.remove();
            }
        }

        List<UMLClassMoveDiff> allClassMoves = new ArrayList<>(modelDiff.getClassMoveDiffList());
        Collections.sort(allClassMoves, (classMove1, classMove2) -> classMove1.getOriginalClass().getQualifiedName()
                .compareTo(classMove2.getOriginalClass().getName()));

        for (int i = 0; i < allClassMoves.size(); i++) {
            UMLClassMoveDiff classMoveI = allClassMoves.get(i);
            for (int j = i + 1; j < allClassMoves.size(); j++) {
                UMLClassMoveDiff classMoveJ = allClassMoves.get(j);
                if (classMoveI.isInnerClassMove(classMoveJ)) {
                    modelDiff.reportInnerClassMoveDiffList(classMoveJ);
                }
            }
        }

        modelDiff.getClassMoveDiffList().removeAll(modelDiff.getInnerClassMoveDiffList());
    }

    private boolean conflictingMoveOfTopLevelClass(UMLModelDiff modelDiff, IClassDeclaration removedClass, IClassDeclaration addedClass) {
        if (!removedClass.isTopLevel() && !addedClass.isTopLevel()) {
//            //check if classMoveDiffList contains already a move for the outer class to a different target
//            for (UMLClassMoveDiff diff : modelDiff.getClassMoveDiffList()) {
//                if ((diff.getOriginalClass().getQualifiedName().startsWith(removedClass.getPackageName())
//                        && !diff.getNextClass().getQualifiedName().startsWith(addedClass.getPackageName()))
//                        || (!diff.getOriginalClass().getName().startsWith(removedClass.getPackageName())
//                        &&  diff.getNextClass().getName().startsWith(addedClass.getPackageName()))) {
//                    return true;
//                }
//            }
        }
        return false;
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
                SourceFileMoveDiff minFileMoveDiff = diffSet.first();
                //minClassMoveDiff.process();
                SourceFileDiffer sourceFileDiffer = new SourceFileDiffer(minFileMoveDiff.getOriginalFile()
                        , minFileMoveDiff.getMovedFile(), modelDiff);
                var sourceDiff = sourceFileDiffer.diff();
                modelDiff.reportFileMoveDiff(minFileMoveDiff);
                modelDiff.getAddedFiles().remove(minFileMoveDiff.getMovedFile());
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
                var minFileRenameDiff = diffSet.first();
                SourceFileDiffer sourceFileDiffer = new SourceFileDiffer(minFileRenameDiff.getOriginalFile()
                        , minFileRenameDiff.getRenamedFile(), modelDiff);
                var containerDiff = sourceFileDiffer.diff();
                modelDiff.getFileRenameDiffList().add(minFileRenameDiff);
                modelDiff.getAddedFiles().remove(minFileRenameDiff.getRenamedFile());
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
                ContainerDiff<ISourceFile> containerDiff = sourceFileDiffer.diff();

                boolean isEmpty = containerDiff.getAddedOperations().isEmpty()
                        && containerDiff.getRemovedOperations().isEmpty()
                        //&& addedAttributes.isEmpty() && removedAttributes.isEmpty() &&
                        // addedEnumConstants.isEmpty() && removedEnumConstants.isEmpty()
                        && containerDiff.getOperationDiffList().isEmpty()
                        //&& attributeDiffList.isEmpty()
                        && containerDiff.getBodyStatementMapper() == null
                        && containerDiff.getOperationBodyMapperList().isEmpty();
                //&& enumConstantDiffList.isEmpty()
                //&& !visibilityChanged && !abstractionChanged;

                if (!isEmpty) {
                    modelDiff.getCommonFilesDiffList().add(containerDiff);
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
