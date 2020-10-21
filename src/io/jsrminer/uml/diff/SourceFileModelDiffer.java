package io.jsrminer.uml.diff;

import io.jsrminer.refactorings.ExtractOperationRefactoring;
import io.jsrminer.refactorings.IRefactoring;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.SourceFileModel;
import io.jsrminer.uml.mapping.FunctionBodyMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SourceFileModelDiffer {

    public final SourceFileModel source1;
    public final SourceFileModel source2;
    public final SourceFileModelDiff sourceDiff;
    public final UMLModelDiff modelDiff;

    //private final Map<String, FunctionBodyMapper> functionBodyMappers = new HashMap<>();
    protected List<IRefactoring> refactorings = new ArrayList<>();
    private List<FunctionBodyMapper> bodyMappers = new ArrayList<>();


    public SourceFileModelDiffer(final SourceFileModel source1, final SourceFileModel source2, final UMLModelDiff modelDiff) {
        this.source1 = source1;
        this.source2 = source2;
        sourceDiff = new SourceFileModelDiff(source1, source2);
        this.modelDiff = modelDiff;
    }

    public SourceFileModelDiff diff() {

        // Find functiondeclarations
        final FunctionDeclaration[] functions1 = source1.getFunctionDeclarations();
        final FunctionDeclaration[] functions2 = source2.getFunctionDeclarations();

        // Check if the common file has some fds
        if (functions2 != null) {
            // region Convert common file's fds to hashmap
            final HashMap<String, FunctionDeclaration> functionMap1 = new LinkedHashMap<>();
            for (FunctionDeclaration function1 : functions1) {
                functionMap1.put(function1.qualifiedName, function1);
            }

            final HashMap<String, FunctionDeclaration> functionMap2 = new LinkedHashMap<>();
            for (FunctionDeclaration function2 : functions2) {
                functionMap2.put(function2.qualifiedName, function2);
            }

            diffOperations(this.sourceDiff, functionMap1, functionMap2);
        }
        return this.sourceDiff;
    }

    /**
     * Diff operations between two common files
     */
    protected void diffOperations(SourceFileModelDiff sourceDiff, final HashMap<String, FunctionDeclaration> functionMap1, final HashMap<String, FunctionDeclaration> functionMap2) {
        // Process Annotations
        // Process Inheritance
        reportAddedAndRemovedOperations(sourceDiff, functionMap1, functionMap2);
        createBodyMappers(sourceDiff, functionMap1, functionMap2);
//        processAttributes();
//        checkForAttributeChanges();
//        processAnonymousClasses();
//        checkForOperationSignatureChanges();
//        checkForInlinedOperations();
        checkForExtractedOperations();
    }

    protected void createBodyMappers(SourceFileModelDiff sourceDiff
            , final HashMap<String, FunctionDeclaration> functionMap1
            , final HashMap<String, FunctionDeclaration> functionMap2) {

        // First map by fully qualified name? TODO revisit
        for (FunctionDeclaration function1 : functionMap1.values()) {
            final FunctionDeclaration function2 = functionMap2.get(function1.qualifiedName);
            // If function exists in both file, try to match their statements
            if (function2 != null) {

                UMLOperationDiff operationDiff = new UMLOperationDiff(function1, function2);

                FunctionBodyMapper mapper = new FunctionBodyMapper(operationDiff, sourceDiff);
                mapper.map();

                this.refactorings.addAll(operationDiff.getRefactorings());

                // save the mapper TODO
                this.bodyMappers.add(mapper);
            }
        }

        for (FunctionDeclaration function1 : functionMap1.values()) {
            // Not qualified but contains the function in the same index?
//            if (!this.functionBodyMappers.containsKey(function1.qualifiedName)
//                    && !sourceDiff.isRemovedOperation(function1.name)) {
//// TODO
//
//            }
        }
    }

    /**
     * Extract is detected by Checking if the already mapped operations contains any calls to
     * any addedOperations.
     */
    private void checkForExtractedOperations() {
        List<FunctionDeclaration> addedOperations = new ArrayList<>(sourceDiff.getAddedOperations().values());
        List<String> operationsToBeRemoved = new ArrayList<>();

        for (FunctionDeclaration addedOperation : addedOperations) {
            for (FunctionBodyMapper mapper : this.bodyMappers) {
                ExtractOperationDetection detection = new ExtractOperationDetection(mapper, addedOperations, sourceDiff, modelDiff);
                List<ExtractOperationRefactoring> refs = detection.check(addedOperation);
                for (ExtractOperationRefactoring refactoring : refs) {
                    refactorings.add(refactoring);
                    FunctionBodyMapper operationBodyMapper = refactoring.getBodyMapper();
                    processMapperRefactorings(operationBodyMapper, refactorings);
                    mapper.addChildMapper(operationBodyMapper);
                    operationsToBeRemoved.add(addedOperation);
                }
                checkForInconsistentVariableRenames(mapper);
            }
        }
//
//        for (Iterator<FunctionDeclaration> addedOperationIterator = addedOperations.iterator();
//             addedOperationIterator.hasNext(); ) {
//            FunctionDeclaration addedOperation = addedOperationIterator.next();
//
//            for (FunctionBodyMapper mapper : this.bodyMappers) {
//                ExtractOperationDetection detection = new ExtractOperationDetection(mapper, addedOperations, this, sourceDiff);
//                List<ExtractOperationRefactoring> refs = detection.check(addedOperation);
//                for (ExtractOperationRefactoring refactoring : refs) {
//                    refactorings.add(refactoring);
//                    UMLOperationBodyMapper operationBodyMapper = refactoring.getBodyMapper();
//                    processMapperRefactorings(operationBodyMapper, refactorings);
//                    mapper.addChildMapper(operationBodyMapper);
//                    operationsToBeRemoved.add(addedOperation);
//                }
//                checkForInconsistentVariableRenames(mapper);
//            }
//        }
        for (String key : operationsToBeRemoved) {
            sourceDiff.getAddedOperations().remove(key);
        }
    }

    // Adds the added and removed ops in the model diff
    private void reportAddedAndRemovedOperations(SourceFileModelDiff sourceDiff, final HashMap<String, FunctionDeclaration> functionMap1, final HashMap<String, FunctionDeclaration> functionMap2) {
        // region Find uncommon functions between the two files
        // For model1 uncommon / not matched functions are the functions that were removed
        // For model2 uncommon/ not matched functions are the functions that were added
        for (FunctionDeclaration fd1 : functionMap1.values()) {
            if (!functionMap2.containsKey(fd1.qualifiedName)) {
                sourceDiff.reportRemovedOperation(fd1);
            }
        }

        for (FunctionDeclaration fd2 : functionMap2.values()) {
            if (!functionMap1.containsKey(fd2.qualifiedName)) {
                sourceDiff.reportAddedOperation(fd2);
            }
        }
        // endregion
    }

}
