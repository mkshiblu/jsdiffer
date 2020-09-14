package io.jsrminer.uml.mapping.replacement;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplacementUtil {
    public static boolean sameCharsBeforeAfter(String completeString1, String completeString2, String commonSubString) {
        Pattern p = Pattern.compile(Pattern.quote(commonSubString));
        Matcher m1 = p.matcher(completeString1);
        Matcher m2 = p.matcher(completeString2);
        int matches = 0;
        int compatibleMatches = 0;
        while (m1.find() && m2.find()) {
            int start1 = m1.start();
            int start2 = m2.start();
            String characterBeforeMatch1 = start1 == 0 ? "" : String.valueOf(completeString1.charAt(start1 - 1));
            String characterBeforeMatch2 = start2 == 0 ? "" : String.valueOf(completeString2.charAt(start2 - 1));
            int end1 = m1.end();
            int end2 = m2.end();
            String characterAfterMatch1 = end1 == completeString1.length() ? "" : String.valueOf(completeString1.charAt(end1));
            String characterAfterMatch2 = end2 == completeString2.length() ? "" : String.valueOf(completeString2.charAt(end2));
            if (characterBeforeMatch1.equals(characterBeforeMatch2) && characterAfterMatch1.equals(characterAfterMatch2)) {
                compatibleMatches++;
            }
            matches++;
        }
        return matches == compatibleMatches;
    }

    public static void removeCommonElements(Set<String> strings1, Set<String> strings2) {
        Set<String> intersection = new HashSet<>(strings1);
        intersection.retainAll(strings2);
        strings1.removeAll(intersection);
        strings2.removeAll(intersection);
    }
}
