const compositeHelper = require('./CompositeStatementHelper');
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
        expressions: [],
        text: "if"
    };

    // TODO: Handle expressions
    const condition = ifStatement.test;
    // TODO handle else if else
    const expression = processExpression(path.get('test'));
    statement.expressions.push(expression);

    // For composite we store the expression that appears inside the bracket and its name
    statement.text = compositeHelper.getTextWithExpressions(statement);

    // Extract body
    const bodyPath = path.get('consequent');

    if (bodyPath)
        processStatement(bodyPath, statement);

    return statement;
};
