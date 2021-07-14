package io.jsrminer.uml;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.diff.RenamePattern;
import io.jsrminer.uml.mapping.replacement.PrefixSuffixUtils;
import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;

import java.util.*;
import java.util.function.Function;

public abstract class UMLClassMatcher {
    public abstract boolean match(IClassDeclaration removedClass, IClassDeclaration addedClass, String renamedFile);

    public static class Move extends UMLClassMatcher {
        public boolean match(IClassDeclaration removedClass, IClassDeclaration addedClass, String renamedFile) {

            return (addedClass.getSourceLocation().getFilePath().equals(renamedFile)
                    || hasSameAttributesAndOperations(removedClass, addedClass))
                    && hasSameNameAndKind(removedClass, addedClass);
        }
    }

    public static class Rename extends UMLClassMatcher {
        public boolean match(IClassDeclaration removedClass, IClassDeclaration addedClass, String renamedFile) {
            return ClassUtil.hasSameKind(removedClass, addedClass)
                    && (hasSameAttributesAndOperations(removedClass, addedClass)
                    || addedClass.getSourceLocation().getFilePath().equals(renamedFile));
        }
    }

    public static class RelaxedMove extends UMLClassMatcher {
        public boolean match(IClassDeclaration removedClass, IClassDeclaration addedClass, String renamedFile) {
            return hasSameNameAndKind(removedClass, addedClass)
                    && (hasCommonAttributesAndOperations(removedClass, addedClass)
                    || addedClass.getSourceLocation().getFilePath().equals(renamedFile));
        }
    }


    public static class RelaxedRename extends UMLClassMatcher {
        public boolean match(IClassDeclaration removedClass, IClassDeclaration addedClass, String renamedFile) {
            return ClassUtil.hasSameKind(removedClass, addedClass)
                    && (hasCommonAttributesAndOperations(removedClass, addedClass)
                    || addedClass.getSourceLocation().getFilePath().equals(renamedFile));
        }
    }

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
            if (!ClassUtil.containsOperationWithTheSameSignatureIgnoringChangedTypes(class1, operation)) {
                return false;
            }
        }
        for (var operation : class2.getFunctionDeclarations()) {
            if (!ClassUtil.containsOperationWithTheSameSignatureIgnoringChangedTypes(class2, operation)) {
                return false;
            }
        }
        for (UMLAttribute attribute : class1.getAttributes()) {
            if (!ClassUtil.containsAttributeWithTheSameNameIgnoringChangedType(class2, attribute)) {
                return false;
            }
        }

        for (UMLAttribute attribute : class2.getAttributes()) {
            if (!ClassUtil.containsAttributeWithTheSameNameIgnoringChangedType(class1, attribute)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasCommonAttributesAndOperations(IClassDeclaration class1, IClassDeclaration class2) {
        String commonPrefix = PrefixSuffixUtils.longestCommonPrefix(class1.getName(), class2.getName());
        String commonSuffix = PrefixSuffixUtils.longestCommonSuffix(class1.getName(), class2.getName());
        RenamePattern pattern = null;
        if (!commonPrefix.isEmpty() && !commonSuffix.isEmpty()) {
            int beginIndexS1 = class1.getName().indexOf(commonPrefix) + commonPrefix.length();
            int endIndexS1 = class1.getName().lastIndexOf(commonSuffix);
            String diff1 = beginIndexS1 > endIndexS1 ? "" : class1.getName().substring(beginIndexS1, endIndexS1);
            int beginIndexS2 = class2.getName().indexOf(commonPrefix) + commonPrefix.length();
            int endIndexS2 = class2.getName().lastIndexOf(commonSuffix);
            String diff2 = beginIndexS2 > endIndexS2 ? "" : class2.getName().substring(beginIndexS2, endIndexS2);
            pattern = new RenamePattern(diff1, diff2);
        }
        Set<IFunctionDeclaration> commonOperations = new LinkedHashSet<>();
        int totalOperations = 0;
        for (var operation : class1.getFunctionDeclarations()) {
            if (!operation.isConstructor() /*&& !operation.overridesObject()*/) {
                totalOperations++;
                if (ClassUtil.containsOperationWithTheSameSignatureIgnoringChangedTypes(class2, operation)
                        || (pattern != null && containsOperationWithTheSameRenamePattern(class2, operation, pattern.reverse()))) {
                    commonOperations.add(operation);
                }
            }
        }
        for (var operation : class2.getFunctionDeclarations()) {
            if (!operation.isConstructor() /*&& !operation.overridesObject()*/) {
                totalOperations++;
                if (ClassUtil.containsOperationWithTheSameSignatureIgnoringChangedTypes(class1, operation) ||
                        (pattern != null && containsOperationWithTheSameRenamePattern(class1, operation, pattern))) {
                    commonOperations.add(operation);
                }
            }
        }
        Set<UMLAttribute> commonAttributes = new LinkedHashSet<>();
        int totalAttributes = 0;
        for (UMLAttribute attribute : class1.getAttributes()) {
            totalAttributes++;
            if (ClassUtil.containsAttributeWithTheSameNameIgnoringChangedType(class2, attribute) ||
                    containsRenamedAttributeWithIdenticalTypeAndInitializer(class2, attribute) ||
                    (pattern != null && ClassUtil.containsAttributeWithTheSameRenamePattern(class2, attribute, pattern.reverse()))) {
                commonAttributes.add(attribute);
            }
        }

        for (UMLAttribute attribute : class2.getAttributes()) {
            totalAttributes++;
            if (ClassUtil.containsAttributeWithTheSameNameIgnoringChangedType(class1, attribute) ||
                    containsRenamedAttributeWithIdenticalTypeAndInitializer(class1, attribute) ||
                    (pattern != null && ClassUtil.containsAttributeWithTheSameRenamePattern(class1, attribute, pattern))) {
                commonAttributes.add(attribute);
            }
        }

//        if (this.isTestClass() && umlClass.isTestClass()) {
//            return commonOperations.size() > Math.floor(totalOperations / 2.0) || commonOperations.containsAll(this.operations);
//        }
//
//        if (this.isSingleAbstractMethodInterface() && umlClass.isSingleAbstractMethodInterface()) {
//            return commonOperations.size() == totalOperations;
//        }

        if ((commonOperations.size() > Math.floor(totalOperations / 2.0) && (commonAttributes.size() > 2 || totalAttributes == 0)) ||
                (commonOperations.size() > Math.floor(totalOperations / 3.0 * 2.0) && (commonAttributes.size() >= 2 || totalAttributes == 0)) ||
                (commonAttributes.size() > Math.floor(totalAttributes / 2.0) && (commonOperations.size() > 2 || totalOperations == 0)) ||
                (commonOperations.size() == totalOperations && commonOperations.size() > 2 && class1.getAttributes().size() == class2.getAttributes().size()) ||
                (commonOperations.size() == totalOperations && commonOperations.size() > 2 && totalAttributes == 1)) {
            return true;
        }

        Set<IFunctionDeclaration> unmatchedOperations = new LinkedHashSet<>(class2.getFunctionDeclarations());
        unmatchedOperations.removeAll(commonOperations);
        Set<IFunctionDeclaration> unmatchedCalledOperations = new LinkedHashSet<>();
        for (IFunctionDeclaration operation : class2.getFunctionDeclarations()) {
            if (commonOperations.contains(operation)) {
                for (OperationInvocation invocation : operation.getBody().getAllOperationInvocations()) {
                    for (var unmatchedOperation : unmatchedOperations) {
                        if (invocation.matchesOperation(unmatchedOperation)) {
                            unmatchedCalledOperations.add(unmatchedOperation);
                            break;
                        }
                    }
                }
            }
        }
        if ((commonOperations.size() + unmatchedCalledOperations.size() > Math.floor(totalOperations / 2.0) && (commonAttributes.size() > 2 || totalAttributes == 0))) {
            return true;
        }
        return false;
    }

    protected boolean containsOperationWithTheSameRenamePattern(IClassDeclaration classDeclaration, IFunctionDeclaration operation, RenamePattern pattern) {
        if (pattern == null)
            return false;
        for (var originalOperation : classDeclaration.getFunctionDeclarations()) {
            String originalOperationName = originalOperation.getName();
            if (originalOperationName.contains(pattern.getBefore())) {
                String originalOperationNameAfterReplacement = originalOperationName.replace(pattern.getBefore(), pattern.getAfter());
                if (originalOperationNameAfterReplacement.equals(operation.getName()))
                    return true;
            }
        }
        return false;
    }

    public boolean containsRenamedAttributeWithIdenticalTypeAndInitializer(IClassDeclaration classDeclaration, UMLAttribute attribute) {
        for (UMLAttribute originalAttribute : classDeclaration.getAttributes()) {
            if (attributeRenamedWithIdenticalTypeAndInitializer(originalAttribute, attribute))
                return true;
        }
        return false;
    }

    public boolean attributeRenamedWithIdenticalTypeAndInitializer(UMLAttribute originalAttribute, UMLAttribute testAttribute) {
        Expression thisInitializer = originalAttribute.getVariableDeclaration().getInitializer();
        Expression otherInitializer = testAttribute.getVariableDeclaration().getInitializer();
        if (thisInitializer != null && otherInitializer != null
                && !originalAttribute.getName().equals(testAttribute.getName())) {

            return thisInitializer.getText().equals(otherInitializer.getText())
                    /**&& originalAttribute..type.equals(testAttribute.type) && this.type.equalsQualified(attribute.type)**/
                    ;
        }
        return false;
    }
}
