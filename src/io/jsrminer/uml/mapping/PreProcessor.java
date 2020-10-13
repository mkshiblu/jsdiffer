package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.sourcetree.Statement;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreProcessor {

    private Map<Statement, String> afterReplacementsMap = new HashMap<>();

    public String replaceParametersWithArguments(Statement statement, Map<String, String> parameterToArgumentMap) {
        if (afterReplacementsMap.containsKey(statement))
            return afterReplacementsMap.get(statement);

        String afterReplacements = statement.getText();

        for (String parameter : parameterToArgumentMap.keySet()) {
            String argument = parameterToArgumentMap.get(parameter);

            if (!parameter.equals(argument)) {
                StringBuffer sb = new StringBuffer();
                Pattern p = Pattern.compile(Pattern.quote(parameter));
                Matcher m = p.matcher(afterReplacements);

                while (m.find()) {
                    //check if the matched string is an argument
                    //previous character should be "(" or "," or " " or there is no previous character
                    int start = m.start();
                    boolean isArgument = false;
                    boolean isInsideStringLiteral = false;
                    if (start >= 1) {
                        String previousChar = afterReplacements.substring(start - 1, start);
                        if (previousChar.equals("(") || previousChar.equals(",") || previousChar.equals(" ") || previousChar.equals("=")) {
                            isArgument = true;
                        }
                        String beforeMatch = afterReplacements.substring(0, start);
                        String afterMatch = afterReplacements.substring(start + parameter.length(), afterReplacements.length());
                        if (quoteBefore(beforeMatch) && quoteAfter(afterMatch)) {
                            isInsideStringLiteral = true;
                        }
                    } else if (start == 0 && !afterReplacements.startsWith("return ")) {
                        isArgument = true;
                    }
                    if (isArgument && !isInsideStringLiteral) {
                        m.appendReplacement(sb, Matcher.quoteReplacement(argument));
                    }
                }
                m.appendTail(sb);
                afterReplacements = sb.toString();
            }
        }
        afterReplacementsMap.put(statement, afterReplacements);
        //.codeFragmentAfterReplacingParametersWithArguments = afterReplacements;
        return afterReplacements;
    }

    public String getArgumentizedString(SingleStatement statement) {
        return this.afterReplacementsMap.get(statement);
    }

    private static boolean quoteBefore(String beforeMatch) {
        if (beforeMatch.contains("\"")) {
            if (beforeMatch.contains("+")) {
                int indexOfQuote = beforeMatch.lastIndexOf("\"");
                int indexOfPlus = beforeMatch.lastIndexOf("+");
                if (indexOfPlus > indexOfQuote) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private static boolean quoteAfter(String afterMatch) {
        if (afterMatch.contains("\"")) {
            if (afterMatch.contains("+")) {
                int indexOfQuote = afterMatch.indexOf("\"");
                int indexOfPlus = afterMatch.indexOf("+");
                if (indexOfPlus < indexOfQuote) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }
}
