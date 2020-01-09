package io.jsrminer.diff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the difference between two folders of their files
 * Contains, added, deleted and common files between the two directories
 */
public class SourceDirDiff extends Diff {

    private Set<SourceFile> commonSourceFiles = new HashSet<>();
    private Set<SourceFile> addedFiles = new HashSet<>();
    private Set<SourceFile> deletedFiles = new HashSet<>();

    void addAddedFile(SourceFile sourceFile) {
        addedFiles.add(sourceFile);
    }

    void addToDeletedFiles(SourceFile sourceFile) {
        deletedFiles.add(sourceFile);
    }

    void addFileAtSameLocation(SourceFile sourceFile) {
        commonSourceFiles.add(sourceFile);
    }

    // TODO using git?
    void checkForMovedFiles() {

    }

    // TODO using git
    void checkForRenamedFiles() {

    }

    public SourceFile[] getCommonSourceFiles() {
        return commonSourceFiles.toArray(SourceFile[]::new);
    }

    public SourceFile[] getAddedFiles() {
        return addedFiles.toArray(SourceFile[]::new);
    }

    public SourceFile[] getDeletedFiles() {
        return deletedFiles.toArray(SourceFile[]::new);
    }
}
