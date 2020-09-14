const declarationProcessor = require('./DeclarationProcessor');
const controlFlowProcessor = require('./ControlFlowProcessor');
const choice = require("./Choice");
const statementProcessor = require('./StatementProcessor');
const expressionProcessor = require('./ExpressionProcessor');
const loopsProcessor = require('./LoopsProcessor');
const exceptions = require('./Exceptions');

function processExpression(path, statement) {
    const expressionInfo = {
        text: path.toString(),
        identifiers: [],
        numericLiterals: [],
        stringLiterals: [],
        variableDeclarations: [],
        infixOperators: [],
        functionInvocations: [],
        constructorInvocations: [],
        objectCreations: [],
        arguments: [],
    };

    const expression = expressionProcessor.processExpression(path, expressionInfo, statement);
    return expression;
}

const processNodePath = (function () {
    const visitor = require('../parser/Visitor');
    var nodePathProcesses = new Map([
        ['FunctionDeclaration', declarationProcessor.processFunctionDeclaration],
        ['VariableDeclaration', declarationProcessor.processVariableDeclaration],
        ['IfStatement', choice.processIfStatement],
        ['BlockStatement', statementProcessor.processBlockStatement],
        ['ReturnStatement', controlFlowProcessor.processReturnStatement],
        ['EmptyStatement', statementProcessor.processEmptyStatement],
        ['ExpressionStatement', statementProcessor.processExpressionStatement],
        ['ForStatement', loopsProcessor.processForStatement],
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