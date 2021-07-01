package io.jsrminer.uml.diff;

import io.jsrminer.uml.ClassUtil;
import io.rminerx.core.api.IClassDeclaration;

public class UMLClassMoveDiff extends UMLClassBaseDiff {

    UMLClassMoveDiff(IClassDeclaration originalClass, IClassDeclaration movedClass) {
        super(originalClass, movedClass);
    }

    public IClassDeclaration getMovedClass() {
        return this.nextClass;
    }

    //return true if "classMoveDiff" represents the move of a class that is inner to this.originalClass
    public boolean isInnerClassMove(UMLClassMoveDiff classDiff) {
        return ClassUtil.isInnerClass(originalClass, classDiff.originalClass)
                && ClassUtil.isInnerClass(nextClass, nextClass);
    }
}
