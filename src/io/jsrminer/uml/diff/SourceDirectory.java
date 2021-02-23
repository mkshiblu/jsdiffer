package io.jsrminer.uml.diff;

import io.jsrminer.io.SourceFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;

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
            sourceFilesMapped = new LinkedHashMap<>();
            Collection<File> sourceFiles = FileUtils.listFiles(new File(directoryPath), SUPPORTED_EXTENSIONS, true);

            for (File file : sourceFiles) {
                SourceFile sourceFile = new SourceFile(file, directoryPath);
                sourceFilesMapped.put(sourceFile.getIdentifier(), sourceFile);
            }
        }
        return sourceFilesMapped;
    }

    public String[] getRelativeSourceFilePaths() {
        List<String> paths = new ArrayList<>(this.sourceFilesMapped.size());
        for (SourceFile file : this.sourceFilesMapped.values()) {
            paths.add(file.getPathFromSourceDirectory());
        }
        return paths.toArray(String[]::new);
    }

    public String[] getSourceFileAbsoultePaths() {
        List<String> paths = new ArrayList<>(this.sourceFilesMapped.size());
        for (SourceFile file : this.sourceFilesMapped.values()) {
            paths.add(file.getFile().getAbsolutePath());
        }
        return paths.toArray(String[]::new);
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
            if (sourceMaps2.containsKey(sourceFile.getPathFromSourceDirectory())) {
                diff.addFileAtSameLocation(sourceFile);
            } else
                diff.addToDeletedFiles(sourceFile);
        }

        for (SourceFile sourceFile : sourceMaps2.values()) {
            if (!sourceMaps1.containsKey(sourceFile.getPathFromSourceDirectory())) {
                diff.addAddedFile(sourceFile);
            }
        }
        return diff;
    }
}
