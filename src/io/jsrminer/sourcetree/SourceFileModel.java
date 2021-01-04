package io.jsrminer.sourcetree;

// Represents a single source file
public class SourceFileModel  {
    public final String path;
    private FunctionDeclaration[] functionDeclarations;

    public SourceFileModel() {
        path = null;
    }

    public SourceFileModel(String path) {
        this.path = path;
    }

    public void setFunctionDeclarations(FunctionDeclaration[] functionDeclarations){
        this.functionDeclarations = functionDeclarations;
    }

    public FunctionDeclaration[] getFunctionDeclarations() {
        return functionDeclarations;
    }

    public void diff(SourceFileModel sourceFileModel){

    }
}
