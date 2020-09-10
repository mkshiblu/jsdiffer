package io.jsrminer.sourcetree;

import java.util.Map;

public enum VariableDeclarationKind {
    VAR, LET, CONST;

    private static Map<String, VariableDeclarationKind> nameKindMap = Map.of("var", VAR,
            "let", LET,
            "const", CONST);

    /**
     * @param kindName The kind name as appeared in the js source files
     * @return
     */
    public static VariableDeclarationKind fromName(String kindName) {
        return nameKindMap.get(kindName);
    }
}
