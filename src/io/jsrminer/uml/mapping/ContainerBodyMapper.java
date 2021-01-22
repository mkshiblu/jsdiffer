package io.jsrminer.uml.mapping;

import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.CandidateAttributeRefactoring;
import io.jsrminer.refactorings.CandidateMergeVariableRefactoring;
import io.jsrminer.refactorings.CandidateSplitVariableRefactoring;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeFragment;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.uml.diff.ContainerDiff;
import io.rminer.core.entities.Container;

import java.util.*;

public class ContainerBodyMapper {

    public final Container container1;
    public final Container container2;
    public final ContainerDiff operationDiff;

    public static final Argumentizer argumentizer = new Argumentizer();

    private Set<CodeFragmentMapping> mappings = new LinkedHashSet<>();
    Map<String, String> parameterToArgumentMap1 = new LinkedHashMap<>();
    Map<String, String> parameterToArgumentMap2 = new LinkedHashMap<>();

    private final Set<SingleStatement> nonMappedLeavesT1 = new LinkedHashSet<>();
    private final Set<SingleStatement> nonMappedLeavesT2 = new LinkedHashSet<>();
    private final Set<BlockStatement> nonMappedInnerNodesT1 = new LinkedHashSet<>();
    private final Set<BlockStatement> nonMappedInnerNodesT2 = new LinkedHashSet<>();

    private Container callerFunction;
    private final ContainerDiff parentContainerDiff;
    private final List<FunctionBodyMapper> childMappers = new ArrayList<>();
    private ContainerBodyMapper parentMapper;
    private Set<IRefactoring> refactorings = new LinkedHashSet<>();
    private Map<CodeFragment, FunctionDeclaration> codeFragmentOperationMap1 = new LinkedHashMap<>();
    private Map<CodeFragment, FunctionDeclaration> codeFragmentOperationMap2 = new LinkedHashMap<>();

    private Set<CandidateAttributeRefactoring> candidateAttributeRenames = new LinkedHashSet<>();
    private Set<CandidateMergeVariableRefactoring> candidateAttributeMerges = new LinkedHashSet<>();
    private Set<CandidateSplitVariableRefactoring> candidateAttributeSplits = new LinkedHashSet<>();

    public ContainerBodyMapper(ContainerDiff operationDiff
            , ContainerDiff parentContainerDiff) {
        this.operationDiff = operationDiff;
        this.container1 = operationDiff.getContainer1();
        this.container2 = operationDiff.getContainer2();
        this.parentContainerDiff = parentContainerDiff;
    }

    public ContainerBodyMapper(Container container1, Container container2
            , ContainerDiff parentContainerDiff) {
        this(new ContainerDiff(container1, container2), parentContainerDiff);
    }

    /**
     * Tries to mapp the function1 of the mapper with the added operation
     */
    public ContainerBodyMapper(ContainerBodyMapper mapper, Container addedOperation, ContainerDiff parentContainerDiff

            , Map<String, String> parameterToArgumentMap1
            , Map<String, String> parameterToArgumentMap2) {
        this.container1 = mapper.container1;
        this.container2 = addedOperation;
        this.callerFunction = mapper.getContainer2();
        this.parentContainerDiff = parentContainerDiff;
        this.operationDiff = new ContainerDiff(this.container1, this.container2);
        this.parentMapper = mapper;
        this.parameterToArgumentMap1 = parameterToArgumentMap1;
        this.parameterToArgumentMap2 = parameterToArgumentMap2;
    }

    public ContainerBodyMapper(FunctionDeclaration removedOperation, ContainerBodyMapper operationBodyMapper
            /*, Map<String, String> parameterToArgumentMap*/, ContainerDiff classDiff) {
        this.parentMapper = operationBodyMapper;
        this.container1 = removedOperation;
        this.container2 = operationBodyMapper.container2;
        this.callerFunction = operationBodyMapper.container1;
        this.parentContainerDiff = classDiff;
        this.operationDiff = new ContainerDiff(this.container1, this.container2);
    }

    /**
     * Maps funciton1 with funciton2
     */
    public void map() {
//        ContainerB body1 = container1.getBody();
//        FunctionBody body2 = function2.getBody();
//
//        if (body1 != null && body2 != null) {
//
//            mapParametersToArguments(this.operationDiff.getAddedParameters(), this.operationDiff.getRemovedParameters());
//
//            BlockStatement block1 = body1.blockStatement;
//            BlockStatement block2 = body2.blockStatement;
//
//            // match leaves
//            Set<SingleStatement> leaves1 = new LinkedHashSet<>(block1.getAllLeafStatementsIncludingNested());
//            Set<SingleStatement> leaves2 = new LinkedHashSet<>(block2.getAllLeafStatementsIncludingNested());
//            argumentizer.clearCache(leaves1, leaves2);
//            replaceParametersWithArguments(leaves1, leaves2);
//
//            if (leaves1.size() > 0 && leaves2.size() > 0)
//                matchLeaves(leaves1, leaves2, new LinkedHashMap<>());
//
//            this.nonMappedLeavesT1.addAll(leaves1);
//            this.nonMappedLeavesT2.addAll(leaves2);
//
//            // Match composites
//
//            Set<BlockStatement> innerNodes1 = new LinkedHashSet<>(block1.getAllBlockStatementsIncludingNested());
//            Set<BlockStatement> innerNodes2 = new LinkedHashSet<>(block2.getAllBlockStatementsIncludingNested());
//
//            // TODO improve recirson of innerNodes by preventing adding itself
//            innerNodes1.remove(block1);
//            innerNodes2.remove(block2);
//
//            argumentizer.clearCache(innerNodes1, innerNodes2);
//            replaceParametersWithArguments(innerNodes1, innerNodes2);
//            if (innerNodes1.size() > 0 && innerNodes2.size() > 0)
//                matchNestedBlockStatements(innerNodes1, innerNodes2, new LinkedHashMap<>());
//
//            this.nonMappedInnerNodesT1.addAll(innerNodes1);
//            this.nonMappedInnerNodesT2.addAll(innerNodes2);
//
//            // Set mappings
//            this.operationDiff.setMappings(this.mappings);
        //}
    }

    public Container getContainer1() {
        return container1;
    }

    public Container getContainer2() {
        return container2;
    }
}
