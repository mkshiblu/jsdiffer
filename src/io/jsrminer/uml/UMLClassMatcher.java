package io.jsrminer.uml;

import io.jsrminer.sourcetree.ClassDeclaration;
import io.jsrminer.sourcetree.JsConfig;
import io.jsrminer.sourcetree.OperationInvocation;
import io.jsrminer.sourcetree.Statement;
import io.jsrminer.uml.diff.RenamePattern;
import io.jsrminer.uml.mapping.replacement.PrefixSuffixUtils;
import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class UMLClassMatcher {
    public abstract boolean match(ClassDeclaration removedClass, ClassDeclaration addedClass, String renamedFile);

    public static class Move extends UMLClassMatcher {
        public boolean match(ClassDeclaration removedClass, ClassDeclaration addedClass, String renamedFile) {

            return (addedClass.getSourceLocation().getFilePath().equals(renamedFile)
                    || hasSameAttributesAndOperations(removedClass, addedClass))
                    && hasSameNameAndKind(removedClass, addedClass);
        }
    }

//    public static class RelaxedMove extends UMLClassMatcher {
//        public boolean match(ISourceFile removedFile, ISourceFile addedFile, String renamedFileHint) {
//            return removedFile.getName().equals(addedFile.getName())
//                    && (addedFile.getFilepath().equals(renamedFileHint)
//                    || hasCommonAttributesAndOperations(removedFile, addedFile));
//        }
//    }
//
//    public static class Rename extends UMLClassMatcher {
//        public boolean match(ISourceFile removedFile, ISourceFile addedFile, String renamedFileHint) {
//            return removedFile.getDirectoryPath().equals(addedFile.getDirectoryPath())
//                    && (addedFile.getFilepath().equals(renamedFileHint)
//                    || hasSameOperationsAndStatements(removedFile, addedFile));
//        }
//    }
//
//    public static class RelaxedRename extends UMLClassMatcher {
//        public boolean match(ISourceFile removedFile, ISourceFile addedFile, String renamedFileHint) {
//            return //removedClass.hasSameKind(addedClass) &&
//                    (addedFile.getFilepath().equals(renamedFileHint)
//                            || hasCommonAttributesAndOperations(removedFile, addedFile));
//        }
//    }

    public boolean hasSameNameAndKind(IClassDeclaration class1, IClassDeclaration class2) {
        return ClassUtil.qualifiedNameEquals(class1, class2)
                && ClassUtil.hasSameKind(class1, class2);
    }

    public boolean hasSameKind(IClassDeclaration class1, IClassDeclaration class2) {
//        if (class1.isInterface != class2.isInterface)
//            return false;
//        if (!equalTypeParameters(umlClass))
//            return false;
        return true;
    }

    public boolean hasSameAttributesAndOperations(IClassDeclaration class1, IClassDeclaration class2) {
        if (class1.getAttributes().size() != class2.getAttributes().size())
            return false;
        if (class1.getFunctionDeclarations().size() != class2.getFunctionDeclarations().size())
            return false;

        for (var operation : class1.getFunctionDeclarations()) {
            if (!class1.containsOperationWithTheSameSignatureIgnoringChangedTypes(operation)) {
                return false;
            }
        }
        for (var operation : class2.getFunctionDeclarations()) {
            if (!class2.containsOperationWithTheSameSignatureIgnoringChangedTypes(operation)) {
                return false;
            }
        }
        for (UMLAttribute attribute : attributes) {
            if (!umlClass.containsAttributeWithTheSameNameIgnoringChangedType(attribute)) {
                return false;
            }
        }
        for (UMLAttribute attribute : umlClass.attributes) {
            if (!this.containsAttributeWithTheSameNameIgnoringChangedType(attribute)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasCommonAttributesAndOperations(IContainer container1, IContainer container2) {
        return ContainerMatcher.COMMON.match(container1, container2);
    }
}
