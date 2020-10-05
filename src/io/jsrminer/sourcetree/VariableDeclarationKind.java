package io.jsrminer.sourcetree;

import java.util.HashMap;
import java.util.Map;

public enum VariableDeclarationKind {

    VAR("var"), LET("let"),

    CONST("const");

    public final String keywordName;

    VariableDeclarationKind(String keywordName) {
        this.keywordName = keywordName;
    }

    private static Map<String, VariableDeclarationKind> nameKindMap = new HashMap<>();

    static {
        for (VariableDeclarationKind type : VariableDeclarationKind.values()) {
            nameKindMap.put(type.keywordName, type);
        }
    }
    
    /**
     * @param kindName The kind name as appeared in the js source files
     * @return
     */
    public static VariableDeclarationKind fromName(String kindName) {
        return nameKindMap.get(kindName);
    }
}
