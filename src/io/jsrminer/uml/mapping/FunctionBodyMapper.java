package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.FunctionBody;
import io.jsrminer.sourcetree.FunctionDeclaration;

import java.util.LinkedHashMap;
import java.util.Map;

public class FunctionBodyMapper {

    public final FunctionDeclaration function1;
    public final FunctionDeclaration function2;

    public FunctionBodyMapper(FunctionDeclaration function1, FunctionDeclaration function2) {
        this.function1 = function1;
        this.function2 = function2;
    }

    public void map() {

        FunctionBody body1 = function1.getBody();
        FunctionBody body2 = function2.getBody();

        if (body1 != null && body2 != null) {
            BlockStatement block1 = body1.blockStatement;
            BlockStatement block2 = body2.blockStatement;

            Map<String, String> parameterToArgumentMap1 = new LinkedHashMap<>();
            Map<String, String> parameterToArgumentMap2 = new LinkedHashMap<>();

            List<UMLParameter> addedParameters = operationDiff.getAddedParameters();
            if(addedParameters.size() == 1) {
                UMLParameter addedParameter = addedParameters.get(0);
                if(UMLModelDiff.looksLikeSameType(addedParameter.getType().getClassType(), operation1.getClassName())) {
                    parameterToArgumentMap1.put("this.", "");
                    //replace "parameterName." with ""
                    parameterToArgumentMap2.put(addedParameter.getName() + ".", "");
                }
            }
            List<UMLParameter> removedParameters = operationDiff.getRemovedParameters();
            if(removedParameters.size() == 1) {
                UMLParameter removedParameter = removedParameters.get(0);
                if(UMLModelDiff.looksLikeSameType(removedParameter.getType().getClassType(), operation2.getClassName())) {
                    parameterToArgumentMap1.put(removedParameter.getName() + ".", "");
                    parameterToArgumentMap2.put("this.", "");
                }
            }

        }
    }

    /** Consists of Abstraction and Argumentization
     * Abstraction: When follows these formats
     * return exp; returned exp
     * Type var = exp; variable initializer
     * var = exp; Right side of an assignment
     * call(exp); single argument of method invocation
     * if (exp) Condition of a composite
     */
    public void preProcess(){

    }
}
