package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.jsrminer.uml.diff.SourceFileModelDiff;
import io.jsrminer.uml.diff.UMLOperationDiff;
import io.jsrminer.uml.mapping.replacement.*;

import java.util.*;

public class FunctionBodyMapper {

    public final FunctionDeclaration function1;
    public final FunctionDeclaration function2;
    public final UMLOperationDiff operationDiff;

    public static final Argumentizer argumentizer = new Argumentizer();

    private Set<CodeFragmentMapping> mappings = new LinkedHashSet<>();
    Map<String, String> parameterToArgumentMap1 = new LinkedHashMap<>();
    Map<String, String> parameterToArgumentMap2 = new LinkedHashMap<>();

    private Set<SingleStatement> nonMappedLeavesT1;
    private Set<SingleStatement> nonMappedLeavesT2;
    private Set<BlockStatement> nonMappedInnerNodesT1;
    private Set<BlockStatement> nonMappedInnerNodesT2;

    private FunctionDeclaration callerFunctionOfAddedOperation;
    private final SourceFileModelDiff sourceFileModelDiff;
    private List<FunctionBodyMapper> childMappers = new ArrayList<>();
    private FunctionBodyMapper parentMapper;

    public FunctionBodyMapper(UMLOperationDiff operationDiff
            , SourceFileModelDiff sourceFileModelDiff) {
        this.operationDiff = operationDiff;
        this.function1 = operationDiff.function1;
        this.function2 = operationDiff.function2;
        this.sourceFileModelDiff = sourceFileModelDiff;
    }

    public UMLOperationBodyMapper(UMLOperation removedOperation, UMLOperationBodyMapper operationBodyMapper,
                                  Map<String, String> parameterToArgumentMap, UMLClassBaseDiff classDiff) throws RefactoringMinerTimedOutException {
        this.parentMapper = operationBodyMapper;
        this.operation1 = removedOperation;
        this.operation2 = operationBodyMapper.operation2;
        this.callSiteOperation = operationBodyMapper.operation1;
        this.classDiff = classDiff;
        this.mappings = new LinkedHashSet<AbstractCodeMapping>();
        this.nonMappedLeavesT1 = new ArrayList<StatementObject>();
        this.nonMappedLeavesT2 = new ArrayList<StatementObject>();
        this.nonMappedInnerNodesT1 = new ArrayList<CompositeStatementObject>();
        this.nonMappedInnerNodesT2 = new ArrayList<CompositeStatementObject>();

        OperationBody removedOperationBody = removedOperation.getBody();
        if(removedOperationBody != null) {
            CompositeStatementObject composite1 = removedOperationBody.getCompositeStatement();
            List<StatementObject> leaves1 = composite1.getLeaves();
            List<StatementObject> leaves2 = operationBodyMapper.getNonMappedLeavesT2();
            //adding leaves that were mapped with replacements or are inexact matches
            Set<StatementObject> addedLeaves2 = new LinkedHashSet<StatementObject>();
            for(AbstractCodeMapping mapping : operationBodyMapper.getMappings()) {
                if(!returnWithVariableReplacement(mapping) && !nullLiteralReplacements(mapping) && (!mapping.getReplacements().isEmpty() || !mapping.getFragment1().equalFragment(mapping.getFragment2()))) {
                    AbstractCodeFragment fragment = mapping.getFragment2();
                    if(fragment instanceof StatementObject) {
                        StatementObject statement = (StatementObject)fragment;
                        if(!leaves2.contains(statement)) {
                            leaves2.add(statement);
                            addedLeaves2.add(statement);
                        }
                    }
                }
            }
            resetNodes(leaves1);
            //replace parameters with arguments in leaves1
            if(!parameterToArgumentMap.isEmpty()) {
                //check for temporary variables that the argument might be assigned to
                for(StatementObject leave2 : leaves2) {
                    List<VariableDeclaration> variableDeclarations = leave2.getVariableDeclarations();
                    for(VariableDeclaration variableDeclaration : variableDeclarations) {
                        for(String parameter : parameterToArgumentMap.keySet()) {
                            String argument = parameterToArgumentMap.get(parameter);
                            if(variableDeclaration.getInitializer() != null && argument.equals(variableDeclaration.getInitializer().toString())) {
                                parameterToArgumentMap.put(parameter, variableDeclaration.getVariableName());
                            }
                        }
                    }
                }
                for(StatementObject leave1 : leaves1) {
                    leave1.replaceParametersWithArguments(parameterToArgumentMap);
                }
            }
            //compare leaves from T1 with leaves from T2
            processLeaves(leaves1, leaves2, parameterToArgumentMap);

            List<CompositeStatementObject> innerNodes1 = composite1.getInnerNodes();
            innerNodes1.remove(composite1);
            List<CompositeStatementObject> innerNodes2 = operationBodyMapper.getNonMappedInnerNodesT2();
            //adding innerNodes that were mapped with replacements or are inexact matches
            Set<CompositeStatementObject> addedInnerNodes2 = new LinkedHashSet<CompositeStatementObject>();
            for(AbstractCodeMapping mapping : operationBodyMapper.getMappings()) {
                if(!mapping.getReplacements().isEmpty() || !mapping.getFragment1().equalFragment(mapping.getFragment2())) {
                    AbstractCodeFragment fragment = mapping.getFragment2();
                    if(fragment instanceof CompositeStatementObject) {
                        CompositeStatementObject statement = (CompositeStatementObject)fragment;
                        if(!innerNodes2.contains(statement)) {
                            innerNodes2.add(statement);
                            addedInnerNodes2.add(statement);
                        }
                    }
                }
            }
            resetNodes(innerNodes1);
            //replace parameters with arguments in innerNodes1
            if(!parameterToArgumentMap.isEmpty()) {
                for(CompositeStatementObject innerNode1 : innerNodes1) {
                    innerNode1.replaceParametersWithArguments(parameterToArgumentMap);
                }
            }
            //compare inner nodes from T1 with inner nodes from T2
            processInnerNodes(innerNodes1, innerNodes2, parameterToArgumentMap);

            //match expressions in inner nodes from T2 with leaves from T1
            List<AbstractExpression> expressionsT2 = new ArrayList<AbstractExpression>();
            for(CompositeStatementObject composite : operationBodyMapper.getNonMappedInnerNodesT2()) {
                for(AbstractExpression expression : composite.getExpressions()) {
                    expressionsT2.add(expression);
                }
            }
            processLeaves(leaves1, expressionsT2, parameterToArgumentMap);

            //remove the leaves that were mapped with replacement, if they are not mapped again for a second time
            leaves2.removeAll(addedLeaves2);
            //remove the innerNodes that were mapped with replacement, if they are not mapped again for a second time
            innerNodes2.removeAll(addedInnerNodes2);
            nonMappedLeavesT1.addAll(leaves1);
            nonMappedLeavesT2.addAll(leaves2);
            nonMappedInnerNodesT1.addAll(innerNodes1);
            nonMappedInnerNodesT2.addAll(innerNodes2);

            for(StatementObject statement : getNonMappedLeavesT2()) {
                temporaryVariableAssignment(statement, nonMappedLeavesT2);
            }
            for(StatementObject statement : getNonMappedLeavesT1()) {
                inlinedVariableAssignment(statement, nonMappedLeavesT2);
            }
        }
    }

    /**
     * Tries to mapp the function1 of the mapper with the added operation
     */
    public FunctionBodyMapper(FunctionBodyMapper mapper, FunctionDeclaration addedOperation, SourceFileModelDiff sourceFileModelDiff

            , Map<String, String> parameterToArgumentMap1
            , Map<String, String> parameterToArgumentMap2) {
        this.function1 = mapper.function1;
        this.function2 = addedOperation;
        this.callerFunctionOfAddedOperation = mapper.function2;
        this.sourceFileModelDiff = sourceFileModelDiff;
        this.operationDiff = new UMLOperationDiff(this.function1, this.function2);
        this.parentMapper = mapper;
        this.parameterToArgumentMap1 = parameterToArgumentMap1;
        this.parameterToArgumentMap2 = parameterToArgumentMap2;
    }

    /**
     * Maps funciton1 with funciton2
     */
    public void map() {
        FunctionBody body1 = function1.getBody();
        FunctionBody body2 = function2.getBody();

        if (body1 != null && body2 != null) {

            mapParametersToArguments(this.operationDiff.getAddedParameters(), this.operationDiff.getRemovedParameters());

            BlockStatement block1 = body1.blockStatement;
            BlockStatement block2 = body2.blockStatement;

            // match leaves
            Set<SingleStatement> leaves1 = new LinkedHashSet<>(block1.getAllLeafStatementsIncludingNested());
            Set<SingleStatement> leaves2 = new LinkedHashSet<>(block2.getAllLeafStatementsIncludingNested());
            argumentizer.clearCache(leaves1, leaves2);
            replaceParametersWithArguments(leaves1, leaves2);

            if (leaves1.size() > 0 && leaves2.size() > 0)
                matchLeaves(leaves1, leaves2, new LinkedHashMap<>());

            this.nonMappedLeavesT1 = leaves1;
            this.nonMappedLeavesT2 = leaves2;

            // Match composites

            Set<BlockStatement> innerNodes1 = new LinkedHashSet<>(block1.getAllBlockStatementsIncludingNested());
            Set<BlockStatement> innerNodes2 = new LinkedHashSet<>(block2.getAllBlockStatementsIncludingNested());

            // TODO improve recirson of innerNodes by preventing adding itself
            innerNodes1.remove(block1);
            innerNodes2.remove(block2);

            argumentizer.clearCache(innerNodes1, innerNodes2);
            replaceParametersWithArguments(innerNodes1, innerNodes2);
            if (innerNodes1.size() > 0 && innerNodes2.size() > 0)
                matchNestedBlockStatements(innerNodes1, innerNodes2, new LinkedHashMap<>());

            this.nonMappedInnerNodesT1 = innerNodes1;
            this.nonMappedInnerNodesT2 = innerNodes2;

            // Set mappings
            this.operationDiff.setMappings(this.mappings);
        }
    }

    /**
     * Maps added operation to function1
     */
    public void mapAddedOperation() {

        FunctionBody addedOperationBody = function2.getBody();
        if (addedOperationBody != null) {
            BlockStatement addedOperationBodyBlock = addedOperationBody.blockStatement;
            //Set<BlockStatement> addedInnerNodes1 = new LinkedHashSet<>();
            //Set<SingleStatement> addedLeaves1 = new LinkedHashSet<>();

            Set<SingleStatement> leaves1 = this.parentMapper.getNonMappedLeavesT1();
            for (CodeFragmentMapping mapping : this.parentMapper.getMappings()) {
                if ((mapping.fragment1 instanceof SingleStatement)
                        && !returnWithVariableReplacement(mapping)
                        && !nullLiteralReplacements(mapping)
                        && (!mapping.getReplacements().isEmpty() || !mapping.equalFragmentText(argumentizer))) {

                    // Add the statement to be matched again.
                    leaves1.add((SingleStatement) mapping.fragment1);
                }
            }

            // TODO add /expand  lambdas

            Set<SingleStatement> leaves2 = new LinkedHashSet<>(addedOperationBodyBlock.getAllLeafStatementsIncludingNested());
            argumentizer.clearCache(leaves1, leaves2);
            replaceParametersWithArguments(leaves1, leaves2);
            matchLeaves(leaves1, leaves2, parameterToArgumentMap2);

            Set<BlockStatement> innerNodes1 = this.parentMapper.getNonMappedInnerNodesT1();
            Set<BlockStatement> innerNodes2 = addedOperationBodyBlock.getAllBlockStatementsIncludingNested();
            Set<BlockStatement> addedInnerNodes1 = new LinkedHashSet<>();

            //adding innerNodes that were mapped with replacements
            for (CodeFragmentMapping mapping : this.parentMapper.getMappings()) {
                CodeFragment fragment = mapping.fragment1;
                if (!innerNodes1.contains(fragment) && fragment instanceof BlockStatement) {
                    if (!mapping.getReplacements().isEmpty() || !mapping.equalFragmentText(argumentizer)) {
                        BlockStatement statement = (BlockStatement) fragment;
                        innerNodes1.add(statement);
                        addedInnerNodes1.add(statement);
                    }
                }
            }

            // Remove itself
            innerNodes2.remove(addedOperationBodyBlock);
            //innerNodes2.addAll(addedInnerNodes2);

            argumentizer.clearCache(innerNodes1, innerNodes2);
            replaceParametersWithArguments(innerNodes1, innerNodes2);
            //compare inner nodes from T1 with inner nodes from T2
            matchNestedBlockStatements(innerNodes1, innerNodes2, parameterToArgumentMap2);

            //match expressions in inner nodes from T1 with leaves from T2
            Set<Expression> expressionsT1 = new LinkedHashSet<>();
            for (BlockStatement composite : this.parentMapper.getNonMappedInnerNodesT1()) {
                for (Expression expression : composite.getExpressions()) {
                    argumentizer.replaceParametersWithArguments(expression, parameterToArgumentMap1);
                    expressionsT1.add(expression);
                }
            }

            int numberOfMappings = mappings.size();

            if (expressionsT1.size() > 0 && leaves2.size() > 0)
                matchLeaves(expressionsT1, leaves2, parameterToArgumentMap2);

//            List<CodeFragmentMapping> mappings = new ArrayList<>(this.mappings);
//            for (int i = numberOfMappings; i < mappings.size(); i++) {
//                mappings.get(i).temporaryVariableAssignment(refactorings);
//            }
            // TODO remove non-mapped inner nodes from T1 corresponding to mapped expressions

            //remove the leaves that were mapped with replacement, if they are not mapped again for a second time
            //leaves1.removeAll(addedLeaves1);
            //leaves2.removeAll(addedLeaves2);
            //remove the innerNodes that were mapped with replacement, if they are not mapped again for a second time
            innerNodes1.removeAll(addedInnerNodes1);
            //innerNodes2.removeAll(addedInnerNodes2);


            this.nonMappedLeavesT1 = leaves1;
            this.nonMappedLeavesT2 = leaves2;

            this.nonMappedInnerNodesT1 = innerNodes1;
            this.nonMappedInnerNodesT2 = innerNodes2;

//            nonMappedLeavesT1.addAll(leaves1);
//            nonMappedLeavesT2.addAll(leaves2);
//            nonMappedInnerNodesT1.addAll(innerNodes1);
//            nonMappedInnerNodesT2.addAll(innerNodes2);
//
//            for (StatementObject statement : getNonMappedLeavesT2()) {
//                temporaryVariableAssignment(statement, nonMappedLeavesT2);
//            }
//            for (StatementObject statement : getNonMappedLeavesT1()) {
//                inlinedVariableAssignment(statement, nonMappedLeavesT2);
//            }
        }

    }

    void matchLeaves(Set<? extends CodeFragment> leaves1, Set<? extends CodeFragment> leaves2, Map<String, String> parameterToArgumentMap) {

        // Exact string+depth matching - leaf nodes
        matchLeavesWithIdenticalText(leaves1, leaves2, false, parameterToArgumentMap);

        if (leaves1.size() == 0 || leaves2.size() == 0)
            return;

        // Exact string any depth
        matchLeavesWithIdenticalText(leaves1, leaves2, true, parameterToArgumentMap);

        if (leaves1.size() == 0 || leaves2.size() == 0)
            return;

        matchLeavesWithVariableRenames(leaves1, leaves2, parameterToArgumentMap);
    }

    void matchLeavesWithIdenticalText(Set<? extends CodeFragment> leaves1, Set<? extends CodeFragment> leaves2,
                                      boolean ignoreNestingDepth, Map<String, String> parameterToArgumentMap) {
        //exact string matching
        for (Iterator<? extends CodeFragment> iterator1 = leaves1.iterator(); iterator1.hasNext(); ) {

            CodeFragment leaf1 = iterator1.next();
            TreeSet<LeafCodeFragmentMapping> mappingSet = new TreeSet<>();

            for (Iterator<? extends CodeFragment> iterator2 = leaves2.iterator(); iterator2.hasNext(); ) {
                CodeFragment leaf2 = iterator2.next();

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

                leaves2.remove(minStatementMapping.fragment2);
                iterator1.remove();
            }
        }
    }

    private void matchLeavesWithVariableRenames(Set<? extends CodeFragment> leaves1, Set<? extends CodeFragment> leaves2, Map<String, String> parameterToArgumentMap) {
        ReplacementFinder replacementFinder = new ReplacementFinder();

        Iterator<? extends CodeFragment> it1 = leaves1.iterator();
        Iterator<? extends CodeFragment> it2 = leaves2.iterator();

        // TODO refactor duplicateed code, extract inner for loop to seprate method
        if (leaves1.size() <= leaves2.size()) {
            for (Iterator<? extends CodeFragment> iterator1 = leaves1.iterator(); iterator1.hasNext(); ) {
                CodeFragment leaf1 = iterator1.next();
                TreeSet<LeafCodeFragmentMapping> mappingSet = new TreeSet<>();

                for (Iterator<? extends CodeFragment> iterator2 = leaves2.iterator(); iterator2.hasNext(); ) {
                    CodeFragment leaf2 = iterator2.next();

                    LeafCodeFragmentMapping mapping = getLeafMappingUsingReplacements(leaf1, leaf2, leaves1, leaves2, parameterToArgumentMap, replacementFinder);
                    if (mapping != null) {
                        mappingSet.add(mapping);
                    }
                }
                if (!mappingSet.isEmpty()) {
                    LeafCodeFragmentMapping minStatementMapping = mappingSet.first();
                    this.mappings.add(minStatementMapping);
                    leaves2.remove(minStatementMapping.fragment2);
                    iterator1.remove();
                }
            }
        } else {
            for (Iterator<? extends CodeFragment> iterator2 = leaves2.iterator(); iterator2.hasNext(); ) {
                CodeFragment leaf2 = iterator2.next();
                TreeSet<LeafCodeFragmentMapping> mappingSet = new TreeSet<>();

                for (Iterator<? extends CodeFragment> iterator1 = leaves1.iterator(); iterator1.hasNext(); ) {
                    CodeFragment leaf1 = iterator1.next();
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
                    leaves1.remove(minStatementMapping.fragment1);
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
                        , this.sourceFileModelDiff.getRemovedOperations(), this.sourceFileModelDiff.getAddedOperations()
                        , this.mappings, this.parentMapper != null);

                String argumentizedString1 = createArgumentizedString(statement1, statement2);
                String argumentizedString2 = createArgumentizedString(statement2, statement1);

                // Check if strings are identical and they are in same depth (or not)
                if ((ignoreNestingDepth || statement1.getDepth() == statement2.getDepth())
                        && (score > 0 || Math.max(statement1.getStatements().size(), statement2.getStatements().size()) == 0)
                        && (statement1.getTextWithExpressions().equals(statement2.getTextWithExpressions()) || argumentizedString1.equals(argumentizedString2))) {
                    BlockCodeFragmentMapping mapping = createCompositeMapping(statement1, statement2
                            , parameterToArgumentMap, score);
                    sortedMappingSet.add(mapping);
                }
            }
            if (!sortedMappingSet.isEmpty()) {
                BlockCodeFragmentMapping minStatementMapping = sortedMappingSet.first();
                mappings.add(minStatementMapping);
                innerNodes1.remove(minStatementMapping.fragment1);
                iterator2.remove();
            }
        }
    }

    /**
     * Match the block statements inside of the body of a function
     */
    void matchNestedBlockStatements(Set<BlockStatement> innerNodes1, Set<BlockStatement> innerNodes2
            , Map<String, String> parameterToArgumentMap) {
       /* if (innerNodes1.size() <= innerNodes2.size()) {
            // TODO
        } else */
        {
            //exact string+depth matching - inner nodes
            matchInnerNodesWithIdenticalText(innerNodes1, innerNodes2, parameterToArgumentMap, false);
            matchInnerNodesWithIdenticalText(innerNodes1, innerNodes2, parameterToArgumentMap, true);
            matchInnerNodersWithVariableRenames(innerNodes1, innerNodes2, parameterToArgumentMap);
        }
    }

    private void matchInnerNodersWithVariableRenames
            (Set<BlockStatement> innerNodes1, Set<BlockStatement> innerNodes2, Map<String, String> parameterToArgumentMap) {
        // exact matching - inner nodes - with variable renames
        ReplacementFinder replacementFinder = new ReplacementFinder();

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
                innerNodes1.remove(minStatementMapping.fragment1);
                innerNodeIterator2.remove();
            }
        }
    }

    private BlockCodeFragmentMapping getCompositeMappingUsingReplacements(BlockStatement statement1, BlockStatement
            statement2
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
                    , this.sourceFileModelDiff.getRemovedOperations(), this.sourceFileModelDiff.getAddedOperations()
                    , this.mappings, this.parentMapper != null);

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

    private LeafCodeFragmentMapping getLeafMappingUsingReplacements(CodeFragment leaf1
            , CodeFragment leaf2
            , Set<? extends CodeFragment> leaves1
            , Set<? extends CodeFragment> leaves2
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

    private ReplacementInfo createReplacementInfo(CodeFragment statement1
            , CodeFragment statement2
            , Set<? extends CodeFragment> statements1
            , Set<? extends CodeFragment> statements2) {
        List<? extends CodeFragment> unmatchedStatments1 = new ArrayList<>(statements1);
        unmatchedStatments1.remove(statement1);
        List<? extends CodeFragment> unmatchedStatements2 = new ArrayList<>(statements2);
        unmatchedStatements2.remove(statement2);
        return new ReplacementInfo(
                createArgumentizedString(statement1, statement2),
                createArgumentizedString(statement2, statement1),
                unmatchedStatments1, unmatchedStatements2);
    }

    // TODO: Similar to processInput without checking for abstractexpression
    private String createArgumentizedString(CodeFragment statement1, CodeFragment statement2) {
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

    private LeafCodeFragmentMapping createLeafMapping(CodeFragment leaf1, CodeFragment
            leaf2, Map<String, String> parameterToArgumentMap) {
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

    public Set<SingleStatement> getNonMappedLeavesT1() {
        return nonMappedLeavesT1;
    }

    public Set<SingleStatement> getNonMappedLeavesT2() {
        return nonMappedLeavesT2;
    }

    public Set<BlockStatement> getNonMappedInnerNodesT1() {
        return nonMappedInnerNodesT1;
    }

    public Set<BlockStatement> getNonMappedInnerNodesT2() {
        return nonMappedInnerNodesT2;
    }

    public Set<CodeFragmentMapping> getMappings() {
        return mappings;
    }

    public Set<Replacement> getReplacementsInvolvingMethodInvocation() {
        Set<Replacement> replacements = new LinkedHashSet<>();
        for (CodeFragmentMapping mapping : getMappings()) {
            for (Replacement replacement : mapping.getReplacements()) {
                if (replacement instanceof MethodInvocationReplacement ||
                        replacement instanceof VariableReplacementWithMethodInvocation ||
                        //replacement instanceof ClassInstanceCreationWithMethodInvocationReplacement ||
                        replacement.getType().equals(Replacement.ReplacementType
                                .ARGUMENT_REPLACED_WITH_RIGHT_HAND_SIDE_OF_ASSIGNMENT_EXPRESSION)) {
                    replacements.add(replacement);
                }
            }
        }
        return replacements;
    }

    public Set<Replacement> getReplacements() {
        Set<Replacement> replacements = new LinkedHashSet<>();
        for (CodeFragmentMapping mapping : getMappings()) {
            replacements.addAll(mapping.getReplacements());
        }
        return replacements;
    }

    public List<CodeFragmentMapping> getExactMatches() {
        List<CodeFragmentMapping> exactMatches = new ArrayList<>();
        for (CodeFragmentMapping mapping : getMappings()) {
            if (mapping.isExact(argumentizer) && mapping.fragment1.countableStatement()
                    && mapping.fragment2.countableStatement() &&
                    !mapping.fragment1.getText().equals("try"))
                exactMatches.add(mapping);
        }
        return exactMatches;
    }

    public int nonMappedLeafElementsT2() {
        int nonMappedLeafCount = 0;
        for (SingleStatement statement : getNonMappedLeavesT2()) {
            if (statement.countableStatement() /*&& !isTemporaryVariableAssignment(statement)*/)
                nonMappedLeafCount++;
        }
        return nonMappedLeafCount;
    }

    public int mappingsWithoutBlocks() {
        int count = 0;
        for (CodeFragmentMapping mapping : getMappings()) {
            if (mapping.fragment1.countableStatement() && mapping.fragment2.countableStatement())
                count++;
        }
        return count;
    }

    public void addChildMapper(FunctionBodyMapper mapper) {
        this.childMappers.add(mapper);
        //TODO add logic to remove the mappings from "this" mapper,
        //which are less similar than the mappings of the mapper passed as parameter
    }


    public int nonMappedElementsT1() {
        int nonMappedInnerNodeCount = 0;
        for (BlockStatement composite : getNonMappedInnerNodesT1()) {
            if (composite.countableStatement())
                nonMappedInnerNodeCount++;
        }
        int nonMappedLeafCount = 0;
        for (SingleStatement statement : getNonMappedLeavesT1()) {
            if (statement.countableStatement())
                nonMappedLeafCount++;
        }
        return nonMappedLeafCount + nonMappedInnerNodeCount;
    }

    public int nonMappedElementsT2() {
        int nonMappedInnerNodeCount = 0;
        for (BlockStatement composite : getNonMappedInnerNodesT2()) {
            if (composite.countableStatement())
                nonMappedInnerNodeCount++;
        }
        int nonMappedLeafCount = 0;
        for (SingleStatement statement : getNonMappedLeavesT2()) {
            if (statement.countableStatement() /*(&& !isTemporaryVariableAssignment(statement)*/)
                nonMappedLeafCount++;
        }
        return nonMappedLeafCount + nonMappedInnerNodeCount;
    }

    public int nonMappedElementsT2CallingAddedOperation(List<FunctionDeclaration> addedOperations) {
        int nonMappedInnerNodeCount = 0;
        for (BlockStatement composite : getNonMappedInnerNodesT2()) {
            if (composite.countableStatement()) {
                Map<String, List<OperationInvocation>> methodInvocationMap = composite.getMethodInvocationMap();
                for (String key : methodInvocationMap.keySet()) {
                    for (OperationInvocation invocation : methodInvocationMap.get(key)) {
                        for (FunctionDeclaration operation : addedOperations) {
                            if (invocation.matchesOperation(operation/*, operation2.variableTypeMap(), modelDiff*/)) {
                                nonMappedInnerNodeCount++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        int nonMappedLeafCount = 0;
        for (SingleStatement statement : getNonMappedLeavesT2()) {
            if (statement.countableStatement()) {
                Map<String, List<OperationInvocation>> methodInvocationMap = statement.getMethodInvocationMap();
                for (String key : methodInvocationMap.keySet()) {
                    for (OperationInvocation invocation : methodInvocationMap.get(key)) {
                        for (FunctionDeclaration operation : addedOperations) {
                            if (invocation.matchesOperation(operation/*, operation2.variableTypeMap(), modelDiff*/)) {
                                nonMappedLeafCount++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return nonMappedLeafCount + nonMappedInnerNodeCount;
    }

    public int nonMappedElementsT1CallingRemovedOperation(List<FunctionDeclaration> removedOperations) {
        int nonMappedInnerNodeCount = 0;
        for (BlockStatement composite : getNonMappedInnerNodesT1()) {
            if (composite.countableStatement()) {
                Map<String, List<OperationInvocation>> methodInvocationMap = composite.getMethodInvocationMap();
                for (String key : methodInvocationMap.keySet()) {
                    for (OperationInvocation invocation : methodInvocationMap.get(key)) {
                        for (FunctionDeclaration operation : removedOperations) {
                            if (invocation.matchesOperation(operation/*, function1.variableTypeMap(), modelDiff*/)) {
                                nonMappedInnerNodeCount++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        int nonMappedLeafCount = 0;
        for (SingleStatement statement : getNonMappedLeavesT1()) {
            if (statement.countableStatement()) {
                Map<String, List<OperationInvocation>> methodInvocationMap = statement.getMethodInvocationMap();
                for (String key : methodInvocationMap.keySet()) {
                    for (OperationInvocation invocation : methodInvocationMap.get(key)) {
                        for (FunctionDeclaration operation : removedOperations) {
                            if (invocation.matchesOperation(operation/*, operation1.variableTypeMap(), modelDiff*/)) {
                                nonMappedLeafCount++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return nonMappedLeafCount + nonMappedInnerNodeCount;
    }

    private boolean returnWithVariableReplacement(CodeFragmentMapping mapping) {
        if (mapping.getReplacements().size() == 1) {
            Replacement r = mapping.getReplacements().iterator().next();
            if (r.getType().equals(Replacement.ReplacementType.VARIABLE_NAME)) {
                String fragment1 = mapping.fragment1.getText();
                String fragment2 = mapping.fragment2.getText();
                if (fragment1.equals("return " + r.getBefore() + JsConfig.STATEMENT_TERMINATOR_CHAR)
                        && fragment2.equals("return " + r.getAfter() + JsConfig.STATEMENT_TERMINATOR_CHAR)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean nullLiteralReplacements(CodeFragmentMapping mapping) {
        int numberOfReplacements = mapping.getReplacements().size();
        int nullLiteralReplacements = 0;
        int methodInvocationReplacementsToIgnore = 0;
        int variableNameReplacementsToIgnore = 0;
        for (Replacement replacement : mapping.getReplacements()) {
            if (replacement.getType().equals(Replacement.ReplacementType.NULL_LITERAL_REPLACED_WITH_CONDITIONAL_EXPRESSION) ||
                    replacement.getType().equals(Replacement.ReplacementType.VARIABLE_REPLACED_WITH_NULL_LITERAL) ||
                    (replacement.getType().equals(Replacement.ReplacementType.ARGUMENT_REPLACED_WITH_VARIABLE) && (replacement.getBefore().equals("null") || replacement.getAfter().equals("null")))) {
                nullLiteralReplacements++;
            } else if (replacement instanceof MethodInvocationReplacement) {
                MethodInvocationReplacement invocationReplacement = (MethodInvocationReplacement) replacement;
                OperationInvocation invokedOperationBefore = invocationReplacement.getInvokedOperationBefore();
                OperationInvocation invokedOperationAfter = invocationReplacement.getInvokedOperationAfter();
                if (invokedOperationBefore.getFunctionName().equals(invokedOperationAfter.getFunctionName()) &&
                        invokedOperationBefore.getArguments().size() == invokedOperationAfter.getArguments().size()) {
                    methodInvocationReplacementsToIgnore++;
                }
            } else if (replacement.getType().equals(Replacement.ReplacementType.VARIABLE_NAME)) {
                variableNameReplacementsToIgnore++;
            }
        }
        return nullLiteralReplacements > 0 && numberOfReplacements == nullLiteralReplacements + methodInvocationReplacementsToIgnore + variableNameReplacementsToIgnore;
    }
}
