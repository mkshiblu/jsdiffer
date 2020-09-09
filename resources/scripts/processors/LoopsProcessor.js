/* interface ForStatement<: Statement {
    type: "ForStatement";
    init: VariableDeclaration | Expression | null;
    test: Expression | null;
    update: Expression | null;
    body: Statement;
} */

exports.processForStatement = (path, processStatement, processExpression) => {
    const statement = {
        type: path.node.type,
        text: 'for',
        statements: []
    };

    processStatement(path.get('body'), statement);

    return statement;
}