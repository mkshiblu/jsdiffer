package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.jsrminer.uml.diff.UMLOperationDiff;
import org.eclipse.jgit.annotations.NonNull;

import java.util.*;

public class FunctionBodyMapper {

    public final FunctionDeclaration function1;
    public final FunctionDeclaration function2;
    protected final PreProcessor preProcessor;

    private Set<StatementMapping> mappings = new LinkedHashSet<>();

    public FunctionBodyMapper(@NonNull FunctionDeclaration function1, @NonNull FunctionDeclaration function2) {
        this.function1 = function1;
        this.function2 = function2;
        this.preProcessor = new PreProcessor();
    }

    public void map() {
        FunctionBody body1 = function1.getBody();
        FunctionBody body2 = function2.getBody();

        if (body1 != null && body2 != null) {
            final UMLOperationDiff operationDiff = new UMLOperationDiff(function1, function2);

            BlockStatement block1 = body1.blockStatement;
            BlockStatement block2 = body2.blockStatement;

            List<SingleStatement> leaves1 = block1.getAllLeafStatementsIncludingNested();
            List<SingleStatement> leaves2 = block2.getAllLeafStatementsIncludingNested();
            replaceParametersWithArguments(operationDiff, leaves1, leaves2);
            // Reset leaves
            matchLeaves(leaves1, leaves2);
        }
    }

    void matchLeaves(List<SingleStatement> leaves1, List<SingleStatement> leaves2) {
        if (leaves1.size() <= leaves2.size()) {
            // Exact string+depth matching - leaf nodes
            matchLeavesByText(leaves1, leaves2, false);
            matchLeavesByText(leaves1, leaves2, true);
        }
    }

    void matchLeavesByText(List<SingleStatement> leaves1, List<SingleStatement> leaves2, boolean ignoreNestingDepth) {
        final Map<String, String> parameterToArgumentMap = new LinkedHashMap<>();

        //exact string+depth matching - leaf nodes
        for (ListIterator<SingleStatement> iterator1 = leaves1.listIterator();
             iterator1.hasNext(); ) {

            SingleStatement leaf1 = iterator1.next();
            TreeSet<LeafStatementMapping> mappingSet = new TreeSet<>();

            for (ListIterator<SingleStatement> iterator2 = leaves2.listIterator(); iterator2.hasNext(); ) {
                SingleStatement leaf2 = iterator2.next();

                String argumentizedString1 = preProcessor.getArgumentizedString(leaf1);
                String argumentizedString2 = preProcessor.getArgumentizedString(leaf2);

                // Check if strings are identical and they are in same depth
                if ((ignoreNestingDepth || leaf1.getDepth() == leaf2.getDepth())
                        && (leaf1.getText().equals(leaf2.getText()) || argumentizedString1.equals(argumentizedString2))) {
                    LeafStatementMapping mapping = createLeafMapping(leaf1, leaf2, parameterToArgumentMap);
                    mappingSet.add(mapping);
                }
            }

            if (!mappingSet.isEmpty()) {
                LeafStatementMapping minStatementMapping = mappingSet.first();
                mappings.add(minStatementMapping);

                leaves2.remove(minStatementMapping.statement2);
                iterator1.remove();
            }
        }
    }

    private String createArgumentizedString(SingleStatement statement1, SingleStatement statement2) {
        String argumentizedString = preProcessor.getArgumentizedString(statement1);

        // TODO replace return value with the argumentaized string
//        if (leaf1 instanceof StatementObject && leaf2 instanceof AbstractExpression) {
//            if (argumentizedString.startsWith("return ") && argumentizedString.endsWith(";\n")) {
//                argumentizedString = argumentizedString.substring("return ".length(),
//                        argumentizedString.lastIndexOf(";\n"));
//            }
//        }
        return argumentizedString;
    }

    private LeafStatementMapping createLeafMapping(SingleStatement leaf1, SingleStatement leaf2, Map<String, String> parameterToArgumentMap) {
//        FunctionDeclaration operation1 = codeFragmentOperationMap1.containsKey(leaf1) ? codeFragmentOperationMap1.get(leaf1) : this.operation1;
//        FunctionDeclaration operation2 = codeFragmentOperationMap2.containsKey(leaf2) ? codeFragmentOperationMap2.get(leaf2) : this.operation2;
        LeafStatementMapping mapping = new LeafStatementMapping(leaf1, leaf2);
//        for(String key : parameterToArgumentMap.keySet()) {
//            String value = parameterToArgumentMap.get(key);
//            if(!key.equals(value) && ReplacementUtil.contains(leaf2.getString(), key) && ReplacementUtil.contains(leaf1.getString(), value)) {
//                mapping.addReplacement(new Replacement(value, key, ReplacementType.VARIABLE_NAME));
//            }
//        }
        return mapping;
    }

    /**
     * Consists of Abstraction and Argumentization
     * Abstraction: When follows these formats
     * return exp; returned exp
     * Type var = exp; variable initializer
     * var = exp; Right side of an assignment
     * call(exp); single argument of method invocation
     * if (exp) Condition of a composite
     */
    public void preProcess() {

    }

    void replaceParametersWithArguments(UMLOperationDiff operationDiff, List<SingleStatement> leaves1,
                                        List<SingleStatement> leaves2) {
        Map<String, String> parameterToArgumentMap1 = new LinkedHashMap<>();
        Map<String, String> parameterToArgumentMap2 = new LinkedHashMap<>();

        mapParametersToArgument(operationDiff, parameterToArgumentMap1, parameterToArgumentMap2);
        leaves1.forEach(leaf -> preProcessor.replaceParametersWithArguments(leaf, parameterToArgumentMap1));
        leaves2.forEach(leaf -> preProcessor.replaceParametersWithArguments(leaf, parameterToArgumentMap2));
    }

    void mapParametersToArgument(UMLOperationDiff operationDiff, Map<String, String> parameterToArgumentMap1,
                                 Map<String, String> parameterToArgumentMap2) {
        final Map<String, UMLParameter> addedParameters = operationDiff.getAddedParameters();
        final Map<String, UMLParameter> removedParameters = operationDiff.getRemovedParameters();
        // TODO revisit since js does not have types
        if (addedParameters.size() == 1) {
            UMLParameter addedParameter = addedParameters.values().iterator().next();
//            if (UMLModelDiff.looksLikeSameType(addedParameter.getType().getClassType(),
//                    function1.getClassName())) {
//                parameterToArgumentMap1.put("this.", "");
//                //replace "parameterName." with ""
//                parameterToArgumentMap2.put(addedParameter.name + ".", "");
//            }
        }

        if (removedParameters.size() == 1) {
            UMLParameter removedParameter = removedParameters.values().iterator().next();
//            if (UMLModelDiff.looksLikeSameType(removedParameter.getType().getClassType(),
//                    function2.getClassName())) {
//                parameterToArgumentMap1.put(removedParameter.name + ".", "");
//                parameterToArgumentMap2.put("this.", "");
//            }
        }
    }
}
