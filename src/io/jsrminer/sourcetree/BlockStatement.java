package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import java.util.List;

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

    public void addStatement(Statement statement){
        this.statements.add(statement);
    }

    //@JsonCreator
    public static BlockStatement fromJson(String blockStatementJson) {
        BlockStatement block = new BlockStatement();
        Any any = JsonIterator.deserialize(blockStatementJson);

        // Parse source location
        SourceLocation location = any.get("loc").as(SourceLocation.class);
        block.setSourceLocation(location);

        // Parse the nested statements
        List<Any> statements = any.get("statements").asList();
        for (Any statement: statements) {
            String type = statement.get("type").toString();
            boolean isComposite =  "BlockStatement".equals(type);

            if (isComposite) {
                // TO Do a block statement again
            }else {
                // A leaf statement
                SingleStatement singleStatement = SingleStatement.fromJson(statement.toString());
                block.addStatement(singleStatement);
            }
        }

        return block;
    }
}
