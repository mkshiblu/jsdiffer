package io.rminer.core.api;

public interface ISourceFile extends IContainer {
    String getFilepath();
    String getName();
    String getDirectoryPath();
    String getDirectoryName();
    double normalizedSourceFolderDistance(ISourceFile c);
}
