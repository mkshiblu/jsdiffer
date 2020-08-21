package io.jsrminer.uml.diff;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.UMLParameter;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.AbstractMap.SimpleEntry;

/**
 * Represents the diff between two functions mostly on their signature diff
 */
public class UMLOperationDiff extends Diff {
    final FunctionDeclaration function1;
    final FunctionDeclaration function2;

    private Map<String, UMLParameter> addedParameters = new HashMap<>();
    private Map<String, UMLParameter> removedParameters = new HashMap<>();
    private List<UMLParameterDiff> parameterDiffs = new ArrayList<>();

    public final boolean nameChanged;
    public final boolean parametersReordered;

    public UMLOperationDiff(FunctionDeclaration function1, FunctionDeclaration function2) {
        this.function1 = function1;
        this.function2 = function2;
        nameChanged = !function1.nameEquals(function2);

        // Diff Parameters.
        List<SimpleEntry<UMLParameter, UMLParameter>> parametersMatchedByNameAndDefaultValue =
                diffParametersByNameAndDefaultValue(function1, function2);
        parametersReordered = checkParametersReordered(parametersMatchedByNameAndDefaultValue.size());
        tryMatchRemovedAndAddedParameters();
    }

    /**
     * For the unmatched paramters, try matching by name, default value index in parent etc
     */
    protected void tryMatchRemovedAndAddedParameters() {
        matchParametersWithSameName();
        matchParametersWithSameIndexPosition();
        matchParametersWithSameDefaultValue();
    }

    //first round match parameters with the same name
    protected void matchParametersWithSameName() {
        final Set<String> removedParameterNames = removedParameters.keySet();
        for (String removedParameterName : removedParameterNames) {
            if (addedParameters.containsKey(removedParameterName)) {
                // Same name found
                UMLParameterDiff parameterDiff = new UMLParameterDiff(removedParameters.get(removedParameterName)
                        , addedParameters.get(removedParameterName));
                parameterDiffs.add(parameterDiff);

                // Since match found, remove it from unmatched parameter list
                addedParameters.remove(removedParameterName);
                removedParameters.remove(removedParameterName);
            }
        }
    }

    /**
     * Only tries to match if both function has ssame number of parameters
     */
    private void matchParametersWithSameIndexPosition() {
        // We only try to match if both function have same number of parameters
        if (function1.parameterCount() == function2.parameterCount()) {
            final Map<Integer, UMLParameter> indexPositionMap = new HashMap<>(addedParameters.size());
            for (UMLParameter addedParameter : addedParameters.values()) {
                indexPositionMap.put(addedParameter.getIndexPositionInParent(), addedParameter);
            }

            final Set<String> removedParameterNames = removedParameters.keySet();
            UMLParameter addedParameter, removedParameter;

            for (String removedParameterName : removedParameterNames) {
                removedParameter = removedParameters.get(removedParameterName);
                addedParameter = indexPositionMap.get(removedParameter.getIndexPositionInParent());

                if (addedParameter != null) {
                    final UMLParameterDiff parameterDiff = new UMLParameterDiff(removedParameter
                            , addedParameter);
                    parameterDiffs.add(parameterDiff);

                    // Since match found, remove it from unmatched parameter list
                    removedParameters.remove(removedParameterName);
                    addedParameters.remove(addedParameter.name);
                }
            }
        }
    }

    // Match parameters with same default value
    private void matchParametersWithSameDefaultValue() {
        final List<UMLParameter> matchedDefaultValueParameters = new ArrayList<>();
        final List<UMLParameter> removedParameterWithDefaultValue = removedParameters.values()
                .stream()
                .filter(param -> param.hasDefaultValue())
                .collect(Collectors.toList());

        final Set<UMLParameter> addedParametersWithDefaultValue = addedParameters.values()
                .stream()
                .filter(param -> param.hasDefaultValue())
                .collect(Collectors.toSet());

        if (addedParametersWithDefaultValue.size() > 0) {
            // TODO optimise by removing On^2 with DP
            for (UMLParameter removedParameter : removedParameterWithDefaultValue) {
                matchedDefaultValueParameters.clear();

                for (UMLParameter addedParameter : addedParametersWithDefaultValue) {
                    if (removedParameter.hasSameDefaultValue(addedParameter)) {
                        matchedDefaultValueParameters.add(addedParameter);
                    }
                }

                if (matchedDefaultValueParameters.size() > 0) {
                    // Sort by index position in parent
                    // Then Sort by name similarity?
                    matchedDefaultValueParameters.sort(Comparator.comparingInt(
                            (UMLParameter parameter) -> Math.abs(removedParameter.getIndexPositionInParent() - parameter.getIndexPositionInParent())
                    ));

                    UMLParameter bestMatchAddedParameter = matchedDefaultValueParameters.get(0);
                    final UMLParameterDiff parameterDiff = new UMLParameterDiff(removedParameter
                            , bestMatchAddedParameter);
                    parameterDiffs.add(parameterDiff);

                    // Since match found, remove it from unmatched parameter list
                    removedParameters.remove(removedParameter.name);
                    addedParameters.remove(bestMatchAddedParameter.name);

                    addedParametersWithDefaultValue.remove(bestMatchAddedParameter);
                }
            }
        }
    }

    /**
     * Returns the matched params1 -> params2 to mapping based on name and default value
     * Also updates the paramDiffs for matched ones, removed and added params
     */
    private List<SimpleEntry<UMLParameter, UMLParameter>> diffParametersByNameAndDefaultValue(FunctionDeclaration function1, FunctionDeclaration function2) {
        final List<SimpleEntry<UMLParameter, UMLParameter>> paramsMatchedByNameAndValue = new ArrayList<>();
        UMLParameter parameter2;
        for (UMLParameter parameter1 : function1.getParameters().values()) {
            // Check if function 2 contains the same named parameter
            // IN java it's equalsIncludingName i.e. full match
            parameter2 = function2.getParameter(parameter1.name);
            if (parameter2 != null && parameter2.hasSameDefaultValue(parameter1)) {
                paramsMatchedByNameAndValue.add(new SimpleEntry<>(parameter1, parameter2));

                // Add to diff list
                UMLParameterDiff parameterDiff = new UMLParameterDiff(parameter1, parameter2);
                parameterDiffs.add(parameterDiff);
            } else {
                // Param1 is not present in function 2 i.e. it's been removed
                this.removedParameters.put(parameter1.name, parameter1);
            }
        }

        for (UMLParameter param2 : function2.getParameters().values()) {
            if (!function1.hasParameterOfName(param2.name)) {
                // Params2 is not present in function . i.e it has been added
                this.addedParameters.put(param2.name, param2);
            }
        }

        return paramsMatchedByNameAndValue;
    }

    private boolean checkParametersReordered(int matchedParameterCount) {
        final Set<String> parameterNames1 = function1.getParameters().keySet();
        final Set<String> parameterNames2 = function2.getParameters().keySet();

        return removedParameters.isEmpty() && addedParameters.isEmpty()
                && parameterNames1.size() > 1
                && matchedParameterCount == parameterNames1.size() && matchedParameterCount == parameterNames2.size()
                && !parameterNames1.equals(parameterNames2);
    }

    // region setters and getters

    public Map<String, UMLParameter> getAddedParameters() {
        return addedParameters;
    }

    public Map<String, UMLParameter> getRemovedParameters() {
        return removedParameters;
    }
    // endregion
}
