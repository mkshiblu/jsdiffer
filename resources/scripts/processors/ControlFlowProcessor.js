const astProcessor = require("../processors/AstNodeProcessor");

// interface ReturnStatement <: Statement {
//     type: "ReturnStatement";
//     argument: Expression | null;
//   }
exports.processReturnStatement = function (path) {
    let statement = {
        type: path.node.type,
        text: path.toString()
    }

    if (path.node.argument != null) {
        const expression = astProcessor.processExpression(path.get('argument'), statement);
        statement = Object.assign(expression, statement);
    }

    return statement;
}
