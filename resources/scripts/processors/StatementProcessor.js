exports.processBlockStatement = (blockStatementPath, processStatement) => {
    const statement = {
        type: blockStatementPath.node.type,
        text: '{',
        statements: []
    };

    blockStatementPath.get('body')
        .forEach(bodyPath => processStatement(bodyPath, statement));

    return statement;
}

exports.processEmptyStatement = (emptyStatementPath) => {
    return {
        type: emptyStatementPath.node.type
    }
}