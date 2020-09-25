package io.jsrminer.sourcetree;

import java.util.LinkedHashSet;
import java.util.Set;

public class OperationInvocation extends Invocation {
    public OperationInvocation() {

    }

    public boolean differentExpressionNameAndArguments(OperationInvocation other) {
        boolean differentExpression = false;
        if (this.expression == null && other.expression != null)
            differentExpression = true;
        if (this.expression != null && other.expression == null)
            differentExpression = true;
        if (this.expression != null && other.expression != null)
            differentExpression = !this.expression.equals(other.expression) &&
                    !this.expression.startsWith(other.expression) && !other.expression.startsWith(this.expression);
        boolean differentName = !this.equalsInovkedFunctionName(other);
        Set<String> argumentIntersection = new LinkedHashSet<String>(this.arguments);
        argumentIntersection.retainAll(other.arguments);
        boolean argumentFoundInExpression = false;
        if (this.expression != null) {
            for (String argument : other.arguments) {
                if (this.expression.contains(argument)) {
                    argumentFoundInExpression = true;
                }
            }
        }
        if (other.expression != null) {
            for (String argument : this.arguments) {
                if (other.expression.contains(argument)) {
                    argumentFoundInExpression = true;
                }
            }
        }
        boolean differentArguments = !this.arguments.equals(other.arguments) &&
                argumentIntersection.isEmpty() && !argumentFoundInExpression;
        return differentExpression && differentName && differentArguments;
    }
}
