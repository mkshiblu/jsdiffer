/**
    interface IfStatement <: Statement {
    type: "IfStatement";
    test: Expression;
    consequent: Statement;
    alternate: Statement | null;
    }
 * @param {*} path 
 * @param {*} processStatement 
 */
exports.processIfStatement = (path, processStatement, processExpression) => {
    const ifStatement = path.node;
    const statement = {
        type: ifStatement.type,
        // For composite we store the expression that appears inside the bracket and it's name
        text: 'if',
        expressions: []
    };

    // TODO: Handle expressions
    const condition = ifStatement.test;
    // TODO handle else if else

    const expression = processExpression(path.get('test'));
    statement.expressions.push(expression);

    // Extract body
    const bodyPath = path.get('consequent');

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