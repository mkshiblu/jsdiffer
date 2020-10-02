package io.jsrminer.sourcetree;

public class ObjectCreation extends Invocation {
    public ObjectCreation() {

    }

    public boolean isArray() {
        return CodeElementType.ARRAY_EXPRESSION == this.type;
    }
}
