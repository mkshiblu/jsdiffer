package io.jsrminer.parser;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLParameter;
import io.rminer.core.entities.SourceFile;
import org.eclipse.jgit.annotations.NonNull;

import java.util.*;

/**
 * A deserializer for the program which is in a composite json format
 */
public class JsonCompositeDeserializer {

    public static SourceFile parseSourceFile(final String containerJson, final String filePath) {
        Any any = JsonIterator.deserialize(containerJson);
        SourceFile sourceFile = new SourceFile(filePath);

        // Statements is wrapped as a Block Any
        BlockStatement blockBody = createBlockStatement(any, filePath);
        sourceFile.getStatements().addAll(blockBody.getStatements());

        // Functions
        List<Any> functionDeclarationAnys = any.get("functionDeclarations").asList();
        for (Any functionDeclarationAny : functionDeclarationAnys) {
            FunctionDeclaration functionDeclaration = new FunctionDeclaration();
            loadFunctionDeclaration(functionDeclarationAny, functionDeclaration, filePath);
            functionDeclaration.setIsTopLevel(true);
            sourceFile.getFunctionDeclarations().add(functionDeclaration);
        }
        return sourceFile;
    }

    public static String generateQualifiedName(Any any) {
        return any.toString("qualifiedName");
    }

    public static void loadFunctionDeclaration(Any any, FunctionDeclaration function, String filePath) {
        //Names
        String qualifiedName = generateQualifiedName(any);
        String name = any.toString("name");

        function.setName(name);
        function.setQualifiedName(qualifiedName);

        // Location
        SourceLocation location = createSourceLocation(any.get("loc"), filePath);
        function.setSourceLocation(location);

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
        BlockStatement bodyBlock = createBlockStatement(body, filePath);
        function.setBody(new FunctionBody(bodyBlock));

        // Todo save Functions from the body to this function?
    }

    static UMLParameter parseUMLParameter(Any parameterAny) {
        return new UMLParameter(parameterAny.toString());
    }

    public static BlockStatement createBlockStatement(final String blockStatementJson, String filePath) {
        Any any = JsonIterator.deserialize(blockStatementJson);
        return createBlockStatement(any, filePath);
    }

    public static BlockStatement createBlockStatement(Any any, String filePath) {
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
                    final SourceLocation location = createSourceLocation(any.get("loc"), filePath);
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
                    Expression expression = createExpression(expressionAny, currentBlock, filePath);
                    currentBlock.addExpression(expression);
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
                    child = createSingleStatement(childAny, currentBlock, filePath);
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

    private static void populateInvocationProperties(Any invocationAny, Invocation invocation, String filePath) {
        invocation.setText(invocationAny.toString("text"));

        // Type
        String type = invocationAny.toString("type");
        invocation.setType(CodeElementType.getFromTitleCase(type));

        //Loc
        invocation.setSourceLocation(createSourceLocation(invocationAny.get("loc"), filePath));

        // Arguments
        List<Any> argumentAnys = invocationAny.get("arguments").asList();
        argumentAnys.forEach(argumentAny -> invocation.getArguments().add(argumentAny.toString()));
    }

    public static ObjectCreation createObjectCreation(Any any, String filePath) {
        ObjectCreation creation = new ObjectCreation();
        populateInvocationProperties(any, creation, filePath);
        creation.setFunctionName(any.toString("typeName"));

        if (any.keys().contains("expressionText")) {
            creation.setExpression(any.toString("expressionText"));
        }

//        any.keys().contains("isArray")
        return creation;
    }

    public static OperationInvocation createOperationInvocation(Any any, String filePath) {
        OperationInvocation operationInvocation = new OperationInvocation();
        populateInvocationProperties(any, operationInvocation, filePath);

        operationInvocation.setFunctionName(any.toString("functionName"));

        if (any.keys().contains("expressionText")) {
            operationInvocation.setExpression(any.toString("expressionText"));
        }

        return operationInvocation;
    }

    /**
     * @param any SingleStatement any node
     */
    public static SingleStatement createSingleStatement(Any any, BlockStatement parent, String filePath) {
        SingleStatement singleStatement = new SingleStatement();
        // Text
        singleStatement.setText(any.toString("text"));

        // Type
        String type = any.toString("type");
        singleStatement.setType(CodeElementType.getFromTitleCase(type));

        loadLeafData(any, singleStatement, parent, filePath);
        return singleStatement;
    }

    public static Expression createExpression(Any any, BlockStatement ownerBlock, String filePath) {
        Expression expression = new Expression();
        //Text
        expression.setText(any.toString("text"));
        loadLeafData(any, expression, ownerBlock, filePath);
        expression.setOwnerBlock(ownerBlock);
        return expression;
    }

    public static void loadLeafData(Any any, CodeFragment leaf, BlockStatement parentOrOwner, String filePath) {

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
                VariableDeclaration declaration = createVariableDeclaration(variableDeclarationAny, parentOrOwner, filePath);
                leaf.getVariableDeclarations().add(declaration);
            }
        }
        // endregion

        // region Function Invocation
        if (any.keys().contains("functionInvocations")) {
            parseAndLoadFunctionInvocations(any.get("functionInvocations"), leaf.getMethodInvocationMap(), filePath);
        }
        // endregion

        // TODO check contents of invocationArguments (i.e. could it be variable?
        if (any.keys().contains("argumentsWithIdentifier")) {
            populateStringListFromAny(any.get("argumentsWithIdentifier"), leaf.getIdentifierArguments());
        }

        // region Object Creations
        if (any.keys().contains("objectCreations")) {
            parseAndLoadObjectCreations(any.get("objectCreations"), leaf.getCreationMap(), filePath);
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
                AnonymousFunctionDeclaration anonymousFunctionDeclaration = loadAnonymousFunction(objAny, filePath);
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

    public static AnonymousFunctionDeclaration loadAnonymousFunction(Any any, String filePath) {
        AnonymousFunctionDeclaration anonymousFunctionDeclaration = new AnonymousFunctionDeclaration();
        loadFunctionDeclaration(any, anonymousFunctionDeclaration, filePath);

        return anonymousFunctionDeclaration;
    }

//    public static IAnonymousClassDeclaration loadAnonymousClass(Any any) {
//        AnonymousClassDeclaration anonymousClassDeclaration = new AnonymousClassDeclaration();
//        return anonymousClassDeclaration;
//    }

    public static void parseAndLoadFunctionInvocations(Any functionInvocationsAny, Map<String, List<OperationInvocation>> operationInvocationMap, String filePath) {
        for (Any operationInvocationAny : functionInvocationsAny.asList()) {
            OperationInvocation operationInvocation = createOperationInvocation(operationInvocationAny, filePath);
            final String text = operationInvocationAny.toString("text");
            List<OperationInvocation> list = operationInvocationMap.get(text);
            if (list == null) {
                list = new ArrayList<>();
                operationInvocationMap.put(text, list);
            }
            list.add(operationInvocation);
        }
    }


    public static void parseAndLoadObjectCreations(Any creationsAny, Map<String, List<ObjectCreation>> creationMap, String filePath) {
        for (Any creationAny : creationsAny.asList()) {
            ObjectCreation creation = createObjectCreation(creationAny, filePath);
            final String text = creationAny.toString("text");
            List<ObjectCreation> list = creationMap.get(text);
            if (list == null) {
                list = new ArrayList<>();
                creationMap.put(text, list);
            }
            list.add(creation);
        }
    }

    public static VariableDeclaration createVariableDeclaration(Any any, BlockStatement owner, String filePath) {
        VariableDeclaration vd = new VariableDeclaration(any.toString("variableName"));
        //vd.setText(any.toString("text"));
        VariableDeclarationKind kind = VariableDeclarationKind.fromName(any.toString("kind"));
        vd.setKind(kind);

        if (any.keys().contains("initializer")) {
            Expression expression = createExpression(any.get("initializer"), owner, filePath);
            vd.setInitializer(expression);
        }
        return vd;
    }

    static SourceLocation createSourceLocation(Any sourceLocationAny, String filePath) {
        SourceLocation loc = sourceLocationAny.as(SourceLocation.class);
        loc.setFile(filePath);
        return loc;
    }

    static void populateStringListFromAny(Any any, @NonNull List<String> listToBePopulated) {
        any.asList().forEach(item -> listToBePopulated.add(item.toString()));
    }
}
