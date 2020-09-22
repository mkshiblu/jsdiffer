/* interface TryStatement<: Statement {
    type: "TryStatement";
    block: BlockStatement;
    handler: CatchClause | null;
    finalizer: BlockStatement | null;
}
A try statement.If handler is null then finalizer must be a BlockStatement. */

const { processExpression } = require("./ExpressionProcessor");

exports.processTryStatement = (path, processStatement, processExpression) => {
    const statement = {
        type: path.node.type,
        text: 'try',
        statements: [],
        catchClause: {}
    };

    processStatement(path.get('block'), statement);
    const catchClause = path.get('handler');

    if (catchClause) {
        const catchStatement = porcessCatchClause(catchClause, processStatement, processExpression);
        statement.catchClause = catchStatement;
    }
    return statement;
}

// interface CatchClause<: Node {
//     type: "CatchClause";
//     param: Pattern | null;
//     body: BlockStatement;
// }
function porcessCatchClause(path, processStatement, processExpression) {
    const statement = {
        type: path.node.type,
        text: 'catch',
        statements: []
    };

    processStatement(path.get('body'), statement);
    return statement;
}