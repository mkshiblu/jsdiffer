package io.jsrminer.io;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SourceFile {
    private File file;
    private File sourceDirectory;
    private String relativePathToSourceDirectory;

    SourceFile(File file, String srcDirPath) {
        this.file = file;
        String path = file.getPath();
        int prefixIndex = path.indexOf(srcDirPath) + srcDirPath.length();
        relativePathToSourceDirectory =  path.substring(prefixIndex);
    }

    public String getRelativePathToSourceDirectory() {
        return relativePathToSourceDirectory;
    }

    /**
     * An identifier which is unique inside the uppermost source directory
     */
    public String getIdentifier() {
        return relativePathToSourceDirectory;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return file.toString();
    }
}
