const { identifier } = require("@babel/types");

const variableDeclaratorProcessor = require('./VariableDeclarator');

exports.processFunctionBody = function processFunctionBody(bodyPath) {

    let statements = [];
    const bodyNodes = bodyPath.get('body');
    for (let i = 0; i < bodyNodes.length; i++) {
        const nodePath = bodyNodes[i];
        const node = nodePath.node;
        switch (node.type) {
            case 'VariableDeclaration':
                statements.push(processVariableDeclaration(nodePath));
                break;
            case 'FunctionDeclaration':
                processFunctionDeclaration(nodePath);
                break;
            case 'ExpressionStatement':
                processExpressionStatement(nodePath);
                break;
            case 'IfStatement':
                const ifStr = processIfStatement(nodePath);
                statements.push(ifStr);
                break;
            default:
                break;
        }
    }

    return statements;
}


// The main function for recursively going deep and extracting all the informations
function processStatement(path) {
    
}

function processIfStatement(ifStatementPath) {
    const ifStatement = ifStatementPath.node;
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

    const expressionStr = ifStatementPath.get('test').toString();
    result.expressionList.push(expressionStr);
    // TODO add the expression to the list
    return result;
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

function processVariableDeclaration(variableDeclarationPath) {
    let temp = [];
    const variableDeclaration = variableDeclarationPath.node;

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
