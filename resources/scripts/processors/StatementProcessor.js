const { expressionStatement } = require("@babel/types");
const astProcessor = require("../processors/AstNodeProcessor");

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
exports.processExpressionStatement = (path) => {

    // const statement = {
    //     type: path.node.type,
    //     text: path.toString()
    // };
    const statement = astProcessor.processExpression(path.get('expression'));
    statement.type = path.node.type;
    statement.text = path.toString;

    return statement;
}