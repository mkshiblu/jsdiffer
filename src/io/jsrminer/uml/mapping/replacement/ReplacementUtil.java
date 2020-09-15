package io.jsrminer.uml.mapping.replacement;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplacementUtil {
    private static final String[] SPECIAL_CHARACTERS = {";", ",", ")", "=", "+", "-", ">", "<", ".", "]", " ", "(", "["};
    private static final String[] SPECIAL_ARGUMENT_CHARACTERS = {";", ",", ")", "=", "+", "-", ">", "<", ".", "]", " "};

    /**
     * TODO revisit and understand what's being done
     **/
    public static String performReplacement(String completeString1, String completeString2, String subString1, String subString2) {
        String temp = completeString1;
        boolean replacementOccurred = false;

        for (String character : SPECIAL_CHARACTERS) {
            if (temp.contains(subString1 + character) && completeString2.contains(subString2 + character)) {
                StringBuffer sb = new StringBuffer();
                Pattern p1 = Pattern.compile(Pattern.quote(subString1 + character));
                Matcher m1 = p1.matcher(temp);
                Pattern p2 = Pattern.compile(Pattern.quote(subString2 + character));
                Matcher m2 = p2.matcher(completeString2);
                while (m1.find() && m2.find()) {
                    int start1 = m1.start();
                    int start2 = m2.start();
                    String characterBeforeMatch1 = start1 == 0 ? "" : String.valueOf(temp.charAt(start1 - 1));
                    String characterBeforeMatch2 = start2 == 0 ? "" : String.valueOf(completeString2.charAt(start2 - 1));

                    if (compatibleCharacterBeforeMatch(characterBeforeMatch1, characterBeforeMatch2)) {
                        m1.appendReplacement(sb, Matcher.quoteReplacement(subString2 + character));
                        replacementOccurred = true;
                    }
                }
                m1.appendTail(sb);
                temp = sb.toString();
            }
        }
        if (!replacementOccurred
                && !ReplacementFinder.containsMethodSignatureOfAnonymousClass(completeString1)
                && !ReplacementFinder.containsMethodSignatureOfAnonymousClass(completeString2)) {
            for (String character : SPECIAL_CHARACTERS) {
                if (temp.contains(character + subString1) && completeString2.contains(character + subString2)) {
                    StringBuffer sb = new StringBuffer();
                    Pattern p1 = Pattern.compile(Pattern.quote(character + subString1));
                    Matcher m1 = p1.matcher(temp);
                    Pattern p2 = Pattern.compile(Pattern.quote(character + subString2));
                    Matcher m2 = p2.matcher(completeString2);
                    while (m1.find() && m2.find()) {
                        int end1 = m1.end();
                        int end2 = m2.end();
                        String characterAfterMatch1 = end1 == temp.length() ? "" : String.valueOf(temp.charAt(end1));
                        String characterAfterMatch2 = end2 == completeString2.length() ? "" : String.valueOf(completeString2.charAt(end2));
                        if (compatibleCharacterAfterMatch(characterAfterMatch1, characterAfterMatch2)) {
                            m1.appendReplacement(sb, Matcher.quoteReplacement(character + subString2));
                            replacementOccurred = true;
                        }
                    }
                    m1.appendTail(sb);
                    temp = sb.toString();
                }
            }
        }
        return temp;
    }

    private static boolean compatibleCharacterBeforeMatch(String characterBefore1, String characterBefore2) {
        if (characterBefore1 != null && characterBefore2 != null) {
            if (characterBefore1.equals(characterBefore2))
                return true;
            if (characterBefore1.equals(",") && characterBefore2.equals("("))
                return true;
            if (characterBefore1.equals("(") && characterBefore2.equals(","))
                return true;
            if (characterBefore1.equals(" ") && characterBefore2.equals(""))
                return true;
            if (characterBefore1.equals("") && characterBefore2.equals(" "))
                return true;
        }
        return false;
    }

    private static boolean compatibleCharacterAfterMatch(String characterAfter1, String characterAfter2) {
        if (characterAfter1 != null && characterAfter2 != null) {
            if (characterAfter1.equals(characterAfter2))
                return true;
            if (characterAfter1.equals(",") && characterAfter2.equals(")"))
                return true;
            if (characterAfter1.equals(")") && characterAfter2.equals(","))
                return true;
        }
        return false;
    }

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
