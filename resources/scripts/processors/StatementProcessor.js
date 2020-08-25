const { expressionStatement } = require("@babel/types");

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
        type: emptyStatementPath.node.type,
        text: emptyStatementPath.toString()
    }
}

//An expression statement, i.e., a statement consisting of a single expression.
// interface ExpressionStatement<: Statement {
//     type: "ExpressionStatement";
//     expression: Expression;
// }
exports.processExpressionStatement = (expressionStatementPath) => {
    return {
        type: expressionStatementPath.node.type,
        text: expressionStatementPath.toString()
    }
}