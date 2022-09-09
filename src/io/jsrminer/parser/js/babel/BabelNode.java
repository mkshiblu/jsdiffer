package io.jsrminer.parser.js.babel;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import io.jsrminer.sourcetree.SourceLocation;
import io.jsrminer.util.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class BabelNode implements AutoCloseable {

    //region JV8
    private final Object value;
    private final V8Object v8Object;
    private final V8Array objectAsArray;

    private final boolean defined;
    private final Function<V8Object, String> toStringFunction;
    private final List<BabelNode> children = new ArrayList<>();
    private final String fileName;
    private final String[] keys;

    BabelNode(Object value, Function<V8Object, String> toStringFunction, String fileName) {
        this.value = value;
        this.fileName = fileName;
        if (value instanceof V8Object) {
            if (value instanceof V8Array) {
                objectAsArray = (V8Array) value;
            } else {
                objectAsArray = null;
            }

            V8Object v8Object = (V8Object) value;
            this.keys = v8Object.getKeys();
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
            this.keys = null;
        }
        this.toStringFunction = toStringFunction;
        this.type = createNodeType();
    }

    public boolean has(String member) {
        return v8Object != null && v8Object.contains(member) && get(member).isDefined();
    }

    public BabelNode get(String member) {
        if (v8Object != null) {
            if (v8Object.contains(member)) {
                return addChild(new BabelNode(v8Object.get(member), toStringFunction, this.fileName));
            } else {
                //throw error("Object has no member '" + member + "'");
                return null;
            }
        } else {
            throw error("Not an object");
        }
    }

    public String getString(String member) {
        return get(member).asString();
    }

    public BabelNode get(int pos) {
        if (objectAsArray != null) {
            return addChild(new BabelNode(objectAsArray.get(pos), toStringFunction, this.fileName));
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

    public Boolean asBoolean() {
        if (value instanceof Boolean) {
            return (Boolean) value;
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

    public String[] getKeys() {
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
        return value == null ? "null" : toStringFunction.apply(v8Object);
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

        if (objectAsArray != null) {
            objectAsArray.release();
        }
    }

    public boolean isDefined() {
        return defined;
    }

    //endregion

    //private Lazy<BabelNodeType> nodeTypeLazy = new Lazy<>(() -> createNodeType());
    private final BabelNodeType type;
    private Lazy<SourceLocation> sourceLocationLazy = new Lazy<>(() -> createSourceLocation());
    private Lazy<String> textLazy = new Lazy<>(() -> createText());

    SourceLocation getSourceLocation() {
        return sourceLocationLazy.getValue();
    }

    private String createText() {
        return toStringFunction.apply(v8Object);
    }

    private BabelNodeType createNodeType() {
        if (v8Object != null && v8Object.contains("type")) {
            var typeString = v8Object.getString("type");
            var type = BabelNodeType.fromTitleCase(typeString);

            if (type == null){
                throw  new BabelException("Unsupported Babel Node type " + typeString);
            }
            return type;
        }
        return null;
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

    public String getText() {
        return textLazy.getValue();
    }

    public BabelNodeType getType() {
        return type;
    }
}
