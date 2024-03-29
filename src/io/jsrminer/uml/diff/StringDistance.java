package io.jsrminer.uml.diff;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class StringDistance {

    /**
     * If the threshold is not null, distance calculations will be limited to a maximum length.
     * If the threshold is null, the unlimited version of the algorithm will be used.
     *
     * @return
     */
    public static int editDistance(String a, String b, int threshold) {
        return new LevenshteinDistance(threshold).apply(a, b);
    }

    public static int editDistance(String a, String b) {
        return new LevenshteinDistance().apply(a, b);
    }

    /**
     * Returns the normalized string edit distance between the two string in lower cases
     */
    public static double normalizedDistanceIgnoringCase(String s1, String s2) {
        int distance = StringDistance.editDistance(s1.toLowerCase(), s2.toLowerCase());
        double normalized = (double) distance / (double) Math.max(s1.length(), s2.length());
        return normalized;
    }
}
