const declarationProcessor = require('./DeclarationProcessor');
const controlFlowProcessor = require('./ControlFlowProcessor');
const choice = require("./Choice");
const statementProcessor = require('./StatementProcessor');
const expressionProcessor = require('./ExpressionProcessor');
const loopsProcessor = require('./Loops');
const exceptions = require('./Exceptions');

function createBaseExpressionInfo(path) {
    return {
        text: path.toString(),
        identifiers: [],
        numericLiterals: [],
        stringLiterals: [],
        nullLiterals: [],
        infixOperators: [],
        prefixOperators: [],
        postfixOperators: [],
        variableDeclarations: [],
        functionInvocations: [],
        constructorInvocations: [],
        objectCreations: [],
        arguments: [],
        loc: {}
    };
}

function processExpression(path, statement) {
    const expressionInfo = createBaseExpressionInfo(path);
    const expression = expressionProcessor.processExpression(path, expressionInfo, statement);
    return expression;
}

const processNodePath = (function () {
    const visitor = require('../parser/Visitor');
    var nodePathProcesses = new Map([
        ['FunctionDeclaration', declarationProcessor.processFunctionDeclaration],
        ['VariableDeclaration', declarationProcessor.processVariableDeclaration],
        
        ['IfStatement', choice.processIfStatement],
        ['SwitchStatement', choice.processSwitchStatement],

        ['BlockStatement', statementProcessor.processBlockStatement],
        
        ['ReturnStatement', controlFlowProcessor.processReturnStatement],
        ['BreakStatement', controlFlowProcessor.processBreakStatement],

        ['EmptyStatement', statementProcessor.processEmptyStatement],
        ['ExpressionStatement', statementProcessor.processExpressionStatement],
        ['ForStatement', loopsProcessor.processForStatement],
        ['ForInStatement', loopsProcessor.processForInStatement],
        ['TryStatement', exceptions.processTryStatement],
    ]);

    return function (nodePath, processStatement) {
        const process = nodePathProcesses.get(nodePath.node.type);
        if (process) {
            // TODO, type, text, location etc.
            const rt = process(nodePath, processStatement, processExpression);
            return rt;
        }

        //return 'Processeor not implemented for : ' + nodePath.node.type + " : " + nodePath.toString();
        throw 'Processeor not implemented for : ' + nodePath.node.type;
    }
})();

exports.processNodePath = processNodePath;
exports.processExpression = processExpression;
exports.createBaseExpressionInfo = createBaseExpressionInfo;