const astUtil = require('../parser/AstUtil');
const t = require('@babel/types');
const astProcessor = require("../processors/AstNodeProcessor");
const templates = require("../parser/Templates");

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
        expressions: [],
        loc: astUtil.getFormattedLocation(path.node),
    };

    const param = path.node.param;


    // Treat the catch clause param identifier as a special variable declaration
    if (param != null) {
        const expression = astProcessor.processExpression(path.get("param"), statement);

        // If identifier treat as a variable declaration
        if (t.isIdentifier(param)) {

            const variableDeclaration = {
                // text: param.name,
                variableName: param.name,
                kind: 'let', // TODO: Does it work as let?
                type: 'VariableDeclaration',
                // loc: astUtil.getFormattedLocation(param),
            };
            expression.identifiers = [];
            expression.variableDeclarations.push(variableDeclaration);
        }

        statement.expressions.push(expression);
    }

    processStatement(path.get('body'), statement);
    return statement;
}

// interface ThrowStatement <: Statement {
//     type: "ThrowStatement";
//     argument: Expression;
//   }
exports.processThrowStatement = (path, processStatement, processExpression) => {
    const statement = templates.getStatementTemplate(path);
    const expression = processExpression(path.get('argument'));
    Object.assign(expression, statement);
    return expression;
}

exports.porcessCatchClause = porcessCatchClause;