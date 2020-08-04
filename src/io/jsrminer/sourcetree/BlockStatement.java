package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import java.util.List;

/**
 * A block statement, i.e., a sequence of statements surrounded by braces {}.
 * May contain other block statements or statements (i.e. composite statements)
 */
public class BlockStatement extends Statement {
    List<Statement> statements;
    // exp
    // vd

    public BlockStatement() {

    }

    //@JsonCreator
    public static BlockStatement fromJson(String blockStatementJson) {
        BlockStatement block = new BlockStatement();
        Any any = JsonIterator.deserialize(blockStatementJson);

        // Parse source location
        SourceLocation location = any.get("loc").as(SourceLocation.class);
        block.sourceLocation = location;

        // Parse the nested statements
        List<Any> statements = any.get("statements").asList();
        for (Any statement: statements) {
            String type = statement.get("type").toString();
            boolean isComposite =  "BlockStatement".equals(type);

            if (isComposite) {
                // TO Do a block statement again
            }else {
                // A leaf statement
                block.statements.add(new SingleStatement(statement.toString()));
            }
        }

        return block;
    }
}
