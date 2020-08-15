package io.jsrminer.uml;

import io.jsrminer.api.Diffable;
import io.jsrminer.sourcetree.SourceFileModel;
import io.jsrminer.uml.diff.UMLModelDiff;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.diff.UMLModelDiffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstracts the source code
 */
public class UMLModel implements Diffable<UMLModel, UMLModelDiff> {

    private HashMap<String, SourceFileModel> sourceModelMap;

    @Override
    public UMLModelDiff diff(UMLModel umlModel) {
        UMLModelDiffer modelDiffer = new UMLModelDiffer();
        UMLModelDiff modelDiff = modelDiffer.diff(this, umlModel);
        return modelDiff;
    }

    public FunctionDeclaration[] getFunctionDeclarationsInSourceFile(String file) {
        SourceFileModel sourceModel;
        if ((sourceModel = sourceModelMap.get(file)) != null) {
            return sourceModel.getFunctionDeclarations();
        }
        return null;
    }

    public Map<String, SourceFileModel> getSourceFileModels() {
        return sourceModelMap;
    }

    public void setSourceFileContents(final HashMap<String, SourceFileModel> sourceModelMap) {
        this.sourceModelMap = sourceModelMap;
    }
}
