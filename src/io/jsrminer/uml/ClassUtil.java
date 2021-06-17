package io.jsrminer.uml;

import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.IFunctionDeclaration;

public class ClassUtil {
    public static boolean isEqual(IClassDeclaration class1
            , IClassDeclaration class2) {
        return class1.getParentContainerQualifiedName().equals(class2.getParentContainerQualifiedName())
                && nameEquals(class1, class2);

        /**
         * return this.packageName.equals(umlClass.packageName)
         *     				&& this.name.equals(umlClass.name)
         *     				&& this.sourceFile.equals(umlClass.sourceFile);
         */
    }

    public static boolean nameEquals(IClassDeclaration class1
            , IClassDeclaration class2) {
        return class1.getName().equals(class1.getName());
    }

    public static boolean qualifiedNameEquals(IClassDeclaration class1
            , IClassDeclaration class2) {
        return class1.getQualifiedName().equals(class1.getQualifiedName());
    }

    public static boolean containsAttributeWithName(IClassDeclaration classDeclaration, UMLAttribute attribute) {
        return classDeclaration.getAttributes()
                .stream()
                .anyMatch(field -> field.getName().equals(attribute.getName()));
    }

    public static UMLAttribute findAttributeWithName(IClassDeclaration classDeclaration, UMLAttribute attribute) {
        return classDeclaration.getAttributes()
                .stream()
                .filter(field -> field.getName().equals(attribute.getName()))
                .findFirst()
                .orElse(null);
    }

    public static UMLAttribute containsAttribute(IClassDeclaration classDeclaration, UMLAttribute attribute) {
        return findAttributeWithName(classDeclaration, attribute);
    }

    public static boolean hasSameKind(IClassDeclaration class1, IClassDeclaration class2) {
    }
}
