package io.jsrminer.sourcetree;

import java.util.ArrayList;
import java.util.List;

public abstract class Invocation extends CodeEntity {
    public enum InvocationCoverageType {
        NONE, ONLY_CALL, RETURN_CALL, THROW_CALL, CAST_CALL, VARIABLE_DECLARATION_INITIALIZER_CALL;
    }

    protected String expression;
    protected List<String> arguments = new ArrayList<>();
    private String functionName;

    public String getExpression() {
        return expression;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * Returns true if the function names are equal
     */
    public boolean equalsInovkedFunctionName(Invocation invocation) {
        return this.functionName != null && this.functionName.equals(invocation.functionName);
    }

    @Override
    public String toString() {
        if (text != null)
            return text;

        StringBuilder sb = new StringBuilder();
        sb.append(functionName);
        sb.append("(");

        if (arguments != null) {
            String.join(", ", arguments);
        }
//        if (arguments != null && arguments.size() > 0) {
//            for (int i = 0; i < typeArguments - 1; i++)
//                sb.append("arg" + i).append(", ");
//            sb.append("arg" + (typeArguments - 1));
//        }
        sb.append(")");
        return sb.toString();
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public String actualString() {
        StringBuilder sb = new StringBuilder();
        if (expression != null) {
            sb.append(expression).append(".");
        }
        sb.append(functionName);
        sb.append("(");
        int size = arguments.size();
        if (size > 0) {
            for (int i = 0; i < size - 1; i++)
                sb.append(arguments.get(i)).append(",");
            sb.append(arguments.get(size - 1));
        }
        sb.append(")");
        return sb.toString();
    }
}
