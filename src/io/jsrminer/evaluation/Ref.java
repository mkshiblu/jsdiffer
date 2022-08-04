package io.jsrminer.evaluation;

import io.jsrminer.sourcetree.SourceLocation;

import java.util.HashMap;
import java.util.Map;

public class Ref {
    int lineNo;
    String project;
    String commitId;
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
        PARAMETERIZE_VARIABLE,
        MOVE_RENAME_CLASS,

        // INTERNALS
        INTERNAL_MOVE_FUNCTION,
        INTERNAL_MOVE_RENAME_FUNCTION;

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
            fromStringMap.put("INTERNAL_MOVE_FUNCTION", INTERNAL_MOVE_FUNCTION);
            fromStringMap.put("INTERNAL_MOVE_RENAME_FUNCTION", INTERNAL_MOVE_RENAME_FUNCTION);
            fromStringMap.put("MOVE_RENAME_FUNCTION", MOVE_AND_RENAME_FUNCTION);
            fromStringMap.put("MOVE_RENAME_CLASS", MOVE_RENAME_CLASS);
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

            var sourceLocationInfo =  splitted[1];
            if (sourceLocationInfo.contains("|")) {
                var segments = sourceLocationInfo.split("\\|");
                var startEndSplitted = segments[0].split("-");
                var start = Integer.parseInt(startEndSplitted[0]);
                var end = Integer.parseInt(startEndSplitted[1]);
                var endLine = 0;
                var endColumn = 0;
                var startLine = 0;
                var startColumn = 0;

                var lineColSplitted = segments[1].split("-");
                if (lineColSplitted.length > 1) {
                    var startLineColSplitted = lineColSplitted[0].substring(1, lineColSplitted[0].length() - 1).split(",");
                    startLine = Integer.parseInt(startLineColSplitted[0]);
                    startColumn = Integer.parseInt(startLineColSplitted[1]);

                    var endLineColSplitted = lineColSplitted[1].substring(1, lineColSplitted[1].length() - 1).split(",");
                    endLine = Integer.parseInt(endLineColSplitted[0]);
                    endColumn = Integer.parseInt(endLineColSplitted[1]);
                } else if (lineColSplitted.length  == 1) {
                    startLine = Integer.parseInt(lineColSplitted[0]);
                }
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

    public String getProject() {
        return project;
    }

    public RefType getRefType() {
        return refType;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getLocalNameAfter() {
        return localNameAfter;
    }

    public String getLocalNameBefore() {
        return localNameBefore;
    }
}
