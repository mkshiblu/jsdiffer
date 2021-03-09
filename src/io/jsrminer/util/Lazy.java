package io.jsrminer.util;

import java.util.function.Supplier;

public class Lazy<T> {
    private T value;
    Supplier<T> valueFactory;
    boolean isInitialized;

    public Lazy(Supplier<T> initializer) {
        this.valueFactory = initializer;
    }

    private T init() {
        return valueFactory.get();
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public T getValue() {
        if (value == null && !isInitialized) {
            value = init();
            isInitialized = true;
        }
        return value;
    }
}
