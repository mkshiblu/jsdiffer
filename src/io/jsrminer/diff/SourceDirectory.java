package io.jsrminer.diff;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a source code folder
 */
public class SourceDirectory {

    public static final String[] SUPPORTED_EXTENSIONS = new String[]{"js"};

    private String directoryPath;

    /**
     * Cashe the mappings of source files so that it does not have to reload again from the desk
     */
    private Map<String, SourceFile> sourceFilesMapped;

    /**
     * Wraps the source directory
     */
    public SourceDirectory(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public Map<String, SourceFile> getSourceFiles() {
        if (sourceFilesMapped == null) {
            sourceFilesMapped = new HashMap<>();
            Collection<File> sourceFiles = FileUtils.listFiles(new File(directoryPath), SUPPORTED_EXTENSIONS, true);

            for (File file : sourceFiles) {
                SourceFile sourceFile = new SourceFile(file, directoryPath);
                sourceFilesMapped.put(sourceFile.getIdentifier(), sourceFile);
            }
        }
        return sourceFilesMapped;
    }

    /**
     * Returns the difference between two directories based on files
     */
    public SourceDirDiff diff(SourceDirectory sourceDirectory) {
        SourceDirDiff diff = new SourceDirDiff();
        Map<String, SourceFile> sourceMaps1 = getSourceFiles();
        Map<String, SourceFile> sourceMaps2 = sourceDirectory.getSourceFiles();

        for (SourceFile sourceFile : sourceMaps1.values()) {

            // Check if common files
            if (sourceMaps2.containsKey(sourceFile.getRelativePathToSourceDirectory())) {
                diff.addFileAtSameLocation(sourceFile);
            } else
                diff.addToDeletedFiles(sourceFile);
        }

        for (SourceFile sourceFile : sourceMaps2.values()) {
            if (!sourceMaps1.containsKey(sourceFile.getRelativePathToSourceDirectory())) {
                diff.addAddedFile(sourceFile);
            }
        }
        return diff;
    }
}
