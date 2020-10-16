package io.jsrminer.sourcetree;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for all the code elements
 */
public abstract class CodeFragment {
    protected SourceLocation sourceLocation;
    protected String text;
    protected CodeElementType type;
    protected int positionIndexInParent = -1;
    protected int depth = -1;
    protected Statement parent;

    public CodeFragment() {
    }

    public CodeFragment(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public boolean equalsSourceLocation(CodeFragment test) {
        if (this.getFile() == null) {
            if (test.getFile() != null)
                return false;
        } else if (!this.getFile().equals(test.getFile()))
            return false;

        return this.sourceLocation.equalsLineAndColumn(test.sourceLocation);
    }

    /**
     * Returns the nesting depth from the original declaring scope such as function body
     */
    public int getDepth() {
        return depth;
    }

    public int getPositionIndexInParent() {
        return positionIndexInParent;
    }

    public Statement getParent() {
        return parent;
    }

    public void setParent(Statement parent) {
        this.parent = parent;
    }

    public void setPositionIndexInParent(int positionIndexInParent) {
        this.positionIndexInParent = positionIndexInParent;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(SourceLocation sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getFile() {
        return sourceLocation.getFile();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setType(CodeElementType type) {
        this.type = type;
    }

    /**
     * Returns the Code Element type of this fragment
     */
    public CodeElementType getType() {
        return type;
    }

    // region API
    public abstract Set<String> getVariables();

    public abstract Map<String, List<OperationInvocation>> getMethodInvocationMap();

    public abstract Map<String, List<ObjectCreation>> getCreationMap();

    public abstract List<String> getStringLiterals();

    public abstract List<String> getNumberLiterals();

    public abstract List<String> getNullLiterals();

    public abstract List<String> getBooleanLiterals();

    public abstract List<String> getInfixOperators();

    public abstract List<String> getArrayAccesses();

    public abstract List<String> getPrefixExpressions();

    public abstract Set<String> getIdentifierArguments();

    public abstract List<VariableDeclaration> getVariableDeclarations();

    public abstract VariableDeclaration getVariableDeclaration(String variableName);

    public abstract VariableDeclaration findVariableDeclarationIncludingParent(String varibleName);

    //endregion
}
