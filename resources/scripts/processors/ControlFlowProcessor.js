exports.processIfStatement = (ifStatementPath, processStatement) => {
    const ifStatement = ifStatementPath.node;
    const statement = {
        type: ifStatement.type,
        // For composite we store the expression that appears inside the bracket and it's name
        text: 'if',
        expressions: []
    };

    // TODO: Handle expressions
    const condition = ifStatement.test;
    // switch (condition.type) {
    //     case 'BinaryExpression':
    //         const left = condition.left;
    //         const operator = condition.operator;
    //         const right = condition.right;
    //         processExpression(right);
    //         break;
    // }
    // TODO handle else if else

    const expressionStr = ifStatementPath.get('test').toString();
    statement.expressions.push(expressionStr);

    // Extract body
    const bodyPath = ifStatementPath.get('consequent');

    if (bodyPath)
        processStatement(bodyPath, statement);

    return statement;
}


exports.processReturnStatement = function (path) {
    return {
        type: path.node.type,
        text: path.toString()
    }
}