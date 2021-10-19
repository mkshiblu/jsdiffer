package io.jsrminer.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DiffUtil {

    public static <T> List<T> common(Collection<T> collection1, Collection<T> collection2) {
        List<T> common = new ArrayList<T>(collection1);
        common.retainAll(collection2);
        return common;
    }

    public static <T> List<T> getUnmatchedInFirstCollection(Collection<T> collection1, Collection<T> collection2) {
        List<T> uncommon = new ArrayList<T>(collection1);
        uncommon.removeAll(collection2);
        return uncommon;
    }
}
