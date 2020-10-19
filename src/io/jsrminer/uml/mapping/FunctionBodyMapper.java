package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.jsrminer.uml.diff.UMLOperationDiff;
import io.jsrminer.uml.mapping.replacement.Replacement;
import io.jsrminer.uml.mapping.replacement.ReplacementFinder;
import io.jsrminer.uml.mapping.replacement.ReplacementInfo;

import java.util.*;

public class FunctionBodyMapper {

    public final FunctionDeclaration function1;
    public final FunctionDeclaration function2;
    public final UMLOperationDiff operationDiff;

    protected final Argumentizer argumentizer;

    private Set<CodeFragmentMapping> mappings = new LinkedHashSet<>();
    Map<String, String> parameterToArgumentMap1 = new LinkedHashMap<>();
    Map<String, String> parameterToArgumentMap2 = new LinkedHashMap<>();

    final Map<String, FunctionDeclaration> addedOperations;
    final Map<String, FunctionDeclaration> removedOperations;

    private Set<SingleStatement> nonMappedLeavesT1;
    private Set<SingleStatement> nonMappedLeavesT2;
    private Set<BlockStatement> nonMappedInnerNodesT1;
    private Set<BlockStatement> nonMappedInnerNodesT2;

    public FunctionBodyMapper(UMLOperationDiff operationDiff
            , Map<String, FunctionDeclaration> addedOperations
            , Map<String, FunctionDeclaration> removedOperations) {
        this.operationDiff = operationDiff;
        this.function1 = operationDiff.function1;
        this.function2 = operationDiff.function2;
        this.addedOperations = addedOperations;
        this.removedOperations = removedOperations;
        this.argumentizer = new Argumentizer();
    }

    public void map() {
        FunctionBody body1 = function1.getBody();
        FunctionBody body2 = function2.getBody();

        if (body1 != null && body2 != null) {

            mapParametersToArguments(this.operationDiff.getAddedParameters(), this.operationDiff.getRemovedParameters());

            BlockStatement block1 = body1.blockStatement;
            BlockStatement block2 = body2.blockStatement;

            // match leaves
            argumentizer.clearCache();
            Set<SingleStatement> leaves1 = new LinkedHashSet<>(block1.getAllLeafStatementsIncludingNested());
            Set<SingleStatement> leaves2 = new LinkedHashSet<>(block2.getAllLeafStatementsIncludingNested());
            replaceParametersWithArguments(leaves1, leaves2);

            if (leaves1.size() > 0 && leaves2.size() > 0)
                matchLeaves(leaves1, leaves2);

            this.nonMappedLeavesT1 = leaves1;
            this.nonMappedLeavesT2 = leaves2;

            // Match composites
            argumentizer.clearCache();
            Set<BlockStatement> innerNodes1 = new LinkedHashSet<>(block1.getAllBlockStatementsIncludingNested());
            Set<BlockStatement> innerNodes2 = new LinkedHashSet<>(block2.getAllBlockStatementsIncludingNested());

            // TODO improve recirson of innerNodes by preventing adding itself
            innerNodes1.remove(block1);
            innerNodes2.remove(block2);

            replaceParametersWithArguments(innerNodes1, innerNodes2);
            if (innerNodes1.size() > 0 && innerNodes2.size() > 0)
                matchNestedBlockStatements(innerNodes1, innerNodes2, new LinkedHashMap<>());

            this.nonMappedInnerNodesT1 = innerNodes1;
            this.nonMappedInnerNodesT2 = innerNodes2;

            // Set mappings
            this.operationDiff.setMappings(this.mappings);
        }
    }


    void matchLeaves(Set<SingleStatement> leaves1, Set<SingleStatement> leaves2) {
//        Set<SingleStatement> unmatchedLeavesA = new LinkedHashSet<>();
//        Set<SingleStatement> unmatchedLeavesB = new LinkedHashSet<>();
//
//        if (leaves1.size() <= leaves2.size()) {
//            unmatchedLeavesA.addAll(leaves1);
//            unmatchedLeavesB.addAll(leaves2);
//        } else {
//            //unmatchedLeavesA.addAll(leaves2);
//            //unmatchedLeavesB.addAll(leaves1);
//        }

        // Exact string+depth matching - leaf nodes
        matchLeavesWithIdenticalText(leaves1, leaves2, false);

        if (leaves1.size() == 0 || leaves2.size() == 0)
            return;

        // Exact string any depth
        matchLeavesWithIdenticalText(leaves1, leaves2, true);

        if (leaves1.size() == 0 || leaves2.size() == 0)
            return;

        matchLeavesWithVariableRenames(leaves1, leaves2);
    }

    void matchLeavesWithIdenticalText(Set<SingleStatement> leaves1, Set<SingleStatement> leaves2, boolean ignoreNestingDepth) {
        final Map<String, String> parameterToArgumentMap = new LinkedHashMap<>();

        //exact string matching
        for (Iterator<SingleStatement> iterator1 = leaves1.iterator(); iterator1.hasNext(); ) {

            SingleStatement leaf1 = iterator1.next();
            TreeSet<LeafCodeFragmentMapping> mappingSet = new TreeSet<>();

            for (Iterator<SingleStatement> iterator2 = leaves2.iterator(); iterator2.hasNext(); ) {
                SingleStatement leaf2 = iterator2.next();

                String argumentizedString1 = createArgumentizedString(leaf1, leaf2);
                String argumentizedString2 = createArgumentizedString(leaf2, leaf1);

                // Check if strings are identical and they are in same depth
                if ((ignoreNestingDepth || leaf1.getDepth() == leaf2.getDepth())
                        && (leaf1.getText().equals(leaf2.getText()) || argumentizedString1.equals(argumentizedString2))) {
                    LeafCodeFragmentMapping mapping = createLeafMapping(leaf1, leaf2, parameterToArgumentMap);
                    mappingSet.add(mapping);
                }
            }

            if (!mappingSet.isEmpty()) {
                LeafCodeFragmentMapping minStatementMapping = mappingSet.first();
                mappings.add(minStatementMapping);

                leaves2.remove(minStatementMapping.statement2);
                iterator1.remove();
            }
        }
    }

    private void matchLeavesWithVariableRenames(Set<SingleStatement> leaves1, Set<SingleStatement> leaves2) {
        final Map<String, String> parameterToArgumentMap = new LinkedHashMap<>();
        ReplacementFinder replacementFinder = new ReplacementFinder();

        Iterator<SingleStatement> it1 = leaves1.iterator();
        Iterator<SingleStatement> it2 = leaves2.iterator();

        // TODO refactor duplicateed code, extract inner for loop to seprate method
        if (leaves1.size() <= leaves2.size()) {
            for (Iterator<SingleStatement> iterator1 = leaves1.iterator(); iterator1.hasNext(); ) {
                SingleStatement leaf1 = iterator1.next();
                TreeSet<LeafCodeFragmentMapping> mappingSet = new TreeSet<>();

                for (Iterator<SingleStatement> iterator2 = leaves2.iterator(); iterator2.hasNext(); ) {
                    SingleStatement leaf2 = iterator2.next();

                    LeafCodeFragmentMapping mapping = getLeafMappingUsingReplacements(leaf1, leaf2, leaves1, leaves2, parameterToArgumentMap, replacementFinder);
                    if (mapping != null) {
                        mappingSet.add(mapping);
                    }
                }
                if (!mappingSet.isEmpty()) {
                    LeafCodeFragmentMapping minStatementMapping = mappingSet.first();
                    this.mappings.add(minStatementMapping);
                    leaves2.remove(minStatementMapping.statement2);
                    iterator1.remove();
                }
            }
        } else {
            for (Iterator<SingleStatement> iterator2 = leaves2.iterator(); iterator2.hasNext(); ) {
                SingleStatement leaf2 = iterator2.next();
                TreeSet<LeafCodeFragmentMapping> mappingSet = new TreeSet<>();

                for (Iterator<SingleStatement> iterator1 = leaves1.iterator(); iterator1.hasNext(); ) {
                    SingleStatement leaf1 = iterator1.next();
                    LeafCodeFragmentMapping mapping = getLeafMappingUsingReplacements(leaf1, leaf2, leaves1, leaves2, parameterToArgumentMap, replacementFinder);
                    if (mapping != null) {
                        mappingSet.add(mapping);
                    }
                }

                // Take the best mapping
                if (!mappingSet.isEmpty()) {
//                    AbstractMap.SimpleEntry<CompositeStatementObject, CompositeStatementObject> switchParentEntry = null;
//                    if(variableDeclarationMappingsWithSameReplacementTypes(mappingSet)) {
//                        //postpone mapping
//                        postponedMappingSets.add(mappingSet);
//                    }
//                    else if((switchParentEntry = multipleMappingsUnderTheSameSwitch(mappingSet)) != null) {
//                        LeafMapping bestMapping = findBestMappingBasedOnMappedSwitchCases(switchParentEntry, mappingSet);
//                        mappings.add(bestMapping);
//                        leaves1.remove(bestMapping.getFragment1());
//                        leafIterator2.remove();
//                    }
//                    else {
//                        LeafMapping minStatementMapping = mappingSet.first();
//                        mappings.add(minStatementMapping);
//                        leaves1.remove(minStatementMapping.getFragment1());
//                        leafIterator2.remove();
//                    }
                    LeafCodeFragmentMapping minStatementMapping = mappingSet.first();
                    this.mappings.add(minStatementMapping);

                    // Remove the matched statmetns
                    leaves1.remove(minStatementMapping.statement1);
                    iterator2.remove();
                }
            }
        }
    }

    void matchInnerNodesWithIdenticalText(Set<BlockStatement> innerNodes1
            , Set<BlockStatement> innerNodes2, Map<String, String> parameterToArgumentMap
            , boolean ignoreNestingDepth) {
        //exact string+depth matching - inner nodes
        for (Iterator<BlockStatement> iterator2 = innerNodes2.iterator(); iterator2.hasNext(); ) {
            BlockStatement statement2 = iterator2.next();
            TreeSet<BlockCodeFragmentMapping> sortedMappingSet = new TreeSet<>();

            for (Iterator<BlockStatement> iterator1 = innerNodes1.iterator(); iterator1.hasNext(); ) {
                BlockStatement statement1 = iterator1.next();
                double score = ChildCountMatcher.computeScore(statement1, statement2
                        , removedOperations, addedOperations, this.mappings, false);

                String argumentizedString1 = createArgumentizedString(statement1, statement2);
                String argumentizedString2 = createArgumentizedString(statement1, statement2);

                // Check if strings are identical and they are in same depth (or not)
                if ((ignoreNestingDepth || statement1.getDepth() == statement2.getDepth()
                        && (score > 0 || Math.max(statement1.getStatements().size(), statement2.getStatements().size()) == 0))
                        && (statement1.getTextWithExpressions().equals(statement2.getTextWithExpressions()) || argumentizedString1.equals(argumentizedString2))) {
                    BlockCodeFragmentMapping mapping = createCompositeMapping(statement1, statement2
                            , parameterToArgumentMap, score);
                    sortedMappingSet.add(mapping);
                }
            }
            if (!sortedMappingSet.isEmpty()) {
                BlockCodeFragmentMapping minStatementMapping = sortedMappingSet.first();
                mappings.add(minStatementMapping);
                innerNodes1.remove(minStatementMapping.statement1);
                iterator2.remove();
            }
        }
    }

    /**
     * Match the block statements inside of the body of a function
     */
    void matchNestedBlockStatements(Set<BlockStatement> innerNodes1, Set<BlockStatement> innerNodes2
            , Map<String, String> parameterToArgumentMap) {
        if (innerNodes1.size() <= innerNodes2.size()) {
            // TODO
        } else {
            //exact string+depth matching - inner nodes
            matchInnerNodesWithIdenticalText(innerNodes1, innerNodes2, parameterToArgumentMap, false);
            matchInnerNodesWithIdenticalText(innerNodes1, innerNodes2, parameterToArgumentMap, true);
            matchInnerNodersWithVariableRenames(innerNodes1, innerNodes2);
        }
    }

    private void matchInnerNodersWithVariableRenames(Set<BlockStatement> innerNodes1, Set<BlockStatement> innerNodes2) {
        // exact matching - inner nodes - with variable renames
        ReplacementFinder replacementFinder = new ReplacementFinder();
        final Map<String, String> parameterToArgumentMap = new LinkedHashMap<>();

        for (Iterator<BlockStatement> innerNodeIterator2 = innerNodes2.iterator(); innerNodeIterator2.hasNext(); ) {
            BlockStatement statement2 = innerNodeIterator2.next();
            TreeSet<BlockCodeFragmentMapping> mappingSet = new TreeSet<>();

            for (Iterator<BlockStatement> innerNodeIterator1 = innerNodes1.iterator();
                 innerNodeIterator1.hasNext(); ) {
                BlockStatement statement1 = innerNodeIterator1.next();

                BlockCodeFragmentMapping mapping = getCompositeMappingUsingReplacements(statement1, statement2,
                        innerNodes1, innerNodes2
                        , parameterToArgumentMap, replacementFinder);

                if (mapping != null)
                    mappingSet.add(mapping);
            }

            if (!mappingSet.isEmpty()) {
                BlockCodeFragmentMapping minStatementMapping = mappingSet.first();
                this.mappings.add(minStatementMapping);
                innerNodes1.remove(minStatementMapping.statement1);
                innerNodeIterator2.remove();
            }
        }
    }

    private BlockCodeFragmentMapping getCompositeMappingUsingReplacements(BlockStatement statement1, BlockStatement statement2
            , Set<BlockStatement> innerNodes1, Set<BlockStatement> innerNodes2
            , Map<String, String> parameterToArgumentMap, ReplacementFinder replacementFinder) {

        ReplacementInfo replacementInfo = createReplacementInfo(statement1, statement2, innerNodes1, innerNodes2);
        Set<Replacement> replacements = replacementFinder.findReplacementsWithExactMatching(statement1
                , statement2
                , parameterToArgumentMap
                , replacementInfo,
                this.argumentizer,
                this.mappings);

        if (replacements != null) {
            double score = ChildCountMatcher.computeScore(statement1, statement2
                    , removedOperations, addedOperations
                    , this.mappings, false);

            if (score == 0 && replacements.size() == 1 &&
                    (replacements.iterator().next().getType().equals(Replacement.ReplacementType.INFIX_OPERATOR)
                            || replacements.iterator().next().getType().equals(Replacement.ReplacementType.INVERT_CONDITIONAL))) {
                //special handling when there is only an infix operator or invert conditional replacement, but no children mapped
                score = 1;
            }
            if (score > 0 || Math.max(statement1.getStatements().size(), statement2.getStatements().size()) == 0) {
                BlockCodeFragmentMapping mapping = createCompositeMapping(statement1, statement2, parameterToArgumentMap, score);
                mapping.addReplacements(replacements);
                return mapping;
            }
        }
        return null;
    }

    private LeafCodeFragmentMapping getLeafMappingUsingReplacements(SingleStatement leaf1
            , SingleStatement leaf2
            , Set<SingleStatement> leaves1
            , Set<SingleStatement> leaves2
            , Map<String, String> parameterToArgumentMap
            , ReplacementFinder replacementFinder) {
        ReplacementInfo replacementInfo = createReplacementInfo(leaf1, leaf2, leaves1, leaves2);
        Set<Replacement> replacements = replacementFinder.findReplacementsWithExactMatching(leaf1
                , leaf2
                , parameterToArgumentMap
                , replacementInfo,
                argumentizer,
                mappings);
        if (replacements != null) {
            LeafCodeFragmentMapping mapping = createLeafMapping(leaf1, leaf2, parameterToArgumentMap);
            mapping.addReplacements(replacements);
//                    for (AbstractCodeFragment leaf : leaves2) {
//                        if (leaf.equals(leaf2)) {
//                            break;
//                        }
//                        UMLClassBaseDiff classDiff = this.classDiff != null ? this.classDiff : parentMapper != null ? parentMapper.classDiff : null;
//                        mapping.temporaryVariableAssignment(leaf, leaves2, refactorings, classDiff);
//                        if (mapping.isIdenticalWithExtractedVariable()) {
//                            break;
//                        }
//                    }
//                    for (AbstractCodeFragment leaf : leaves1) {
//                        if (leaf.equals(leaf1)) {
//                            break;
//                        }
//                        mapping.inlinedVariableAssignment(leaf, leaves2, refactorings);
//                        if (mapping.isIdenticalWithInlinedVariable()) {
//                            break;
//                        }
//                    }
            return mapping;
        }

        return null;
    }

    private ReplacementInfo createReplacementInfo(Statement statement1
            , Statement statement2
            , Set<? extends Statement> statements1
            , Set<? extends Statement> statements2) {
        List<? extends Statement> unmatchedStatments1 = new ArrayList<>(statements1);
        unmatchedStatments1.remove(statement1);
        List<? extends Statement> unmatchedStatements2 = new ArrayList<>(statements2);
        unmatchedStatements2.remove(statement2);
        return new ReplacementInfo(
                createArgumentizedString(statement1, statement2),
                createArgumentizedString(statement2, statement1),
                unmatchedStatments1, unmatchedStatements2);
    }

    // TODO: Similar to processInput without checking for abstractexpression
    private String createArgumentizedString(Statement statement1, Statement statement2) {
        String argumentizedString = argumentizer.getArgumentizedString(statement1);

        // TODO replace return value with the argumentaized string
//        if (statement1 instanceof SingleStatement && statement2 instanceof Expression) {
//            if (argumentizedString.startsWith("return ") && argumentizedString.endsWith(";\n")) {
//                argumentizedString = argumentizedString.substring("return ".length(),
//                        argumentizedString.lastIndexOf(";\n"));
//            }
//        }
        return argumentizedString;
    }

    private LeafCodeFragmentMapping createLeafMapping(SingleStatement leaf1, SingleStatement leaf2, Map<String, String> parameterToArgumentMap) {
//        FunctionDeclaration operation1 = codeFragmentOperationMap1.containsKey(leaf1) ? codeFragmentOperationMap1.get(leaf1) : this.operation1;
//        FunctionDeclaration operation2 = codeFragmentOperationMap2.containsKey(leaf2) ? codeFragmentOperationMap2.get(leaf2) : this.operation2;
        LeafCodeFragmentMapping mapping = new LeafCodeFragmentMapping(leaf1, leaf2);
        for (String key : parameterToArgumentMap.keySet()) {
            String value = parameterToArgumentMap.get(key);
//            if(!key.equals(value) && ReplacementUtil.contains(leaf2.getString(), key) && ReplacementUtil.contains(leaf1.getString(), value)) {
//                mapping.addReplacement(new Replacement(value, key, ReplacementType.VARIABLE_NAME));
//            }
        }
        return mapping;
    }

    private BlockCodeFragmentMapping createCompositeMapping(BlockStatement statement1,
                                                            BlockStatement statement2
            , Map<String, String> parameterToArgumentMap, double score) {
//        FunctionDeclaration operation1 = /*codeFragmentOperationMap1.containsKey(statement1)
//                ? codeFragmentOperationMap1.get(statement1) :*/ this.function1;
//        FunctionDeclaration operation2 = /*codeFragmentOperationMap2.containsKey(statement2)
//                ? codeFragmentOperationMap2.get(statement2) :*/ this.function2;

        BlockCodeFragmentMapping mapping = new BlockCodeFragmentMapping(statement1, statement2
                /*, operation1, operation2*/, score);
//        for (String key : parameterToArgumentMap.keySet()) {
//            String value = parameterToArgumentMap.get(key);
////            if (!key.equals(value) && ReplacementUtil.contains(statement2.getString(), key) && ReplacementUtil.contains(statement1.getString(), value)) {
//            //              mapping.addReplacement(new Replacement(value, key, ReplacementType.VARIABLE_NAME));
//            //        }
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

    void replaceParametersWithArguments(Set<? extends Statement> statements1,
                                        Set<? extends Statement> statements2) {

        //mapParametersToArgument(operationDiff, parameterToArgumentMap1, parameterToArgumentMap2);
        statements1.forEach(statement -> argumentizer.replaceParametersWithArguments(statement, parameterToArgumentMap1));
        statements2.forEach(statement -> argumentizer.replaceParametersWithArguments(statement, parameterToArgumentMap2));
    }

    /**
     * Populate parameters to argument maps (1 and 2)
     */

    public void mapParametersToArguments(final Map<String, UMLParameter> addedParameters,
                                         final Map<String, UMLParameter> removedParameters) {

        this.parameterToArgumentMap1.clear();
        this.parameterToArgumentMap2.clear();
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
