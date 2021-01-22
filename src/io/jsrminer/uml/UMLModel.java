package io.jsrminer.uml;

import io.jsrminer.uml.diff.SourceFileDiff;
import io.jsrminer.uml.diff.SourceDiffer;
import io.jsrminer.uml.diff.UMLModelDiff;
import io.rminer.core.api.ISourceFile;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Abstracts the source code of the whole code base
 */
public class UMLModel /*implements Diffable<UMLModel, UMLModelDiff>*/ {

    private HashMap<String, ISourceFile> sourceModelMap;
    LinkedHashSet repositoryDirectories = new LinkedHashSet();

    public UMLModelDiff diff(UMLModel umlModel, Map<String, String> renamedFileHints) {
        final UMLModelDiff modelDiff = new UMLModelDiff(this, umlModel);

        reportAddedAndRemovedSourceFiles(modelDiff, umlModel);

        //modelDiff.checkForMovedClasses(renamedFileHints, umlModel.repositoryDirectories, new UMLClassMatcher.Move());
        modelDiff.checkForMovedFunctions(renamedFileHints, umlModel.repositoryDirectories, new UMLClassMatcher.Move());
        //modelDiff.checkForRenamedClasses(renamedFileHints, new UMLClassMatcher.Rename());

        diffCommonNamedFiles(modelDiff, umlModel);
        //modelDiff.checkForMovedClasses(renamedFileHints, umlModel.repositoryDirectories, new UMLClassMatcher.RelaxedMove());
        //modelDiff.checkForRenamedClasses(renamedFileHints, new UMLClassMatcher.RelaxedRename());
        return modelDiff;
    }

    private void reportAddedAndRemovedSourceFiles(UMLModelDiff modelDiff, UMLModel model2) {
        for (ISourceFile umlClass : this.sourceModelMap.values()) {
            if (!model2.sourceModelMap.containsKey(umlClass.getFilepath()))
                modelDiff.reportRemovedFile(umlClass);
        }

        for (ISourceFile umlClass : model2.sourceModelMap.values()) {
            if (!this.sourceModelMap.containsKey(umlClass.getFilepath()))
                modelDiff.reportAddedFile(umlClass);
        }
    }

    private void diffCommonNamedFiles(UMLModelDiff modelDiff, UMLModel umlModel2) {
        for (Map.Entry<String, ISourceFile> entry : sourceModelMap.entrySet()) {
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

    public boolean containsSourceFileModel(String file) {
        return sourceModelMap.containsKey(file);
    }

    public ISourceFile getSourceFileModel(String file) {
        return sourceModelMap.get(file);
    }

    public Map<String, ISourceFile> getSourceFileModels() {
        return sourceModelMap;
    }

    public void setSourceFileModels(final HashMap<String, ISourceFile> sourceModelMap) {
        this.sourceModelMap = sourceModelMap;
    }

    public LinkedHashSet getRepositoryDirectories() {
        return repositoryDirectories;
    }
}
