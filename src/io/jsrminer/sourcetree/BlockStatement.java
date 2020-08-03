package io.jsrminer.sourcetree;

import com.jsoniter.JsonIterator;

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

//    public BlockStatement(String blockStatementJson) {
//        fromJson(blockStatementJson);
//    }

    public static BlockStatement fromJson(String blockStatementJson) {
        //BlockStatement block = new BlockStatement();
        BlockStatement block = JsonIterator.deserialize(blockStatementJson, BlockStatement.class);
        return null;
    }
}
