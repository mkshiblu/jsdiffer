const { identifier } = require("@babel/types");

const variableDeclaratorProcessor = require('./VariableDeclarator');

exports.processFunctionBody = function processFunctionBody(functionBody) {

    let statements = [];

    const bodyNodes = functionBody.body;
    for (let i = 0; i < bodyNodes.length; i++) {
        const node = bodyNodes[i];
        switch (node.type) {
            case 'VariableDeclaration':
                statements.push(processVariableDeclaration(node));
                break;
            case 'FunctionDeclaration':
                processFunctionDeclaration(node);
                break;
            case 'ExpressionStatement':
                processExpressionStatement(node);
                break;
            case 'IfStatement':
                processIfStatement(node);
                break;
            default:
                break;
        }
    }

    return statements;
}

function processIfStatement(ifStatement) {
    const result = {
        name: 'if',
        expressionList: []
    };

    const ifBody = ifStatement.consequent; // type could be a block statement
    const condition = ifStatement.test;
    switch (condition.type) {
        case 'BinaryExpression':
            const left = condition.left;
            const operator = condition.operator;
            const right = condition.right;
            processExpression(right);
            break;
    }

    // TODO add the expression to the list
    return result.expressionList.push(condition);
}

function processExpression(expression) {
    switch (expression.type) {
        case 'BinaryExpression':
            const left = condition.left;
            const operator = condition.operator;
            const right = condition.right;
            break;
        case 'NumericLiteral':
            const value = expression.value;
            break;
    }
}

function processVariableDeclaration(variableDeclaration) {
    let temp = [];
    const declarationNodes = variableDeclaration.declarations;
    for (let i = 0; i < declarationNodes.length; i++) {
        const declaration = declarationNodes[i];
        switch (declaration.type) {

            case 'VariableDeclarator':
                temp.push(variableDeclaratorProcessor.toStatement(declaration));
                break;
        }
    }
    return temp;
}

function processFunctionDeclaration(functionDeclaration) {

}

function processExpressionStatement(expressionStatement) {

}
