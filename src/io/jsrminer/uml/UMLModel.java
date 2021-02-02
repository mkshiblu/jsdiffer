package io.jsrminer.uml;

import io.rminerx.core.api.ISourceFile;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Abstracts the source code of the whole code base
 */
public class UMLModel /*implements Diffable<UMLModel, UMLModelDiff>*/ {

    private HashMap<String, ISourceFile> sourceModelMap;
    LinkedHashSet repositoryDirectories = new LinkedHashSet();

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
