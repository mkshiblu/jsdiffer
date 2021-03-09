package io.rminerx.core.api;

public interface ISourceFile extends IContainer {
    String getFilepath();

    String getName();

    /**
     * Returns the path of the directory where this file is
     */
    String getDirectoryPath();

    String getDirectoryName();

    double normalizedSourceFolderDistance(ISourceFile c);
}
