package io.jsrminer.parser.js.babel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import io.jsrminer.sourcetree.SourceLocation;
import io.jsrminer.util.Lazy;

class BabelNode implements AutoCloseable {

    //region JV8
    private final Object value;
    private final V8Object v8Object;
    private final V8Array objectAsArray;

    private final boolean defined;
    private final Function<Object, String> toJsonFunction;
    private final List<BabelNode> children = new ArrayList<>();
    String fileName;
    String fileContent;

    BabelNode(Object value, Function<Object, String> toJsonFunction, String fileName, String fileContent) {
        this(value, toJsonFunction);
        this.fileContent = fileContent;
        this.fileName = fileName;
    }

    private BabelNode(Object value, Function<Object, String> toJsonFunction) {
        this.value = value;
        if (value instanceof V8Object) {
            if (value instanceof V8Array) {
                objectAsArray = (V8Array) value;
            } else {
                objectAsArray = null;
            }

            V8Object v8Object = (V8Object) value;
            if (!v8Object.isUndefined()) {
                this.v8Object = v8Object;
                this.defined = true;
            } else {
                this.v8Object = null;
                this.defined = false;
            }
        } else {
            this.defined = value != null;
            this.v8Object = null;
            this.objectAsArray = null;
        }
        this.toJsonFunction = toJsonFunction;
    }

    public boolean has(String member) {
        return v8Object != null && v8Object.contains(member) && get(member).isDefined();
    }

    public BabelNode get(String member) {
        if (v8Object != null) {
            if (v8Object.contains(member)) {
                return addChild(new BabelNode(v8Object.get(member), toJsonFunction));
            } else {
                throw error("Object has no member '" + member + "'");
            }
        } else {
            throw error("Not an object");
        }
    }

    public BabelNode get(int pos) {
        if (objectAsArray != null) {
            return addChild(new BabelNode(objectAsArray.get(pos), toJsonFunction));
        } else {
            throw error("Not an array");
        }
    }

    public String asString() {
        if (value instanceof String) {
            return (String) value;
        } else {
            throw error("Not a string");
        }
    }

    public int asInt() {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            throw error("Not a number");
        }
    }

    public String[] getOwnKeys() {
        if (v8Object != null) {
            return v8Object.getKeys();
        } else {
            throw error("Not an object");
        }
    }

    public boolean isObject() {
        return v8Object != null && !(v8Object instanceof V8Array);
    }

    public boolean isArray() {
        return v8Object instanceof V8Array;
    }

    public int size() {
        if (v8Object instanceof V8Array) {
            return ((V8Array) v8Object).length();
        } else {
            throw error("Not an array");
        }
    }

    @Override
    public String toString() {
        return value == null ? "null" : toJsonFunction.apply(value);
    }

    private RuntimeException error(String string) {
        return new RuntimeException(string + ":\n" + toString());
    }

    private BabelNode addChild(BabelNode value) {
        if (value.v8Object != null) {
            this.children.add(value);
        }
        return value;
    }

    @Override
    public void close() {
        for (BabelNode child : children) {
            child.close();
        }
        if (v8Object != null) {
            v8Object.release();
        }
    }

    public boolean isDefined() {
        return defined;
    }

    //endregion

    private Lazy<SourceLocation> sourceLocationLazy = new Lazy<>(() -> createSourceLocation());

    SourceLocation getSourceLocation() {
        return sourceLocationLazy.getValue();
    }

    private SourceLocation createSourceLocation() {
        var node = this;
        int start = node.get("start").asInt();
        int end = node.get("end").asInt();

        var loc = node.get("loc");
        var startLoc = loc.get("start");
        var endLoc = loc.get("end");
        int startLine = startLoc.get("line").asInt();
        int startColumn = startLoc.get("column").asInt();
        int endLine = endLoc.get("line").asInt();
        int endColumn = endLoc.get("column").asInt();

//        loc.close();

        return new SourceLocation(
                this.fileName
                , startLine
                , startColumn
                , endLine
                , endColumn
                , start
                , end
        );
    }
}
