package io.jsrminer.sourcetree;

import io.jsrminer.uml.diff.StringDistance;

public class ObjectCreation extends Invocation {

    private String typeName;

    public ObjectCreation() {

    }

    public boolean identicalName(Invocation call) {
        // getType().equals(((ObjectCreation)call).getType());
        return getName().equals(call.getName());
    }

    public boolean isArray() {
        return CodeElementType.ARRAY_EXPRESSION == this.type;
    }

    public double normalizedNameDistance(Invocation call) {
        String s1 = getName().toString().toLowerCase();
        String s2 = ((ObjectCreation) call).getName().toString().toLowerCase();
        int distance = StringDistance.editDistance(s1, s2);
        double normalized = (double) distance / (double) Math.max(s1.length(), s2.length());
        return normalized;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    //    public boolean identicalArrayInitializer(ObjectCreation other) {
//        if (this.isArray && other.isArray) {
//            if (this.anonymousClassDeclaration != null && other.anonymousClassDeclaration != null) {
//                return this.anonymousClassDeclaration.equals(other.anonymousClassDeclaration);
//            } else if (this.anonymousClassDeclaration == null && other.anonymousClassDeclaration == null) {
//                return true;
//            }
//        }
//        return false;
//    }
}
