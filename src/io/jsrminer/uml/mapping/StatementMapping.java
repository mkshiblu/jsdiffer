package io.jsrminer.uml.mapping;

import io.jsrminer.sourcetree.Statement;

public abstract class StatementMapping {
    public final Statement statement1;
    public final Statement statement2;

    public StatementMapping(Statement statement1, Statement statement2) {
        this.statement1 = statement1;
        this.statement2 = statement2;
    }

    public abstract boolean isExactMatch();
}
