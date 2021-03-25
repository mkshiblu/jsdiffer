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
}
