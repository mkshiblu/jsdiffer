package io.jsrminer.refactorings;

import io.jsrminer.api.CodeRange;
import io.jsrminer.uml.diff.MovedClassToAnotherSourceFolder;
import io.jsrminer.uml.diff.RenamePattern;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;

public class MoveSourceFolderRefactoring extends Refactoring {
    private List<MovedClassToAnotherSourceFolder> movedClassesToAnotherSourceFolder = new ArrayList<>();
    private Map<String, String> identicalFilePaths = new HashMap<>();
    private RenamePattern pattern;

    public MoveSourceFolderRefactoring(RenamePattern pattern) {
        this.pattern = pattern;
    }

    public MoveSourceFolderRefactoring(MovedClassToAnotherSourceFolder movedClassToAnotherSourceFolder) {
        this.movedClassesToAnotherSourceFolder.add(movedClassToAnotherSourceFolder);
        this.pattern = movedClassToAnotherSourceFolder.getRenamePattern();
    }

    public void putIdenticalFilePaths(String filePathBefore, String filePathAfter) {
        identicalFilePaths.put(filePathBefore, filePathAfter);
    }

    public void addMovedClassToAnotherSourceFolder(MovedClassToAnotherSourceFolder movedClassToAnotherSourceFolder) {
        movedClassesToAnotherSourceFolder.add(movedClassToAnotherSourceFolder);
    }

    public List<MovedClassToAnotherSourceFolder> getMovedClassesToAnotherSourceFolder() {
        return movedClassesToAnotherSourceFolder;
    }

    public Map<String, String> getIdenticalFilePaths() {
        return identicalFilePaths;
    }

    public RenamePattern getPattern() {
        return pattern;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("\t");
        String originalPath = pattern.getBefore().endsWith("/") ? pattern.getBefore().substring(0, pattern.getBefore().length() - 1) : pattern.getBefore();
        sb.append(originalPath);
        sb.append(" to ");
        String movedPath = pattern.getAfter().endsWith("/") ? pattern.getAfter().substring(0, pattern.getAfter().length() - 1) : pattern.getAfter();
        sb.append(movedPath);
        return sb.toString();
    }

    public String getName() {
        return this.getRefactoringType().getDisplayName();
    }

    public RefactoringType getRefactoringType() {
        return RefactoringType.MOVE_SOURCE_FOLDER;
    }
}
