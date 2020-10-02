package io.jsrminer.uml.diff;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.SourceFileModel;
import io.jsrminer.uml.mapping.FunctionBodyMapper;

import java.util.HashMap;
import java.util.Map;

public class SourceFileModelDiffer {

    public final SourceFileModel source1;
    public final SourceFileModel source2;

    private final Map<String, FunctionBodyMapper> functionBodyMappers = new HashMap<>();

    public SourceFileModelDiffer(final SourceFileModel source1, final SourceFileModel source2) {
        this.source1 = source1;
        this.source2 = source2;
    }

    public SourceFileModelDiff diff() {
        final SourceFileModelDiff sourceDiff = new SourceFileModelDiff(source1, source2);

        // Find functiondeclarations
        final FunctionDeclaration[] functions1 = source1.getFunctionDeclarations();
        final FunctionDeclaration[] functions2 = source2.getFunctionDeclarations();

        // Check if the common file has some fds
        if (functions2 != null) {
            // region Convert common file's fds to hashmap
            final HashMap<String, FunctionDeclaration> functionMap1 = new HashMap<>();
            for (FunctionDeclaration function1 : functions1) {
                functionMap1.put(function1.qualifiedName, function1);
            }

            final HashMap<String, FunctionDeclaration> functionMap2 = new HashMap<>();
            for (FunctionDeclaration function2 : functions2) {
                functionMap2.put(function2.qualifiedName, function2);
            }

            diffOperations(sourceDiff, functionMap1, functionMap2);
        }
        return sourceDiff;
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
//        checkForExtractedOperations();
    }

    protected void createBodyMappers(SourceFileModelDiff sourceDiff, final HashMap<String, FunctionDeclaration> functionMap1, final HashMap<String, FunctionDeclaration> functionMap2) {

        // First map by fully qualified name? TODO revisit
        for (FunctionDeclaration function1 : functionMap1.values()) {
            final FunctionDeclaration function2 = functionMap2.get(function1.qualifiedName);
            // If function exists in both file
            if (function2 != null) {
                FunctionBodyMapper mapper = new FunctionBodyMapper(function1, function2);
                mapper.map();
            }
        }

        for (FunctionDeclaration function1 : functionMap1.values()) {
            // Not qualified but contains the function in the same index?
            if (!this.functionBodyMappers.containsKey(function1.qualifiedName)
                    && !sourceDiff.isRemovedOperation(function1.name)) {
// TODO

            }
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
