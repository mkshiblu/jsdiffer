package io.jsrminer.sourcetree;

import java.util.List;

public class OperationInvocation extends Invocation {
    private String functionName;

    protected String expression;
    protected List<String> arguments;

    public OperationInvocation() {

    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }


    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
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
}
