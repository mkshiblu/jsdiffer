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

    private String directorypath;

    private Map<String, SourceFile> sourceFilesMapped;

    public SourceDirectory(String directoryPath) {
        this.directorypath = directoryPath;
    }

    public Map<String, SourceFile> getSourceFiles() {
        if (sourceFilesMapped == null) {
            sourceFilesMapped = new HashMap<>();
            Collection<File> sourceFiles = FileUtils.listFiles(new File(directorypath), new String[]{"js"}, true);

            for (File file : sourceFiles) {
                SourceFile sourceFile = new SourceFile(file, directorypath);
                sourceFilesMapped.put(sourceFile.getIdentifier(), sourceFile);
            }
        }
        return sourceFilesMapped;
    }

    public SourceDirDiff diff(SourceDirectory sourceDirectory) {
        SourceDirDiff diff = new SourceDirDiff();
        Map<String, SourceFile> sourceMaps2 = sourceDirectory.getSourceFiles();

        for (SourceFile sourceFile : getSourceFiles().values()) {
            if (sourceMaps2.containsKey(sourceFile.getIdentifier())){
                diff.reportSourceFileAtSameLocation(sourceFile);
            }
        }
        return diff;
    }
}
