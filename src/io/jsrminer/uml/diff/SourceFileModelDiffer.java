package io.jsrminer.uml.diff;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.SourceFileModel;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.UMLModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SourceFileModelDiffer {

    public final SourceFileModel source1;
    public final SourceFileModel source2;

    private final List<FunctionBodyMapper> operationBodyMapperList = new ArrayList<>();

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
                functionMap1.put(function1.getFullyQualifiedName(), function1);
            }

            final HashMap<String, FunctionDeclaration> functionMap2 = new HashMap<>();
            for (FunctionDeclaration function2 : functions2) {
                functionMap2.put(function2.getFullyQualifiedName(), function2);
            }

            reportAddedAndRemovedOperations(sourceDiff, functionMap1, functionMap2);
            reportAddedAndRemovedClass();

        }
        return sourceDiff;
    }


    private void reportAddedAndRemovedClass() {
        // TODO
    }

    // Adds the added and removed ops in the model diff
    private void reportAddedAndRemovedOperations(SourceFileModelDiff sourceDiff, final HashMap<String, FunctionDeclaration> functionMap1, final HashMap<String, FunctionDeclaration> functionMap2) {

        // region Find uncommon functions between the two files
        // For model1 uncommon / not matched functions are the functions that were removed
        // For model2 uncommon/ not matched functions are the functions that were added
        for (FunctionDeclaration fd1 : functionMap1.values()) {
            if (!functionMap2.containsKey(fd1.getFullyQualifiedName())) {
                sourceDiff.reportRemovedOperation(fd1);
            }
        }

        for (FunctionDeclaration fd2 : functionMap2.values()) {
            if (!functionMap1.containsKey(fd2.getFullyQualifiedName())) {
                sourceDiff.reportAddedOperation(fd2);
            }
        }
        // endregion
    }

    protected void createBodyMappers(FunctionDeclaration[] functions1, HashMap<String, FunctionDeclaration> functionMap2) {

        // TOdo reviesit
        for (FunctionDeclaration function1 : functions1) {
            final FunctionDeclaration function2 = functionMap2.get(function1.getFullyQualifiedName());
            // If function exists in both file
            if (function2 != null) {
                FunctionBodyMapper mapper = new FunctionBodyMapper(function1, function2);
                mapper.mapStatements();
            }
        }
    }
}
