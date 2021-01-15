package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.uml.diff.StringDistance;

public class Replacement {
    private String before;
    private String after;
    private ReplacementType type;

    public Replacement(String before, String after, ReplacementType type) {
        this.before = before;
        this.after = after;
        this.type = type;
    }

    public String getBefore() {
        return before;
    }

    public String getAfter() {
        return after;
    }

    public ReplacementType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((after == null) ? 0 : after.hashCode());
        result = prime * result + ((before == null) ? 0 : before.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof Replacement) {
            Replacement other = (Replacement) obj;
            return this.before.equals(other.before) && this.after.equals(other.after) && this.type.equals(other.type);
        }
        return false;
    }

    public String toString() {
        return before + " -> " + after;
    }

    public double normalizedEditDistance() {
        String s1 = getBefore();
        String s2 = getAfter();
        int distance = StringDistance.editDistance(s1, s2);
        double normalized = (double) distance / (double) Math.max(s1.length(), s2.length());
        return normalized;
    }

    public boolean involvesVariable() {
        return type.equals(ReplacementType.VARIABLE_NAME) ||
                type.equals(ReplacementType.BOOLEAN_REPLACED_WITH_VARIABLE) ||
                type.equals(ReplacementType.TYPE_LITERAL_REPLACED_WITH_VARIABLE) ||
                type.equals(ReplacementType.ARGUMENT_REPLACED_WITH_VARIABLE) ||
                type.equals(ReplacementType.VARIABLE_REPLACED_WITH_METHOD_INVOCATION) ||
                type.equals(ReplacementType.VARIABLE_REPLACED_WITH_EXPRESSION_OF_METHOD_INVOCATION) ||
                type.equals(ReplacementType.VARIABLE_REPLACED_WITH_ARRAY_ACCESS) ||
                type.equals(ReplacementType.VARIABLE_REPLACED_WITH_PREFIX_EXPRESSION) ||
                type.equals(ReplacementType.VARIABLE_REPLACED_WITH_STRING_LITERAL) ||
                type.equals(ReplacementType.VARIABLE_REPLACED_WITH_NULL_LITERAL) ||
                type.equals(ReplacementType.VARIABLE_REPLACED_WITH_NUMBER_LITERAL);
    }
}
