const astProcessor = require("../processors/AstNodeProcessor");
const declarationProcessor = require("../processors/DeclarationProcessor");

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
            const declarationStatement = declarationProcessor.processVariableDeclaration(path.get("init"));
            const expression = astProcessor.createBaseExpressionInfo(path.get("iniit"));
            Object.assign(expression, declarationStatement);
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
}