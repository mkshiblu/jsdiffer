package io.jsrminer.sourcetree;

public abstract class Invocation {

    public enum InvocationCoverageType {
        NONE, ONLY_CALL, RETURN_CALL, THROW_CALL, CAST_CALL, VARIABLE_DECLARATION_INITIALIZER_CALL;
    }
}
