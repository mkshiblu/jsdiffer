package io.jsrminer.uml;

import io.rminerx.core.api.ISourceFile;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstracts the source code of the whole code base
 */
public class UMLModel /*implements Diffable<UMLModel, UMLModelDiff>*/ {

    private LinkedHashMap<String, ISourceFile> sourceModelMap = new LinkedHashMap<>();
    private Set<String> repositoryDirectories = new LinkedHashSet();

    public boolean containsSourceFileModel(String file) {
        return sourceModelMap.containsKey(file);
    }

    public ISourceFile getSourceFileModel(String file) {
        return sourceModelMap.get(file);
    }

    public Map<String, ISourceFile> getSourceFileModels() {
        return sourceModelMap;
    }

    public Set<String> getRepositoryDirectories() {
        return repositoryDirectories;
    }
}
