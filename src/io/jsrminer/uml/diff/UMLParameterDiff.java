package io.jsrminer.uml.diff;

import io.jsrminer.uml.UMLParameter;

public class UMLParameterDiff extends Diff {

    final boolean isNameChanged;
    final boolean defaultValueChanged;
    final UMLParameter parameter1;
    final UMLParameter parameter2;

    public UMLParameterDiff(UMLParameter parameter1, UMLParameter parameter2) {
        this.parameter1 = parameter1;
        this.parameter2 = parameter2;
        isNameChanged = !parameter1.name.equals(parameter2.name);
        defaultValueChanged = !parameter1.hasSameDefaultValue(parameter2);
        // TODO handle default value since it could expression and other function calls or a huge function expression
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("parameter ")
                .append(parameter1)
                .append(':')
                .append(System.lineSeparator());
        if (isNameChanged) {
            sb.append("name changed from ")
                    .append(parameter1.name)
                    .append(" to ")
                    .append(parameter2.name)
                    .append(System.lineSeparator());
        }

        return sb.toString();
    }
}
