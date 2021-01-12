package io.jsrminer.sourcetree;

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
}
