package io.jsrminer.uml.diff;

import io.jsrminer.uml.mapping.FunctionBodyMapper;

import java.util.Comparator;

public class UMLOperationBodyMapperComparator implements Comparator<FunctionBodyMapper> {

    @Override
    public int compare(FunctionBodyMapper o1, FunctionBodyMapper o2) {
        int thisOperationNameEditDistance = o1.operationNameEditDistance();
        int otherOperationNameEditDistance = o2.operationNameEditDistance();
        if (thisOperationNameEditDistance != otherOperationNameEditDistance)
            return Integer.compare(thisOperationNameEditDistance, otherOperationNameEditDistance);
        else
            return o1.compareTo(o2);
    }
}
