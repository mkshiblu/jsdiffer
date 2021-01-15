package io.jsrminer.sourcetree;

import io.jsrminer.uml.UMLParameter;
import io.rminer.core.api.IAnonymousFunctionDeclaration;
import io.rminer.core.api.IFunctionDeclaration;
import io.rminer.core.entities.DeclarationContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionDeclaration extends DeclarationContainer implements IFunctionDeclaration {

    /**
     * The name of the function.
     */
    private String name;
    /**
     * Name parameter map
     */
    private List<UMLParameter> parameters = new ArrayList<>();

    //public final String namespace;

    /**
     * Fully Qualified name including the filename, parent function name if any.
     * For example if function y() is declared inside x() in file f.js, it will return f.x.y.
     */
    private String fullyQualifiedName;

    /**
     * Holds the body of the function
     */
    private FunctionBody body;

    private String containerName;
    /**
     * Stores whether the body of the function is empty or not
     */
    private boolean isEmptyBody;

    /**
     * Stores whether this function is a 'Top-Level' i.e. declared directly inside a
     * file and not nested
     */
    private boolean isTopLevel;

    /**
     * True if the function is also a constructor
     */
    private boolean isConstructor;

    public FunctionDeclaration() {
    }

    public void setSourceLocation(SourceLocation sourceLocation) {
        super.sourceLocation = sourceLocation;
    }

    public boolean hasIdenticalBody(FunctionDeclaration fd) {
        return this.body.equals(fd.body);
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public List<String> getParameterNameList() {
        return getParameters().stream().map(parameter -> parameter.name).collect(Collectors.toList());
    }

    // region Setters & getters
    public List<UMLParameter> getParameters() {
        return parameters;
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
        return parameters.stream().anyMatch(p -> p.name.equals(name));
    }

    public UMLParameter getParameter(String parameterName) {
        return parameters.stream()
                .filter(p -> parameterName.equals(p.name))
                .findAny()
                .orElse(null);
    }

    public boolean nameEquals(FunctionDeclaration function) {
        return this.name != null && this.name.equals(function.name);
    }

    public int parameterCount() {
        return this.parameters.size();
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public void setIsConstructor(boolean constructor) {
        isConstructor = constructor;
    }

    public void setIsTopLevel(boolean isTopLevel) {
        this.isTopLevel = isTopLevel;
    }

    public boolean isTopLevel() {
        return isTopLevel;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    /**
     * The name of the function.
     */
    public String getName() {
        return name;
    }

    /**
     * The name of the function.
     */
    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAllVariables() {
        if (this.getBody() != null)
            return this.getBody().blockStatement.getAllVariablesIncludingNested();
        return new ArrayList<>();
    }

    public List<VariableDeclaration> getAllVariableDeclarations() {
        if (body != null)
            return body.blockStatement.getAllVariableDeclarations();
        return new ArrayList<>();
    }

    public BlockStatement loopWithVariables(String currentElementName, String collectionName) {
        return body.blockStatement.loopWithVariables(currentElementName, collectionName);
    }

    public VariableDeclaration getVariableDeclaration(String variableName) {
        return body.blockStatement.getVariableDeclaration(variableName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getQualifiedName());
        sb.append('(');
        String commaSeparatedParams = this.getParameters()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" ,"));
        sb.append(commaSeparatedParams);
        sb.append(')');
        return sb.toString();
    }

    public List<FunctionDeclaration> getOperationsInsideAnonymousFunctionDeclarations(List<IAnonymousFunctionDeclaration> allAddedAnonymousClasses) {
        List<FunctionDeclaration> operationsInsideAnonymousClass = new ArrayList<>();
        if (this.body != null) {
            List<IAnonymousFunctionDeclaration> anonymousClassDeclarations = this.getBody().blockStatement.getAllAnonymousFunctionDeclarations();
            for (IAnonymousFunctionDeclaration anonymousClassDeclaration : anonymousClassDeclarations) {
                for (IAnonymousFunctionDeclaration anonymousFunction : allAddedAnonymousClasses) {
                    if (anonymousFunction.getSourceLocation().equals(anonymousClassDeclaration.getSourceLocation())) {
                        anonymousFunction.getFunctionDeclarations().forEach(iaf -> {
                            operationsInsideAnonymousClass.add((FunctionDeclaration) iaf);
                        });
                    }
                }
            }
        }
        return operationsInsideAnonymousClass;
    }
}
