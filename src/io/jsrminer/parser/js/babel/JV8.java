package io.jsrminer.parser.js.babel;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

import java.io.Closeable;
import java.io.IOException;

class JV8 implements AutoCloseable {
    private final V8Object v8Object;

    JV8(V8Object v8Object) {
        this.v8Object = v8Object;
    }

    @Override
    public void close() throws IOException {
        if (v8Object !=null) {
            v8Object.release();
        }
    }

//    public static String[] toStringArray(V8Array v8Array) {
//        final String[] arr = new String[v8Array.length()];
//        for (int i = 0; i < arr.length; i++) {
//            arr[i] = v8Array.getString(i);
//        }
//        v8Array.release();
//        return arr;
//    }
}
