package io.jsrminer.sourcetree;

import io.jsrminer.api.RefactoringMinerTimedOutException;
import io.jsrminer.io.FileUtil;
import io.jsrminer.uml.ClassUtil;
import io.jsrminer.uml.FunctionUtil;
import io.jsrminer.uml.UMLType;
import io.jsrminer.uml.diff.StringDistance;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.IFunctionDeclaration;

import java.util.ArrayList;
import java.util.List;

public class UMLClassBaseDiff {
    protected IClassDeclaration originalClass;
    protected IClassDeclaration nextClass;
    private boolean superclassChanged;
    private UMLType oldSuperclass;
    private UMLType newSuperclass;
    protected List<IFunctionDeclaration> addedOperations = new ArrayList<>();
    protected List<IFunctionDeclaration> removedOperations = new ArrayList<>();
    protected List<FunctionBodyMapper> operationBodyMapperList = new ArrayList<>();

    public UMLClassBaseDiff(IClassDeclaration originalClass, IClassDeclaration nextClass) {
        this.originalClass = originalClass;
        this.nextClass = nextClass;
    }

    public void process() throws RefactoringMinerTimedOutException {
        // processModifiers();
        // processAnnotations();
        //processEnumConstants();
        processInheritance();
        processOperations();
//        createBodyMappers();
//        processAnonymousClasses();
//        checkForOperationSignatureChanges();
//        processAttributes();
//        checkForAttributeChanges();
//        checkForInlinedOperations();
//        checkForExtractedOperations();
    }

    private void processInheritance() {
        if (originalClass.getSuperClass() != null && nextClass.getSuperClass() != null) {
            if (!originalClass.getSuperClass().equals(nextClass.getSuperClass())) {
                setSuperclassChanged(true);
            }
            setOldSuperclass(originalClass.getSuperClass());
            setNewSuperclass(nextClass.getSuperClass());
        } else if (originalClass.getSuperClass() != null && nextClass.getSuperClass() == null) {
            setSuperclassChanged(true);
            setOldSuperclass(originalClass.getSuperClass());
            setNewSuperclass(nextClass.getSuperClass());
        } else if (originalClass.getSuperClass() == null && nextClass.getSuperClass() != null) {
            setSuperclassChanged(true);
            setOldSuperclass(originalClass.getSuperClass());
            setNewSuperclass(nextClass.getSuperClass());
        }
//        for (UMLType implementedInterface : originalClass.getImplementedInterfaces()) {
//            if (!nextClass.getImplementedInterfaces().contains(implementedInterface))
//                reportRemovedImplementedInterface(implementedInterface);
//        }
//        for (UMLType implementedInterface : nextClass.getImplementedInterfaces()) {
//            if (!originalClass.getImplementedInterfaces().contains(implementedInterface))
//                reportAddedImplementedInterface(implementedInterface);
//        }
    }

    protected void processOperations() throws RefactoringMinerTimedOutException {
        for (var operation : originalClass.getFunctionDeclarations()) {
            var operationWithTheSameSignature = ClassUtil.operationWithTheSameSignatureIgnoringChangedTypes(nextClass, operation);
            if (operationWithTheSameSignature == null) {
                this.removedOperations.add(operation);
            } else if (!mapperListContainsOperation(operation, operationWithTheSameSignature)) {
                var mapper = new FunctionBodyMapper(operation, operationWithTheSameSignature, this);
                this.operationBodyMapperList.add(mapper);
            }
        }
        for (var operation : nextClass.getFunctionDeclarations()) {
            var operationWithTheSameSignature = ClassUtil.operationWithTheSameSignatureIgnoringChangedTypes(originalClass, operation);
            if (operationWithTheSameSignature == null) {
                this.addedOperations.add(operation);
            } else if (!mapperListContainsOperation(operationWithTheSameSignature, operation)) {
                var mapper = new FunctionBodyMapper(operationWithTheSameSignature, operation, this);
                this.operationBodyMapperList.add(mapper);
            }
        }
    }


    public double normalizedSourceFolderDistance() {
        String s1 = FileUtil.getFolder(originalClass.getSourceLocation().getFilePath().toLowerCase());
        String s2 = FileUtil.getFolder(nextClass.getSourceLocation().getFilePath().toLowerCase());
        int distance = StringDistance.editDistance(s1, s2);
        double normalized = (double) distance / (double) Math.max(s1.length(), s2.length());
        return normalized;
    }

    private boolean mapperListContainsOperation(IFunctionDeclaration operation1, IFunctionDeclaration operation2) {
        for (var mapper : operationBodyMapperList) {
            if (FunctionUtil.equalQualifiedName(mapper.getOperation1(), operation1)
                    || FunctionUtil.equalQualifiedName(mapper.getOperation2(), operation2))
                return true;
        }
        return false;
    }

    //region Setters & getters
    public IClassDeclaration getNextClass() {
        return this.nextClass;
    }

    public IClassDeclaration getOriginalClass() {
        return originalClass;
    }

    private void setSuperclassChanged(boolean superclassChanged) {
        this.superclassChanged = superclassChanged;
    }

    public boolean isSuperclassChanged() {
        return superclassChanged;
    }

    private void setOldSuperclass(UMLType oldSuperclass) {
        this.oldSuperclass = oldSuperclass;
    }

    private void setNewSuperclass(UMLType newSuperclass) {
        this.newSuperclass = newSuperclass;
    }

    public UMLType getOldSuperclass() {
        return oldSuperclass;
    }

    public UMLType getNewSuperclass() {
        return newSuperclass;
    }

    //endregion
}
