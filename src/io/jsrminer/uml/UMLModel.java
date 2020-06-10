package io.jsrminer.uml;

import io.jsrminer.sourcetree.FunctionDeclaration;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstracts the source code
 */
public class UMLModel {

    private HashMap<String, FunctionDeclaration[]> functionDeclarations;

    public UMLModelDiff diff(UMLModel umlModel) {

        UMLModelDiff diff = new UMLModelDiff();
        for (Map.Entry<String, FunctionDeclaration[]> entry : functionDeclarations.entrySet()) {

            final String file = entry.getKey();
            final FunctionDeclaration[] fds1 = entry.getValue();

            FunctionDeclaration[] fds2 = umlModel.functionDeclarations.get(file);

            if (fds2 != null) {

                // Convert to hashmap
                HashMap<String, FunctionDeclaration> fdMap1 = new HashMap<>();
                for (FunctionDeclaration fd : fds1) {
                    fdMap1.put(fd.getFullyQualifiedName(), fd);
                }

                HashMap<String, FunctionDeclaration> fdMap2 = new HashMap<>();
                for (FunctionDeclaration fd : fds2) {
                    fdMap2.put(fd.getFullyQualifiedName(), fd);
                }


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

                for (FunctionDeclaration fd1 : uncommon1.values()) {

                    for (FunctionDeclaration fd2 : uncommon2.values())
                        if (fd1.hasIdenticalBody(fd2) &&
                                ((fd1.namespace != null && fd2.namespace != null && fd1.namespace.equals(fd2.namespace)))
                                || fd1.namespace == fd2.namespace) {
                            // fd1 has renamved to fd2
                            diff.addRefactoring(fd1.getFullyQualifiedName() + " renamed to " + fd2.getFullyQualifiedName());
                        }
                }
            }
        }

        return diff;
    }

    public void setFunctionDeclarations(final HashMap<String, FunctionDeclaration[]> functionDeclarations) {
        this.functionDeclarations = functionDeclarations;
    }

    public HashMap<String, FunctionDeclaration[]> getFunctionDeclarations() {
        return functionDeclarations;
    }
}
