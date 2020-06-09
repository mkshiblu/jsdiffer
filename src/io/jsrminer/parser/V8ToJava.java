package io.jsrminer.parser;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import io.jsrminer.sourcetree.SourceLocation;

class V8ToJava {
    static SourceLocation parseLocation(V8Object v8Location) {
        return new SourceLocation(v8Location.getInteger("startLine")
                , v8Location.getInteger("startColumn")
                , v8Location.getInteger("endLine")
                , v8Location.getInteger("endColumn")
        );
    }

    public static String[] toStringArray(V8Array v8Array) {
        final String[] arr = new String[v8Array.length()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = v8Array.getString(i);
        }
        v8Array.release();
        return arr;
    }
}
