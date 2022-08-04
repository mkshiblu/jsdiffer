package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.matchers.ContainerMatcher;
import io.jsrminer.uml.UMLParameter;
import io.jsrminer.uml.diff.ContainerDiff;
import io.jsrminer.uml.diff.ContainerDiffer;
import io.jsrminer.uml.diff.UMLOperationDiff;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.rminerx.core.api.IAnonymousFunctionDeclaration;
import io.rminerx.core.api.IFunctionDeclaration;

import java.util.*;

public class AnonymousFunctionReplacementFinder {

    Map<String, String> parameterToArgumentMap;
    FunctionBodyMapper parentOperationsMapper;

    public AnonymousFunctionReplacementFinder(Map<String, String> parameterToArgumentMap, FunctionBodyMapper parentOperationsMapper) {
        this.parameterToArgumentMap = parameterToArgumentMap;
        this.parentOperationsMapper = parentOperationsMapper;
    }

    public Set<Replacement> replaceInAnonymousFunctions(CodeFragment statement1
            , CodeFragment statement2
            , FunctionDeclaration function1
            , FunctionDeclaration function2) {

        var replacements = new LinkedHashSet<Replacement>(); // Previous replacements are empty it should be passed
        List<IAnonymousFunctionDeclaration> anonymousContainers1 = new ArrayList<>(statement1.getAnonymousFunctionDeclarations());
        List<IAnonymousFunctionDeclaration> anonymousContainers2 = new ArrayList<>(statement2.getAnonymousFunctionDeclarations());

        // First exact matches
        matchAnonymousContainers(anonymousContainers1, anonymousContainers2, ContainerMatcher.SAME,
                statement1, statement2, function1, function2, replacements);

        // Relaxed
        matchAnonymousContainers(anonymousContainers1, anonymousContainers2, ContainerMatcher.COMMON,
                statement1, statement2, function1, function2, replacements);

        // Relaxed
        matchAnonymousContainers(anonymousContainers1, anonymousContainers2, ContainerMatcher.COMMON_PARAMETERS,
                statement1, statement2, function1, function2, replacements);

        return replacements.size() > 0 ? replacements : null;
    }

    void matchAnonymousContainers(List<IAnonymousFunctionDeclaration> anonymousContainers1
            , List<IAnonymousFunctionDeclaration> anonymousContainers2
            , ContainerMatcher matcher
            , CodeFragment statement1
            , CodeFragment statement2
            , FunctionDeclaration function1
            , FunctionDeclaration function2
            , LinkedHashSet<Replacement> replacements) {
        final OperationInvocation invocationCoveringTheEntireStatement1 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement1);
        final OperationInvocation invocationCoveringTheEntireStatement2 = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(statement2);

        for (ListIterator<? extends IAnonymousFunctionDeclaration> listIterator1 = anonymousContainers1.listIterator(); listIterator1.hasNext(); ) {
            var anonymousClassDeclaration1 = listIterator1.next();

            for (ListIterator<? extends IAnonymousFunctionDeclaration> listIterator2 = anonymousContainers2.listIterator(); listIterator2.hasNext(); ) {
                var anonymousClassDeclaration2 = listIterator2.next();
                String statementWithoutAnonymous1 = statementWithoutAnonymous(statement1, anonymousClassDeclaration1, function1);
                String statementWithoutAnonymous2 = statementWithoutAnonymous(statement2, anonymousClassDeclaration2, function2);

                if (statementWithoutAnonymous1.equals(statementWithoutAnonymous2) ||
                        // This pair wont be matched since replacemnt is empty here
                        identicalAfterVariableAndTypeReplacements(statementWithoutAnonymous1, statementWithoutAnonymous2, replacements)
                        || (invocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null
                        && (invocationCoveringTheEntireStatement1.identicalWithMergedArguments(invocationCoveringTheEntireStatement2, replacements)
                        || invocationCoveringTheEntireStatement1.identicalWithDifferentNumberOfArguments(invocationCoveringTheEntireStatement2, replacements, parameterToArgumentMap)))
                ) {
                    if (matcher.match(anonymousClassDeclaration1, anonymousClassDeclaration2)) {
                        Replacement replacement = diffAnonymousPair(anonymousClassDeclaration1, anonymousClassDeclaration2);
                        if (replacement != null) {
                            replacements.add(replacement);
                            listIterator1.remove();
                            listIterator2.remove();
                            break;
                        }
                    }
                }
            }
        }
    }

    Replacement diffAnonymousPair(IAnonymousFunctionDeclaration anonymousFunctionDeclaration1, IAnonymousFunctionDeclaration anonymousClassDeclaration2) {
        ContainerDiffer<IAnonymousFunctionDeclaration, ContainerDiff<IAnonymousFunctionDeclaration>> differ
                = new ContainerDiffer<>(new ContainerDiff<>(anonymousFunctionDeclaration1, anonymousClassDeclaration2));
        var diff = differ.diff();
//                    for (IFunctionDeclaration operation1 : anonymousClass1.getFunctionDeclarations()) {
//                        for (IFunctionDeclaration operation2 : anonymousClass2.getFunctionDeclarations()) {
//                            if (operation1.equals(operation2)
//                                    || equalSignature(operation1, operation2)
//                                    || equalSignatureWithIdenticalNameIgnoringChangedTypes(operation1, operation2)
//                            ) {
//                                boolean isMatched = createMapperOfFunctionsInsideAnonymous((FunctionDeclaration) operation1, (FunctionDeclaration) operation2);
//                                if (isMatched)
//                                    matchedOperations++;
//                            }
//                        }
//                    }

        if (isAnonymousBodyMatched(diff)) {
            copyMappingsAndRefactoringsToParentMapper(diff);
            Replacement replacement = new Replacement(anonymousFunctionDeclaration1.toString(), anonymousClassDeclaration2.toString(), ReplacementType.ANONYMOUS_CLASS_DECLARATION);
            return replacement;
        }
        return null;
    }

    boolean isAnonymousBodyMatched(ContainerDiff<IAnonymousFunctionDeclaration> anonymousDiff) {
        int matchedOperations = anonymousDiff.getOperationBodyMapperList().size();
        if (matchedOperations > 0) {
            return true;
        }

        var statementMapper = anonymousDiff.getBodyStatementMapper();
        if (statementMapper != null) {
            int mappings = statementMapper.mappingsWithoutBlocks();
            if ((mappings > statementMapper.nonMappedElementsT1()
                    && mappings > statementMapper.nonMappedElementsT2())) {
                return true;
            }
        }

        boolean bothHasZeroStatements = anonymousDiff.getContainer1().getStatements().size() == 0
                && anonymousDiff.getContainer2().getStatements().size() == 0;

        if (bothHasZeroStatements) {
            // check params
            return true;
        }

        return false;
    }

    private void copyMappingsAndRefactoringsToParentMapper(ContainerDiff<IAnonymousFunctionDeclaration> anonymousClassDiff) {
        var matchedOperationMappers = anonymousClassDiff.getOperationBodyMapperList();
        var bodyStatementMapper = anonymousClassDiff.getBodyStatementMapper();
        var allMappers = new LinkedHashSet<FunctionBodyMapper>();

        if (bodyStatementMapper != null) {
            allMappers.add(bodyStatementMapper);
        }

        allMappers.addAll(matchedOperationMappers);

        if (allMappers.size() > 0) {
            // Copy operation mapper mappings ?
            for (var mapper : allMappers) {
                this.parentOperationsMapper.getMappings().addAll(mapper.getMappings());
                this.parentOperationsMapper.getNonMappedInnerNodesT1().addAll(mapper.getNonMappedInnerNodesT1());
                this.parentOperationsMapper.getNonMappedInnerNodesT2().addAll(mapper.getNonMappedInnerNodesT2());
                this.parentOperationsMapper.getNonMappedLeavesT1().addAll(mapper.getNonMappedLeavesT1());
                this.parentOperationsMapper.getNonMappedLeavesT2().addAll(mapper.getNonMappedLeavesT2());
            }
            this.parentOperationsMapper.getRefactoringsAfterPostProcessing().addAll(anonymousClassDiff.getAllRefactorings());
        }
    }

    private boolean createMapperOfFunctionsInsideAnonymous(FunctionDeclaration operation1, FunctionDeclaration operation2) {
        boolean isMatched = false;
        FunctionBodyMapper mapper = new FunctionBodyMapper(operation1, operation2, parentOperationsMapper.getParentDiff());
        int mappings = mapper.mappingsWithoutBlocks();
        if (mappings > 0) {
            int nonMappedElementsT1 = mapper.nonMappedElementsT1();
            int nonMappedElementsT2 = mapper.nonMappedElementsT2();
            if (mappings > nonMappedElementsT1 && mappings > nonMappedElementsT2) {

                this.parentOperationsMapper.getMappings().addAll(mapper.getMappings());
                this.parentOperationsMapper.getNonMappedInnerNodesT1().addAll(mapper.getNonMappedInnerNodesT1());
                this.parentOperationsMapper.getNonMappedInnerNodesT2().addAll(mapper.getNonMappedInnerNodesT2());
                this.parentOperationsMapper.getNonMappedLeavesT1().addAll(mapper.getNonMappedLeavesT1());
                this.parentOperationsMapper.getNonMappedLeavesT2().addAll(mapper.getNonMappedLeavesT2());
                isMatched = true;

                UMLOperationDiff operationDiff = new UMLOperationDiff(operation1, operation2, mapper.getMappings());
                this.parentOperationsMapper.getRefactoringsAfterPostProcessing().addAll(mapper.getRefactoringsByVariableAnalysis());
                this.parentOperationsMapper.getRefactoringsAfterPostProcessing().addAll(operationDiff.getRefactorings());
            }
        }

        return isMatched;
    }

    public static boolean equalSignature(IFunctionDeclaration function1, IFunctionDeclaration function2) {
        //boolean equalParameterTypes =  this.getParameterTypeList().equals(operation.getParameterTypeList());
        boolean equalParameterCount = function1.getParameters().size() == function2.getParameters().size();

//        boolean compatibleParameterTypes = false;
//        if(!equalParameterTypes) {
//            List<UMLType> thisParameterTypeList = this.getParameterTypeList();
//            List<UMLType> otherParameterTypeList = operation.getParameterTypeList();
//
//            if(thisParameterTypeList.size() == otherParameterTypeList.size()) {
//                int compatibleTypes = 0;
//                int equalTypes = 0;
//                for(int i=0; i<thisParameterTypeList.size(); i++) {
//                    UMLType thisParameterType = thisParameterTypeList.get(i);
//                    UMLType otherParameterType = otherParameterTypeList.get(i);
//                    if((thisParameterType.getClassType().endsWith("." + otherParameterType.getClassType()) ||
//                            otherParameterType.getClassType().endsWith("." + thisParameterType.getClassType())) &&
//                            thisParameterType.getArrayDimension() == otherParameterType.getArrayDimension()) {
//                        compatibleTypes++;
//                    }
//                    else if(thisParameterType.equals(otherParameterType)) {
//                        equalTypes++;
//                    }
//                }
//                if(equalTypes + compatibleTypes == thisParameterTypeList.size()) {
//                    compatibleParameterTypes = true;
//                }
//            }
//        }
        //return this.name.equals(operation.name) && equalTypeParameters(operation) && (equalParameterTypes || compatibleParameterTypes) && equalReturnParameter(operation);
        return equalParameterCount;
    }

    public static boolean equalSignatureWithIdenticalNameIgnoringChangedTypes(IFunctionDeclaration function1, IFunctionDeclaration function2) {
        if (!(function1.isConstructor() &&
                function2.isConstructor() || function1.getName().equals(function2.getName())))
            return false;
//        if (this.isAbstract != operation.isAbstract)
//            return false;
		/*if(this.isStatic != operation.isStatic)
			return false;
		if(this.isFinal != operation.isFinal)
			return false;*/
        if (function1.getParameters().size() != function2.getParameters().size())
            return false;
//        if (!equalTypeParameters(operation))
//            return false;
        int i = 0;

        for (UMLParameter parameter1 : function1.getParameters()) {
            UMLParameter parameter2 = function2.getParameters().get(i);
            if (!parameter1.name.equals(parameter2.name))
                return false;
            i++;
        }
        return true;
    }

    private String statementWithoutAnonymous(CodeFragment statement, IAnonymousFunctionDeclaration anonymousClassDeclaration
            , IFunctionDeclaration operation) {
        int index = statement.getText().indexOf(anonymousClassDeclaration.getText());
        if (index != -1) {
            return statement.getText().substring(0, index);
        } else {
//            for (LambdaExpressionObject lambda : statement.getLambdas()) {
//                OperationBody body = lambda.getBody();
//                if (body != null) {
//                    List<StatementObject> leaves = body.getCompositeStatement().getLeaves();
//                    for (StatementObject leaf : leaves) {
//                        for (AnonymousClassDeclarationObject anonymousObject : leaf.getAnonymousClassDeclarations()) {
//                            if (anonymousObject.getLocationInfo().equals(anonymousClassDeclaration.getLocationInfo())) {
//                                String statementWithoutAnonymous = statementWithoutAnonymous(leaf, anonymousClassDeclaration, operation);
//                                if (statementWithoutAnonymous != null) {
//                                    return statementWithoutAnonymous;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
            Map<String, List<ObjectCreation>> creationMap = statement.getCreationMap();
            // TODO Creation
//            for (String key : creationMap.keySet()) {
//                List<ObjectCreation> objectCreations = creationMap.get(key);
//                for (ObjectCreation creation : objectCreations) {
//                    if (creation.getAnonymousClassDeclaration() != null && creation.getAnonymousClassDeclaration().equals(anonymousClassDeclaration.toString()) &&
//                            creation.getLocationInfo().subsumes(anonymousClassDeclaration.getLocationInfo())) {
//                        return creation.actualString();
//                    }
//                }
//            }

            List<IFunctionDeclaration> anonymousOperations = new ArrayList<>();
            for (IAnonymousFunctionDeclaration anonymousObject : statement.getAnonymousFunctionDeclarations()) {
                for (IAnonymousFunctionDeclaration anonymousClass : operation.getAnonymousFunctionDeclarations()) {
                    if (ReplacementUtil.equalsSourceLocation(anonymousClass, anonymousObject)) {
                        anonymousOperations.addAll(anonymousClass.getFunctionDeclarations());
                    }
                }
            }

            for (IFunctionDeclaration anonymousOperation : anonymousOperations) {
                FunctionBody body = anonymousOperation.getBody();
                if (body != null) {
                    List<SingleStatement> leaves = body.blockStatement.getAllLeafStatementsIncludingNested();
                    for (SingleStatement leaf : leaves) {
                        for (IAnonymousFunctionDeclaration anonymousObject : leaf.getAnonymousFunctionDeclarations()) {
                            if (ReplacementUtil.equalsSourceLocation(anonymousObject, anonymousClassDeclaration) ||
                                    anonymousObject.getSourceLocation().subsumes(anonymousClassDeclaration.getSourceLocation())) {
                                return statementWithoutAnonymous(leaf, anonymousClassDeclaration, anonymousOperation);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean identicalAfterVariableAndTypeReplacements(String s1, String s2, Set<Replacement> replacements) {
        String s1AfterReplacements = new String(s1);

        for (Replacement replacement : replacements) {
            if (replacement.getType().equals(ReplacementType.VARIABLE_NAME)
                    || replacement.getType().equals(ReplacementType.TYPE)) {
                s1AfterReplacements = ReplacementUtil.performReplacement(s1AfterReplacements, s2, replacement.getBefore(), replacement.getAfter());
            }
        }

        if (s1AfterReplacements.equals(s2)) {
            return true;
        }
        return false;
    }

    private IAnonymousFunctionDeclaration findAnonymousClass(IAnonymousFunctionDeclaration anonymousClassDeclaration1, FunctionDeclaration operation) {
        for (IAnonymousFunctionDeclaration anonymousClass : operation.getAnonymousFunctionDeclarations()) {
            if (ReplacementUtil.equalsSourceLocation(anonymousClass, anonymousClassDeclaration1)) {
                return anonymousClass;
            }
        }
        return null;
    }
}