package io.jsrminer.sourcetree;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for all the code elements that has apis  to provide all the variables, identifiers appeared in the code
 */
public abstract class CodeFragment extends CodeEntity {
    //    protected SourceLocation sourceLocation;
//    protected String text;
//    protected CodeElementType type;
    protected int positionIndexInParent = -1;
    protected int depth = -1;

    public CodeFragment() {
    }

    public CodeFragment(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        if (text == null)
            return super.toString();
        return text;
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

    public void setPositionIndexInParent(int positionIndexInParent) {
        this.positionIndexInParent = positionIndexInParent;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

//    public SourceLocation getSourceLocation() {
//        return sourceLocation;
//    }
//
//    public void setSourceLocation(SourceLocation sourceLocation) {
//        this.sourceLocation = sourceLocation;
//    }
//
//    public String getFile() {
//        return sourceLocation.getFile();
//    }
//
//    public String getText() {
//        return text;
//    }
//
//    public void setText(String text) {
//        this.text = text;
//    }
//
//    public void setType(CodeElementType type) {
//        this.type = type;
//    }
//
//    /**
//     * Returns the Code Element type of this fragment
//     */
//    public CodeElementType getType() {
//        return type;
//    }

    // region API
    public abstract List<String> getVariables();

    public abstract Map<String, List<OperationInvocation>> getMethodInvocationMap();

    public abstract Map<String, List<ObjectCreation>> getCreationMap();

    public abstract List<String> getStringLiterals();

    public abstract List<String> getNumberLiterals();

    public abstract List<String> getNullLiterals();

    public abstract List<String> getBooleanLiterals();

    public abstract List<String> getInfixOperators();

    public abstract List<String> getArrayAccesses();

    public abstract List<String> getPrefixExpressions();

    public abstract List<String> getIdentifierArguments();

    public abstract List<VariableDeclaration> getVariableDeclarations();

    public abstract VariableDeclaration getVariableDeclaration(String variableName);

    public abstract VariableDeclaration findVariableDeclarationIncludingParent(String varibleName);

    //endregion
}
