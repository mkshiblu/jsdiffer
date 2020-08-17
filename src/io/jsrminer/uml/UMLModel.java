package io.jsrminer.uml;

import io.jsrminer.api.Diffable;
import io.jsrminer.sourcetree.SourceFileModel;
import io.jsrminer.uml.diff.SourceFileModelDiff;
import io.jsrminer.uml.diff.SourceFileModelDiffer;
import io.jsrminer.uml.diff.UMLModelDiff;
import io.jsrminer.sourcetree.FunctionDeclaration;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstracts the source code
 */
public class UMLModel implements Diffable<UMLModel, UMLModelDiff> {

    private HashMap<String, SourceFileModel> sourceModelMap;

    @Override
    public UMLModelDiff diff(UMLModel umlModel) {
        final UMLModelDiff modelDiff = new UMLModelDiff(this, umlModel);

        // Diff on common files and functions
        for (Map.Entry<String, SourceFileModel> entry : sourceModelMap.entrySet()) {
            final String file = entry.getKey();
            final SourceFileModel sourceFileModel2 = umlModel.getSourceFileModel(file);

            // Check if model2 contains the same file
            if (sourceFileModel2 != null) {
                SourceFileModelDiffer sourceDiffer = new SourceFileModelDiffer(entry.getValue(), sourceFileModel2);
                SourceFileModelDiff sourceDiff = sourceDiffer.diff();
            }
        }

        return modelDiff;
    }

    public FunctionDeclaration[] getFunctionDeclarationsInSource(String file) {
        SourceFileModel sourceFileModel;
        if ((sourceFileModel = sourceModelMap.get(file)) != null) {
            return sourceFileModel.getFunctionDeclarations();
        }
        return null;
    }

    public boolean containsSourceFileModel(String file) {
        return sourceModelMap.containsKey(file);
    }

    public SourceFileModel getSourceFileModel(String file) {
        return sourceModelMap.get(file);
    }

    public Map<String, SourceFileModel> getSourceFileModels() {
        return sourceModelMap;
    }

    public void setSourceFileModels(final HashMap<String, SourceFileModel> sourceModelMap) {
        this.sourceModelMap = sourceModelMap;
    }
}
