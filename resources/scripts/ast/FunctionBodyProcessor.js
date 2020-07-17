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

    let fd = {};
    const bodyNodes = bodyPath.get('body');
    for (let i = 0; i < bodyNodes.length; i++) {
        const nodePath = bodyNodes[i];
        processStatement(nodePath, fd);
    }

    return fd;
}

// The main function for recursively going deep and extracting all the informations
// Parent is the parent node
function processStatement(path, parent) {

    const process = processors.get(path.node.type);
    if (process) {
        const res = process(path);

        if (res.bodyPaths) {
            res.bodyPaths.forEach(bodyPath => processStatement(bodyPath, res));
        }

        addStatement(parent, res);

  //      processedNodes.push(res);
    } else {
        console.log("Processor to be implemented" + path.node.type);
    }

//    return processedNodes;
}


function addStatement(parent, childStatement) {
    if (!parent.children) {
        parent.children = [];
    }

    parent.children.push(childStatement);
}


function processBlockStatement(path) {
    return {
        type: path.node.type,
        statement: '{',
        bodyPaths: path.get('body'),
        children: []
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
