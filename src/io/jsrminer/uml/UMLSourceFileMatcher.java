package io.jsrminer.uml;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.Statement;
import io.jsrminer.uml.mapping.FunctionUtil;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;

public abstract class UMLSourceFileMatcher {
    //public boolean match(IFunctionDeclaration removedClass, IFunctionDeclaration addedClass, String renamedFile);
    public abstract boolean match(ISourceFile removedFile, ISourceFile addedFile, String renamedFile);

    public static class Move extends UMLSourceFileMatcher {
        public boolean match(ISourceFile removedFile, ISourceFile addedFile, String renamedFile) {
            return removedFile.getName().equals(addedFile.getName())
                    && (hasSameOperationsAndStatements(removedFile, addedFile)
                    || removedFile.getDirectoryPath().equals(renamedFile)
                    || removedFile.getDirectoryPath().equals(addedFile.getDirectoryPath()));
        }
    }

    public boolean hasEqualTopLevelFunctionsCount(IContainer container1, IContainer container2) {
        return container1.getFunctionDeclarations().size() == container2.getFunctionDeclarations().size();
    }

    public boolean hasEqualStatementCount(IContainer container1, IContainer container2) {
        return container1.getStatements().size() == container2.getStatements().size();
    }

    public boolean hasSameOperationsAndStatements(IContainer container1, IContainer container2) {
        if (!hasEqualTopLevelFunctionsCount(container1, container2))
            return false;

        if (!hasEqualStatementCount(container1, container2))
            return false;

        for (var operation : container2.getFunctionDeclarations()) {
            if (!this.containsOperationWithTheSameSignatureIgnoringChangedTypes(container1, operation)) {
                return false;
            }
        }

//        for (var attribute : container1.getStatements()) {
//            if (!this.containsAttributeWithTheSameNameIgnoringChangedType(container2, attribute)) {
//                return false;
//            }
//        }
//        for (var attribute : container2.getStatements()) {
//            if (!this.containsAttributeWithTheSameNameIgnoringChangedType(container1, attribute)) {
//                return false;
//            }
//        }
        return true;
    }

    protected boolean containsAttributeWithTheSameNameIgnoringChangedType(IContainer container, Statement attribute) {
        return false;
    }

    boolean containsOperationWithTheSameSignatureIgnoringChangedTypes(IContainer container, IFunctionDeclaration testOperation) {
        for (var function : container.getFunctionDeclarations()) {
            if (function.getName().equals(testOperation.getName()))
                return true;
        }
        return false;
    }

//    public static class Move implements UMLSourceFileMatcher {
//        public boolean match(IFunctionDeclaration removedFunction, IFunctionDeclaration addedFunction, String renamedFile) {
//            return FunctionUtil.equalNameAndParameterCount(removedFunction, addedFunction) &&
//                    addedFunction.getSourceLocation().getFilePath().equals(renamedFile);
////
////            return removedFunction.hasSameNameAndKind(addedFunction)
////                    && (removedFunction.hasSameAttributesAndOperations(addedFunction)
////                    || addedFunction.getSourceFile().equals(renamedFile));
//        }
//    }

//    public static class RelaxedMove implements UMLClassMatcher {
//        public boolean match(UMLClass removedClass, UMLClass addedClass, String renamedFile) {
//            return removedClass.hasSameNameAndKind(addedClass)
//                    && (removedClass.hasCommonAttributesAndOperations(addedClass) || addedClass.getSourceFile().equals(renamedFile));
//        }
//    }
//
//    public static class ExtremelyRelaxedMove implements UMLClassMatcher {
//        public boolean match(UMLClass removedClass, UMLClass addedClass, String renamedFile) {
//            return removedClass.hasSameNameAndKind(addedClass)
//                    && (removedClass.hasAttributesAndOperationsWithCommonNames(addedClass) || addedClass.getSourceFile().equals(renamedFile));
//        }
//    }
//
//    public static class Rename implements UMLClassMatcher {
//        public boolean match(UMLClass removedClass, UMLClass addedClass, String renamedFile) {
//            return removedClass.hasSameKind(addedClass)
//                    && (removedClass.hasSameAttributesAndOperations(addedClass) || addedClass.getSourceFile().equals(renamedFile));
//        }
//    }
//
//    public static class RelaxedRename implements UMLClassMatcher {
//        public boolean match(UMLClass removedClass, UMLClass addedClass, String renamedFile) {
//            return removedClass.hasSameKind(addedClass)
//                    && (removedClass.hasCommonAttributesAndOperations(addedClass) || addedClass.getSourceFile().equals(renamedFile));
//        }
//    }
//
//    public static class ExtremelyRelaxedRename implements UMLClassMatcher {
//        public boolean match(UMLClass removedClass, UMLClass addedClass, String renamedFile) {
//            return removedClass.hasSameKind(addedClass)
//                    && (removedClass.hasAttributesAndOperationsWithCommonNames(addedClass) || addedClass.getSourceFile().equals(renamedFile));
//        }
//    }
}
