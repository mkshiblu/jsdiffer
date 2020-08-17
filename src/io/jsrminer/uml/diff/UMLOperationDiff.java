package io.jsrminer.uml.diff;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.UMLParameter;


import java.util.ArrayList;
import java.util.List;
import static  java.util.AbstractMap.SimpleEntry;

public class UMLOperationDiff extends Diff {

    final FunctionDeclaration function1;
    final FunctionDeclaration function2;

    private List<UMLParameter> addedParameters = new ArrayList<>();
    private List<UMLParameter> removedParameters = new ArrayList<>();

    final boolean isRenamed;

    public UMLOperationDiff(FunctionDeclaration function1, FunctionDeclaration function2) {
        this.function1 = function1;
        this.function2 = function2;
        isRenamed = !function1.nameEquals(function2);

    }

    private List<SimpleEntry<UMLParameter, UMLParameter>> updateAddedRemovedParameters(FunctionDeclaration function1, FunctionDeclaration function2) {
        final List<SimpleEntry<UMLParameter, UMLParameter>> matchedParameters = new ArrayList<>();
        for(UMLParameter parameter1 : function1.getParameters()) {
            if(!parameter1.getKind().equals("return")) {
                boolean found = false;
                for(UMLParameter parameter2 : function2.getParameters()) {
                    if(parameter1.equalsIncludingName(parameter2)) {
                        matchedParameters.add(new SimpleEntry<UMLParameter, UMLParameter>(parameter1, parameter2));
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    this.removedParameters.add(parameter1);
                }
            }
        }
        for(UMLParameter parameter1 : function2.getParameters()) {
            if(!parameter1.getKind().equals("return")) {
                boolean found = false;
                for(UMLParameter parameter2 : function1.getParameters()) {
                    if(parameter1.equalsIncludingName(parameter2)) {
                        matchedParameters.add(new SimpleEntry<UMLParameter, UMLParameter>(parameter2, parameter1));
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    this.addedParameters.add(parameter1);
                }
            }
        }
        return matchedParameters;
    }
}
