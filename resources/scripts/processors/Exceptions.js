const astUtil = require('../parser/AstUtil');

/* interface TryStatement<: Statement {
    type: "TryStatement";
    block: BlockStatement;
    handler: CatchClause | null;
    finalizer: BlockStatement | null;
}
A try statement.If handler is null then finalizer must be a BlockStatement. */
exports.processTryStatement = (path, processStatement) => {
    const statement = {
        type: path.node.type,
        text: 'try',
        statements: [],
        catchClause: null
    };

    processStatement(path.get('block'), statement);
    const catchClause = path.get('handler');

    if (catchClause) {
        const catchStatement = porcessCatchClause(catchClause, processStatement);
        statement.catchClause = catchStatement;
    }
    return statement;
}

// interface CatchClause<: Node {
//     type: "CatchClause";
//     param: Pattern | null;
//     body: BlockStatement;
// }
function porcessCatchClause(path, processStatement) {
    const statement = {
        type: path.node.type,
        text: 'catch',
        statements: [],
        loc: astUtil.getFormattedLocation(path.node),
    };

    processStatement(path.get('body'), statement);
    return statement;
}

exports.porcessCatchClause = porcessCatchClause;