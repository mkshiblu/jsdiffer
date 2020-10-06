package io.jsrminer.sourcetree;

import io.jsrminer.uml.UMLParameter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FunctionDeclaration extends CodeFragment {

    /**
     * Name parameter map
     */
    private Map<String, UMLParameter> nameParameterMap = new HashMap<>();
    /**
     * The name of the function.
     */
    public final String name;

    /**
     * Qualified name excluding the filename but including the parent function name.
     * For example if function y() is declared inside x(), it will return x.y.
     */
    public final String qualifiedName;

    public final String namespace;

    /**
     * Fully Qualified name including the filename, parent function name if any.
     * For example if function y() is declared inside x() in file f.js, it will return f.x.y.
     */
    private String fullyQualifiedName;

    /**
     * Holds the body of the function
     */
    private FunctionBody body;

    /**
     * Stores whether the body of the function is empty or not
     */
    private boolean isEmptyBody;

    /**
     * Stores whether this function is a 'Top-Level' i.e. declared directly inside a
     * file and not nested
     */
    public final boolean isTopLevel;

    public FunctionDeclaration(String qualifiedName, boolean isTopLevel) {
        this.isTopLevel = isTopLevel;
        this.qualifiedName = qualifiedName;
        int idx = qualifiedName.lastIndexOf('.');
        if (idx != -1) {
            name = qualifiedName.substring(idx + 1);
            namespace = qualifiedName.substring(0, idx);
        } else {
            name = qualifiedName;
            namespace = null;
        }
    }

    @Override
    public String toString() {
        return qualifiedName;
    }

    @Override
    public void setSourceLocation(SourceLocation sourceLocation) {
        super.setSourceLocation(sourceLocation);
        fullyQualifiedName = sourceLocation.getFile() + "|" + qualifiedName;
    }

    public boolean hasIdenticalBody(FunctionDeclaration fd) {
        return this.body.equals(fd.body);
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    // region Setters & getters
    public Map<String, UMLParameter> getParameters() {
        return nameParameterMap;
    }

    public void setParameters(UMLParameter[] parameters) {
        this.nameParameterMap.clear();
        LinkedHashMap<String, String> x;
        UMLParameter parameter;
        for (int i = 0; i < parameters.length; i++) {
            parameter = parameters[i];
            parameter.setIndexPositionInParent(i);
            nameParameterMap.put(parameter.name, parameter);
        }
    }

    public void setBody(FunctionBody body) {
        this.body = body;
    }

    public FunctionBody getBody() {
        return body;
    }

    public void setIsEmptyBody(boolean isEmptyBody) {
        this.isEmptyBody = isEmptyBody;
    }
    //endregion

    public boolean hasParameterOfName(String name) {
        return nameParameterMap.containsKey(name);
    }

    public UMLParameter getParameter(String parameterName) {
        return nameParameterMap.get(parameterName);
    }

    public boolean nameEquals(FunctionDeclaration function) {
        return this.name != null && this.name.equals(function.name);
    }

    public int parameterCount(){
        return this.nameParameterMap.size();
    }
}
