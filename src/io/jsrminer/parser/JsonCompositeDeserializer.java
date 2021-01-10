package io.jsrminer.parser;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.rminer.core.api.IContainer;
import io.rminer.core.entities.Container;
import io.rminer.core.entities.DeclarationContainer;
import io.rminer.core.entities.SourceFile;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.util.StringUtils;

import java.util.*;

/**
 * A deserializer for the program which is in a composite json format
 */
public class JsonCompositeDeserializer {
    final String sourcePath;

    private HashMap<String, FunctionDeclaration> loadedFunctions = new HashMap<>();

    public JsonCompositeDeserializer(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public SourceFile parseSourceFile(final String containerJson) {
        Any any = JsonIterator.deserialize(containerJson);
        SourceFile sourceFile = new SourceFile(sourcePath);

        // Statements is wrapped as a Block Any
        BlockStatement blockBody = createBlockStatement(any, sourceFile);
        sourceFile.getStatements().addAll(blockBody.getStatements());

        return sourceFile;
    }

    private FunctionDeclaration getFunctionIfAlreadyLoaded(Any any, Container parentContainer) {
        String name = generateName(any, parentContainer);
        String qualifiedName = generateQualifiedName(name, parentContainer);
        return loadedFunctions.getOrDefault(qualifiedName, null);
    }

    public String generateQualifiedName(String name, Container parentContainer) {
        String namespace = null;

        if (parentContainer instanceof DeclarationContainer) {
            namespace = ((DeclarationContainer) parentContainer).getQualifiedName();
        }
        return namespace == null ? name : namespace + "." + name;
    }

    public String generateName(Any any, Container parentContainer) {
        String name = any.toString("name");

        // Anonymous
        if (StringUtils.isEmptyOrNull(name)) {
            name = parentContainer.getAnonymousFunctionDeclarations().size() + 1 + "";
        }
        return name;
    }

    public void loadFunctionDeclaration(Any any, FunctionDeclaration function, Container parentContainer) {
        // Location
        SourceLocation location = createSourceLocation(any.get("loc"));
        function.setSourceLocation(location);

        String name = generateName(any, parentContainer);
        String qualifiedName = generateQualifiedName(name, parentContainer);

        function.setName(name);
        function.setQualifiedName(qualifiedName);
        function.setFullyQualifiedName(qualifiedName);
        // Params
        List<Any> params = any.get("params").asList();
        for (int i = 0; i < params.size(); i++) {
            Any paramAny = params.get(i);
            UMLParameter parameter = parseUMLParameter(paramAny);
            parameter.setIndexPositionInParent(i);
            function.getParameters().add(parameter);
        }

        // Body Statements will be always 1 block
        Any body = any.get("statements").asList().get(0);

        // Use the new function as the parent container
        BlockStatement bodyBlock = createBlockStatement(body, function);
        function.setBody(new FunctionBody(bodyBlock));

        loadedFunctions.put(function.getQualifiedName(), function);
    }

    UMLParameter parseUMLParameter(Any parameterAny) {
        return new UMLParameter(parameterAny.toString());
    }

    public BlockStatement createBlockStatement(final String blockStatementJson, Container parentContainer) {
        Any any = JsonIterator.deserialize(blockStatementJson);
        return createBlockStatement(any, parentContainer);
    }

    public BlockStatement createBlockStatement(Any any, Container parentContainer) {
        // Helper variables
        BlockStatement currentBlock, childBlock;
        Statement child;
        boolean isComposite;
        int indexInParent;
        List<Any> statements;
        Map.Entry<BlockStatement, Any> currentEntry;

        final Queue<Map.Entry<BlockStatement, Any>> blocksToBeProcessed = new LinkedList<>();
        final BlockStatement newBlock = new BlockStatement();
        newBlock.setDepth(0);

        //Enqueue to process
        blocksToBeProcessed.add(new AbstractMap.SimpleImmutableEntry<>(newBlock, any));

        while (!blocksToBeProcessed.isEmpty()) {
            indexInParent = -1;

            // Extract the block and the corresponding json stored as any
            currentEntry = blocksToBeProcessed.remove();
            currentBlock = currentEntry.getKey();
            any = currentEntry.getValue();

            // Parse source location
            try {

                if (any.keys().contains("loc")) {
                    final SourceLocation location = createSourceLocation(any.get("loc"));
                    currentBlock.setSourceLocation(location);
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
            // Parse the nested statements
            statements = any.get("statements").asList();

            // Parse Type
            currentBlock.setCodeElementType(CodeElementType.getFromTitleCase(any.toString("type")));

            // Parse Expressions (Todo optimize
            if (any.keys().contains("expressions")) {
                for (Any expressionAny : any.get("expressions").asList()) {
                    Expression expression = createExpression(expressionAny, currentBlock, parentContainer);
                    currentBlock.addExpression(expression);
                }
            }

            // parse the nested functionDeclarations
            if (any.keys().contains("functionDeclarations")) {
                for (Any functionAny : any.get("functionDeclarations").asList()) {
                    FunctionDeclaration functionDeclaration = getFunctionIfAlreadyLoaded(any, parentContainer);
                    if (functionDeclaration == null)
                        functionDeclaration = new FunctionDeclaration();

                    loadFunctionDeclaration(functionAny, functionDeclaration, parentContainer);

                    if (parentContainer.getContainerType().equals(IContainer.ContainerType.File)) {
                        functionDeclaration.setIsTopLevel(true);
                    }
                    parentContainer.getFunctionDeclarations().add(functionDeclaration);
                }
            }

            // Parse text
            currentBlock.setText(any.toString("text"));

            // Check if it's try statement and contains any catchBlock
            if (any.keys().contains("catchClause")) {
                processCatchClause(currentBlock, blocksToBeProcessed, any);
            }

            // Parse childs of this block
            for (Any childAny : statements) {
                isComposite = childAny.keys().contains("statements");

                if (isComposite) {

                    // If composite enqueue the block and corresponding json to be processed later
                    boolean isTry = childAny.toString("type")
                            .equals(CodeElementType.TRY_STATEMENT.titleCase);

                    childBlock = isTry ? new TryStatement() : new BlockStatement();
                    blocksToBeProcessed.add(new AbstractMap.SimpleImmutableEntry<>(childBlock, childAny));
                    child = childBlock;
                } else {
                    // A leaf statement
                    child = createSingleStatement(childAny, currentBlock, parentContainer);
                }

                indexInParent = addChildToParentBlock(currentBlock, child, indexInParent);
            }
        }

        return newBlock;
    }

    private static int addChildToParentBlock(BlockStatement parentBlock, Statement child, int indexInParent) {
        child.setParent(parentBlock);
        child.setPositionIndexInParent(++indexInParent);
        child.setDepth(parentBlock.getDepth() + 1);
        parentBlock.addStatement(child);
        return indexInParent;
    }

    private static void processCatchClause(BlockStatement tryBlock, Queue<Map.Entry<BlockStatement, Any>> blocksToBeProcessed, Any any) {
        BlockStatement catchClause = new BlockStatement();
        blocksToBeProcessed.add(new AbstractMap.SimpleImmutableEntry<>(catchClause, any.get("catchClause")));

        // Add the catchblacue as seprate composite to the parent of the try block
        catchClause.setPositionIndexInParent(tryBlock.getPositionIndexInParent() + 1);
        catchClause.setDepth(tryBlock.getDepth());

        // Insert the catch after the try block in position of their parent's statements
        // TODO improve
        BlockStatement parent = tryBlock.getParent();
        catchClause.setParent(parent);
        parent.getStatements().add(catchClause.getPositionIndexInParent(), catchClause);

        // Shift position index of other child
        for (int i = catchClause.getPositionIndexInParent() + 1; i < parent.getStatements().size(); i++) {
            parent.getStatements().get(i).setPositionIndexInParent(i);
        }

        // Add the catchclause to the try block
        ((TryStatement) tryBlock).getCatchClauses().add(catchClause);
    }

    private void populateInvocationProperties(Any invocationAny, Invocation invocation, Container parentContainer) {
        invocation.setText(invocationAny.toString("text"));

        // Type
        String type = invocationAny.toString("type");
        invocation.setType(CodeElementType.getFromTitleCase(type));

        //Loc
        invocation.setSourceLocation(createSourceLocation(invocationAny.get("loc")));

        // Arguments
        List<Any> argumentAnys = invocationAny.get("arguments").asList();
        argumentAnys.forEach(argumentAny -> invocation.getArguments().add(argumentAny.toString()));
    }

    public ObjectCreation createObjectCreation(Any any, Container parentContainer) {
        ObjectCreation creation = new ObjectCreation();
        populateInvocationProperties(any, creation, parentContainer);
        creation.setFunctionName(any.toString("typeName"));

        if (any.keys().contains("expressionText")) {
            creation.setExpression(any.toString("expressionText"));
        }

//        any.keys().contains("isArray")
        return creation;
    }

    public OperationInvocation createOperationInvocation(Any any, Container parentContainer) {
        OperationInvocation operationInvocation = new OperationInvocation();
        populateInvocationProperties(any, operationInvocation, parentContainer);

        operationInvocation.setFunctionName(any.toString("functionName"));

        if (any.keys().contains("expressionText")) {
            operationInvocation.setExpression(any.toString("expressionText"));
        }

        return operationInvocation;
    }

    /**
     * @param any SingleStatement any node
     */
    public SingleStatement createSingleStatement(Any any, BlockStatement parent, Container parentContainer) {
        SingleStatement singleStatement = new SingleStatement();
        // Text
        singleStatement.setText(any.toString("text"));

        // Type
        String type = any.toString("type");
        singleStatement.setType(CodeElementType.getFromTitleCase(type));

        loadLeafData(any, singleStatement, parent, parentContainer);
        return singleStatement;
    }

    public Expression createExpression(Any any, BlockStatement ownerBlock, Container parentContainer) {
        Expression expression = new Expression();
        //Text
        expression.setText(any.toString("text"));
        loadLeafData(any, expression, ownerBlock, parentContainer);
        expression.setOwnerBlock(ownerBlock);
        return expression;
    }

    public void loadLeafData(Any any, CodeFragment leaf, BlockStatement parentOrOwner, Container parentContainer) {

        // Additional info if present
        // region
        // Identifiers
        if (any.keys().contains("identifiers")) {
            populateStringListFromAny(any.get("identifiers"), leaf.getVariables());
        }
        //endregion

        // region Variable declarations
        if (any.keys().contains("variableDeclarations")) {
            for (Any variableDeclarationAny : any.get("variableDeclarations").asList()) {
                VariableDeclaration declaration = createVariableDeclaration(variableDeclarationAny, parentOrOwner, parentContainer);
                leaf.getVariableDeclarations().add(declaration);
            }
        }
        // endregion

        // region Function Invocation
        if (any.keys().contains("functionInvocations")) {
            parseAndLoadFunctionInvocations(any.get("functionInvocations"), leaf.getMethodInvocationMap(), parentContainer);
        }
        // endregion

        // TODO check contents of invocationArguments (i.e. could it be variable?
        if (any.keys().contains("argumentsWithIdentifier")) {
            populateStringListFromAny(any.get("argumentsWithIdentifier"), leaf.getIdentifierArguments());
        }

        // region Object Creations
        if (any.keys().contains("objectCreations")) {
            parseAndLoadObjectCreations(any.get("objectCreations"), leaf.getCreationMap(), parentContainer);
        }
        // endregion

        // Info
        if (any.keys().contains("numericLiterals")) {
            populateStringListFromAny(any.get("numericLiterals"), leaf.getNumberLiterals());
        }
        if (any.keys().contains("infixOperators")) {
            populateStringListFromAny(any.get("infixOperators"), leaf.getInfixOperators());
        }
        if (any.keys().contains("postfixExpressions")) {
            populateStringListFromAny(any.get("postfixExpressions"), leaf.getPostfixExpressions());
        }
        if (any.keys().contains("prefixExpressions")) {
            populateStringListFromAny(any.get("prefixExpressions"), leaf.getPrefixExpressions());
        }
        //endregion

        // Anonymous FunctionDeclarations
        if (any.keys().contains("functionDeclarations")) {
            for (Any objAny : any.get("functionDeclarations").asList()) {
                AnonymousFunctionDeclaration anonymousFunctionDeclaration = loadAnonymousFunction(objAny, parentContainer);
                leaf.getAnonymousFunctionDeclarations().add(anonymousFunctionDeclaration);
            }
        }
        // AnonymousClass
//        if (any.keys().contains("objectExpressions")) {
//            for (Any objAny : any.get("objectExpressions").asList()) {
//                singleStatement.getAnonymousClassDeclarations()
//                        .add(loadAnonymousClass(objAny));
//            }
//        }
    }

    public AnonymousFunctionDeclaration loadAnonymousFunction(Any any, Container parentContainer) {
        AnonymousFunctionDeclaration anonymousFunctionDeclaration;
        FunctionDeclaration functionDeclaration = getFunctionIfAlreadyLoaded(any, parentContainer);
        if (functionDeclaration == null) {
            anonymousFunctionDeclaration = new AnonymousFunctionDeclaration();
            loadFunctionDeclaration(any, anonymousFunctionDeclaration, parentContainer);
            parentContainer.getAnonymousFunctionDeclarations().add(anonymousFunctionDeclaration);
        } else {
            anonymousFunctionDeclaration = (AnonymousFunctionDeclaration) functionDeclaration;
        }

        // Text
        anonymousFunctionDeclaration.setText(any.toString("text"));
        return anonymousFunctionDeclaration;
    }

//    public static IAnonymousClassDeclaration loadAnonymousClass(Any any) {
//        AnonymousClassDeclaration anonymousClassDeclaration = new AnonymousClassDeclaration();
//        return anonymousClassDeclaration;
//    }

    public void parseAndLoadFunctionInvocations(Any functionInvocationsAny, Map<String, List<OperationInvocation>> operationInvocationMap, Container parentContainer) {
        for (Any operationInvocationAny : functionInvocationsAny.asList()) {
            OperationInvocation operationInvocation = createOperationInvocation(operationInvocationAny, parentContainer);
            final String text = operationInvocationAny.toString("text");
            List<OperationInvocation> list = operationInvocationMap.get(text);
            if (list == null) {
                list = new ArrayList<>();
                operationInvocationMap.put(text, list);
            }
            list.add(operationInvocation);
        }
    }


    public void parseAndLoadObjectCreations(Any creationsAny, Map<String, List<ObjectCreation>> creationMap, Container parentContainer) {
        for (Any creationAny : creationsAny.asList()) {
            ObjectCreation creation = createObjectCreation(creationAny, parentContainer);
            final String text = creationAny.toString("text");
            List<ObjectCreation> list = creationMap.get(text);
            if (list == null) {
                list = new ArrayList<>();
                creationMap.put(text, list);
            }
            list.add(creation);
        }
    }

    public VariableDeclaration createVariableDeclaration(Any any, BlockStatement owner, Container parentContainer) {
        VariableDeclaration vd = new VariableDeclaration(any.toString("variableName"));
        //vd.setText(any.toString("text"));
        VariableDeclarationKind kind = VariableDeclarationKind.fromName(any.toString("kind"));
        vd.setKind(kind);

        if (any.keys().contains("initializer")) {
            Expression expression = createExpression(any.get("initializer"), owner, parentContainer);
            vd.setInitializer(expression);
        }
        return vd;
    }

    SourceLocation createSourceLocation(Any sourceLocationAny) {
        SourceLocation loc = sourceLocationAny.as(SourceLocation.class);
        loc.setFile(sourcePath);
        return loc;
    }

    void populateStringListFromAny(Any any, @NonNull List<String> listToBePopulated) {
        any.asList().forEach(item -> listToBePopulated.add(item.toString()));
    }
}
