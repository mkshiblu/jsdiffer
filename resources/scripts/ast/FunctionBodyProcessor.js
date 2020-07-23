const { identifier } = require("@babel/types");

const variableDeclaratorProcessor = require('./VariableDeclarator');
const functionDeclarationProcessor = require('../parser/FunctionDeclarationProcessor');
const astUtil = require('./AstUtil');

const nodePathProcesses = new Map();
let bodyPaths = [];

function initNodeProcessors() {
    nodePathProcesses.set('FunctionDeclaration', processFunctionDeclaration);
    nodePathProcesses.set('IfStatement', processIfStatement);
    nodePathProcesses.set('VariableDeclaration', processVariableDeclaration);
    nodePathProcesses.set('BlockStatement', processBlockStatement);
    nodePathProcesses.set('ReturnStatement', processReturnStatement);
    nodePathProcesses.set('EmptyStatement', processEmptyStatement);
}

function processFunctionDeclaration(functionDeclarationPath) {
    // const node = functionDeclarationPath.node;
    // const qualifiedName = astUtil.getFunctionQualifiedName(functionDeclarationPath);
    // const name = node.id.name
    // bodyPaths.push(functionDeclarationPath.get('body'));

    // return {
    //     type: node.type,
    //     qualifiedName,
    //     name,
    //     params: node.params.map(id => id.name)
    // };

    return functionDeclarationProcessor.processFunctionDeclaration(functionDeclarationPath, processStatement);
}

exports.processFunctionBody = function processFunctionBody(bodyPath) {
    initNodeProcessors();

    let functionBody = {};
    const bodyNodes = bodyPath.get('body');
    for (let i = 0; i < bodyNodes.length; i++) {
        const nodePath = bodyNodes[i];
        processStatement(nodePath, functionBody);
    }

    return functionBody;
}

// The main function for recursively going deep and extracting all the informations
// Parent is the parent node
function processStatement(path, parent) {

    const process = nodePathProcesses.get(path.node.type);
    if (process) {
        bodyPaths = [];
        const statement = process(path);

        statement.sourceLocation = astUtil.getFormattedLocation(path.node);

        if (bodyPaths) {
            bodyPaths.forEach(bodyPath => processStatement(bodyPath, statement));
        }

        addStatement(parent, statement);
    } else {
        console.log("Processor to be implemented" + path.node.type);
    }
}


function addStatement(parent, childStatement) {
    if (!parent.statements) {
        parent.statements = [];
    }
    parent.statements.push(childStatement);
}


function processBlockStatement(path) {
    bodyPaths = path.get('body');
    return {
        type: path.node.type,
        text: '{',
        statements: []
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
        text: variableDeclarationPath.toString(),
    };
}

function processIfStatement(ifStatementPath) {
    const ifStatement = ifStatementPath.node;
    const result = {
        type: ifStatement.type,
        // For composite we store the expression that appears inside the bracket and it's name
        text: 'if',
        expressions: []
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
        bodyPaths.push(bodyPath);

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


function processEmptyStatement(emptyStatementPath) {
    return {
        type: emptyStatementPath.node.type
    }
}

function processReturnStatement(path) {
    return {
        type: path.node.type,
        text: path.toString()
    }
}


exports.processFunctionDeclaration = processFunctionDeclaration;