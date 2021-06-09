package io.jsrminer.uml;

import io.jsrminer.sourcetree.SourceLocation;

import java.util.ArrayList;
import java.util.List;

public class UMLType {
    private final SourceLocation sourceLocation;
    private int arrayDimension;
    private List<UMLType> typeArguments = new ArrayList<>();
    //protected List<UMLAnnotation> annotations = new ArrayList<UMLAnnotation>();

    private final String typeName;
    private final String typeQualifiedName;

    public UMLType(String typeName, String typeQualifiedName,SourceLocation sourceLocation){
        this.typeName = typeName;
        this.typeQualifiedName = typeQualifiedName;
        this.sourceLocation = sourceLocation;
    }

    public SourceLocation getSourceLocation() {
        return this.sourceLocation;
    }

    public List<UMLType> getTypeArguments() {
        return typeArguments;
    }

    public String getTypeQualifiedName() {
        return typeQualifiedName;
    }

    public String getTypeName() {
        return typeName;
    }
}
