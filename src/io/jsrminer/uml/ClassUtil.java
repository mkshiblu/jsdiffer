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
}
