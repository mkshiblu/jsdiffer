package io.jsrminer.uml;

import io.jsrminer.sourcetree.SourceLocation;
import io.jsrminer.sourcetree.VariableDeclaration;
import io.jsrminer.sourcetree.VariableDeclarationKind;
import io.jsrminer.uml.diff.StringDistance;

import java.io.Serializable;

public class UMLAttribute implements Comparable<UMLAttribute>, Serializable {
    private SourceLocation locationInfo;
    private String name;
    private String visibility;
    private String className;
    private boolean isFinal;
    private boolean isStatic;
    private VariableDeclaration variableDeclaration;
    //private UMLJavadoc javadoc;

    public UMLAttribute(String name, SourceLocation locationInfo) {
        this.locationInfo = locationInfo;
        this.name = name;
    }

    public SourceLocation getLocationInfo() {
        return locationInfo;
    }

//    public UMLType getType() {
//        return type;
//    }
//
//    public void setType(UMLType type) {
//        this.type = type;
//    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public String getNonQualifiedClassName() {
        return className.contains(".") ? className.substring(className.lastIndexOf(".") + 1, className.length()) : className;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public VariableDeclaration getVariableDeclaration() {
        return variableDeclaration;
    }

    public void setVariableDeclaration(VariableDeclaration variableDeclaration) {
        this.variableDeclaration = variableDeclaration;
    }

//
//    public List<UMLAnnotation> getAnnotations() {
//        return variableDeclaration.getAnnotations();
//    }

    public boolean equalsIgnoringChangedType(UMLAttribute attribute) {
        if (this.isStatic != attribute.isStatic)
            return false;
        if (this.isFinal != attribute.isFinal)
            return false;
        return this.name.equals(attribute.name);
    }

    public boolean equalsIgnoringChangedVisibility(UMLAttribute attribute) {
        return this.name.equals(attribute.name);
    }

//    public CodeRange codeRange() {
//        LocationInfo info = getLocationInfo();
//        return info.codeRange();
//    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof UMLAttribute) {
            UMLAttribute umlAttribute = (UMLAttribute) o;
            return this.name.equals(umlAttribute.name) &&
                    this.visibility.equals(umlAttribute.visibility);
        }
        return false;
    }

    public boolean equalsQualified(UMLAttribute umlAttribute) {
        return this.name.equals(umlAttribute.name) &&
                this.visibility.equals(umlAttribute.visibility)
                //&& this.type.equalsQualified(umlAttribute.type)
                ;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(visibility);
        sb.append(" ");
        sb.append(name);
        sb.append(" : ");
        return sb.toString();
    }

    public String toQualifiedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(visibility);
        sb.append(" ");
        sb.append(name);
        sb.append(" : ");
        //sb.append(type.toQualifiedString());
        return sb.toString();
    }

    public int compareTo(UMLAttribute attribute) {
        return this.toString().compareTo(attribute.toString());
    }

    public double normalizedNameDistance(UMLAttribute attribute) {
        String s1 = getName().toLowerCase();
        String s2 = attribute.getName().toLowerCase();
        int distance = StringDistance.editDistance(s1, s2);
        double normalized = (double) distance / (double) Math.max(s1.length(), s2.length());
        return normalized;
    }

}
