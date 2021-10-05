package io.jsrminer.parser.js.babel;

import java.util.EnumMap;


public final class EnumMapBuilder<K extends Enum<K>, V> {
    private final EnumMap<K, V> map;

    public EnumMapBuilder(Class<K> keyType) {
        map = new EnumMap<>(keyType);
    }

    public EnumMapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public EnumMap<K, V> build() {
        return map;
    }
}

