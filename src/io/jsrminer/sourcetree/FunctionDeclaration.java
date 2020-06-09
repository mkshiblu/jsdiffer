package io.jsrminer.sourcetree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class FunctionDeclaration extends CodeElement {
    private String[] parameters;

    /**
     * The name of the function.
     */
    public final String name;

    /**
     * Qualified name excluding the filename but including the parent function name.
     * For example if function y() is declared inside x(), it will return x.y.
     */
    public final String qualifiedName;


    /**
     * Fully Qualified name including the filename, parent function name if any.
     * For example if function y() is declared inside x() in file f.js, it will return f.x.y.
     */
    private String fullyQualifiedName;

    private String body;

    public FunctionDeclaration(String qualifiedName) {
        this.qualifiedName = qualifiedName;
        int idx = qualifiedName.lastIndexOf('.');
        if (idx != -1) {
            name = qualifiedName.substring(idx + 1);
        } else {
            name = qualifiedName;
        }
    }

//
//    /**
//     * Fully Qualified name including the filename, parent function name if any.
//     * For example if function y() is declared inside x() in file f.js, it will return f.x.y.
//     * From this, the name and the qualified names are extracted
//     */
//    public FunctionDeclaration(String fullyQualifiedName) {
//        this.fullyQualifiedName = fullyQualifiedName;
//
//        int idx = fullyQualifiedName.lastIndexOf('.');
//        if (idx != -1) {
//            name = fullyQualifiedName.substring(idx + 1);
//        } else {
//            name = fullyQualifiedName;
//        }
//
//        if ((idx = fullyQualifiedName.indexOf('.')) != -1)
//            qualifiedName = fullyQualifiedName.substring(idx + 1);
//        else {
//            qualifiedName = fullyQualifiedName;
//        }
//    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public String[] getParameters() {
        return parameters;
    }

//    @Override
//    public String toString() {
//        return fullyQualifiedName;
//    }

    @Override
    public String toString() {
        return qualifiedName;
    }

    @Override
    public void setLocation(SourceLocation location) {
        super.setLocation(location);
        fullyQualifiedName = location.getFile() + "|" + qualifiedName;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
