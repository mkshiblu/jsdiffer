package io.jsrminer.diff;

import java.util.ArrayList;
import java.util.List;

public class SourceDirDiff {

    private List<SourceFile> commonSourceFiles = new ArrayList<>();

    void reportAddedSourceFile(SourceFile sourceFile) {

    }

    void reportRemovedSourceFile(SourceFile sourceFile) {

    }

    void reportSourceFileAtSameLocation(SourceFile sourceFile) {
        commonSourceFiles.add(sourceFile);
    }

    void checkForMovedFiles() {

    }

    void checkForRenamedFiles() {

    }

    public SourceFile[] getCommonSourceFiles() {
        return commonSourceFiles.toArray(SourceFile[]::new);
    }
}
