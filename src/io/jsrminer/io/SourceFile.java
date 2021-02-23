package io.jsrminer.io;

import java.io.File;

public class SourceFile {
    private final File file;
    private final String pathFromSourceDirectory;

    public SourceFile(String filePath) {
        this(new File(filePath), null);
    }

    public SourceFile(File file, String srcDirPath) {
        this.file = file;
        String path = file.getPath();
        if (srcDirPath == null) {
            pathFromSourceDirectory = file.getName();
        } else {
            int prefixIndex = path.indexOf(srcDirPath) + srcDirPath.length() + 1;
            pathFromSourceDirectory = path.substring(prefixIndex);
        }
    }

    public String getPathFromSourceDirectory() {
        return pathFromSourceDirectory;
    }

    /**
     * An identifier which is unique inside the uppermost source directory
     */
    public String getIdentifier() {
        return pathFromSourceDirectory;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return file.toString();
    }
}
