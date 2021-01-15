package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO rename class
public class ReplacementHeuristic {

    public static boolean isIdentificalMethodInvocation(OperationInvocation assignmentInvocationCoveringTheEntireStatement1,
                                                        OperationInvocation invocationCoveringTheEntireStatement2
            , Map<String, List<? extends Invocation>> methodInvocationMap1
            , ReplacementInfo replacementInfo
            , CodeFragment statement1, CodeFragment statement2) {

        if (assignmentInvocationCoveringTheEntireStatement1 != null && invocationCoveringTheEntireStatement2 != null) {
            for (String key1 : methodInvocationMap1.keySet()) {
                for (Invocation invocation1 : methodInvocationMap1.get(key1)) {
                    if (invocation1.identical(invocationCoveringTheEntireStatement2,
                            replacementInfo.getReplacements()) &&
                            !assignmentInvocationCoveringTheEntireStatement1.getArguments().contains(key1)) {
                        String expression1 = assignmentInvocationCoveringTheEntireStatement1.getExpression();
                        if (expression1 == null || !expression1.contains(key1)) {
                            //   return replacementInfo.getReplacements();
                            return true;
                        }
                    } else if (invocation1.identicalName(invocationCoveringTheEntireStatement2) && invocation1.equalArguments(invocationCoveringTheEntireStatement2) &&
                            !assignmentInvocationCoveringTheEntireStatement1.getArguments().contains(key1) && invocationCoveringTheEntireStatement2.getExpression() != null) {
                        boolean expressionMatched = false;
                        Set<CodeFragment> additionallyMatchedStatements2 = new LinkedHashSet<>();
                        for (CodeFragment codeFragment : replacementInfo.unMatchedStatements2) {
                            VariableDeclaration variableDeclaration = codeFragment.getVariableDeclaration(invocationCoveringTheEntireStatement2.getExpression());
                            OperationInvocation invocationCoveringEntireCodeFragment = InvocationCoverage.INSTANCE.getInvocationCoveringEntireFragment(codeFragment);
                            if (variableDeclaration != null && variableDeclaration.getInitializer() != null && invocation1.getExpression() != null && invocation1.getExpression().equals(variableDeclaration.getInitializer().getText())) {
                                Replacement r = new Replacement(invocation1.getExpression(), variableDeclaration.getVariableName(), ReplacementType.VARIABLE_REPLACED_WITH_EXPRESSION_OF_METHOD_INVOCATION);
                                replacementInfo.getReplacements().add(r);
                                additionallyMatchedStatements2.add(codeFragment);
                                expressionMatched = true;
                            }
                            if (invocationCoveringEntireCodeFragment != null && assignmentInvocationCoveringTheEntireStatement1.identicalName(invocationCoveringEntireCodeFragment) &&
                                    assignmentInvocationCoveringTheEntireStatement1.equalArguments(invocationCoveringEntireCodeFragment)) {
                                additionallyMatchedStatements2.add(codeFragment);
                            }
                        }
                        if (expressionMatched) {
                            if (additionallyMatchedStatements2.size() > 0) {
                                Replacement r = new CompositeReplacement(statement1.getText(), statement2.getText(), new LinkedHashSet<>(), additionallyMatchedStatements2);
                                replacementInfo.getReplacements().add(r);
                            }
//                            return replacementInfo.getReplacements();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isObjectCreationReplacedWithArrayDeclaration(CodeFragment statement1, CodeFragment statement2
            , ReplacementInfo replacementInfo) {
        final ObjectCreation creationCoveringTheEntireStatement1 = InvocationCoverage.INSTANCE.creationCoveringEntireFragment(statement1);
        final ObjectCreation creationCoveringTheEntireStatement2 = InvocationCoverage.INSTANCE.creationCoveringEntireFragment(statement2);
        final List<VariableDeclaration> variableDeclarations1 = statement1.getVariableDeclarations();
        final List<VariableDeclaration> variableDeclarations2 = statement2.getVariableDeclarations();

        //check if array creation is replaced with data structure creation
        if (creationCoveringTheEntireStatement1 != null && creationCoveringTheEntireStatement2 != null &&
                variableDeclarations1.size() == 1 && variableDeclarations2.size() == 1) {

            VariableDeclaration v1 = variableDeclarations1.get(0);
            VariableDeclaration v2 = variableDeclarations2.get(0);

            String initializer1 = v1.getInitializer() != null ? v1.getInitializer().getText() : null;
            String initializer2 = v2.getInitializer() != null ? v2.getInitializer().getText() : null;

//            Replacement r;

//            boolean isArrayCreationReplacedWithObjectCreation = (creationCoveringTheEntireStatement1.isArray()
//                    && !creationCoveringTheEntireStatement2.isArray());
//
//            boolean isObjectCreationReplacedWithArrayCreation = (creationCoveringTheEntireStatement2.isArray()
//                    && !creationCoveringTheEntireStatement1.isArray());
//
//            boolean sameArguments = false;
//
//            if (initializer1 != null && initializer2 != null) {
//                String arrayElements1 = initializer1
//                        .substring(initializer1.indexOf("[") + 1
//                                , initializer1.lastIndexOf("]"));
//                String objectCreationArguments1 = initializer2.substring(initializer2.indexOf("(") + 1,
//                        initializer2.lastIndexOf("]"));
//
//                sameArguments = arrayElements1.equals(objectCreationArguments1);
//            }

            boolean creation2IsArrayConstructor = "Array".equals(creationCoveringTheEntireStatement2.getFunctionName());
            boolean creation1IsArrayConstructor = "Array".equals(creationCoveringTheEntireStatement1.getFunctionName());

//            boolean creation1IsEmptyArray = "[]".equals(creationCoveringTheEntireStatement1.getText());
            //          boolean creation2IsEmptyArray = "[]".equals(creationCoveringTheEntireStatement2.getText());

            // Check if replaced With built int types;
            boolean arrayCreationReplacedWithArrayConstructor = creationCoveringTheEntireStatement1.isArray()
                    && creation2IsArrayConstructor;
            boolean arrayConstructorReplacedWithArrayCreation = creationCoveringTheEntireStatement2.isArray()
                    && creation1IsArrayConstructor;

            if (arrayConstructorReplacedWithArrayCreation || arrayCreationReplacedWithArrayConstructor) {
                Replacement replacement = new ObjectCreationReplacement(initializer1, initializer2,
                        creationCoveringTheEntireStatement1, creationCoveringTheEntireStatement2, ReplacementType.ARRAY_CONSTRUCTOR_REPLACED_WITH_ARRAY_CREATION);
                replacementInfo.addReplacement(replacement);
                return true;
            }
        }
        return false;
    }
}
