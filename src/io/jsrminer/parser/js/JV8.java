package io.jsrminer.parser.js;

import com.eclipsesource.v8.V8Array;

class JV8 {
    public static String[] toStringArray(V8Array v8Array) {
        final String[] arr = new String[v8Array.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = v8Array.getString(i);
        }
        v8Array.release();
        return arr;
    }
}
