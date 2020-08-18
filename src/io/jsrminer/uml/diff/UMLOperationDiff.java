package io.jsrminer.uml.diff;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.UMLParameter;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.AbstractMap.SimpleEntry;

/**
 * Represents the diff between two functions mostly on their signature diff
 */
public class UMLOperationDiff extends Diff {

    final FunctionDeclaration function1;
    final FunctionDeclaration function2;

    private List<UMLParameter> addedParameters = new ArrayList<>();
    private List<UMLParameter> removedParameters = new ArrayList<>();
    private List<UMLParameterDiff> parameterDiffs = new ArrayList<>();

    public final boolean nameChanged;
    public final boolean parametersReordered;

    public UMLOperationDiff(FunctionDeclaration function1, FunctionDeclaration function2) {
        this.function1 = function1;
        this.function2 = function2;
        nameChanged = !function1.nameEquals(function2);

        // Diff Parameters
        List<SimpleEntry<UMLParameter, UMLParameter>> matchedParameters =
                updateAddedRemovedParameters(function1, function2);
        diffParameters(matchedParameters);
        parametersReordered = checkParamsReordered(matchedParameters.size());

        matchParameterWithSameName();
        matchParameterWithSameType();
        matchParameterDifferentTypeAndName(matchedParameters.size());
    }

    //first round match parameters with the same name
    protected void matchParameterWithSameName() {
        UMLParameter removedParameter;
        final Iterator<UMLParameter> removedParameterIterator = removedParameters.iterator();
        while (removedParameterIterator.hasNext()) {
            removedParameter = removedParameterIterator.next();
            for (Iterator<UMLParameter> addedParameterIterator = addedParameters.iterator(); addedParameterIterator.hasNext(); ) {
                UMLParameter addedParameter = addedParameterIterator.next();


                if (removedParameter.name.equals(addedParameter.name)) {
                    UMLParameterDiff parameterDiff = new UMLParameterDiff(removedParameter, addedParameter
                            /*, removedOperation, addedOperation, mappings*/);
                    parameterDiffs.add(parameterDiff);
                    addedParameterIterator.remove();
                    removedParameterIterator.remove();
                    break;
                }
            }
        }
    }

    //second round match parameters with the same type
    protected void matchParameterWithSameType() {
//        //second round match parameters with the same type
//        for (Iterator<UMLParameter> removedParameterIterator = removedParameters.iterator(); removedParameterIterator.hasNext(); ) {
//            UMLParameter removedParameter = removedParameterIterator.next();
//            for (Iterator<UMLParameter> addedParameterIterator = addedParameters.iterator(); addedParameterIterator.hasNext(); ) {
//                UMLParameter addedParameter = addedParameterIterator.next();
//                if (removedParameter.getType().equalsQualified(addedParameter.getType()) &&
//                        !existsAnotherAddedParameterWithTheSameType(addedParameter)) {
//                    UMLParameterDiff parameterDiff = new UMLParameterDiff(removedParameter, addedParameter, removedOperation, addedOperation, mappings);
//                    parameterDiffList.add(parameterDiff);
//                    addedParameterIterator.remove();
//                    removedParameterIterator.remove();
//                    break;
//                }
//            }
//        }
    }

    //third round match parameters with different type and name
    protected void matchParameterDifferentTypeAndName(int matchedParameterCount) {
////third round match parameters with different type and name
//        List<UMLParameter> removedParametersWithoutReturnType = function1.getParametersWithoutReturnType();
//        List<UMLParameter> addedParametersWithoutReturnType = function2.getParametersWithoutReturnType();
//        if (matchedParameterCount == removedParametersWithoutReturnType.size() - 1 && matchedParameterCount == addedParametersWithoutReturnType.size() - 1) {
//            for (Iterator<UMLParameter> removedParameterIterator = removedParameters.iterator(); removedParameterIterator.hasNext(); ) {
//                UMLParameter removedParameter = removedParameterIterator.next();
//                int indexOfRemovedParameter = removedParametersWithoutReturnType.indexOf(removedParameter);
//                for (Iterator<UMLParameter> addedParameterIterator = addedParameters.iterator(); addedParameterIterator.hasNext(); ) {
//                    UMLParameter addedParameter = addedParameterIterator.next();
//                    int indexOfAddedParameter = addedParametersWithoutReturnType.indexOf(addedParameter);
//                    if (indexOfRemovedParameter == indexOfAddedParameter) {
//                        UMLParameterDiff parameterDiff = new UMLParameterDiff(removedParameter, addedParameter, removedOperation, addedOperation, mappings);
//                        parameterDiffs.add(parameterDiff);
//                        addedParameterIterator.remove();
//                        removedParameterIterator.remove();
//                        break;
//                    }
//                }
//            }
//        }
    }

    private void diffParameters(final List<SimpleEntry<UMLParameter, UMLParameter>> matchedParameters) {
        for (SimpleEntry<UMLParameter, UMLParameter> matchedParameter : matchedParameters) {
            UMLParameter parameter1 = matchedParameter.getKey();
            UMLParameter parameter2 = matchedParameter.getValue();
            UMLParameterDiff parameterDiff = new UMLParameterDiff(parameter1, parameter2
                    /*, removedOperation, addedOperation, mappings*/);
            parameterDiffs.add(parameterDiff);
        }
    }

    private List<SimpleEntry<UMLParameter, UMLParameter>> updateAddedRemovedParameters(FunctionDeclaration function1, FunctionDeclaration function2) {
        final List<SimpleEntry<UMLParameter, UMLParameter>> matchedParameters = new ArrayList<>();
        UMLParameter parameter2;
        for (UMLParameter parameter1 : function1.getParameters().values()) {
            // Check if function 2 contains the same named parameter
            // IN java it's equalsIncludingName i.e. full match
            if ((parameter2 = function2.getParameter(parameter1.name)) != null) {
                matchedParameters.add(new SimpleEntry<>(parameter1, parameter2));
                
            } else {
                // Param1 is not present in function 2 i.e. it's been removed
                this.removedParameters.add(parameter1);
            }
        }

        for (UMLParameter param2 : function2.getParameters().values()) {
            if (!function1.hasParameterOfName(param2.name)) {
                this.addedParameters.add(param2);
            }
        }

        return matchedParameters;
    }

    private boolean checkParamsReordered(int matchedParameterCount) {
        final Set<String> parameterNames1 = function1.getParameters().keySet();
        final Set<String> parameterNames2 = function2.getParameters().keySet();

        return removedParameters.isEmpty() && addedParameters.isEmpty()
                && parameterNames1.size() > 1
                && matchedParameterCount == parameterNames1.size() && matchedParameterCount == parameterNames2.size()
                && !parameterNames1.equals(parameterNames2);
    }
}
