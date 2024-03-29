package io.jsrminer.uml.matchers;

import io.jsrminer.sourcetree.JsConfig;
import io.jsrminer.sourcetree.OperationInvocation;
import io.jsrminer.sourcetree.Statement;
import io.jsrminer.uml.FunctionUtil;
import io.jsrminer.uml.diff.RenamePattern;
import io.jsrminer.uml.mapping.replacement.PrefixSuffixUtils;
import io.rminerx.core.api.IAnonymousFunctionDeclaration;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.IFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class ContainerMatcher implements IContainerMatcher {

    public abstract boolean match(IContainer container1, IContainer container2);

    public static final ContainerMatcher SAME = new ContainerMatcher() {

        @Override
        public boolean match(IContainer container1, IContainer container2) {
            return hasSameOperationsAndStatements(container1, container2);
        }

        @Override
        public String toString() {
            return "ContainerMatcher.SAME";
        }
    };

    public static final ContainerMatcher COMMON = new ContainerMatcher() {

        @Override
        public boolean match(IContainer container1, IContainer container2) {
            return hasCommonAttributesAndOperations(container1, container2);
        }
        public String toString() {
            return "ContainerMatcher.COMMON";
        }
    };

    public static final ContainerMatcher COMMON_PARAMETERS = new ContainerMatcher() {

        @Override
        public boolean match(IContainer container1, IContainer container2) {
            return hasCommonParameters(container1, container2);
        }

        public String toString() {
            return "ContainerMatcher.COMMON_PARAMETERS";
        }
    };


    public ContainerMatcher() {

    }

    public boolean hasCommonParameters(IContainer container1, IContainer container2) {
        if (container1 instanceof IFunctionDeclaration && container2 instanceof IFunctionDeclaration) {
            return FunctionUtil.isCommonParameterNamesMoreThanUncommon((IAnonymousFunctionDeclaration) container1, (IAnonymousFunctionDeclaration) container2);
        }
        return false;
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

//        if (!bothContainSameTopLevelOperations(container1, container2)) {
//            return false;
//        }

        if (!bothContainsSameNestedFunctionDeclarations(container1, container2)) {
            return false;
        }

        if (!FunctionUtil.equalTopLevelAnonymousFunctionDeclarationCount(container1, container2)) {
            return false;
        }

        // Check child

//        for (var attribute : container1.getStatements()) {
//            if (!this.containsAttributeWithTheSameNameIgnoringChangedType(container2, attribute)) {
//                return false;
//            }
//        }
//        for (var attribute : container2.getStatements()) {
//            if (!this.containsAttributeWithTheSameNameIgnoringChangedType(container1, attribute)) {
//                return false;
//            }
//        }
        return true;
    }

    public boolean hasCommonAttributesAndOperations(IContainer container1, IContainer container2) {
        int checkingDepth = JsConfig.NESTED_FUNCTION_DEPTH_CHECK;
        String name1 = container1.getName();
        String name2 = container2.getName();
        String commonPrefix = PrefixSuffixUtils.longestCommonPrefix(name1, name2);
        String commonSuffix = PrefixSuffixUtils.longestCommonSuffix(name1, name2);
        RenamePattern pattern = null;

        if (!commonPrefix.isEmpty() && !commonSuffix.isEmpty()) {
            int beginIndexS1 = name1.indexOf(commonPrefix) + commonPrefix.length();
            int endIndexS1 = name1.lastIndexOf(commonSuffix);
            String diff1 = beginIndexS1 > endIndexS1 ? "" : name1.substring(beginIndexS1, endIndexS1);
            int beginIndexS2 = name2.indexOf(commonPrefix) + commonPrefix.length();
            int endIndexS2 = name2.lastIndexOf(commonSuffix);
            String diff2 = beginIndexS2 > endIndexS2 ? "" : name2.substring(beginIndexS2, endIndexS2);
            pattern = new RenamePattern(diff1, diff2);
        }
        var commonOperations = new LinkedHashSet<IFunctionDeclaration>();
        int totalOperations = 0;

        var functions1 = container1.getFunctionDeclarationsUpToDepth(checkingDepth);
        var functions2 = container2.getFunctionDeclarationsUpToDepth(checkingDepth);

        Function<List<IFunctionDeclaration>, Map<String, IFunctionDeclaration>> nameMapperFunction = (functions) -> {
            var functionNameMap = new LinkedHashMap<String, IFunctionDeclaration>(functions1.size());
            for (var function : functions) {
                if (!functionNameMap.containsKey(function.getName())) {
                    functionNameMap.put(function.getName(), function);
                }
            }
            return functionNameMap;
        };

        var functionNameMap1 = nameMapperFunction.apply(functions1);
        var functionNameMap2 = nameMapperFunction.apply(functions2);
        var unmatchedOperations = new LinkedHashSet<IFunctionDeclaration>();

        for (var operation : functions1) {
            if (!operation.isConstructor() /*&& !operation.overridesObject()*/) {
                totalOperations++;
                if (containsOperationWithTheSameSignatureIgnoringChangedTypesUpToDepth(functionNameMap2, operation) ||
                        (pattern != null && containsOperationWithTheSameRenamePattern(container2, operation, pattern.reverse()))) {
                    commonOperations.add(operation);
                }
            }
        }
        for (var operation : container2.getFunctionDeclarationsUpToDepth(checkingDepth)) {
            if (!operation.isConstructor()  /*&&!operation.overridesObject()*/) {
                totalOperations++;
                if (this.containsOperationWithTheSameSignatureIgnoringChangedTypesUpToDepth(functionNameMap1, operation) ||
                        (pattern != null && this.containsOperationWithTheSameRenamePattern(container1, operation, pattern))) {
                    commonOperations.add(operation);
                } else {
                    unmatchedOperations.add(operation);
                }
            }
        }

//        var commonAttributes = new LinkedHashSet<Statement>();
//        int totalAttributes = 0;
//        for (var attribute : container1.getStatements()) {
//            totalAttributes++;
//            if (containsAttributeWithTheSameNameIgnoringChangedType(container2, attribute) ||
//                    containsRenamedAttributeWithIdenticalTypeAndInitializer(container1, attribute) ||
//                    (pattern != null && container2.containsAttributeWithTheSameRenamePattern(attribute, pattern.reverse()))) {
//                commonAttributes.add(attribute);
//            }
//        }
//
//        for (UMLAttribute attribute : container2.attributes) {
//            totalAttributes++;
//            if (this.containsAttributeWithTheSameNameIgnoringChangedType(attribute) ||
//                    this.containsRenamedAttributeWithIdenticalTypeAndInitializer(attribute) ||
//                    (pattern != null && this.containsAttributeWithTheSameRenamePattern(attribute, pattern))) {
//                commonAttributes.add(attribute);
//            }
//        }

//        if (this.isTestClass() && umlClass.isTestClass()) {
//            return commonOperations.size() > Math.floor(totalOperations / 2.0) || commonOperations.containsAll(this.operations);
//        }
//
//        if (this.isSingleAbstractMethodInterface() && umlClass.isSingleAbstractMethodInterface()) {
//            return commonOperations.size() == totalOperations;
//        }
//
//        if ((commonOperations.size() > Math.floor(totalOperations / 2.0)
//                && (commonAttributes.size() > 2 || totalAttributes == 0)) ||
//                (commonOperations.size() > Math.floor(totalOperations / 3.0 * 2.0) && (commonAttributes.size() >= 2 || totalAttributes == 0)) ||
//                (commonAttributes.size() > Math.floor(totalAttributes / 2.0) && (commonOperations.size() > 2 || totalOperations == 0)) ||
//                (commonOperations.size() == totalOperations && commonOperations.size() > 2 && this.attributes.size() == umlClass.attributes.size()) ||
//                (commonOperations.size() == totalOperations && commonOperations.size() > 2 && totalAttributes == 1)) {
//            return true;
//        }

//        commonOperations.forEach(function -> {
//            if (functionNameMap2.containsKey(function.getName())) {
//                functionNameMap2.remove(function.getName());
//            }
//        });

        if ((commonOperations.size() > Math.floor(totalOperations / 2.0)
                && commonOperations.size() > unmatchedOperations.size())) {
            return true;
        }

        var unmatchedCalledOperations = new LinkedHashSet<IFunctionDeclaration>();
        for (var operation : functions2) {
            if (commonOperations.contains(operation)) {
                for (OperationInvocation invocation : operation.getBody().getAllOperationInvocations()) {
                    for (var unmatchedOperation : unmatchedOperations) {
                        if (invocation.matchesOperation(unmatchedOperation)) {
                            unmatchedCalledOperations.add(unmatchedOperation);
                            break;
                        }
                    }
                }
            }
        }
        if ((commonOperations.size() + unmatchedCalledOperations.size() > Math.floor(totalOperations / 2.0)
                //        && (commonAttributes.size() > 2 || totalAttributes == 0)
        )) {
            return true;
        }


        return false;
    }

    boolean containsOperationWithTheSameSignatureIgnoringChangedTypesUpToDepth(Map<String, IFunctionDeclaration> operationNameMap, IFunctionDeclaration testOperation) {
        return operationNameMap.containsKey(testOperation.getName())
                && FunctionUtil.equalParameterCount(operationNameMap.get(testOperation.getName()), testOperation);
    }

    protected boolean containsOperationWithTheSameRenamePattern(IContainer container, IFunctionDeclaration operation, RenamePattern pattern) {
        if (pattern == null)
            return false;
        for (var originalOperation : container.getFunctionDeclarationsUpToDepth(JsConfig.NESTED_FUNCTION_DEPTH_CHECK)) {
            String originalOperationName = originalOperation.getName();
            if (originalOperationName.contains(pattern.getBefore())) {
                String originalOperationNameAfterReplacement = originalOperationName.replace(pattern.getBefore(), pattern.getAfter());
                if (originalOperationNameAfterReplacement.equals(operation.getName()))
                    return true;
            }
        }
        return false;
    }

    boolean bothContainsSameNestedFunctionDeclarations(IContainer container1, IContainer container2) {
        var functionMap1 = container1.getFunctionDeclarationsQualifiedNameMapUpToDepth(JsConfig.NESTED_FUNCTION_DEPTH_CHECK);
        var functionMap2 = container2.getFunctionDeclarationsQualifiedNameMapUpToDepth(JsConfig.NESTED_FUNCTION_DEPTH_CHECK);

        for (var entry : functionMap1.entrySet()) {
            var function = functionMap2.get(entry.getKey());

            if (function == null || !FunctionUtil.equalParameterCount(entry.getValue(), function)) {
                return false;
            }
        }

        for (var entry : functionMap2.entrySet()) {
            var function = functionMap1.get(entry.getKey());

            if (function == null || !FunctionUtil.equalParameterCount(entry.getValue(), function)) {
                return false;
            }
        }

        return true;
    }

    protected boolean containsAttributeWithTheSameNameIgnoringChangedType(IContainer container, Statement attribute) {
        return false;
    }

    protected boolean containsRenamedAttributeWithIdenticalTypeAndInitializer(ISourceFile container1, Statement attribute) {
        return false;
    }
}
