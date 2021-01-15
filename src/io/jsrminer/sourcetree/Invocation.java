package io.jsrminer.sourcetree;

import io.jsrminer.uml.mapping.replacement.MergeVariableReplacement;
import io.jsrminer.uml.mapping.replacement.Replacement;
import io.jsrminer.uml.mapping.replacement.ReplacementType;
import io.jsrminer.uml.mapping.replacement.ReplacementUtil;

import java.util.*;

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

    public abstract boolean identicalName(Invocation call);

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

    public boolean equalArguments(Invocation call) {
        return getArguments().equals(call.getArguments());
    }

    public boolean identicalWithDifferentNumberOfArguments(Invocation call, Set<Replacement> replacements, Map<String, String> parameterToArgumentMap) {
        if (onlyArgumentsChanged(call, replacements)) {
            int argumentIntersectionSize = argumentIntersectionSize(call, parameterToArgumentMap);
            if (argumentIntersectionSize > 0 || getArguments().size() == 0 || call.getArguments().size() == 0) {
                return true;
            }
        }
        return false;
    }

    private int argumentIntersectionSize(Invocation call, Map<String, String> parameterToArgumentMap) {
        Set<String> argumentIntersection = argumentIntersection(call);
        int argumentIntersectionSize = argumentIntersection.size();
        for (String parameter : parameterToArgumentMap.keySet()) {
            String argument = parameterToArgumentMap.get(parameter);
            if (getArguments().contains(argument) &&
                    call.getArguments().contains(parameter)) {
                argumentIntersectionSize++;
            }
        }
        return argumentIntersectionSize;
    }

    public Set<String> argumentIntersection(Invocation call) {
        List<String> args1 = preprocessArguments(getArguments());
        List<String> args2 = preprocessArguments(call.getArguments());
        Set<String> argumentIntersection = new LinkedHashSet<String>(args1);
        argumentIntersection.retainAll(args2);
        return argumentIntersection;
    }

    private List<String> preprocessArguments(List<String> arguments) {
        List<String> args = new ArrayList<>();
        for (String arg : arguments) {
            if (arg.contains("\n")) {
                args.add(arg.substring(0, arg.indexOf("\n")));
            } else {
                args.add(arg);
            }
        }
        return args;
    }

    public boolean identicalWithMergedArguments(Invocation call, Set<Replacement> replacements) {
        if (onlyArgumentsChanged(call, replacements)) {
            List<String> updatedArguments1 = new ArrayList<>(this.arguments);
            Map<String, Set<Replacement>> commonVariableReplacementMap = new LinkedHashMap<>();
            for (Replacement replacement : replacements) {
                if (replacement.getType().equals(ReplacementType.VARIABLE_NAME)) {
                    String key = replacement.getAfter();
                    if (commonVariableReplacementMap.containsKey(key)) {
                        commonVariableReplacementMap.get(key).add(replacement);
                        int index = updatedArguments1.indexOf(replacement.getBefore());
                        if (index != -1) {
                            updatedArguments1.remove(index);
                        }
                    } else {
                        Set<Replacement> r = new LinkedHashSet<Replacement>();
                        r.add(replacement);
                        commonVariableReplacementMap.put(key, r);
                        int index = updatedArguments1.indexOf(replacement.getBefore());
                        if (index != -1) {
                            updatedArguments1.remove(index);
                            updatedArguments1.add(index, key);
                        }
                    }
                }
            }
            if (updatedArguments1.equals(call.arguments)) {
                for (String key : commonVariableReplacementMap.keySet()) {
                    Set<Replacement> r = commonVariableReplacementMap.get(key);
                    if (r.size() > 1) {
                        replacements.removeAll(r);
                        Set<String> mergedVariables = new LinkedHashSet<String>();
                        for (Replacement replacement : r) {
                            mergedVariables.add(replacement.getBefore());
                        }
                        MergeVariableReplacement merge = new MergeVariableReplacement(mergedVariables, key);
                        replacements.add(merge);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean onlyArgumentsChanged(Invocation call, Set<Replacement> replacements) {
        return identicalExpression(call, replacements) &&
                identicalName(call) &&
                !equalArguments(call) &&
                getArguments().size() != call.getArguments().size();
    }

    public boolean identicalExpression(Invocation call, Set<Replacement> replacements) {
        return identicalExpression(call) ||
                identicalExpressionAfterTypeReplacements(call, replacements);
    }

    private boolean identicalExpressionAfterTypeReplacements(Invocation call, Set<Replacement> replacements) {
        if (getExpression() != null && call.getExpression() != null) {
            String expression1 = getExpression();
            String expression2 = call.getExpression();
            String expression1AfterReplacements = new String(expression1);
            for (Replacement replacement : replacements) {
                if (replacement.getType().equals(ReplacementType.TYPE)) {
                    expression1AfterReplacements = ReplacementUtil.performReplacement(expression1AfterReplacements, expression2, replacement.getBefore(), replacement.getAfter());
                }
            }
            if (expression1AfterReplacements.equals(expression2)) {
                return true;
            }
        }
        return false;
    }

    public boolean identicalExpression(Invocation call) {
        return (getExpression() != null && call.getExpression() != null &&
                getExpression().equals(call.getExpression())) ||
                (getExpression() == null && call.getExpression() == null);
    }

    public boolean identical(Invocation call, Set<Replacement> replacements) {
        return identicalExpression(call, replacements) &&
                identicalName(call) &&
                equalArguments(call);
    }
}
