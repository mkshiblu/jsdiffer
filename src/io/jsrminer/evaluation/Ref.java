package io.jsrminer.evaluation;

import io.jsrminer.sourcetree.SourceLocation;

import java.util.HashMap;
import java.util.Map;

public class Ref {
    int lineNo;
    String repository;
    String commit;
    RefType refType;

    String localNameBefore;
    String localNameAfter;

    private SourceLocation locationBefore;
    private SourceLocation locationAfter;
    private String locationBeforeStr;
    private String locationAfterStr;

    private ValidationType validationType = ValidationType.Unknown;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(200);
        builder.append(refType.toString());
        builder.append(" (");
        builder.append(locationBeforeStr);
        builder.append(") ");
        builder.append(localNameBefore);
        builder.append(" (");
        builder.append(locationAfterStr);
        builder.append(") ");
        builder.append(localNameAfter);
        builder.append(" ");
        builder.append(validationType);
        return builder.toString();
    }

    public ValidationType getValidationType() {
        return validationType;
    }

    public void setValidationType(ValidationType validationType) {
        this.validationType = validationType;
    }

    public enum RefType {

        // SUPPORTED BY RD
        EXTRACT_AND_MOVE_FUNCTION,
        MOVE_AND_RENAME_FILE,
        MOVE_AND_RENAME_FUNCTION,
        RENAME_FUNCTION,
        MOVE_FUNCTION,
        MOVE_FILE,
        RENAME_FILE,
        EXTRACT_FUNCTION,
        INLINE_FUNCTION,
        CONVERT_TYPE_FUNCTION,
        CONVERT_TYPE_CLASS,
        MOVE_CLASS,
        RENAME_CLASS,

        // SUPPORTED BY RM
        REMOVE_PARAMETER,
        RENAME_PARAMETER,
        ADD_PARAMETER,
        RENAME_VARIABLE,
        PARAMETERIZE_VARIABLE;

        public static Map<String, RefType> fromStringMap = new HashMap<>();

        static {
            fromStringMap.put("MOVE_FUNCTION", MOVE_FUNCTION);
            fromStringMap.put("RENAME_FUNCTION", RENAME_FUNCTION);
            fromStringMap.put("MOVE_FILE", MOVE_FILE);
            fromStringMap.put("RENAME_FILE", RENAME_FILE);
            fromStringMap.put("EXTRACT_FUNCTION", EXTRACT_FUNCTION);
            fromStringMap.put("INLINE_FUNCTION", INLINE_FUNCTION);
            fromStringMap.put("MOVE_CLASS", MOVE_CLASS);
            fromStringMap.put("EXTRACT_MOVE_FUNCTION", EXTRACT_AND_MOVE_FUNCTION);
            fromStringMap.put("INTERNAL_MOVE_FUNCTION", MOVE_FUNCTION);
            fromStringMap.put("MOVE_RENAME_FUNCTION", MOVE_AND_RENAME_FUNCTION);
            fromStringMap.put("MOVE_RENAME_FILE", MOVE_AND_RENAME_FILE);
            fromStringMap.put("MOVE_AND_RENAME_FILE", MOVE_AND_RENAME_FILE);
            fromStringMap.put("RENAME_CLASS", RENAME_CLASS);
            fromStringMap.put("CONVERT_TYPE_FUNCTION", CONVERT_TYPE_FUNCTION);
            fromStringMap.put("CONVERT_TYPE_CLASS", CONVERT_TYPE_CLASS);

            fromStringMap.put("REMOVE_PARAMETER", REMOVE_PARAMETER);
            fromStringMap.put("ADD_PARAMETER", ADD_PARAMETER);
            fromStringMap.put("RENAME_PARAMETER", RENAME_PARAMETER);
            fromStringMap.put("RENAME_VARIABLE", RENAME_VARIABLE);
            fromStringMap.put("PARAMETERIZE_VARIABLE", PARAMETERIZE_VARIABLE);
        }
    }

    private SourceLocation toSourceLocation(String location) {

        if (location.contains(":")) {
            var splitted = location.split(":");
            String filePath = splitted[0];

            if (splitted[1].contains("|")) {
                var segments = splitted[1].split("\\|");
                var startEndSplitted = segments[0].split("-");
                var start = Integer.parseInt(startEndSplitted[0]);
                var end = Integer.parseInt(startEndSplitted[1]);

                var lineColSplitted = segments[1].split("-");
                var startLineColSplitted = lineColSplitted[0].substring(1, lineColSplitted[0].length() - 1).split(",");
                var endLineColSplitted = lineColSplitted[1].substring(1, lineColSplitted[1].length() - 1).split(",");

                var startLine = Integer.parseInt(startLineColSplitted[0]);
                var startColumn = Integer.parseInt(startLineColSplitted[1]);

                var endLine = Integer.parseInt(endLineColSplitted[0]);
                var endColumn = Integer.parseInt(endLineColSplitted[1]);
                return new SourceLocation(filePath, startLine, startColumn, endLine, endColumn, start, end);
            } else {

                var startEndSplitted = splitted[1].split("-");
                var start = Integer.parseInt(startEndSplitted[0]);
                var end = Integer.parseInt(startEndSplitted[1]);

                return new SourceLocation(filePath, 0, 0, 0, 0, start, end);
            }
        } else {
            return new SourceLocation(location, 0, 0, 0, 0, 0, 0);
        }
    }

    public void setLocationBefore(String location) {
        this.locationBeforeStr = location;
        locationBefore = toSourceLocation(location);
    }

    public void setLocationAfter(String location) {
        this.locationAfterStr = location;
        locationAfter = toSourceLocation(location);
    }

    public SourceLocation getLocationBefore() {
        return locationBefore;
    }

    public SourceLocation getLocationAfter() {
        return locationAfter;
    }

    public String getRepository() {
        return repository;
    }

    public RefType getRefType() {
        return refType;
    }

    public String getCommit() {
        return commit;
    }

    public String getLocalNameAfter() {
        return localNameAfter;
    }

    public String getLocalNameBefore() {
        return localNameBefore;
    }
}
