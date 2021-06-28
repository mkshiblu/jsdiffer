package io.jsrminer.uml;

import io.jsrminer.uml.diff.StringDistance;
import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.IFunctionDeclaration;

import java.util.ArrayList;
import java.util.List;

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
//        if(this.isInterface != umlClass.isInterface)
//            return false;
//        if(!equalTypeParameters(umlClass))
//            return false;
//        return true;
//
        return true;
    }

    public static boolean containsOperationWithTheSameSignatureIgnoringChangedTypes(IClassDeclaration classDeclaration, IFunctionDeclaration operation) {
        for (var originalOperation : classDeclaration.getFunctionDeclarations()) {
            if (FunctionUtil.equalSignatureIgnoringChangedTypes(originalOperation, operation)) {
                boolean originalOperationEmptyBody = originalOperation.getBody() == null
                        || originalOperation.hasEmptyBody();
                boolean operationEmptyBody = operation.getBody() == null || operation.hasEmptyBody();
                if (originalOperationEmptyBody == operationEmptyBody)
                    return true;
            }
        }
        return false;
    }

    public static boolean containsAttributeWithTheSameNameIgnoringChangedType(IClassDeclaration classDeclaration, UMLAttribute attribute) {
        for (UMLAttribute originalAttribute : classDeclaration.getAttributes()) {
            if (originalAttribute.equalsIgnoringChangedType(attribute))
                return true;
        }
        return false;
    }

    public static IFunctionDeclaration operationWithTheSameSignatureIgnoringChangedTypes(IClassDeclaration classDeclaration, IFunctionDeclaration operation) {
        List<IFunctionDeclaration> matchingOperations = new ArrayList<>();
        for (var originalOperation : classDeclaration.getFunctionDeclarations()) {
            boolean matchesOperation = //isInterface() ?
                    //originalOperation.equalSignatureIgnoringChangedTypes(operation) :
                    FunctionUtil.equalSignatureWithIdenticalNameIgnoringChangedTypes(originalOperation, operation);
            if (matchesOperation) {
                boolean originalOperationEmptyBody = originalOperation.getBody() == null || originalOperation.hasEmptyBody();
                boolean operationEmptyBody = operation.getBody() == null || operation.hasEmptyBody();
                if (originalOperationEmptyBody == operationEmptyBody)
                    matchingOperations.add(originalOperation);
            }
        }
        if (matchingOperations.size() == 1) {
            return matchingOperations.get(0);
        } else if (matchingOperations.size() > 1) {
            int minDistance = StringDistance.editDistance(matchingOperations.get(0).toString(), operation.toString());
            var matchingOperation = matchingOperations.get(0);
            for (int i = 1; i < matchingOperations.size(); i++) {
                int distance = StringDistance.editDistance(matchingOperations.get(i).toString(), operation.toString());
                if (distance < minDistance) {
                    minDistance = distance;
                    matchingOperation = matchingOperations.get(i);
                }
            }
            return matchingOperation;
        }
        return null;
    }

    public static boolean isInnerClass(IClassDeclaration parentClass, IClassDeclaration childClass) {
        var parentQualifiedNameWithFilename = parentClass.getSourceLocation().getFilePath()
                + "\\" + parentClass.getQualifiedName();
        var childPackageName = childClass.getSourceLocation().getFilePath()
                + "\\" + childClass.getParentContainerQualifiedName();
        return parentQualifiedNameWithFilename == childPackageName;
    }
}
