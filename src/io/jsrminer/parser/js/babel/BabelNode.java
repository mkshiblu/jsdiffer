package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.SourceLocation;

public abstract class BabelNode implements AutoCloseable {
    public abstract BabelNode get(String member);

    public abstract String getString(String member);

    public abstract BabelNode get(int pos);

    public abstract String asString();

    public abstract Boolean asBoolean();

    public abstract int asInt();

    public abstract SourceLocation getSourceLocation();

    public abstract BabelNodeType getType();

    public abstract int size();

    public abstract boolean isDefined();

    public abstract String getText();

    public abstract void close();
}
