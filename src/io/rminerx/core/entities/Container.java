package io.rminerx.core.entities;

import io.jsrminer.sourcetree.SourceLocation;
import io.jsrminer.sourcetree.Statement;
import io.rminerx.core.api.IAnonymousFunctionDeclaration;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.IFunctionDeclaration;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Container implements IContainer {
    protected final ContainerType containerType;
    protected final List<Statement> statements = new ArrayList<>();
    protected final List<IFunctionDeclaration> functionDeclarations = new ArrayList<>();
    protected SourceLocation sourceLocation;
    private List<IAnonymousFunctionDeclaration> anonymousFunctionDeclarations = new ArrayList<>();
    private Map<Integer, List<IFunctionDeclaration>> nestedFunctionsDepthMap = new HashMap<>();
    private Map<Integer, Map<String, IFunctionDeclaration>> nestedFunctionsQualifiedNameDepthMap = new HashMap<>();

    /**
     * Qualified name excluding the filename but including the parent function name.
     * For example if function y() is declared inside x(), it will return x.y.
     */
    protected String qualifiedName;

    public Container(ContainerType containerType) {
        this.containerType = containerType;
    }

    @Override
    public ContainerType getContainerType() {
        return containerType;
    }

    @Override
    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public List<IFunctionDeclaration> getFunctionDeclarations() {
        return functionDeclarations;
    }

    public SourceLocation getSourceLocation() {
        return this.sourceLocation;
    }

    public List<IAnonymousFunctionDeclaration> getAnonymousFunctionDeclarations() {
        return anonymousFunctionDeclarations;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public void setSourceLocation(SourceLocation sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public abstract String getFullyQualifiedName();

    public List<IFunctionDeclaration> getFunctionDeclarationsUpToDepth(int depth) {
        if (!nestedFunctionsDepthMap.containsKey(depth)) {
            List<IFunctionDeclaration> functions = null;
            switch (depth) {
                case 1:
                    functions = new ArrayList<>(this.getFunctionDeclarations());
                    break;
                default:
                    functions = new ArrayList<>(getFunctionDeclarationsUpToDepth(depth - 1));
                    var children = new LinkedList<IFunctionDeclaration>();
                    for (var function : functions) {
                        children.addAll(function.getFunctionDeclarationsUpToDepth(depth - 1));
                    }
                    functions.addAll(children);

                    for (var ano : this.getAnonymousFunctionDeclarations()) {
                        functions.addAll(ano.getFunctionDeclarationsUpToDepth(depth - 1));
                    }
                    break;
            }

            nestedFunctionsDepthMap.put(depth, functions);
        }

        return nestedFunctionsDepthMap.get(depth);
    }

    @Override
    public Map<String, IFunctionDeclaration> getFunctionDeclarationsQualifiedNameMapUpToDepth(int depth) {
        if (!nestedFunctionsQualifiedNameDepthMap.containsKey(depth)) {
            var list = getFunctionDeclarationsUpToDepth(depth);
            LinkedHashMap<String, IFunctionDeclaration> qualifiedNameFunctionMap = list
                    .stream()
                    .collect(
                            LinkedHashMap::new,                           // Supplier
                            (map, item) -> map.put(item.getQualifiedName(), item),  // Accumulator
                            Map::putAll);                                 // Combiner

            nestedFunctionsQualifiedNameDepthMap.put(depth, qualifiedNameFunctionMap);
        }

        return nestedFunctionsQualifiedNameDepthMap.get(depth);
    }

//    @Override
//    public Map<String, IFunctionDeclaration> getFunctionDeclarationsQualifiedNameMapAtDepth(int depth) {
//        var map = nestedFunctionsQualifiedNameDepthMap.get(depth);
//
//        return
//    }
}
