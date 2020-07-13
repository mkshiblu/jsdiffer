const { identifier } = require("@babel/types");

const variableDeclaratorProcessor = require('./VariableDeclarator');

const processors = new Map();

function initNodeProcessors() {
    processors.set('IfStatement', processIfStatement);
    processors.set('VariableDeclaration', processVariableDeclaration);
    processors.set('BlockStatement', processBlockStatement);
    processors.set('ReturnStatement', processReturnStatement);
}

exports.processFunctionBody = function processFunctionBody(bodyPath) {

    initNodeProcessors();

    let statements = [];
    const bodyNodes = bodyPath.get('body');
    for (let i = 0; i < bodyNodes.length; i++) {
        const nodePath = bodyNodes[i];
        processStatement(nodePath);

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

    const res = [];
    const process = processors.get(path.node.type);

    if (process) {
        const statement = process(path);

        if (statement.bodyPaths) {
            statement.bodyPaths.forEach(processStatement);
        }

        res.push(statement);
    } else {
        console.log("Processor to be implemented" + path.node.type);
    }

    return res;
}

function processBlockStatement(path) {
    return {
        type: path.node.type,
        statement: '{',
        bodyPaths: path.get('body')
    }
}

// It's a leaf node?
function processVariableDeclaration(variableDeclarationPath) {
    let temp = [];
    const variableDeclaration = variableDeclarationPath.node;

    // const declarationNodes = variableDeclaration.declarations;
    // for (let i = 0; i < declarationNodes.length; i++) {
    //     const declaration = declarationNodes[i];
    //     switch (declaration.type) {

    //         case 'VariableDeclarator':
    //             temp.push(variableDeclaratorProcessor.toStatement(declaration));
    //             break;
    //     }
    // }

    // //return temp;
    return {
        type: variableDeclaration.type,
        statement: variableDeclarationPath.toString(),
    };
}

function processIfStatement(ifStatementPath) {
    const ifStatement = ifStatementPath.node;
    const result = {
        type: ifStatement.type,
        // For composite we store the expression that appears inside the bracket and it's name
        statement: 'if',
        expressions: [],
        bodyPaths: []
    };

    // TODO: Handle expressions
    const condition = ifStatement.test;
    switch (condition.type) {
        case 'BinaryExpression':
            const left = condition.left;
            const operator = condition.operator;
            const right = condition.right;
            processExpression(right);
            break;
    }
    // TODO handle else if else

    const expressionStr = ifStatementPath.get('test').toString();
    result.expressions.push(expressionStr);

    // Extract body
    const bodyPath = ifStatementPath.get('consequent');

    if (bodyPath)
        result.bodyPaths.push(bodyPath);

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

function processFunctionDeclaration(functionDeclaration) {

}

function processExpressionStatement(expressionStatement) {

}

function processReturnStatement(path) {
  return {
      type: path.node.type,
      statement: path.toString()
  }    
}
