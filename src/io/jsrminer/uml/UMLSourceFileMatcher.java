package io.jsrminer.uml;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.uml.mapping.FunctionUtil;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;

public abstract class UMLSourceFileMatcher {
    //public boolean match(IFunctionDeclaration removedClass, IFunctionDeclaration addedClass, String renamedFile);
    abstract boolean match(ISourceFile removedFile, ISourceFile addedFile, String renamedFile);

    public static class Move extends UMLSourceFileMatcher {
        public boolean match(ISourceFile removedFile, ISourceFile addedFile, String renamedFile) {
            return removedFile.getName().equals(addedFile.getName()) &&
                    hasEqualTopLevelFunctionsCount(removedFile, addedFile)
                    && (file2ContainsAllFunctionOfFile1(removedFile, addedFile)
                    || removedFile.getDirectoryPath().equals(renamedFile)
                    || removedFile.getDirectoryPath().equals(addedFile.getDirectoryPath())));
        }

        public boolean file2ContainsAllFunctionOfFile1(ISourceFile sourceFile1, ISourceFile sourceFile2) {
//        if(this.attributes.size() != other.attributes.size())
//            return false;
//        if (this.functionDeclarations.size() != other.functionDeclarations.size())
//            return false;


//        Set<IFunctionDeclaration>  otherFunctions = new LinkedHashSet<>(other.functionDeclarations);
            for (IFunctionDeclaration function1 : sourceFile1.getFunctionDeclarations()) {

//            if(!other.containsOperationWithTheSameSignatureIgnoringChangedTypes(operation)) {
//                return false;
//            }

                boolean contains = false;
                for (IFunctionDeclaration function2 : sourceFile2.getFunctionDeclarations()) {
                    if (FunctionUtil.nameEqualsIgnoreCaseAndEqualParameterCount(function1, function2)) {
                        contains = true;
                        break;
                    }
                }

                if (!contains)
                    return false;
            }

//        for(UMLOperation operation : other.operations) {
//            if(!this.containsOperationWithTheSameSignatureIgnoringChangedTypes(operation)) {
//                return false;
//            }
//        }
//        for(UMLAttribute attribute : attributes) {
//            if(!other.containsAttributeWithTheSameNameIgnoringChangedType(attribute)) {
//                return false;
//            }
//        }
//        for(UMLAttribute attribute : other.attributes) {
//            if(!this.containsAttributeWithTheSameNameIgnoringChangedType(attribute)) {
//                return false;
//            }
//        }
            return true;
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

    boolean containsOperationWithTheSameSignatureIgnoringChangedTypes(IContainer container, IFunctionDeclaration operation) {
        
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
