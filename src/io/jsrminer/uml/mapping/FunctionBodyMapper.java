package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.FunctionBody;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.UMLParameter;
import io.jsrminer.uml.diff.UMLOperationDiff;
import org.eclipse.jgit.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class FunctionBodyMapper {

    public final FunctionDeclaration function1;
    public final FunctionDeclaration function2;

    public FunctionBodyMapper(@NonNull FunctionDeclaration function1, @NonNull FunctionDeclaration function2) {
        this.function1 = function1;
        this.function2 = function2;
    }

    public void map() {
        FunctionBody body1 = function1.getBody();
        FunctionBody body2 = function2.getBody();

        if (body1 != null && body2 != null) {
            final UMLOperationDiff operationDiff = new UMLOperationDiff(function1, function2);

            BlockStatement block1 = body1.blockStatement;
            BlockStatement block2 = body2.blockStatement;

            mapParametersToArgument(operationDiff);
            // Reset leaves
            //processLeaves();
        }
    }

    void mapParametersToArgument(UMLOperationDiff operationDiff) {
        final Map<String, UMLParameter> addedParameters = operationDiff.getAddedParameters();
        final Map<String, UMLParameter> removedParameters = operationDiff.getRemovedParameters();
        final Map<String, String> parameterToArgumentMap1 = new LinkedHashMap<>();
        final Map<String, String> parameterToArgumentMap2 = new LinkedHashMap<>();

        // TODO revisit since js does not have types
//        if (addedParameters.size() == 1) {
//            UMLParameter addedParameter = addedParameters.values().iterator().next();
//            if (UMLModelDiff.looksLikeSameType(addedParameter.getType().getClassType(),
//                    function1.getClassName())) {
//                parameterToArgumentMap1.put("this.", "");
//                //replace "parameterName." with ""
//                parameterToArgumentMap2.put(addedParameter.name + ".", "");
//            }
//        }
//
//        if (removedParameters.size() == 1) {
//            UMLParameter removedParameter = removedParameters.values().iterator().next();
//            if (UMLModelDiff.looksLikeSameType(removedParameter.getType().getClassType(),
//                    function2.getClassName())) {
//                parameterToArgumentMap1.put(removedParameter.name + ".", "");
//                parameterToArgumentMap2.put("this.", "");
//            }
//        }
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
}
