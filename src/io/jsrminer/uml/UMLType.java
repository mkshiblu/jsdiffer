package io.jsrminer.uml;

import io.jsrminer.sourcetree.SourceLocation;

import java.util.ArrayList;
import java.util.List;

public class UMLType {
    private SourceLocation locationInfo;
    private int arrayDimension;
    private List<UMLType> typeArguments = new ArrayList<>();
    //protected List<UMLAnnotation> annotations = new ArrayList<UMLAnnotation>();

    public SourceLocation getLocationInfo() {
        return locationInfo;
    }

    public List<UMLType> getTypeArguments() {
        return typeArguments;
    }
}
