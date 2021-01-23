package io.jsrminer.uml.diff;

import io.jsrminer.uml.UMLModel;
import io.rminer.core.api.ISourceFile;

import java.util.List;
import java.util.Map;

public class UMLModelDiffer {
    UMLModel umlModel1;
    UMLModel umlModel2;

    public UMLModelDiffer(UMLModel umlModel1, UMLModel umlModel2) {
        this.umlModel1 = umlModel1;
        this.umlModel2 = umlModel2;
    }

    public UMLModelDiff diff(Map<String, String> renamedFileHints) {
        final UMLModelDiff modelDiff = new UMLModelDiff(umlModel1, umlModel2);

        reportAddedAndRemovedSourceFiles(modelDiff);

        //modelDiff.checkForMovedClasses(renamedFileHints, umlModel.repositoryDirectories, new UMLClassMatcher.Move());
        //modelDiff.checkForMovedFiles(renamedFileHints, umlModel.repositoryDirectories, new UMLSourceFileMatcher.Move());
        //modelDiff.checkForRenamedClasses(renamedFileHints, new UMLClassMatcher.Rename());

        diffCommonNamedFiles(modelDiff);
        //modelDiff.checkForMovedClasses(renamedFileHints, umlModel.repositoryDirectories, new UMLClassMatcher.RelaxedMove());
        //modelDiff.checkForRenamedClasses(renamedFileHints, new UMLClassMatcher.RelaxedRename());

        diffUncommonFiles(modelDiff);
        return modelDiff;
    }

    private void reportAddedAndRemovedSourceFiles(UMLModelDiff modelDiff) {
        for (ISourceFile umlClass : umlModel1.getSourceFileModels().values()) {
            if (!modelDiff.model2.getSourceFileModels().containsKey(umlClass.getFilepath()))
                modelDiff.reportRemovedFile(umlClass);
        }

        for (ISourceFile umlClass : modelDiff.model2.getSourceFileModels().values()) {
            if (!umlModel1.getSourceFileModels().containsKey(umlClass.getFilepath()))
                modelDiff.reportAddedFile(umlClass);
        }
    }

    private void diffCommonNamedFiles(UMLModelDiff modelDiff) {
        for (Map.Entry<String, ISourceFile> entry : umlModel1.getSourceFileModels().entrySet()) {
            final String file = entry.getKey();
            final ISourceFile sourceFileModel2 = umlModel2.getSourceFileModel(file);

            // Check if model2 contains the same file
            if (sourceFileModel2 != null) {
                SourceDiffer sourceDiffer = new SourceDiffer(entry.getValue(), sourceFileModel2, modelDiff);
                SourceFileDiff sourceDiff = sourceDiffer.diff();

                boolean isEmpty = sourceDiff.getAddedOperations().isEmpty()
                        && sourceDiff.getAddedOperations().isEmpty()
                        //&& addedAttributes.isEmpty() && removedAttributes.isEmpty() &&
                        // addedEnumConstants.isEmpty() && removedEnumConstants.isEmpty()
                        && sourceDiff.getOperationDiffList().isEmpty()
                        //&& attributeDiffList.isEmpty()
                        && sourceDiff.getBodyMapperList().isEmpty();
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
