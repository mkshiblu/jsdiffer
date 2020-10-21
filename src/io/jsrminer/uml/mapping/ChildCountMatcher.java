package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.mapping.replacement.InvocationCoverage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChildCountMatcher {

    static double computeScore(BlockStatement statement1
            , BlockStatement statement2
            , Map<String, FunctionDeclaration> removedOperations
            , Map<String, FunctionDeclaration> addedOperations,
                               Set<CodeFragmentMapping> mappings, boolean hasParentMapper) {

        if (statement1.getCodeElementType() == CodeElementType.TRY_STATEMENT
                && statement2.getCodeElementType() == CodeElementType.TRY_STATEMENT) {
            return tryStatementsChildMatchingScore((TryStatement) statement1
                    , (TryStatement) statement2, mappings, removedOperations, addedOperations, hasParentMapper);
        }

        return compositeChildMatchingScore(statement1, statement2
                , mappings, removedOperations, addedOperations, hasParentMapper);
    }

    static double tryStatementsChildMatchingScore(TryStatement try1,
                                                  TryStatement try2
            , Set<CodeFragmentMapping> mappings
            , Map<String, FunctionDeclaration> removedOperations
            , Map<String, FunctionDeclaration> addedOperations,
                                                  boolean hasParentMapper) {

        double score = compositeChildMatchingScore(try1
                , try2
                , mappings, removedOperations, addedOperations, hasParentMapper);
        List<BlockStatement> catchClauses1 = try1.getCatchClauses();
        List<BlockStatement> catchClauses2 = try2.getCatchClauses();
        if (catchClauses1.size() == catchClauses2.size()) {
            for (int i = 0; i < catchClauses1.size(); i++) {
                double tmpScore = compositeChildMatchingScore(catchClauses1.get(i)
                        , catchClauses2.get(i), mappings, removedOperations, addedOperations, hasParentMapper);
                if (tmpScore == 1) {
                    score += tmpScore;
                }
            }
        }
//        if (try1.getFinallyClause() != null && try2.getFinallyClause() != null) {
//            double tmpScore = compositeChildMatchingScore(try1.getFinallyClause(), try2.getFinallyClause(), mappings, removedOperations, addedOperations);
//            if (tmpScore == 1) {
//                score += tmpScore;
//            }
//        }
        return score;
    }

    static double compositeChildMatchingScore(BlockStatement comp1
            , BlockStatement comp2
            , Set<CodeFragmentMapping> mappings
            , Map<String, FunctionDeclaration> removedOperations
            , Map<String, FunctionDeclaration> addedOperations
            , boolean hasParentMapper) {

        List<Statement> statements1 = comp1.getStatements();
        List<Statement> statements2 = comp2.getStatements();
        int childrenSize1 = statements1.size();
        int childrenSize2 = statements2.size();

        if (hasParentMapper
                && comp1.getCodeElementType().equals(comp2.getCodeElementType())
                && childrenSize1 == 1 && childrenSize2 == 1
                && !comp1.getText().equals("{")
                && !comp2.getText().equals("{")) {

            if (statements1.get(0).getText().equals("{")
                    && !statements2.get(0).getText().equals("{")) {
                BlockStatement block = (BlockStatement) statements1.get(0);
                statements1.addAll(block.getStatements());
            }
            if (!statements1.get(0).getText().equals("{") && statements2.get(0).getText().equals("{")) {
                BlockStatement block = (BlockStatement) statements2.get(0);
                statements2.addAll(block.getStatements());
            }
        }

        int mappedChildrenSize = 0;
        for (CodeFragmentMapping mapping : mappings) {
            if (statements1.contains(mapping.statement1)
                    && statements2.contains(mapping.statement2)) {
                mappedChildrenSize++;
            }
        }

        if (mappedChildrenSize == 0) {
            Set<SingleStatement> leaves1 = new LinkedHashSet<>(comp1.getAllLeafStatementsIncludingNested());
            Set<SingleStatement> leaves2 = new LinkedHashSet<>(comp2.getAllLeafStatementsIncludingNested());

            int leaveSize1 = leaves1.size();
            int leaveSize2 = leaves2.size();
            int mappedLeavesSize = 0;

            for (CodeFragmentMapping mapping : mappings) {
                if (leaves1.contains(mapping.statement1) && leaves2.contains(mapping.statement2)) {
                    mappedLeavesSize++;
                }
            }

            if (mappedLeavesSize == 0) {

                //check for possible extract or inline
                if (leaveSize2 <= 2) {
                    for (SingleStatement leaf2 : leaves2) {
                        OperationInvocation invocation = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(leaf2);
                        if (invocation != null
                                && functionCallsMatchesDeclaration(invocation, addedOperations
                                /*, operation2.variableTypeMap()*/)) {
                            mappedLeavesSize++;
                        }
                    }
                } else if (leaveSize1 <= 2) {
                    for (SingleStatement leaf1 : leaves1) {
                        OperationInvocation invocation = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(leaf1);
                        if (invocation != null
                                && functionCallsMatchesDeclaration(invocation, removedOperations
                                /*, operation1.variableTypeMap()*/)) {
                            mappedLeavesSize++;
                        }
                    }
                }

                if (leaveSize1 == 1 && leaveSize2 == 1
                        && leaves1.iterator().next().getText().equals("continue;")
                        && leaves2.iterator().next().getText().equals("return null;")) {
                    mappedLeavesSize++;
                }
            }


            int max = Math.max(leaveSize1, leaveSize2);
            if (max == 0)
                return 0;
            else
                return (double) mappedLeavesSize / (double) max;
        }

        int max = Math.max(childrenSize1, childrenSize2);
        if (max == 0)
            return 0;
        else
            return (double) mappedChildrenSize / (double) max;
    }

    static boolean functionCallsMatchesDeclaration(OperationInvocation invocation,
                                                   Map<String, FunctionDeclaration> functionDeclarations
                                                   /*List<FunctionDeclaration> operations/*,
                                                   Map<String, UMLType> variableTypeMap*/) {
//        for (FunctionDeclaration operation : operations) {
//            // if (invocation.matchesOperation(operation, variableTypeMap, modelDiff))
//            if (operation.name != null && operation.name.equals(invocation.getFunctionName()))
//                return true;
//        }
//        return false;
        return functionDeclarations.containsKey(invocation.getFunctionName());
    }
}
