package io.jsrminer.sourcetree;

import io.jsrminer.uml.diff.StringDistance;

public class ObjectCreation extends Invocation {
    public ObjectCreation() {

    }

    public boolean identicalName(Invocation call) {
        // getType().equals(((ObjectCreation)call).getType());
        return getFunctionName().equals(((ObjectCreation) call).getFunctionName());
    }

    public boolean isArray() {
        return CodeElementType.ARRAY_EXPRESSION == this.type;
    }

    public double normalizedNameDistance(Invocation call) {
        String s1 = getFunctionName().toString().toLowerCase();
        String s2 = ((ObjectCreation) call).getFunctionName().toString().toLowerCase();
        int distance = StringDistance.editDistance(s1, s2);
        double normalized = (double) distance / (double) Math.max(s1.length(), s2.length());
        return normalized;
    }
}
