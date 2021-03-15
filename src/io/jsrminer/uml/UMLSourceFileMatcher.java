package io.jsrminer.uml;

import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ISourceFile;

public abstract class UMLSourceFileMatcher {
    //public boolean match(IFunctionDeclaration removedClass, IFunctionDeclaration addedClass, String renamedFile);
    public abstract boolean match(ISourceFile removedFile, ISourceFile addedFile, String renamedFile);

    public static class Move extends UMLSourceFileMatcher {
        public boolean match(ISourceFile removedFile, ISourceFile addedFile, String renamedFileHint) {
            return removedFile.getName().equals(addedFile.getName())
                    && (addedFile.getFilepath().equals(renamedFileHint)
                    || hasSameOperationsAndStatements(removedFile, addedFile));
        }
    }

    public static class RelaxedMove extends UMLSourceFileMatcher {
        public boolean match(ISourceFile removedFile, ISourceFile addedFile, String renamedFileHint) {
            return removedFile.getName().equals(addedFile.getName())
                    && (addedFile.getFilepath().equals(renamedFileHint)
                    || hasCommonAttributesAndOperations(removedFile, addedFile));
        }
    }

    public static class Rename extends UMLSourceFileMatcher {
        public boolean match(ISourceFile removedFile, ISourceFile addedFile, String renamedFileHint) {
            return removedFile.getDirectoryPath().equals(addedFile.getDirectoryPath())
                    && (addedFile.getFilepath().equals(renamedFileHint)
                    || hasSameOperationsAndStatements(removedFile, addedFile));
        }
    }

    public static class RelaxedRename extends UMLSourceFileMatcher {
        public boolean match(ISourceFile removedFile, ISourceFile addedFile, String renamedFileHint) {
            return //removedClass.hasSameKind(addedClass) &&
                    (addedFile.getFilepath().equals(renamedFileHint)
                            || hasCommonAttributesAndOperations(removedFile, addedFile));
        }
    }

    public boolean hasSameOperationsAndStatements(IContainer container1, IContainer container2) {
        return ContainerMatcher.SAME.match(container1, container2);
    }

    public boolean hasCommonAttributesAndOperations(IContainer container1, IContainer container2) {
        return ContainerMatcher.COMMON.match(container1, container2);
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
