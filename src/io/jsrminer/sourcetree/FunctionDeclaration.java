package io.jsrminer.sourcetree;

import io.jsrminer.uml.UMLParameter;
import io.rminerx.core.api.IAnonymousFunctionDeclaration;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.entities.DeclarationContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionDeclaration extends DeclarationContainer implements IFunctionDeclaration {

    /**
     * Name parameter map
     */
    private List<UMLParameter> parameters = new ArrayList<>();

    //public final String namespace;

    /**
     * Holds the body of the function
     */
    private FunctionBody body;

    /**
     * Stores whether the body of the function is empty or not
     */
    private boolean emptyBody;

    /**
     * True if the function is also a constructor
     */
    private boolean isConstructor;

    private boolean isStatic;

    public FunctionDeclaration() {
    }

    public void setSourceLocation(SourceLocation sourceLocation) {
        super.sourceLocation = sourceLocation;
    }

    public boolean hasIdenticalBody(FunctionDeclaration fd) {
        return this.body.equals(fd.body);
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
        this.emptyBody = isEmptyBody;
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
        return getName() != null && getName().equals(function.getName());
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
        return getSignatureText();
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

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public boolean isStatic() {
        return isStatic;
    }

    /**
     * Add paramter
     *
     * @return
     */
    public void registerParameter(UMLParameter parameter) {
        this.getParameters().add(parameter);
    }

    @Override
    public List<Statement> getStatements() {
        return body.blockStatement.getStatements();
    }

    @Override
    public boolean hasEmptyBody() {
        return this.emptyBody;
    }

    public void setEmptyBody(boolean emptyBody) {
        this.emptyBody = emptyBody;
    }

    @Override
    public String getSignatureText() {
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
}
