package io.jsrminer.uml.diff;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.SourceFileModel;
import io.jsrminer.uml.mapping.FunctionBodyMapper;
import io.jsrminer.uml.UMLModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UMLModelDiffer {

    private final List<FunctionBodyMapper> operationBodyMapperList = new ArrayList<>();

    public UMLModelDiff diff(final UMLModel model1, final UMLModel model2) {
        final UMLModelDiff modelDiff = new UMLModelDiff(model1, model2);

        // Diff on common files and functions
        for (Map.Entry<String, SourceFileModel> entry : model1.getSourceFileModels().entrySet()) {
            final String file = entry.getKey();

            // Find functiondeclarations in the common file
            final FunctionDeclaration[] functions1 = entry.getValue().getFunctionDeclarations();
            final FunctionDeclaration[] functions2 = model2.getFunctionDeclarationsInSourceFile(file);

            reportAddedAndRemovedOperationsInCommonFile(modelDiff, functions1, functions2);
        }

        createBodyMappers();
        return modelDiff;
    }

    // Adds the added and removed ops in the model diff
    private void reportAddedAndRemovedOperationsInCommonFile(UMLModelDiff modelDiff, FunctionDeclaration[] functions1, FunctionDeclaration[] functions2) {
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
            // endregion

            // region Find uncommon functions between the two files
            // For model1 uncommon / not matched functions are the functions that were removed
            // For model2 uncommon/ not matched functions are the functions that were added
            for (FunctionDeclaration fd1 : functions1) {
                if (!functionMap2.containsKey(fd1.getFullyQualifiedName())) {
                    modelDiff.reportRemovedOperation(fd1);
                }
            }

            for (FunctionDeclaration fd2 : functions2) {
                if (!functionMap1.containsKey(fd2.getFullyQualifiedName())) {
                    modelDiff.reportAddedOperation(fd2);
                }
            }
            // endregion
        }
    }

    protected void createBodyMappers() {

    }

    public void diffOperations(UMLModel model1, UMLModel model2) {
    }
}
