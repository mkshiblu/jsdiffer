const astProcessor = require("./AstNodeProcessor");
const declarationProcessor = require("./DeclarationProcessor");
const templates = require('../parser/Templates');

// interface WhileStatement<: Statement {
//     type: "WhileStatement";
//     test: Expression;
//     body: Statement;
// }
exports.processWhileStatement = (path, processStatement, processExpression) => {
    const node = path.node;
    const statement = {
        type: node.type,
        text: 'while',
        statements: [],
        expressions: []
    };

    const test = astProcessor.processExpression(path.get("test"), statement);
    statement.expressions.push(test);

    processStatement(path.get('body'), statement);

    return statement;
};


/* interface ForStatement<: Statement {
    type: "ForStatement";
    init: VariableDeclaration | Expression | null;
    test: Expression | null;
    update: Expression | null;
    body: Statement;
} */
exports.processForStatement = (path, processStatement, processExpression) => {
    const node = path.node;
    const statement = {
        type: node.type,
        text: 'for',
        statements: [],
        expressions: []
    };

    if (node.init) {
        if (node.init.type == 'VariableDeclaration') {
            // processStatement(path.get('init'), statement);
            const expression = processLoopVariableDeclarationAsExpression(path.get("init"), statement);
            statement.expressions.push(expression);
        } else {
            // It's an expression
            const initializer = astProcessor.processExpression(path.get("init"), statement);
            statement.expressions.push(initializer);
        }
    }

    if (node.test) {
        const test = astProcessor.processExpression(path.get("test"), statement);
        statement.expressions.push(test);
    }

    if (node.update) {
        const update = astProcessor.processExpression(path.get("update"), statement);
        statement.expressions.push(update);
    }

    processStatement(path.get('body'), statement);

    return statement;
};

// interface ForInStatement <: Statement {
//     type: "ForInStatement";
//     left: VariableDeclaration |  Expression;
//     right: Expression;
//     body: Statement;
//   }
//   A for/in statement.
exports.processForInStatement = (path, processStatement, processExpression) => {
    const node = path.node;
    const statement = {
        type: node.type,
        text: 'for',
        statements: [],
        expressions: []
    };

    if (node.left.type == 'VariableDeclaration') {
        const expression = processLoopVariableDeclarationAsExpression(path.get("left"), statement);
        statement.expressions.push(expression);
    } else {
        // It's an expression
        const initializer = astProcessor.processExpression(path.get("left"), statement);
        statement.expressions.push(initializer);
    }

    const right = astProcessor.processExpression(path.get("right"), statement);
    statement.expressions.push(right);

    processStatement(path.get('body'), statement);

    return statement;
};

function processLoopVariableDeclarationAsExpression(path, statement) {
    const declarationStatement = declarationProcessor.processVariableDeclaration(path);
    const expression = astProcessor.createBaseExpressionInfo(path);
    Object.assign(expression, declarationStatement);
    return expression;
}
