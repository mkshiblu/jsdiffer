package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import io.jsrminer.parser.JsonCompositeFactory;

import java.util.*;

import static java.util.AbstractMap.SimpleImmutableEntry;

/**
 * A block statement, i.e., a sequence of statements surrounded by braces {}.
 * May contain other block statements or statements (i.e. composite statements)
 */
public class BlockStatement extends Statement {
    protected List<Statement> statements;
    protected List<Expression> expressions = new ArrayList<>();
    // exp
    // vd

    public BlockStatement() {
    }

    public void addStatement(Statement statement) {
        this.statements.add(statement);
    }

    void addExpression(Expression expression) {
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
        blocksToBeProcessed.add(new SimpleImmutableEntry<>(newBlock, any));

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
            currentBlock.type = CodeElementType.getFromTitleCase(any.toString("type"));

            // Parse Expressions (Todo optimize
            if (any.keys().contains("expressions")) {
                for (Any expressionAny : any.get("expressions").asList()) {
                    Expression expression = Expression.fromJSON(expressionAny.toString());
                    currentBlock.addExpression(expression);
                }
            }

            // Parse text
            currentBlock.text = any.toString("text");

            // Check if it's try statement and contains any catchBlock
            if (any.keys().contains("catchClause")) {
                BlockStatement catchClause = new BlockStatement();
                blocksToBeProcessed.add(new SimpleImmutableEntry<>(catchClause, any.get("catchClause")));

                // Add the catchblacue as seprate composite to the parent of the try block
                catchClause.positionIndexInParent = currentBlock.positionIndexInParent++;
                catchClause.depth = currentBlock.depth;
                ((BlockStatement) currentBlock.parent).statements.add(catchClause);
            }

            // Parse childs of this block
            for (Any childAny : statements) {
                isComposite = childAny.keys().contains("statements");

                if (isComposite) {

                    // If composite enqueue the block and corresponding json to be processed later
                    childBlock = new BlockStatement();
                    blocksToBeProcessed.add(new SimpleImmutableEntry<>(childBlock, childAny));
                    child = childBlock;
                } else {
                    // A leaf statement
                    child = JsonCompositeFactory.createSingleStatement(childAny);
                }

                child.parent = currentBlock;
                child.positionIndexInParent = ++indexInParent;
                child.depth = currentBlock.depth + 1;
                currentBlock.addStatement(child);
            }

        }

        return newBlock;
    }

    /**
     * Returns all the single statements including children's of children in a bottom up fashion
     * (i.e. lowest level children will be the first elements of the list sorted by their index position in parent)
     */
    public Set<SingleStatement> getAllLeafStatementsIncludingNested() {
        final Set<SingleStatement> leaves = new LinkedHashSet<>();
        for (Statement statement : this.statements) {
            if (statement instanceof BlockStatement) {
                leaves.addAll(((BlockStatement) statement).getAllLeafStatementsIncludingNested());
            } else {
                leaves.add((SingleStatement) statement);
            }
        }
        return leaves;
    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append(this.type.titleCase);
//        if (expressions.size() > 0) {
//            sb.append("(");
//            for (int i = 0; i < expressions.size() - 1; i++) {
//                sb.append(expressions.get(i).toString()).append("; ");
//            }
//            sb.append(expressions.get(expressions.size() - 1).toString());
//            sb.append(")");
//        }
//        return sb.toString();
//    }

    /**
     * Similar to getInnerNodes of RM
     *
     * @return
     */
    public Set<BlockStatement> getAllBlockStatementsIncludingNested() {
        final Set<BlockStatement> innerNodes = new LinkedHashSet<>();
        for (Statement statement : this.statements) {
            if (statement instanceof BlockStatement) {
                BlockStatement composite = (BlockStatement) statement;
                innerNodes.addAll(composite.getAllBlockStatementsIncludingNested());
            }
        }
       // innerNodes.add(this);
        return innerNodes;
    }
}
