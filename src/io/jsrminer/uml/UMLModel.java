package io.jsrminer.uml;

import io.jsrminer.api.Diffable;
import io.jsrminer.uml.diff.SourceFileModelDiff;
import io.jsrminer.uml.diff.SourceFileModelDiffer;
import io.jsrminer.uml.diff.UMLModelDiff;
import io.jsrminer.sourcetree.FunctionDeclaration;
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
                SourceFileModelDiffer sourceDiffer = new SourceFileModelDiffer(entry.getValue(), sourceFileModel2, modelDiff);
                SourceFileModelDiff sourceDiff = sourceDiffer.diff();
                sourceDiff.refactorings.addAll(sourceDiffer.getRefactorings());
                modelDiff.getRefactorings().addAll(sourceDiffer.getRefactorings());
            }
        }

        return modelDiff;
    }

    public FunctionDeclaration[] getFunctionDeclarationsInSource(String file) {
        ISourceFile sourceFileModel;
        if ((sourceFileModel = sourceModelMap.get(file)) != null) {
            return sourceFileModel.getFunctionDeclarations();
        }
        return null;
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
