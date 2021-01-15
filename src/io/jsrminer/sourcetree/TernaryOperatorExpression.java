package io.jsrminer.sourcetree;

import io.jsrminer.uml.mapping.replacement.Replacement;
import io.jsrminer.uml.mapping.replacement.ReplacementType;
import io.rminer.core.api.ITernaryOperatorExpression;

public class TernaryOperatorExpression implements ITernaryOperatorExpression {
    private Expression condition;
    private Expression thenExpression;
    private Expression elseExpression;
    private String text;

    public TernaryOperatorExpression(String text, Expression condition, Expression thenExpression, Expression elseExpression) {
        this.text = text;
        this.condition = condition;
        this.thenExpression = thenExpression;
        this.elseExpression = elseExpression;
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getThenExpression() {
        return thenExpression;
    }

    public Expression getElseExpression() {
        return elseExpression;
    }

    public String getText() {
        return text;
    }

    public Replacement makeReplacementWithTernaryOnTheRight(String statement) {
        if (getElseExpression().getText().equals(statement)) {
            return new Replacement(statement, getText(), ReplacementType.EXPRESSION_REPLACED_WITH_TERNARY_ELSE);
        }
        if (getThenExpression().getText().equals(statement)) {
            return new Replacement(statement, getText(), ReplacementType.EXPRESSION_REPLACED_WITH_TERNARY_THEN);
        }
        return null;
    }

    public Replacement makeReplacementWithTernaryOnTheLeft(String statement) {
        if (getElseExpression().getText().equals(statement)) {
            return new Replacement(getText(), statement, ReplacementType.EXPRESSION_REPLACED_WITH_TERNARY_ELSE);
        }
        if (getThenExpression().getText().equals(statement)) {
            return new Replacement(getText(), statement, ReplacementType.EXPRESSION_REPLACED_WITH_TERNARY_THEN);
        }
        return null;
    }
}
