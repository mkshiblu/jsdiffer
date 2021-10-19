package io.jsrminer.parser.js.babel;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class JV8 implements AutoCloseable {
    private final Object value;
    private final V8Object v8Object;
    private final V8Array objectAsArray;

    private final boolean defined;
    private final Function<Object, String> toJsonFunction;
    private final List<JV8> children = new ArrayList<>();

    public JV8(Object value, Function<Object, String> toJsonFunction) {
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

    public JV8 get(String member) {
        if (v8Object != null) {
            if (v8Object.contains(member)) {
                return addChild(new JV8(v8Object.get(member), toJsonFunction));
            } else {
                throw error("Object has no member '" + member + "'");
            }
        } else {
            throw error("Not an object");
        }
    }

    public JV8 get(int pos) {
        if (objectAsArray != null) {
            return addChild(new JV8(objectAsArray.get(pos), toJsonFunction));
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

    private JV8 addChild(JV8 value) {
        if (value.v8Object != null) {
            this.children.add(value);
        }
        return value;
    }

    @Override
    public void close() {
        for (JV8 child : children) {
            child.close();
        }
        if (v8Object != null) {
            v8Object.release();
        }
    }

    public boolean isDefined() {
        return defined;
    }
}
