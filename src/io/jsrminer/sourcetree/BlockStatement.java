package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import java.security.KeyStore;
import java.util.*;

/**
 * A block statement, i.e., a sequence of statements surrounded by braces {}.
 * May contain other block statements or statements (i.e. composite statements)
 */
public class BlockStatement extends Statement {
    protected List<Statement> statements;
    // exp
    // vd

    public BlockStatement() {
    }

    public void addStatement(Statement statement) {
        this.statements.add(statement);
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

            for (Any childAny : statements) {
                isComposite = CodeElementType.BLOCK_STATEMENT
                        .titleCase.equals(childAny.get("type").toString());

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
                currentBlock.addStatement(child);
            }
        }

        return newBlock;
    }
}
