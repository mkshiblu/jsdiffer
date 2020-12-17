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

    const expression = processExpression(path.get('test'));
    statement.expressions.push(expression);

    // For composite we store the expression that appears inside the bracket and its name
    //statement.text = compositeHelper.getTextWithExpressions(statement);

    // Extract body
    const bodyPath = path.get('consequent');

    if (bodyPath)
        processStatement(bodyPath, statement);

    // Handle Else If
    if (path.node.alternate != null) {
        const alternatePath = path.get('alternate');
        processStatement(alternatePath, statement);
    }
    return statement;
};


// SwitchStatement
// interface SwitchStatement <: Statement {
//   type: "SwitchStatement";
//   discriminant: Expression;
//   cases: [ SwitchCase ];
// }
// A switch statement.



exports.processSwitchStatement = (path, processStatement, processExpression) => {
    const node = path.node;
    const statement = {
        type: node.type,
        expressions: [],
        text: "switch"
    };

    const expression = processExpression(path.get('discriminant'));
    statement.expressions.push(expression);

    // For composite we store the expression that appears inside the bracket and its name
    statement.text = compositeHelper.getTextWithExpressions(statement);

    // Extract body
    const switchCases = path.get('cases');
    switchCases.forEach((switchCasePath) => processStatement(switchCasePath, statement));

    return statement;
};

// SwitchCase
// interface SwitchCase <: Node {
//   type: "SwitchCase";
//   test: Expression | null;
//   consequent: [ Statement ];
// }
// A case (if test is an Expression) or default (if test === null) clause in the body of a switch statement.
exports.processSwitchCase = (path, processStatement, processExpression) => {
    const node = path.node;
    const statement = {
        type: node.type,
        expressions: [],
        text: "case"
    };

    if (node.test) {
        const expression = processExpression(path.get('test'));
        statement.expressions.push(expression);
        statement.text = "case " + expression.text + ":";
    }

    // Extract body
    path.get('consequent')
        .forEach((childStatementPath) => processStatement(childStatementPath, statement));

    return statement;
};