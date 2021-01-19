package io.jsrminer.sourcetree;

import io.jsrminer.uml.diff.ContainerDiff;
import io.jsrminer.uml.diff.ContainerDiffer;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.mapping.replacement.MergeVariableReplacement;
import io.jsrminer.uml.mapping.replacement.Replacement;
import io.jsrminer.uml.mapping.replacement.ReplacementType;
import io.jsrminer.uml.mapping.replacement.ReplacementUtil;

import java.util.*;

public abstract class Invocation extends CodeEntity {
    public enum InvocationCoverageType {
        NONE, ONLY_CALL, RETURN_CALL, THROW_CALL, CAST_CALL, VARIABLE_DECLARATION_INITIALIZER_CALL;
    }

    public abstract double normalizedNameDistance(Invocation call);

    protected String expressionText;
    protected List<String> arguments = new ArrayList<>();
    private String functionName;

    public String getExpressionText() {
        return expressionText;
    }

    public String getName() {
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

    public void setExpressionText(String expressionText) {
        this.expressionText = expressionText;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public String actualString() {
        StringBuilder sb = new StringBuilder();
        if (expressionText != null) {
            sb.append(expressionText).append(".");
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
        if (getExpressionText() != null && call.getExpressionText() != null) {
            String expression1 = getExpressionText();
            String expression2 = call.getExpressionText();
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
        return (getExpressionText() != null && call.getExpressionText() != null &&
                getExpressionText().equals(call.getExpressionText())) ||
                (getExpressionText() == null && call.getExpressionText() == null);
    }

    public boolean identical(Invocation call, Set<Replacement> replacements) {
        return identicalExpression(call, replacements) &&
                identicalName(call) &&
                equalArguments(call);
    }

    public boolean identicalOrReplacedArguments(Invocation call, Set<Replacement> replacements) {
        List<String> arguments1 = getArguments();
        List<String> arguments2 = call.getArguments();
        if (arguments1.size() != arguments2.size())
            return false;
        for (int i = 0; i < arguments1.size(); i++) {
            String argument1 = arguments1.get(i);
            String argument2 = arguments2.get(i);
            boolean argumentReplacement = false;
            for (Replacement replacement : replacements) {
                if (replacement.getBefore().equals(argument1) && replacement.getAfter().equals(argument2)) {
                    argumentReplacement = true;
                    break;
                }
            }
            if (!argument1.equals(argument2) && !argumentReplacement)
                return false;
        }
        return true;
    }

    public boolean identicalOrWrappedArguments(Invocation call) {
        List<String> arguments1 = getArguments();
        List<String> arguments2 = call.getArguments();
        if (arguments1.size() != arguments2.size())
            return false;
        for (int i = 0; i < arguments1.size(); i++) {
            String argument1 = arguments1.get(i);
            String argument2 = arguments2.get(i);
            boolean argumentWrapped = false;
            if (argument1.contains("(" + argument2 + ")") ||
                    argument2.contains("(" + argument1 + ")")) {
                argumentWrapped = true;
            }
            if (!argument1.equals(argument2) && !argumentWrapped)
                return false;
        }
        return true;
    }

    public boolean renamedWithIdenticalExpressionAndArguments(Invocation call, Set<Replacement> replacements, double distance) {
        boolean identicalOrReplacedArguments = identicalOrReplacedArguments(call, replacements);
        boolean allArgumentsReplaced = allArgumentsReplaced(call, replacements);
        return getExpressionText() != null && call.getExpressionText() != null &&
                identicalExpression(call, replacements) &&
                !identicalName(call) &&
                (equalArguments(call) || (allArgumentsReplaced && normalizedNameDistance(call) <= distance) || (identicalOrReplacedArguments && !allArgumentsReplaced));
    }

    public boolean allArgumentsReplaced(Invocation call, Set<Replacement> replacements) {
        int replacedArguments = 0;
        List<String> arguments1 = getArguments();
        List<String> arguments2 = call.getArguments();
        if (arguments1.size() == arguments2.size()) {
            for (int i = 0; i < arguments1.size(); i++) {
                String argument1 = arguments1.get(i);
                String argument2 = arguments2.get(i);
                for (Replacement replacement : replacements) {
                    if (replacement.getBefore().equals(argument1) && replacement.getAfter().equals(argument2)) {
                        replacedArguments++;
                        break;
                    }
                }
            }
        }
        return replacedArguments > 0 && replacedArguments == arguments1.size();
    }

    public boolean identicalOrConcatenatedArguments(Invocation call) {
        List<String> arguments1 = getArguments();
        List<String> arguments2 = call.getArguments();
        if (arguments1.size() != arguments2.size())
            return false;
        for (int i = 0; i < arguments1.size(); i++) {
            String argument1 = arguments1.get(i);
            String argument2 = arguments2.get(i);
            boolean argumentConcatenated = false;
            if ((argument1.contains("+") || argument2.contains("+")) && !argument1.contains("++") && !argument2.contains("++")) {
                Set<String> tokens1 = new LinkedHashSet<String>(Arrays.asList(argument1.split(JsConfig.SPLIT_CONCAT_STRING_PATTERN)));
                Set<String> tokens2 = new LinkedHashSet<String>(Arrays.asList(argument2.split(JsConfig.SPLIT_CONCAT_STRING_PATTERN)));
                Set<String> intersection = new LinkedHashSet<String>(tokens1);
                intersection.retainAll(tokens2);
                int size = intersection.size();
                int threshold = Math.max(tokens1.size(), tokens2.size()) - size;
                if (size > 0 && size >= threshold) {
                    argumentConcatenated = true;
                }
            }
            if (!argument1.equals(argument2) && !argumentConcatenated)
                return false;
        }
        return true;
    }

    public boolean renamedWithIdenticalArgumentsAndNoExpression(Invocation call, double distance, List<FunctionBodyMapper> lambdaMappers) {
        boolean allExactLambdaMappers = lambdaMappers.size() > 0;
        for (FunctionBodyMapper lambdaMapper : lambdaMappers) {
            if (!ContainerDiff.allMappingsAreExactMatches(lambdaMapper)) {
                allExactLambdaMappers = false;
                break;
            }
        }
        return this.getExpressionText() == null && call.getExpressionText() == null &&
                !identicalName(call) &&
                (normalizedNameDistance(call) <= distance || allExactLambdaMappers) &&
                equalArguments(call);
    }

    public boolean renamedWithDifferentExpressionAndIdenticalArguments(Invocation call) {
        return (this.getName().contains(call.getName()) || call.getName().contains(this.getName())) &&
                equalArguments(call) && this.arguments.size() > 0 &&
                ((this.getExpressionText() == null && call.getExpressionText() != null)
                        || (call.getExpressionText() == null && this.getExpressionText() != null));
    }

    public boolean renamedWithIdenticalExpressionAndDifferentNumberOfArguments(Invocation call, Set<Replacement> replacements, double distance, List<FunctionBodyMapper> lambdaMappers) {
        boolean allExactLambdaMappers = lambdaMappers.size() > 0;
        for (FunctionBodyMapper lambdaMapper : lambdaMappers) {
            if (!ContainerDiffer.allMappingsAreExactMatches(lambdaMapper)) {
                allExactLambdaMappers = false;
                break;
            }
        }
        return getExpressionText() != null && call.getExpressionText() != null &&
                identicalExpression(call, replacements) &&
                (normalizedNameDistance(call) <= distance || allExactLambdaMappers) &&
                !equalArguments(call) &&
                getArguments().size() != call.getArguments().size();
    }

    public Replacement makeReplacementForReturnedArgument(String statement) {
        if (argumentIsReturned(statement)) {
            return new Replacement(getArguments().get(0), statement.substring(7, statement.length() - 2),
                    ReplacementType.ARGUMENT_REPLACED_WITH_RETURN_EXPRESSION);
        } else if (argumentIsEqual(statement)) {
            return new Replacement(getArguments().get(0), statement.substring(0, statement.length() - 2),
                    ReplacementType.ARGUMENT_REPLACED_WITH_STATEMENT);
        }
        return null;
    }

    private boolean argumentIsReturned(String statement) {
        return statement.startsWith("return ") && getArguments().size() == 1 &&
                //length()-2 to remove ";\n" from the end of the return statement, 7 to remove the prefix "return "
                equalsIgnoringExtraParenthesis(getArguments().get(0), statement.substring(7, statement.length() - 2));
    }

    private boolean argumentIsEqual(String statement) {
        return statement.endsWith(JsConfig.STATEMENT_TERMINATOR_CHAR + "") && getArguments().size() == 1 &&
                //length()-2 to remove ";\n" from the end of the statement
                equalsIgnoringExtraParenthesis(getArguments().get(0), statement.substring(0, statement.length() - 2));
    }

    private static boolean equalsIgnoringExtraParenthesis(String s1, String s2) {
        if (s1.equals(s2))
            return true;
        String parenthesizedS1 = "(" + s1 + ")";
        if (parenthesizedS1.equals(s2))
            return true;
        String parenthesizedS2 = "(" + s2 + ")";
        if (parenthesizedS2.equals(s1))
            return true;
        return false;
    }

    public Replacement makeReplacementForWrappedCall(String statement) {
        if (argumentIsReturned(statement)) {
            return new Replacement(statement.substring(7, statement.length() - 2), getArguments().get(0),
                    ReplacementType.ARGUMENT_REPLACED_WITH_RETURN_EXPRESSION);
        } else if (argumentIsEqual(statement)) {
            return new Replacement(statement.substring(0, statement.length() - 2), getArguments().get(0),
                    ReplacementType.ARGUMENT_REPLACED_WITH_STATEMENT);
        }
        return null;
    }

    public boolean argumentIsAssigned(String statement) {
        return getArguments().size() == 1 && statement.contains("=") && statement.endsWith(JsConfig.STATEMENT_TERMINATOR_CHAR + "") &&
                //length()-2 to remove ";\n" from the end of the assignment statement, indexOf("=")+1 to remove the left hand side of the assignment
                equalsIgnoringExtraParenthesis(getArguments().get(0), statement.substring(statement.indexOf("=") + 1, statement.length() - 2));
    }

    public boolean expressionIsNullOrThis() {
        if (expressionText == null) {
            return true;
        } else if (expressionText.equals("this")) {
            return true;
        }
        return false;
    }
}
