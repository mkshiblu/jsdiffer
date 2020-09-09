/* interface TryStatement<: Statement {
    type: "TryStatement";
    block: BlockStatement;
    handler: CatchClause | null;
    finalizer: BlockStatement | null;
}
A try statement.If handler is null then finalizer must be a BlockStatement. */

exports.processTryStatement = (path, processStatement, processExpression) => {
    const statement = {
        type: path.node.type,
        text: 'try',
        statements: []
    };

    processStatement(path.get('block'), statement);

    return statement;
}