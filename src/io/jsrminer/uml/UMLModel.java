package io.jsrminer.uml;

import io.jsrminer.api.Diffable;
import io.jsrminer.uml.diff.UMLModelDiff;
import io.jsrminer.sourcetree.FunctionDeclaration;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstracts the source code
 */
public class UMLModel implements Diffable<UMLModel, UMLModelDiff> {

    private HashMap<String, FunctionDeclaration[]> fileFunctionDeclarations;

    @Override
    public UMLModelDiff diff(UMLModel umlModel) {

        UMLModelDiff diff = new UMLModelDiff();
        for (Map.Entry<String, FunctionDeclaration[]> entry : fileFunctionDeclarations.entrySet()) {
            final String file = entry.getKey();
            final FunctionDeclaration[] fds1 = entry.getValue();

            FunctionDeclaration[] fds2 = umlModel.fileFunctionDeclarations.get(file);

            // Check if the common file has some fds
            if (fds2 != null) {

                // Convert common file's fds to hashmap
                HashMap<String, FunctionDeclaration> fdMap1 = new HashMap<>();
                for (FunctionDeclaration fd : fds1) {
                    fdMap1.put(fd.getFullyQualifiedName(), fd);
                }

                HashMap<String, FunctionDeclaration> fdMap2 = new HashMap<>();
                for (FunctionDeclaration fd : fds2) {
                    fdMap2.put(fd.getFullyQualifiedName(), fd);
                }

                // region Find uncommon functions between the two files
                HashMap<String, FunctionDeclaration> uncommon1 = new HashMap<>();
                HashMap<String, FunctionDeclaration> uncommon2 = new HashMap<>();
                for (FunctionDeclaration fd1 : fds1) {
                    if (!fdMap2.containsKey(fd1.getFullyQualifiedName())) {
                        uncommon1.put(fd1.getFullyQualifiedName(), fd1);
                    }
                }

                for (FunctionDeclaration fd2 : fds2) {
                    if (!fdMap1.containsKey(fd2.getFullyQualifiedName())) {
                        uncommon2.put(fd2.getFullyQualifiedName(), fd2);
                    }
                }
                // endregion


                // region match body of the common named functions
                for (FunctionDeclaration fd1 : uncommon1.values()) {
                    for (FunctionDeclaration fd2 : uncommon2.values()) {
                        if (fd1.hasIdenticalBody(fd2) &&
                                ((fd1.namespace != null && fd1.namespace.equals(fd2.namespace))
                                        || fd1.namespace == fd2.namespace)) {
                            // fd1 has renamved to fd2
                            // diff.addRefactoring(fd1.getFullyQualifiedName() + " renamed to " + fd2.getFullyQualifiedName());
                        }
                    }
                }
                // endregion
            }
        }

        return diff;
    }

    public void setFileFunctionDeclarations(final HashMap<String, FunctionDeclaration[]> fileFunctionDeclarations) {
        this.fileFunctionDeclarations = fileFunctionDeclarations;
    }
}
