package io.jsrminer.uml;

import io.jsrminer.api.Diffable;
import io.jsrminer.uml.diff.ContainerDiff;
import io.jsrminer.uml.diff.ContainerDiffer;
import io.jsrminer.uml.diff.UMLModelDiff;
import io.rminer.core.api.ISourceFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstracts the source code of the whole code base
 */
public class UMLModel implements Diffable<UMLModel, UMLModelDiff> {

    private HashMap<String, ISourceFile> sourceModelMap;

    @Override
    public UMLModelDiff diff(UMLModel umlModel) {
        final UMLModelDiff modelDiff = new UMLModelDiff(this, umlModel);

        // Diff on common files and functions
        for (Map.Entry<String, ISourceFile> entry : sourceModelMap.entrySet()) {
            final String file = entry.getKey();
            final ISourceFile sourceFileModel2 = umlModel.getSourceFileModel(file);

            // Check if model2 contains the same file
            if (sourceFileModel2 != null) {
                ContainerDiffer sourceDiffer = new ContainerDiffer(entry.getValue(), sourceFileModel2, modelDiff);
                ContainerDiff sourceDiff = sourceDiffer.diff();
                sourceDiff.refactorings.addAll(sourceDiffer.getRefactorings());
                modelDiff.getRefactorings().addAll(sourceDiffer.getRefactorings());
            }
        }

        return modelDiff;
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
}
