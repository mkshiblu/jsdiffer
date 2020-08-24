package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import java.util.*;

/**
 * A block statement, i.e., a sequence of statements surrounded by braces {}.
 * May contain other block statements or statements (i.e. composite statements)
 */
public class BlockStatement extends Statement {
    protected List<Statement> statements;
    protected List<String> expressions = new ArrayList<>();
    // exp
    // vd

    public BlockStatement() {
    }

    public void addStatement(Statement statement) {
        this.statements.add(statement);
    }

    void addExpression(String expression) {
        this.expressions.add(expression);
    }

    public static BlockStatement fromJson(final String blockStatementJson) {
        // Helper variables
        BlockStatement currentBlock, childBlock;
        Statement child;
        boolean isComposite;
        int indexInParent;
        List<Any> statements;
        Map.Entry<BlockStatement, Any> currentEntry;

        final Queue<Map.Entry<BlockStatement, Any>> blocksToBeProcessed = new LinkedList<>();
        final BlockStatement newBlock = new BlockStatement();
        newBlock.depth = 0;

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
            final SourceLocation location = any.get("loc").as(SourceLocation.class);
            currentBlock.setSourceLocation(location);

            // Parse the nested statements
            statements = any.get("statements").asList();
            currentBlock.statements = new ArrayList<>(statements.size());

            // Parse Type
            String type = any.toString("type");
            currentBlock.type = CodeElementType.getFromTitleCase(type);

            // Parse Expression (Todo optimize
            if (any.keys().contains("expressions")) {
                for (Any exp : any.get("expressions").asList()) {
                    currentBlock.addExpression(exp.toString());
                }
            }

            for (Any childAny : statements) {
                isComposite = childAny.keys().contains("statements");

                if (isComposite) {

                    // If composite enqueue the block and corresponding json to be processed later
                    childBlock = new BlockStatement();
                    blocksToBeProcessed.add(new AbstractMap.SimpleImmutableEntry<>(childBlock, childAny));
                    child = childBlock;
                } else {
                    // A leaf statement
                    child = SingleStatement.fromJson(childAny.toString());
                }

                child.positionIndexInParent = ++indexInParent;
                child.depth = currentBlock.depth + 1;
                currentBlock.addStatement(child);
            }
        }

        return newBlock;
    }

    public List<SingleStatement> getAllLeafStatementsIncludingNested() {
        final List<SingleStatement> leaves = new ArrayList<>();
        for (Statement statement : this.statements) {
            if (statement instanceof BlockStatement) {
                leaves.addAll(((BlockStatement) statement).getAllLeafStatementsIncludingNested());
            } else {
                leaves.add((SingleStatement) statement);
            }
        }
        return leaves;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.type.titleCase);
        if (expressions.size() > 0) {
            sb.append("(");
            for (int i = 0; i < expressions.size() - 1; i++) {
                sb.append(expressions.get(i).toString()).append("; ");
            }
            sb.append(expressions.get(expressions.size() - 1).toString());
            sb.append(")");
        }
        return sb.toString();
    }
}
