package io.jsrminer.util;

public class AstUtils {
    public static String normalizeAttribute(String attributeDescription) {
        int idx = attributeDescription.indexOf(':');
        if (idx == -1) {
            return attributeDescription.trim();
        } else {
            int start = attributeDescription.indexOf(' ');
            if (start == -1) {
                return attributeDescription.substring(0, idx).trim();
            } else {
                return attributeDescription.substring(start, idx).trim();
            }
        }
    }

    public static String normalizeMethodSignature(String methodSignature) {
        StringBuilder sb = new StringBuilder();
        int start = 0;
        int openPar = methodSignature.indexOf('(');
        int closePar = methodSignature.lastIndexOf(')');
        if (openPar == -1 || closePar == -1) {
            throw new IllegalArgumentException("Invalid method signature: " + methodSignature);
        }
        int lastSpace = methodSignature.lastIndexOf(' ', openPar);
        if (lastSpace != -1) {
            start = lastSpace + 1;
        }
        sb.append(methodSignature, start, openPar);
        sb.append('(');

        String[] parameters;
        String parametersStr = stripTypeArguments(methodSignature.substring(openPar + 1, closePar));
        if (parametersStr.length() > 0) {
            parameters = parametersStr.split(" *, *");
        } else {
            parameters = new String[0];
        }
        for (int i = 0; i < parameters.length; i++) {
            String parameter = parameters[i];
            int space = parameter.lastIndexOf(' ');
            if (space == -1) {
                sb.append(parameter);
            } else {
                sb.append(parameter.substring(space + 1));
            }
            if (i < parameters.length - 1) {
                sb.append(',');
            }
        }
        sb.append(')');
        return sb.toString();
    }

    public static String stripTypeArguments(String entity) {
        StringBuilder sb = new StringBuilder();
        int openGenerics = 0;
        for (int i = 0; i < entity.length(); i++) {
            char c = entity.charAt(i);
            if (c == '<') {
                openGenerics++;
            }
            if (openGenerics == 0) {
                sb.append(c);
            }
            if (c == '>') {
                openGenerics--;
            }
        }
        return sb.toString();
    }

    public static String stripQualifiedTypeName(String qualifiedTypeName) {
        int dotPos = qualifiedTypeName.lastIndexOf('.');
        if (dotPos >= 0) {
            return qualifiedTypeName.substring(dotPos + 1);
        }
        return qualifiedTypeName;
    }
}
