package io.rminer.core.api;

public interface ISourceFile extends IContainer {
    String getFilepath();
    String getName();
    String getDirectoryPath();
    double normalizedSourceFolderDistance(ISourceFile c);
}
