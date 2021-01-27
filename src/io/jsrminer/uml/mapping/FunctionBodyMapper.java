package io.jsrminer.uml.mapping;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.CandidateAttributeRefactoring;
import io.jsrminer.refactorings.CandidateMergeVariableRefactoring;
import io.jsrminer.refactorings.CandidateSplitVariableRefactoring;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.jsrminer.uml.diff.SourceFileDiff;
import io.jsrminer.uml.diff.StringDistance;
import io.jsrminer.uml.diff.UMLOperationDiff;
import io.jsrminer.uml.mapping.replacement.*;
import io.rminer.core.api.IAnonymousFunctionDeclaration;
import io.rminer.core.api.IFunctionDeclaration;

import java.util.*;

public class FunctionBodyMapper implements Comparable<FunctionBodyMapper> {

    public FunctionDeclaration function1;
    public FunctionDeclaration function2;
    public UMLOperationDiff operationDiff;

    public static final Argumentizer argumentizer = new Argumentizer();

    private Set<CodeFragmentMapping> mappings = new LinkedHashSet<>();
    Map<String, String> parameterToArgumentMap1 = new LinkedHashMap<>();
    Map<String, String> parameterToArgumentMap2 = new LinkedHashMap<>();

    private final Set<SingleStatement> nonMappedLeavesT1 = new LinkedHashSet<>();
    private final Set<SingleStatement> nonMappedLeavesT2 = new LinkedHashSet<>();
    private final Set<BlockStatement> nonMappedInnerNodesT1 = new LinkedHashSet<>();
    private final Set<BlockStatement> nonMappedInnerNodesT2 = new LinkedHashSet<>();

    private FunctionDeclaration callerFunction;
    private SourceFileDiff sourceFileDiff;
    private final List<FunctionBodyMapper> childMappers = new ArrayList<>();
    private FunctionBodyMapper parentMapper;
    private Set<IRefactoring> refactorings = new LinkedHashSet<>();
    private Map<CodeFragment, FunctionDeclaration> codeFragmentOperationMap1 = new LinkedHashMap<>();
    private Map<CodeFragment, FunctionDeclaration> codeFragmentOperationMap2 = new LinkedHashMap<>();

    private Set<CandidateAttributeRefactoring> candidateAttributeRenames = new LinkedHashSet<>();
    private Set<CandidateMergeVariableRefactoring> candidateAttributeMerges = new LinkedHashSet<>();
    private Set<CandidateSplitVariableRefactoring> candidateAttributeSplits = new LinkedHashSet<>();

    public FunctionBodyMapper(UMLOperationDiff operationDiff
            , SourceFileDiff sourceFileDiff) {
        this.operationDiff = operationDiff;
        this.function1 = operationDiff.function1;
        this.function2 = operationDiff.function2;
        this.sourceFileDiff = sourceFileDiff;
    }

    public FunctionBodyMapper(FunctionDeclaration function1, FunctionDeclaration function2
            , SourceFileDiff sourceFileDiff) {
        this(new UMLOperationDiff(function1, function2), sourceFileDiff);
    }

    /**
     * Tries to mapp the function1 of the mapper with the added operation
     */
    public FunctionBodyMapper(FunctionBodyMapper mapper, FunctionDeclaration addedOperation, SourceFileDiff sourceFileDiff

            , Map<String, String> parameterToArgumentMap1
            , Map<String, String> parameterToArgumentMap2) {
        this.function1 = mapper.function1;
        this.function2 = addedOperation;
        this.callerFunction = mapper.function2;
        this.sourceFileDiff = sourceFileDiff;
        this.operationDiff = new UMLOperationDiff(this.function1, this.function2);
        this.parentMapper = mapper;
        this.parameterToArgumentMap1 = parameterToArgumentMap1;
        this.parameterToArgumentMap2 = parameterToArgumentMap2;
    }

    public FunctionBodyMapper(FunctionDeclaration removedOperation, FunctionBodyMapper operationBodyMapper
            /*, Map<String, String> parameterToArgumentMap*/, SourceFileDiff classDiff) {
        this.parentMapper = operationBodyMapper;
        this.function1 = removedOperation;
        this.function2 = operationBodyMapper.function2;
        this.callerFunction = operationBodyMapper.function1;
        this.sourceFileDiff = classDiff;
        this.operationDiff = new UMLOperationDiff(this.function1, this.function2);
    }

    /**
     * Maps two sets of statements similar to lambda body mapper
     */
    public FunctionBodyMapper(FunctionDeclaration function1, FunctionDeclaration function2) {

        this.function1 = function1;
        this.function2 = function2;

        List<Statement> statements1 = function1.getBody().blockStatement.getStatements();
        List<Statement> statements2 = function2.getBody().blockStatement.getStatements();
        if (statements1.size() > 0 && statements2.size() > 0) {
            // Add all leaves and composite from statements
            LinkedHashSet<SingleStatement> leaves1 = new LinkedHashSet<>();
            LinkedHashSet<SingleStatement> leaves2 = new LinkedHashSet<>();
            LinkedHashSet<BlockStatement> innerNodes1 = new LinkedHashSet<>();
            LinkedHashSet<BlockStatement> innerNodes2 = new LinkedHashSet<>();

//            for (Statement statement : statements1) {
//                if (statement instanceof SingleStatement) {
//                    leaves1.add((SingleStatement) statement);
//                } else {
//                    innerNodes1.add((BlockStatement) statement);
//                }
//            }
//
//            for (Statement statement : statements2) {
//                if (statement instanceof SingleStatement) {
//                    leaves2.add((SingleStatement) statement);
//                } else {
//                    innerNodes2.add((BlockStatement) statement);
//                }
//            }

            leaves1.addAll(getLeavesRecursiveOrder(statements1));
            leaves2.addAll(getLeavesRecursiveOrder(statements2));

            // First round match with immediate leaves vs leaves
            matchLeaves(leaves1, leaves2, new LinkedHashMap<>());

            innerNodes1.addAll(getBlockStatementsRecursiveOrder(statements1));
            innerNodes2.addAll(getBlockStatementsRecursiveOrder(statements2));

            matchBlockStatements(innerNodes1, innerNodes2, new LinkedHashMap<>());

            // 2nd round match with each composites and their leaves

            nonMappedLeavesT1.addAll(leaves1);
            nonMappedLeavesT2.addAll(leaves2);
            nonMappedInnerNodesT1.addAll(innerNodes1);
            nonMappedInnerNodesT2.addAll(innerNodes2);

//            for (StatementObject statement : getNonMappedLeavesT2()) {
//                temporaryVariableAssignment(statement, nonMappedLeavesT2);
//            }
//            for (StatementObject statement : getNonMappedLeavesT1()) {
//                inlinedVariableAssignment(statement, nonMappedLeavesT2);
//
        }
    }

    private Set<SingleStatement> getLeavesRecursiveOrder(List<Statement> statements) {
        final Set<SingleStatement> leaves = new LinkedHashSet<>();
        for (Statement statement : statements) {
            if (statement instanceof BlockStatement) {
                leaves.addAll(((BlockStatement) statement).getAllLeafStatementsIncludingNested());
            } else {
                leaves.add((SingleStatement) statement);
            }
        }
        return leaves;
    }

    private Set<BlockStatement> getBlockStatementsRecursiveOrder(List<Statement> statements) {
        final Set<BlockStatement> innerNodes = new LinkedHashSet<>();
        for (Statement statement : statements) {
            if (statement instanceof BlockStatement) {
                BlockStatement composite = (BlockStatement) statement;
                innerNodes.addAll(composite.getAllBlockStatementsIncludingNested());
            }
        }
        //innerNodes.add(this);
        return innerNodes;
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

            this.nonMappedLeavesT1.addAll(leaves1);
            this.nonMappedLeavesT2.addAll(leaves2);

            // Match composites

            Set<BlockStatement> innerNodes1 = new LinkedHashSet<>(block1.getAllBlockStatementsIncludingNested());
            Set<BlockStatement> innerNodes2 = new LinkedHashSet<>(block2.getAllBlockStatementsIncludingNested());

            // TODO improve recirson of innerNodes by preventing adding itself
            innerNodes1.remove(block1);
            innerNodes2.remove(block2);

            argumentizer.clearCache(innerNodes1, innerNodes2);
            replaceParametersWithArguments(innerNodes1, innerNodes2);
            if (innerNodes1.size() > 0 && innerNodes2.size() > 0)
                matchBlockStatements(innerNodes1, innerNodes2, new LinkedHashMap<>());

            this.nonMappedInnerNodesT1.addAll(innerNodes1);
            this.nonMappedInnerNodesT2.addAll(innerNodes2);

            // Set mappings
            this.operationDiff.setMappings(this.mappings);
        }
    }

    public void mapRemovedOperation(Map<String, String> parameterToArgumentMap) {
        FunctionDeclaration removedOperation = function1;
        FunctionBodyMapper operationBodyMapper = this.parentMapper;

        FunctionBody removedOperationBody = removedOperation.getBody();
        if (removedOperationBody != null) {
            BlockStatement composite1 = removedOperationBody.blockStatement;
            Set<SingleStatement> leaves1 = new LinkedHashSet<>(composite1.getAllLeafStatementsIncludingNested());
            Set<SingleStatement> leaves2 = operationBodyMapper.getNonMappedLeavesT2();

            //adding leaves that were mapped with replacements or are inexact matches
            Set<SingleStatement> addedLeaves2 = new LinkedHashSet<>();
            for (CodeFragmentMapping mapping : operationBodyMapper.getMappings()) {
                if ((mapping.fragment2 instanceof SingleStatement)
                        && !returnWithVariableReplacement(mapping)
                        && !nullLiteralReplacements(mapping)
                        && (!mapping.getReplacements().isEmpty()
                        || !mapping.equalFragmentText())
                ) {
                    SingleStatement statement = (SingleStatement) mapping.getFragment2();
                    if (!leaves2.contains(statement)) {
                        leaves2.add(statement);

                        //addedLeaves2.add((SingleStatement) mapping.fragment2);
                        addedLeaves2.add(statement);
                    }
                }
            }

            //resetNodes(leaves1);
            argumentizer.clearCache(leaves1);

            //replace parameters with arguments in leaves1
            if (!parameterToArgumentMap.isEmpty()) {
                //check for temporary variables that the argument might be assigned to
                for (SingleStatement leave2 : leaves2) {
                    List<VariableDeclaration> variableDeclarations = leave2.getVariableDeclarations();
                    for (VariableDeclaration variableDeclaration : variableDeclarations) {
                        for (String parameter : parameterToArgumentMap.keySet()) {
                            String argument = parameterToArgumentMap.get(parameter);
                            if (variableDeclaration.getInitializer() != null && argument.equals(variableDeclaration.getInitializer().toString())) {
                                parameterToArgumentMap.put(parameter, variableDeclaration.variableName);
                            }
                        }
                    }
                }
                for (SingleStatement leave1 : leaves1) {
                    //leave1.replaceParametersWithArguments(parameterToArgumentMap);
                    argumentizer.replaceParametersWithArguments(leave1, parameterToArgumentMap);
                }
            }

            //compare leaves from T1 with leaves from T2
            matchLeaves(leaves1, leaves2, parameterToArgumentMap);

            Set<BlockStatement> innerNodes1 = composite1.getAllBlockStatementsIncludingNested();
            innerNodes1.remove(composite1);

            Set<BlockStatement> innerNodes2 = operationBodyMapper.getNonMappedInnerNodesT2();

            //adding innerNodes that were mapped with replacements or are inexact matches
            Set<BlockStatement> addedInnerNodes2 = new LinkedHashSet<>();
            for (CodeFragmentMapping mapping : operationBodyMapper.getMappings()) {
                if ((mapping.fragment2 instanceof BlockStatement)
                        && (!mapping.getReplacements().isEmpty() || !mapping.equalFragmentText())) {

                    BlockStatement statement = (BlockStatement) mapping.fragment2;
                    if (!innerNodes2.contains(statement)) {
                        innerNodes2.add(statement);
                        addedInnerNodes2.add(statement);
                    }
                }
            }

            //resetNodes(innerNodes1);
            argumentizer.clearCache(innerNodes1);

            //replace parameters with arguments in innerNodes1
            if (!parameterToArgumentMap.isEmpty()) {
                for (BlockStatement innerNode1 : innerNodes1) {
                    //innerNode1.replaceParametersWithArguments(parameterToArgumentMap);
                    argumentizer.replaceParametersWithArguments(innerNode1, parameterToArgumentMap);
                }
            }

            //compare inner nodes from T1 with inner nodes from T2
            matchBlockStatements(innerNodes1, innerNodes2, parameterToArgumentMap);

            //match expressions in inner nodes from T2 with leaves from T1
            Set<Expression> expressionsT2 = new LinkedHashSet<>();
            for (BlockStatement composite : operationBodyMapper.getNonMappedInnerNodesT2()) {
                for (Expression expression : composite.getExpressions()) {
                    expressionsT2.add(expression);
                }
            }

            matchLeaves(leaves1, expressionsT2, parameterToArgumentMap);

            //remove the leaves that were mapped with replacement, if they are not mapped again for a second time
            leaves2.removeAll(addedLeaves2);
            //remove the innerNodes that were mapped with replacement, if they are not mapped again for a second time
            innerNodes2.removeAll(addedInnerNodes2);

            nonMappedLeavesT1.addAll(leaves1);
            nonMappedLeavesT2.addAll(leaves2);
            nonMappedInnerNodesT1.addAll(innerNodes1);
            nonMappedInnerNodesT2.addAll(innerNodes2);

//            for (StatementObject statement : getNonMappedLeavesT2()) {
//                temporaryVariableAssignment(statement, nonMappedLeavesT2);
//            }
//            for (StatementObject statement : getNonMappedLeavesT1()) {
//                inlinedVariableAssignment(statement, nonMappedLeavesT2);
//            }
        }
    }

    /**
     * Maps added operation to function1
     */
    public void mapAddedOperation() {

        FunctionBody addedOperationBody = function2.getBody();
        FunctionBodyMapper operationBodyMapper = this.parentMapper;
        if (addedOperationBody != null) {

            BlockStatement composite2 = addedOperationBody.blockStatement;
            Set<SingleStatement> leaves1 = operationBodyMapper.getNonMappedLeavesT1();
            Set<BlockStatement> innerNodes1 = operationBodyMapper.getNonMappedInnerNodesT1();

            //adding leaves that were mapped with replacements
            Set<SingleStatement> addedLeaves1 = new LinkedHashSet<>();
            Set<BlockStatement> addedInnerNodes1 = new LinkedHashSet<>();
            for (SingleStatement nonMappedLeaf1 : new ArrayList<>(operationBodyMapper.getNonMappedLeavesT1())) {
                expandAnonymousAndLambdas(nonMappedLeaf1, leaves1, innerNodes1, addedLeaves1, addedInnerNodes1, operationBodyMapper);
            }
            for (CodeFragmentMapping mapping : operationBodyMapper.getMappings()) {
                if (!returnWithVariableReplacement(mapping) && !nullLiteralReplacements(mapping) && (!mapping.getReplacements().isEmpty() || !mapping.equalFragmentText())) {
                    CodeFragment fragment = mapping.getFragment1();
                    expandAnonymousAndLambdas(fragment, leaves1, innerNodes1, addedLeaves1, addedInnerNodes1, operationBodyMapper);
                }
            }
            Set<SingleStatement> leaves2 = new LinkedHashSet<>(composite2.getAllLeafStatementsIncludingNested());
            Set<BlockStatement> innerNodes2 = composite2.getAllBlockStatementsIncludingNested();

            Set<SingleStatement> addedLeaves2 = new LinkedHashSet<>();
            Set<BlockStatement> addedInnerNodes2 = new LinkedHashSet<>();
            for (SingleStatement statement : leaves2) {
                if (!statement.getAnonymousFunctionDeclarations().isEmpty()) {
                    List<IAnonymousFunctionDeclaration> anonymousList = this.function2.getAnonymousFunctionDeclarations();
                    for (IAnonymousFunctionDeclaration anonymous : anonymousList) {
                        if (FunctionUtil.isDirectlyNested(anonymous)
                                && statement.getSourceLocation().subsumes(anonymous.getSourceLocation())) {
                            for (IFunctionDeclaration anonymousOperation : anonymous.getFunctionDeclarations()) {
                                List<SingleStatement> anonymousClassLeaves
                                        = anonymousOperation.getBody().blockStatement.getAllLeafStatementsIncludingNested();
                                for (SingleStatement anonymousLeaf : anonymousClassLeaves) {
                                    if (!leaves2.contains(anonymousLeaf)) {
                                        addedLeaves2.add(anonymousLeaf);
                                        codeFragmentOperationMap2.put(anonymousLeaf, (FunctionDeclaration) anonymousOperation);
                                    }
                                }
                                Set<BlockStatement> anonymousClassInnerNodes
                                        = anonymousOperation.getBody().blockStatement.getAllBlockStatementsIncludingNested();
                                for (BlockStatement anonymousInnerNode : anonymousClassInnerNodes) {
                                    if (!innerNodes2.contains(anonymousInnerNode)) {
                                        addedInnerNodes2.add(anonymousInnerNode);
                                        codeFragmentOperationMap2.put(anonymousInnerNode, (FunctionDeclaration) anonymousOperation);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            leaves2.addAll(addedLeaves2);
            argumentizer.clearCache(leaves1);
            //resetNodes(leaves1);
            //replace parameters with arguments in leaves1
            if (!parameterToArgumentMap1.isEmpty()) {
                for (SingleStatement leave1 : leaves1) {
                    argumentizer.replaceParametersWithArguments(leave1, parameterToArgumentMap1);
                    //leave1.replaceParametersWithArguments(parameterToArgumentMap1);
                }
            }
            argumentizer.clearCache(leaves2);
            //replace parameters with arguments in leaves2
            if (!parameterToArgumentMap2.isEmpty()) {
                for (SingleStatement leave2 : leaves2) {
                    //leave2.replaceParametersWithArguments(parameterToArgumentMap2);
                    argumentizer.replaceParametersWithArguments(leave2, parameterToArgumentMap2);
                }
            }
            //compare leaves from T1 with leaves from T2
            if (leaves1.size() > 0 && leaves2.size() > 0)
                matchLeaves(leaves1, leaves2, parameterToArgumentMap2);

            //adding innerNodes that were mapped with replacements
            for (CodeFragmentMapping mapping : operationBodyMapper.getMappings()) {
                if (!mapping.getReplacements().isEmpty()
                        || !mapping.equalFragmentText()) {
                    CodeFragment fragment = mapping.getFragment1();
                    if (fragment instanceof BlockStatement) {
                        BlockStatement statement = (BlockStatement) fragment;
                        if (!innerNodes1.contains(statement)) {
                            innerNodes1.add(statement);
                            addedInnerNodes1.add(statement);
                        }
                    }
                }
            }
            innerNodes2.remove(composite2);
            innerNodes2.addAll(addedInnerNodes2);
            //resetNodes(innerNodes1);
            argumentizer.clearCache(innerNodes1);

            //replace parameters with arguments in innerNodes1
            if (!parameterToArgumentMap1.isEmpty()) {
                for (BlockStatement innerNode1 : innerNodes1) {
                    //innerNode1.replaceParametersWithArguments(parameterToArgumentMap1);
                    argumentizer.replaceParametersWithArguments(innerNode1, parameterToArgumentMap1);
                }
            }
            //resetNodes(innerNodes2);
            argumentizer.clearCache(innerNodes2);
            //replace parameters with arguments in innerNode2
            if (!parameterToArgumentMap2.isEmpty()) {
                for (BlockStatement innerNode2 : innerNodes2) {
                    //innerNode2.replaceParametersWithArguments(parameterToArgumentMap2);
                    argumentizer.replaceParametersWithArguments(innerNode2, parameterToArgumentMap2);
                }
            }
            //compare inner nodes from T1 with inner nodes from T2
            matchBlockStatements(innerNodes1, innerNodes2, parameterToArgumentMap2);

            //match expressions in inner nodes from T1 with leaves from T2
            Set<Expression> expressionsT1 = new LinkedHashSet<>();
            for (BlockStatement composite : operationBodyMapper.getNonMappedInnerNodesT1()) {
                for (Expression expression : composite.getExpressions()) {
                    //expression.replaceParametersWithArguments(parameterToArgumentMap1);
                    argumentizer.replaceParametersWithArguments(expression, parameterToArgumentMap1);
                    expressionsT1.add(expression);
                }
            }
            int numberOfMappings = mappings.size();
            matchLeaves(expressionsT1, leaves2, parameterToArgumentMap2);
            List<CodeFragmentMapping> mappings = new ArrayList<>(this.mappings);

//            for (int i = numberOfMappings; i < mappings.size(); i++) {
//                mappings.get(i).temporaryVariableAssignment(refactorings);
//            }
//
            // TODO remove non-mapped inner nodes from T1 corresponding to mapped expressions
            //remove the leaves that were mapped with replacement, if they are not mapped again for a second time
            leaves1.removeAll(addedLeaves1);
            leaves2.removeAll(addedLeaves2);
            //remove the innerNodes that were mapped with replacement, if they are not mapped again for a second time
            innerNodes1.removeAll(addedInnerNodes1);
            innerNodes2.removeAll(addedInnerNodes2);
            nonMappedLeavesT1.addAll(leaves1);
            nonMappedLeavesT2.addAll(leaves2);
            nonMappedInnerNodesT1.addAll(innerNodes1);
            nonMappedInnerNodesT2.addAll(innerNodes2);

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

        List<TreeSet<LeafCodeFragmentMapping>> postponedMappingSets = matchLeavesWithVariableRenames(leaves1, leaves2, parameterToArgumentMap);

        for (TreeSet<LeafCodeFragmentMapping> postponed : postponedMappingSets) {
            Set<LeafCodeFragmentMapping> mappingsToBeAdded = new LinkedHashSet<>();
            for (LeafCodeFragmentMapping variableDeclarationMapping : postponed) {
                for (CodeFragmentMapping previousMapping : this.mappings) {
                    Set<Replacement> intersection
                            = variableDeclarationMapping.commonReplacements(previousMapping);
                    if (!intersection.isEmpty()) {
                        for (Replacement commonReplacement : intersection) {
                            if (commonReplacement.getType().equals(ReplacementType.VARIABLE_NAME) &&
                                    variableDeclarationMapping.getFragment1().getVariableDeclaration(commonReplacement.getBefore()) != null &&
                                    variableDeclarationMapping.getFragment2().getVariableDeclaration(commonReplacement.getAfter()) != null) {
                                mappingsToBeAdded.add(variableDeclarationMapping);
                            }
                        }
                    }
                }
            }
            if (mappingsToBeAdded.size() == 1) {
                LeafCodeFragmentMapping minStatementMapping = mappingsToBeAdded.iterator().next();
                this.mappings.add(minStatementMapping);
                leaves1.remove(minStatementMapping.getFragment1());
                leaves2.remove(minStatementMapping.getFragment2());
            } else {
                LeafCodeFragmentMapping minStatementMapping = postponed.first();
                this.mappings.add(minStatementMapping);
                leaves1.remove(minStatementMapping.getFragment1());
                leaves2.remove(minStatementMapping.getFragment2());
            }
        }
    }

    void matchLeavesWithIdenticalText(Set<? extends CodeFragment> leaves1, Set<? extends
            CodeFragment> leaves2, boolean ignoreNestingDepth, Map<String, String> parameterToArgumentMap) {

        if (leaves1.size() <= leaves2.size()) {
            //exact string matching
            for (ListIterator<? extends CodeFragment> listIterator1 = new ArrayList<>(leaves1).listIterator(); listIterator1.hasNext(); ) {
                CodeFragment leaf1 = listIterator1.next();
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
                    listIterator1.remove();
                    leaves1.remove(minStatementMapping.fragment1);
                }
            }
        } else {
            //exact string+depth matching - leaf nodes
            for (ListIterator<? extends CodeFragment> listIterator2 = new ArrayList<>(leaves2).listIterator(); listIterator2.hasNext(); ) {
                CodeFragment leaf2 = listIterator2.next();
                TreeSet<LeafCodeFragmentMapping> mappingSet = new TreeSet<>();
                for (Iterator<? extends CodeFragment> leafIterator1 = leaves1.iterator(); leafIterator1.hasNext(); ) {
                    CodeFragment leaf1 = leafIterator1.next();
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
                    leaves1.remove(minStatementMapping.getFragment1());
                    listIterator2.remove();
                    leaves2.remove(minStatementMapping.getFragment2());
                }
            }
        }
    }

    private List<TreeSet<LeafCodeFragmentMapping>> matchLeavesWithVariableRenames(Set<? extends CodeFragment> leaves1, Set<? extends
            CodeFragment> leaves2, Map<String, String> parameterToArgumentMap) {
        ReplacementFinder replacementFinder = new ReplacementFinder(this, sourceFileDiff);
        List<TreeSet<LeafCodeFragmentMapping>> postponedMappingSets = new ArrayList<>();

        Iterator<? extends CodeFragment> it1 = leaves1.iterator();
        Iterator<? extends CodeFragment> it2 = leaves2.iterator();

        // TODO refactor duplicateed code, extract inner for loop to seprate method
        if (leaves1.size() <= leaves2.size()) {
            for (ListIterator<? extends CodeFragment> listIterator1 = new ArrayList<>(leaves1).listIterator(); listIterator1.hasNext(); ) {
                CodeFragment leaf1 = listIterator1.next();
                TreeSet<LeafCodeFragmentMapping> mappingSet = new TreeSet<>();

                for (Iterator<? extends CodeFragment> iterator2 = leaves2.iterator(); iterator2.hasNext(); ) {
                    CodeFragment leaf2 = iterator2.next();

                    LeafCodeFragmentMapping mapping = getLeafMappingUsingReplacements(leaf1, leaf2, leaves1, leaves2, parameterToArgumentMap, replacementFinder);
                    if (mapping != null) {
                        mappingSet.add(mapping);
                    }
                }

                if (!mappingSet.isEmpty()) {
                    AbstractMap.SimpleEntry<BlockStatement, BlockStatement> switchParentEntry = null;
                    if (variableDeclarationMappingsWithSameReplacementTypes(mappingSet)) {
                        //postpone mapping
                        postponedMappingSets.add(mappingSet);
                    } else if ((switchParentEntry = multipleMappingsUnderTheSameSwitch(mappingSet)) != null) {
                        LeafCodeFragmentMapping bestMapping = findBestMappingBasedOnMappedSwitchCases(switchParentEntry, mappingSet);
                        mappings.add(bestMapping);
                        leaves2.remove(bestMapping.getFragment2());
                        listIterator1.remove();
                        leaves1.remove((bestMapping.getFragment1()));
                    } else {
                        LeafCodeFragmentMapping minStatementMapping = mappingSet.first();
                        mappings.add(minStatementMapping);
                        leaves2.remove(minStatementMapping.getFragment2());
                        listIterator1.remove();
                        leaves1.remove(minStatementMapping.getFragment1());
                    }
                }
            }
        } else {
            for (ListIterator<? extends CodeFragment> listIterator2 = new ArrayList<>(leaves2).listIterator(); listIterator2.hasNext(); ) {
                CodeFragment leaf2 = listIterator2.next();
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
                    AbstractMap.SimpleEntry<BlockStatement, BlockStatement> switchParentEntry = null;
                    if (variableDeclarationMappingsWithSameReplacementTypes(mappingSet)) {
                        //postpone mapping
                        postponedMappingSets.add(mappingSet);
                    } else if ((switchParentEntry = multipleMappingsUnderTheSameSwitch(mappingSet)) != null) {
                        CodeFragmentMapping bestMapping = findBestMappingBasedOnMappedSwitchCases(switchParentEntry, mappingSet);
                        mappings.add(bestMapping);
                        leaves1.remove(bestMapping.getFragment1());
                        listIterator2.remove();
                        leaves2.remove(bestMapping.getFragment2());
                    } else {
                        LeafCodeFragmentMapping minStatementMapping = mappingSet.first();
                        mappings.add(minStatementMapping);
                        leaves1.remove(minStatementMapping.getFragment1());
                        listIterator2.remove();
                        leaves2.remove(minStatementMapping.getFragment2());
                    }
                }
            }
        }
        return postponedMappingSets;
    }

    /**
     * Match the block statements inside of the body of a function
     */
    void matchBlockStatements(Set<BlockStatement> innerNodes1, Set<BlockStatement> innerNodes2
            , Map<String, String> parameterToArgumentMap) {
        //exact string+depth matching - inner nodes
        matchInnerNodesWithIdenticalText(innerNodes1, innerNodes2, parameterToArgumentMap, false);
        matchInnerNodesWithIdenticalText(innerNodes1, innerNodes2, parameterToArgumentMap, true);
        matchInnerNodesWithVariableRenames(innerNodes1, innerNodes2, parameterToArgumentMap);
    }

    void matchInnerNodesWithIdenticalText(Set<BlockStatement> innerNodes1
            , Set<BlockStatement> innerNodes2, Map<String, String> parameterToArgumentMap
            , boolean ignoreNestingDepth) {

        List<FunctionDeclaration> removedOperations = sourceFileDiff != null ? sourceFileDiff.getRemovedOperations() : new ArrayList<>();
        List<FunctionDeclaration> addedOperations = sourceFileDiff != null ? sourceFileDiff.getAddedOperations() : new ArrayList<>();

        if (innerNodes1.size() <= innerNodes2.size()) {
            for (ListIterator<BlockStatement> listIterator1 = new ArrayList<>(innerNodes1).listIterator(); listIterator1.hasNext(); ) {
                BlockStatement statement1 = listIterator1.next();
                TreeSet<BlockCodeFragmentMapping> sortedMappingSet = new TreeSet<>();
                for (Iterator<BlockStatement> innerNodeIterator2 = innerNodes2.iterator(); innerNodeIterator2.hasNext(); ) {
                    BlockStatement statement2 = innerNodeIterator2.next();
                    double score = ChildCountMatcher.computeScore(statement1, statement2
                            , removedOperations, addedOperations
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
                    innerNodes2.remove(minStatementMapping.getFragment2());
                    listIterator1.remove();
                    innerNodes1.remove(minStatementMapping.getFragment1());
                }
            }
        } else {
            //exact string+depth matching - inner nodes
            for (ListIterator<BlockStatement> listIterator2 = new ArrayList<>(innerNodes2).listIterator(); listIterator2.hasNext(); ) {
                BlockStatement statement2 = listIterator2.next();
                TreeSet<BlockCodeFragmentMapping> sortedMappingSet = new TreeSet<>();

                for (Iterator<BlockStatement> iterator1 = innerNodes1.iterator(); iterator1.hasNext(); ) {
                    BlockStatement statement1 = iterator1.next();
                    double score = ChildCountMatcher.computeScore(statement1, statement2
                            , removedOperations, addedOperations
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
                    listIterator2.remove();
                    innerNodes2.remove(minStatementMapping.fragment2);
                }
            }
        }
    }

    private void matchInnerNodesWithVariableRenames
            (Set<BlockStatement> innerNodes1, Set<BlockStatement> innerNodes2, Map<String, String> parameterToArgumentMap) {
        // exact matching - inner nodes - with variable renames
        ReplacementFinder replacementFinder = new ReplacementFinder(this, sourceFileDiff);

        if (innerNodes1.size() <= innerNodes2.size()) {
            // exact matching - inner nodes - with variable renames
            for (ListIterator<BlockStatement> listIterator1 = new ArrayList<>(innerNodes1).listIterator(); listIterator1.hasNext(); ) {
                BlockStatement statement1 = listIterator1.next();
                TreeSet<BlockCodeFragmentMapping> mappingSet = new TreeSet<>();
                for (Iterator<BlockStatement> innerNodeIterator2 = innerNodes2.iterator(); innerNodeIterator2.hasNext(); ) {
                    BlockStatement statement2 = innerNodeIterator2.next();

                    BlockCodeFragmentMapping mapping = getCompositeMappingUsingReplacements(statement1, statement2,
                            innerNodes1, innerNodes2
                            , parameterToArgumentMap, replacementFinder);

                    if (mapping != null)
                        mappingSet.add(mapping);
                }
                if (!mappingSet.isEmpty()) {
                    BlockCodeFragmentMapping minStatementMapping = mappingSet.first();
                    mappings.add(minStatementMapping);
                    innerNodes2.remove(minStatementMapping.getFragment2());
                    listIterator1.remove();
                    innerNodes1.remove(minStatementMapping.getFragment1());
                }
            }
        } else {
            for (ListIterator<BlockStatement> listIterator2 = new ArrayList<>(innerNodes2).listIterator(); listIterator2.hasNext(); ) {
                BlockStatement statement2 = listIterator2.next();
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
                    listIterator2.remove();
                    innerNodes2.remove(minStatementMapping.fragment2);
                }
            }
        }

    }

    private BlockCodeFragmentMapping getCompositeMappingUsingReplacements(BlockStatement
                                                                                  statement1, BlockStatement
                                                                                  statement2
            , Set<BlockStatement> innerNodes1, Set<BlockStatement> innerNodes2
            , Map<String, String> parameterToArgumentMap, ReplacementFinder replacementFinder) {

        List<FunctionDeclaration> removedOperations = sourceFileDiff != null ? sourceFileDiff.getRemovedOperations() : new ArrayList<>();
        List<FunctionDeclaration> addedOperations = sourceFileDiff != null ? sourceFileDiff.getAddedOperations() : new ArrayList<>();

        ReplacementInfo replacementInfo = createReplacementInfo(statement1, statement2, innerNodes1, innerNodes2);
        Set<Replacement> replacements = replacementFinder.findReplacementsWithExactMatching(statement1
                , statement2
                , parameterToArgumentMap
                , replacementInfo,
                this.argumentizer);

        if (replacements != null) {
            double score = ChildCountMatcher.computeScore(statement1, statement2
                    , removedOperations, addedOperations
                    , this.mappings, this.parentMapper != null);

            if (score == 0 && replacements.size() == 1 &&
                    (replacements.iterator().next().getType().equals(ReplacementType.INFIX_OPERATOR)
                            || replacements.iterator().next().getType().equals(ReplacementType.INVERT_CONDITIONAL))) {
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
                argumentizer);
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

    public Set<IRefactoring> getRefactoringsAfterPostProcessing() {
        return refactorings;
    }

    public SourceFileDiff getContainerDiff() {
        return sourceFileDiff;
    }

    public Set<IRefactoring> getRefactoringsByVariableAnalysis() {
        VariableReplacementAnalysis analysis = new VariableReplacementAnalysis(this, refactorings, sourceFileDiff);
        // Local (variables & Paramters)
        refactorings.addAll(analysis.getVariableRenames());
        refactorings.addAll(analysis.getVariableMerges());
        refactorings.addAll(analysis.getVariableSplits());

        // Parent container (Attributes)
        candidateAttributeRenames.addAll(analysis.getCandidateAttributeRenames());
        candidateAttributeMerges.addAll(analysis.getCandidateAttributeMerges());
        candidateAttributeSplits.addAll(analysis.getCandidateAttributeSplits());
        //TypeReplacementAnalysis typeAnalysis = new TypeReplacementAnalysis(this.getMappings());
        //refactorings.addAll(typeAnalysis.getChangedTypes());
        return refactorings;
    }

    // TODO: Similar to processInput
    private String createArgumentizedString(CodeFragment statement1, CodeFragment statement2) {
        String argumentizedString = argumentizer.getArgumentizedString(statement1);


        // TODO replace return value with the argumentaized string
        if (statement1 instanceof SingleStatement && statement2 instanceof Expression) {
            if (argumentizedString.startsWith("return ") && argumentizedString.endsWith(JsConfig.STATEMENT_TERMINATOR_CHAR + "")) {
                argumentizedString = argumentizedString.substring("return ".length(),
                        argumentizedString.lastIndexOf(JsConfig.STATEMENT_TERMINATOR_CHAR));
            }
        }
        return argumentizedString;
    }

    private LeafCodeFragmentMapping createLeafMapping(CodeFragment leaf1, CodeFragment
            leaf2, Map<String, String> parameterToArgumentMap) {
        FunctionDeclaration operation1 = codeFragmentOperationMap1.containsKey(leaf1) ? codeFragmentOperationMap1.get(leaf1) : this.function1;
        FunctionDeclaration operation2 = codeFragmentOperationMap2.containsKey(leaf2) ? codeFragmentOperationMap2.get(leaf2) : this.function2;
        LeafCodeFragmentMapping mapping = new LeafCodeFragmentMapping(leaf1, leaf2, function1, function2, argumentizer);
        for (String key : parameterToArgumentMap.keySet()) {
            String value = parameterToArgumentMap.get(key);
            if (!key.equals(value) && ReplacementUtil.contains(leaf2.getText(), key) && ReplacementUtil.contains(leaf1.getText(), value)) {
                mapping.addReplacement(new Replacement(value, key, ReplacementType.VARIABLE_NAME));
            }
        }
        return mapping;
    }

    private BlockCodeFragmentMapping createCompositeMapping(BlockStatement statement1,
                                                            BlockStatement statement2
            , Map<String, String> parameterToArgumentMap, double score) {

        FunctionDeclaration operation1 = codeFragmentOperationMap1.containsKey(statement1) ? codeFragmentOperationMap1.get(statement1) : this.function1;
        FunctionDeclaration operation2 = codeFragmentOperationMap2.containsKey(statement2) ? codeFragmentOperationMap2.get(statement2) : this.function2;
        BlockCodeFragmentMapping mapping = new BlockCodeFragmentMapping(statement1, statement2, score, operation1, operation2, argumentizer);
        for (String key : parameterToArgumentMap.keySet()) {
            String value = parameterToArgumentMap.get(key);
            if (!key.equals(value) && ReplacementUtil.contains(statement2.getText(), key) && ReplacementUtil.contains(statement1.getText(), value)) {
                mapping.getReplacements().add(new Replacement(value, key, ReplacementType.VARIABLE_NAME));
            }
        }
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
                        replacement.getType().equals(ReplacementType
                                .ARGUMENT_REPLACED_WITH_RIGHT_HAND_SIDE_OF_ASSIGNMENT_EXPRESSION)) {
                    replacements.add(replacement);
                }
            }
        }
        return replacements;
    }

    private void expandAnonymousAndLambdas(CodeFragment fragment, Set<SingleStatement> leaves1,
                                           Set<BlockStatement> innerNodes1, Set<SingleStatement> addedLeaves1,
                                           Set<BlockStatement> addedInnerNodes1, FunctionBodyMapper operationBodyMapper) {
        if (fragment instanceof SingleStatement) {
            SingleStatement statement = (SingleStatement) fragment;
            if (!leaves1.contains(statement)) {
                leaves1.add(statement);
                addedLeaves1.add(statement);
            }
            if (!statement.getAnonymousFunctionDeclarations().isEmpty()) {
                List<IAnonymousFunctionDeclaration> anonymousList = operationBodyMapper.getOperation1().getAnonymousFunctionDeclarations();
                for (IAnonymousFunctionDeclaration anonymous : anonymousList) {
                    if (statement.getSourceLocation().subsumes(anonymous.getSourceLocation())) {
                        for (IFunctionDeclaration ifd : anonymous.getFunctionDeclarations()) {
                            FunctionDeclaration anonymousOperation = (FunctionDeclaration) ifd;
                            List<SingleStatement> anonymousClassLeaves = anonymousOperation.getBody().blockStatement.getAllLeafStatementsIncludingNested();
                            for (SingleStatement anonymousLeaf : anonymousClassLeaves) {
                                if (!leaves1.contains(anonymousLeaf)) {
                                    leaves1.add(anonymousLeaf);
                                    addedLeaves1.add(anonymousLeaf);
                                    codeFragmentOperationMap1.put(anonymousLeaf, anonymousOperation);
                                }
                            }
                            Set<BlockStatement> anonymousClassInnerNodes = anonymousOperation.getBody()
                                    .blockStatement.getAllBlockStatementsIncludingNested();
                            for (BlockStatement anonymousInnerNode : anonymousClassInnerNodes) {
                                if (!innerNodes1.contains(anonymousInnerNode)) {
                                    innerNodes1.add(anonymousInnerNode);
                                    addedInnerNodes1.add(anonymousInnerNode);
                                    codeFragmentOperationMap1.put(anonymousInnerNode, anonymousOperation);
                                }
                            }
                        }
                    }
                }
            }
//
//            if (!statement.getLambdas().isEmpty()) {
//                for (LambdaExpressionObject lambda : statement.getLambdas()) {
//                    if (lambda.getBody() != null) {
//                        List<StatementObject> lambdaLeaves = lambda.getBody().getCompositeStatement().getLeaves();
//                        for (StatementObject lambdaLeaf : lambdaLeaves) {
//                            if (!leaves1.contains(lambdaLeaf)) {
//                                leaves1.add(lambdaLeaf);
//                                addedLeaves1.add(lambdaLeaf);
//                                codeFragmentOperationMap1.put(lambdaLeaf, operation1);
//                            }
//                        }
//                        List<CompositeStatementObject> lambdaInnerNodes = lambda.getBody().getCompositeStatement().getInnerNodes();
//                        for (CompositeStatementObject lambdaInnerNode : lambdaInnerNodes) {
//                            if (!innerNodes1.contains(lambdaInnerNode)) {
//                                innerNodes1.add(lambdaInnerNode);
//                                addedInnerNodes1.add(lambdaInnerNode);
//                                codeFragmentOperationMap1.put(lambdaInnerNode, operation1);
//                            }
//                        }
//                    }
//                }
//            }
//
        }
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
            if (mapping.isExact() && mapping.fragment1.countableStatement()
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


    /**
     * Returns the non mapped count
     *
     * @return
     */
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
            if (r.getType().equals(ReplacementType.VARIABLE_NAME)) {
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
            if (replacement.getType().equals(ReplacementType.NULL_LITERAL_REPLACED_WITH_CONDITIONAL_EXPRESSION) ||
                    replacement.getType().equals(ReplacementType.VARIABLE_REPLACED_WITH_NULL_LITERAL) ||
                    (replacement.getType().equals(ReplacementType.ARGUMENT_REPLACED_WITH_VARIABLE) && (replacement.getBefore().equals("null") || replacement.getAfter().equals("null")))) {
                nullLiteralReplacements++;
            } else if (replacement instanceof MethodInvocationReplacement) {
                MethodInvocationReplacement invocationReplacement = (MethodInvocationReplacement) replacement;
                OperationInvocation invokedOperationBefore = invocationReplacement.getInvokedOperationBefore();
                OperationInvocation invokedOperationAfter = invocationReplacement.getInvokedOperationAfter();
                if (invokedOperationBefore.getName().equals(invokedOperationAfter.getName()) &&
                        invokedOperationBefore.getArguments().size() == invokedOperationAfter.getArguments().size()) {
                    methodInvocationReplacementsToIgnore++;
                }
            } else if (replacement.getType().equals(ReplacementType.VARIABLE_NAME)) {
                variableNameReplacementsToIgnore++;
            }
        }
        return nullLiteralReplacements > 0 && numberOfReplacements == nullLiteralReplacements + methodInvocationReplacementsToIgnore + variableNameReplacementsToIgnore;
    }

    /**
     * Returns the mapping's any replacements of function invocation name
     *
     * @return
     */
    public Set<MethodInvocationReplacement> getMethodInvocationRenameReplacements() {
        Set<MethodInvocationReplacement> replacements = new LinkedHashSet<>();
        for (CodeFragmentMapping mapping : getMappings()) {
            for (Replacement replacement : mapping.getReplacements()) {
                if (replacement.getType().equals(ReplacementType.METHOD_INVOCATION_NAME) ||
                        replacement.getType().equals(ReplacementType.METHOD_INVOCATION_NAME_AND_ARGUMENT)) {
                    replacements.add((MethodInvocationReplacement) replacement);
                }
            }
        }
        return replacements;
    }

    public int operationNameEditDistance() {
        return StringDistance.editDistance(this.function1.getName(), this.function2.getName());
    }

    @Override

    public int compareTo(FunctionBodyMapper operationBodyMapper) {
        int thisCallChainIntersectionSum = 0;
        for (CodeFragmentMapping mapping : this.mappings) {
            if (mapping instanceof LeafCodeFragmentMapping) {
                //      thisCallChainIntersectionSum += ((LeafCodeFragmentMapping)mapping).callChainIntersection().size();
            }
        }
        int otherCallChainIntersectionSum = 0;
        for (CodeFragmentMapping mapping : operationBodyMapper.mappings) {
            if (mapping instanceof LeafCodeFragmentMapping) {
                //    otherCallChainIntersectionSum += ((LeafCodeFragmentMapping)mapping).callChainIntersection().size();
            }
        }
        if (thisCallChainIntersectionSum != otherCallChainIntersectionSum) {
            return -Integer.compare(thisCallChainIntersectionSum, otherCallChainIntersectionSum);
        }
        int thisMappings = this.mappingsWithoutBlocks();
//        for(CodeFragmentMapping mapping : this.getMappings()) {
//            if(mapping.isIdenticalWithExtractedVariable() || mapping.isIdenticalWithInlinedVariable()) {
//                thisMappings++;
//            }
//        }
        int otherMappings = operationBodyMapper.mappingsWithoutBlocks();
//        for(CodeFragmentMapping mapping : operationBodyMapper.getMappings()) {
//            if(mapping.isIdenticalWithExtractedVariable() || mapping.isIdenticalWithInlinedVariable()) {
//                otherMappings++;
//            }
//        }
        if (thisMappings != otherMappings) {
            return -Integer.compare(thisMappings, otherMappings);
        } else {
            int thisExactMatches = this.getExactMatches().size();
            int otherExactMatches = operationBodyMapper.getExactMatches().size();
            if (thisExactMatches != otherExactMatches) {
                return -Integer.compare(thisExactMatches, otherExactMatches);
            } else {
                int thisEditDistance = this.editDistance();
                int otherEditDistance = operationBodyMapper.editDistance();
                if (thisEditDistance != otherEditDistance) {
                    return Integer.compare(thisEditDistance, otherEditDistance);
                } else {
                    int thisOperationNameEditDistance = this.operationNameEditDistance();
                    int otherOperationNameEditDistance = operationBodyMapper.operationNameEditDistance();
                    return Integer.compare(thisOperationNameEditDistance, otherOperationNameEditDistance);
                }
            }
        }
    }

    private int editDistance() {
        int count = 0;
        for (CodeFragmentMapping mapping : getMappings()) {
//            if(mapping.isIdenticalWithExtractedVariable() || mapping.isIdenticalWithInlinedVariable()) {
//            }
//                continue;
            String s1 = createArgumentizedString(mapping.fragment1, mapping.fragment2);
            String s2 = createArgumentizedString(mapping.fragment2, mapping.fragment1);

            if (!s1.equals(s2)) {
                count += StringDistance.editDistance(s1, s2);
            }
        }
        return count;
    }

    public FunctionBodyMapper getParentMapper() {
        return parentMapper;
    }

    public List<FunctionBodyMapper> getChildMappers() {
        return childMappers;
    }

    public FunctionDeclaration getCallerFunction() {
        return this.callerFunction;
    }

    public FunctionDeclaration getOperation1() {
        return function1;
    }

    public FunctionDeclaration getOperation2() {
        return function2;
    }

    public Set<CandidateAttributeRefactoring> getCandidateAttributeRenames() {
        return candidateAttributeRenames;
    }

    public Set<CandidateMergeVariableRefactoring> getCandidateAttributeMerges() {
        return candidateAttributeMerges;
    }

    private boolean variableDeclarationMappingsWithSameReplacementTypes(Set<LeafCodeFragmentMapping> mappingSet) {
        if (mappingSet.size() > 1) {
            Set<LeafCodeFragmentMapping> variableDeclarationMappings = new LinkedHashSet<>();
            for (LeafCodeFragmentMapping mapping : mappingSet) {
                if (mapping.getFragment1().getVariableDeclarations().size() > 0 &&
                        mapping.getFragment2().getVariableDeclarations().size() > 0) {
                    variableDeclarationMappings.add(mapping);
                }
            }
            if (variableDeclarationMappings.size() == mappingSet.size()) {
                Set<ReplacementType> replacementTypes = null;
                Set<LeafCodeFragmentMapping> mappingsWithSameReplacementTypes = new LinkedHashSet<>();
                for (LeafCodeFragmentMapping mapping : variableDeclarationMappings) {
                    if (replacementTypes == null) {
                        replacementTypes = mapping.getReplacementTypes();
                        mappingsWithSameReplacementTypes.add(mapping);
                    } else if (mapping.getReplacementTypes().equals(replacementTypes)) {
                        mappingsWithSameReplacementTypes.add(mapping);
                    } else if (mapping.getReplacementTypes().containsAll(replacementTypes) || replacementTypes.containsAll(mapping.getReplacementTypes())) {
                        OperationInvocation invocation1 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(mapping.getFragment1());
                        OperationInvocation invocation2 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(mapping.getFragment2());
                        if (invocation1 != null && invocation2 != null) {
                            for (Replacement replacement : mapping.getReplacements()) {
                                if (replacement.getType().equals(ReplacementType.VARIABLE_NAME)) {
                                    if (invocation1.getName().equals(replacement.getBefore()) && invocation2.getName().equals(replacement.getAfter())) {
                                        mappingsWithSameReplacementTypes.add(mapping);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (mappingsWithSameReplacementTypes.size() == mappingSet.size()) {
                    return true;
                }
            }
        }
        return false;
    }

    private AbstractMap.SimpleEntry<BlockStatement, BlockStatement> multipleMappingsUnderTheSameSwitch(Set<LeafCodeFragmentMapping> mappingSet) {
        BlockStatement switchParent1 = null;
        BlockStatement switchParent2 = null;
        if (mappingSet.size() > 1) {
            for (LeafCodeFragmentMapping mapping : mappingSet) {
                CodeFragment fragment1 = mapping.getFragment1();
                CodeFragment fragment2 = mapping.getFragment2();
                if (fragment1 instanceof CodeFragment && fragment2 instanceof CodeFragment) {
                    CodeFragment statement1 = (CodeFragment) fragment1;
                    CodeFragment statement2 = (CodeFragment) fragment2;
                    BlockStatement parent1 = statement1.getParent();
                    BlockStatement parent2 = statement2.getParent();
                    if (parent1.getCodeElementType().equals(CodeElementType.SWITCH_STATEMENT) &&
                            parent2.getCodeElementType().equals(CodeElementType.SWITCH_STATEMENT)) {
                        if (switchParent1 == null && switchParent2 == null) {
                            switchParent1 = parent1;
                            switchParent2 = parent2;
                        } else if (switchParent1 != parent1 || switchParent2 != parent2) {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            }
        }
        if (switchParent1 != null && switchParent2 != null) {
            return new AbstractMap.SimpleEntry<>(switchParent1, switchParent2);
        }
        return null;
    }

    private LeafCodeFragmentMapping findBestMappingBasedOnMappedSwitchCases(AbstractMap.SimpleEntry<BlockStatement, BlockStatement> switchParentEntry, TreeSet<LeafCodeFragmentMapping> mappingSet) {
        BlockStatement switchParent1 = switchParentEntry.getKey();
        BlockStatement switchParent2 = switchParentEntry.getValue();
        CodeFragmentMapping currentSwitchCase = null;
        for (CodeFragmentMapping mapping : this.mappings) {
            CodeFragment fragment1 = mapping.getFragment1();
            CodeFragment fragment2 = mapping.getFragment2();
            if (fragment1 instanceof Statement && fragment2 instanceof Statement) {
                Statement statement1 = (Statement) fragment1;
                Statement statement2 = (Statement) fragment2;
                BlockStatement parent1 = statement1.getParent();
                BlockStatement parent2 = statement2.getParent();
                if (parent1 == switchParent1 && parent2 == switchParent2 && mapping.isExact() &&
                        statement1.getCodeElementType().equals(CodeElementType.SWITCH_CASE) &&
                        statement2.getCodeElementType().equals(CodeElementType.SWITCH_CASE)) {
                    currentSwitchCase = mapping;
                } else if (parent1 == switchParent1 && parent2 == switchParent2 &&
                        statement1.getCodeElementType().equals(CodeElementType.BREAK_STATEMENT) &&
                        statement2.getCodeElementType().equals(CodeElementType.BREAK_STATEMENT)) {
                    if (currentSwitchCase != null) {
                        for (LeafCodeFragmentMapping leafMapping : mappingSet) {
                            if (leafMapping.getFragment1().getPositionIndexInParent() > currentSwitchCase.getFragment1().getPositionIndexInParent() &&
                                    leafMapping.getFragment2().getPositionIndexInParent() > currentSwitchCase.getFragment2().getPositionIndexInParent() &&
                                    leafMapping.getFragment1().getPositionIndexInParent() < mapping.getFragment1().getPositionIndexInParent() &&
                                    leafMapping.getFragment2().getPositionIndexInParent() < mapping.getFragment2().getPositionIndexInParent()) {
                                return leafMapping;
                            }
                        }
                    }
                } else if (parent1 == switchParent1 && parent2 == switchParent2 &&
                        statement1.getCodeElementType().equals(CodeElementType.RETURN_STATEMENT) &&
                        statement2.getCodeElementType().equals(CodeElementType.RETURN_STATEMENT)) {
                    if (currentSwitchCase != null) {
                        for (LeafCodeFragmentMapping leafMapping : mappingSet) {
                            if (leafMapping.getFragment1().getPositionIndexInParent() > currentSwitchCase.getFragment1().getPositionIndexInParent() &&
                                    leafMapping.getFragment2().getPositionIndexInParent() > currentSwitchCase.getFragment2().getPositionIndexInParent() &&
                                    leafMapping.getFragment1().getPositionIndexInParent() < mapping.getFragment1().getPositionIndexInParent() &&
                                    leafMapping.getFragment2().getPositionIndexInParent() < mapping.getFragment2().getPositionIndexInParent()) {
                                return leafMapping;
                            }
                        }
                    }
                }
            }
        }
        return mappingSet.first();
    }

    public Set<CandidateSplitVariableRefactoring> getCandidateAttributeSplits() {
        return candidateAttributeSplits;
    }

    public double normalizedEditDistance() {
        double editDistance = 0;
        double maxLength = 0;
        for (CodeFragmentMapping mapping : getMappings()) {
//            if(mapping.isIdenticalWithExtractedVariable()
//                    || mapping.isIdenticalWithInlinedVariable()) {
//                continue;
//            }
            String s1 = createArgumentizedString(mapping.getFragment1(), mapping.getFragment2());
            String s2 = createArgumentizedString(mapping.getFragment2(), mapping.getFragment1());
            if (!s1.equals(s2)) {
                editDistance += StringDistance.editDistance(s1, s2);
                maxLength += Math.max(s1.length(), s2.length());
            }
        }
        return editDistance / maxLength;
    }

}
