package io.jsrminer.evaluation;

import io.jsrminer.sourcetree.CodeElementType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Ref {
    String repository;
    String commit;
    RefType refType;

    enum RefType {

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
        RENAME_VARIABLE;

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
            fromStringMap.put("RENAME_CLASS", RENAME_CLASS);
            fromStringMap.put("CONVERT_TYPE_FUNCTION", CONVERT_TYPE_FUNCTION);
            fromStringMap.put("CONVERT_TYPE_CLASS", CONVERT_TYPE_CLASS);

            fromStringMap.put("REMOVE_PARAMETER", REMOVE_PARAMETER);
            fromStringMap.put("ADD_PARAMETER", ADD_PARAMETER);
            fromStringMap.put("RENAME_PARAMETER", RENAME_PARAMETER);
            fromStringMap.put("RENAME_VARIABLE", RENAME_VARIABLE);
        }
    }
}
