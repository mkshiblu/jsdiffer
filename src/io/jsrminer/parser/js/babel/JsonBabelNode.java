package io.jsrminer.parser.js.babel;

import com.eclipsesource.v8.V8Array;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import io.jsrminer.sourcetree.SourceLocation;
import io.jsrminer.util.Lazy;

import java.util.function.Function;

public class JsonBabelNode extends BabelNode {

    private final Any any;
    private final String fileName;
    private final Function<String, String> formatCodeFunction;
    private final BabelNodeType type;
    private boolean defined;
    private Lazy<SourceLocation> sourceLocationLazy = new Lazy<>(() -> createSourceLocation());
    private Lazy<String> textLazy = new Lazy<>(() -> createText());

    JsonBabelNode(String json, Function<String, String> toStringFunction, String fileName) {
        this(JsonIterator.deserialize(json), toStringFunction, fileName);
    }

    JsonBabelNode(Any any, Function<String, String> toStringFunction, String fileName) {
        this.any = any;
        this.fileName = fileName;
//        if (this.any instanceof V8Object) {
//            if (this.any instanceof V8Array) {
//                objectAsArray = (V8Array) this.any;
//            } else {
//                objectAsArray = null;
//            }
//
//            V8Object v8Object = (V8Object) this.any;
//            this.keys = v8Object.getKeys();
//            if (!v8Object.isUndefined()) {
//                this.v8Object = v8Object;
//                this.defined = true;
//            } else {
//                this.v8Object = null;
//                this.defined = false;
//            }
//        } else {
        this.defined = this.any != null;
//            this.v8Object = null;
//            this.objectAsArray = null;
//            this.keys = null;
//        }
        this.formatCodeFunction = toStringFunction;
        this.type = createNodeType();
    }

    @Override
    public BabelNode get(String member) {
        if (any != null) {
            if (any.keys().contains(member)) {
                return new JsonBabelNode(any.get(member), formatCodeFunction, this.fileName);
            } else {
                throw error("Object has no member '" + member + "'");
            }
        } else {
            throw error("Not an object");
        }
    }

    @Override
    public String getString(String member) {
        return get(member).asString();
    }

    @Override
    public BabelNode get(int pos) {
        return new JsonBabelNode(any.get(pos), formatCodeFunction, this.fileName);
//        if (objectAsArray != null) {
//            return addChild(new V8BabelNode(objectAsArray.get(pos), toStringFunction, this.fileName));
//        } else {
//            throw error("Not an array");
//        }
    }

    @Override
    public String asString() {
        if (any != null) {
            return any.toString();
        } else {
            throw error("Any is null");
        }
    }

    @Override
    public Boolean asBoolean() {
        if (any != null) {
            return any.toBoolean();
        } else {
            throw error("Any is null");
        }
    }

    @Override
    public int asInt() {
        if (any != null) {
            return any.toInt();
        } else {
            throw error("Any is null");
        }
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocationLazy.getValue();
    }

    @Override
    public BabelNodeType getType() {
        return type;
    }

    @Override
    public int size() {
       return any.size();
    }

    @Override
    public boolean isDefined() {
        return defined;
    }

    @Override
    public String getText() {
        return textLazy.getValue();
    }

    @Override
    public void close() {

    }

    private BabelNodeType createNodeType() {
        if (any != null && any.keys().contains("type")) {
            var typeString = any.toString("type");
            var type = BabelNodeType.fromTitleCase(typeString);

            if (type == null) {
                throw new BabelException("Unsupported Babel Node type " + typeString);
            }
            return type;
        }
        return null;
    }

    private RuntimeException error(String string) {
        return new RuntimeException(string + ":\n" + toString());
    }

    private String createText() {
        return formatCodeFunction.apply(asString());
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
}
