package io.jsrminer.parser;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import io.jsrminer.sourcetree.*;
import org.eclipse.jgit.annotations.NonNull;

import java.util.*;

public class JsonCompositeParser {

    public static BlockStatement createBlockStatement(final String blockStatementJson) {
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
        Any any = JsonIterator.deserialize(blockStatementJson);
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
                    final SourceLocation location = any.get("loc").as(SourceLocation.class);
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
                    Expression expression = createExpression(expressionAny, currentBlock);
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
                    child = JsonCompositeParser.createSingleStatement(childAny, currentBlock);
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

    private static void populateInvocationProperties(Any invocationAny, Invocation invocation) {
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

    public static ObjectCreation createObjectCreation(Any any) {
        ObjectCreation creation = new ObjectCreation();
        populateInvocationProperties(any, creation);
        creation.setFunctionName(any.toString("typeName"));

        if (any.keys().contains("expressionText")) {
            creation.setExpression(any.toString("expressionText"));
        }

//        any.keys().contains("isArray")
        return creation;
    }

    public static OperationInvocation createOperationInvocation(Any any) {
        OperationInvocation operationInvocation = new OperationInvocation();
        populateInvocationProperties(any, operationInvocation);

        operationInvocation.setFunctionName(any.toString("functionName"));

        if (any.keys().contains("expressionText")) {
            operationInvocation.setExpression(any.toString("expressionText"));
        }

        return operationInvocation;
    }

    /**
     * @param any SingleStatement any node
     */
    public static SingleStatement createSingleStatement(Any any, BlockStatement parent) {
        SingleStatement singleStatement = new SingleStatement();
        // Text
        singleStatement.setText(any.toString("text"));

        // Type
        String type = any.toString("type");
        singleStatement.setType(CodeElementType.getFromTitleCase(type));

        //Loc
        singleStatement.setSourceLocation(any.get("loc").as(SourceLocation.class));

        // Additional info if present
        // region
        // Identifiers
        if (any.keys().contains("identifiers")) {
            populateStringListFromAny(any.get("identifiers"), singleStatement.getVariables());
        }
        //endregion

        // region Variable declarations
        if (any.keys().contains("variableDeclarations")) {
            // TODO check if a list should be used?
            List<VariableDeclaration> vds = singleStatement.getVariableDeclarations();
            List<Any> vdAnys = any.get("variableDeclarations").asList();
            vdAnys.forEach((variableDeclarationAny -> {
                VariableDeclaration vd = createVariableDeclaration(variableDeclarationAny, parent);
                vds.add(vd);
            }));
        }
        // endregion

        // region Function Invocation
        if (any.keys().contains("functionInvocations")) {
            parseAndLoadFunctionInvocations(any.get("functionInvocations"), singleStatement.getMethodInvocationMap());
        }
        // endregion

        // region Object Creations
        if (any.keys().contains("objectCreations")) {
            parseAndLoadObjectCreations(any.get("objectCreations"), singleStatement.getCreationMap());
        }
        // endregion

        // TODO check contents of invocationArguments (i.e. could it be variable?
        if (any.keys().contains("argumentsWithIdentifier")) {
            populateStringListFromAny(any.get("argumentsWithIdentifier"), singleStatement.getIdentifierArguments());
        }

        return singleStatement;
    }

    public static void parseAndLoadFunctionInvocations(Any functionInvocationsAny, Map<String, List<OperationInvocation>> operationInvocationMap) {
        for (Any operationInvocationAny : functionInvocationsAny.asList()) {
            OperationInvocation operationInvocation = createOperationInvocation(operationInvocationAny);
            final String text = operationInvocationAny.toString("text");
            List<OperationInvocation> list = operationInvocationMap.get(text);
            if (list == null) {
                list = new ArrayList<>();
                operationInvocationMap.put(text, list);
            }
            list.add(operationInvocation);
        }
    }


    public static void parseAndLoadObjectCreations(Any creationsAny, Map<String, List<ObjectCreation>> creationMap) {
        for (Any creationAny : creationsAny.asList()) {
            ObjectCreation creation = createObjectCreation(creationAny);
            final String text = creationAny.toString("text");
            List<ObjectCreation> list = creationMap.get(text);
            if (list == null) {
                list = new ArrayList<>();
                creationMap.put(text, list);
            }
            list.add(creation);
        }
    }

    public static VariableDeclaration createVariableDeclaration(Any any, BlockStatement owner) {
        VariableDeclaration vd = new VariableDeclaration(any.toString("variableName"));
        //vd.setText(any.toString("text"));
        VariableDeclarationKind kind = VariableDeclarationKind.fromName(any.toString("kind"));
        vd.setKind(kind);

        if (any.keys().contains("initializer")) {
            Expression expression = createExpression(any.get("initializer"), owner);
            vd.setInitializer(expression);
        }
        return vd;
    }

    public static Expression createExpression(Any any, BlockStatement ownerBlock) {
        Expression expression = new Expression();
        //Text
        expression.setText(any.toString("text"));

        // Info
        populateStringListFromAny(any.get("identifiers"), expression.getVariables());
        populateStringListFromAny(any.get("numericLiterals"), expression.getNumberLiterals());
        populateStringListFromAny(any.get("infixOperators"), expression.getInfixOperators());
        populateStringListFromAny(any.get("postfixOperators"), expression.getPostfixExpressions());
        populateStringListFromAny(any.get("prefixOperators"), expression.getPrefixExpressions());

        final List<Any> anys = any.get("variableDeclarations").asList();

        // Vds
        for (Any variableDeclarationAny : anys) {
            VariableDeclaration declaration = createVariableDeclaration(variableDeclarationAny, ownerBlock);
            expression.getVariableDeclarations().add(declaration);
        }

        // Additional info if present
        //endregion
        parseAndLoadFunctionInvocations(any.get("functionInvocations"), expression.getMethodInvocationMap());
        parseAndLoadObjectCreations(any.get("objectCreations"), expression.getCreationMap());

        expression.setOwnerBlock(ownerBlock);
        return expression;
    }

    static SourceLocation createSourceLocation(Any sourceLocationAny) {
        return sourceLocationAny.as(SourceLocation.class);
    }

    static void populateStringListFromAny(Any any, @NonNull List<String> listToBePopulated) {
        any.asList().forEach(item -> listToBePopulated.add(item.toString()));
    }
}
