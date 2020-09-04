const declarationProcessor = require('./DeclarationProcessor');
const controlFlowProcessor = require('./ControlFlowProcessor');
const statementProcessor = require('./StatementProcessor');
const expressionProcessor = require('./ExpressionProcessor');

function processExpression(path) {
    const expressionInfo = {
        text: path.toString(),
        identifiers: [],
        numericLiterals: [],
        variableDeclarations: [],
        infixOperators: []
    };

    const expression = expressionProcessor.processExpression(path, expressionInfo);
    return expression;
}

const processNodePath = (function () {
    const visitor = require('../parser/Visitor');
    var nodePathProcesses = new Map([
        ['FunctionDeclaration', declarationProcessor.processFunctionDeclaration],
        ['VariableDeclaration', declarationProcessor.processVariableDeclaration],
        ['IfStatement', controlFlowProcessor.processIfStatement],
        ['BlockStatement', statementProcessor.processBlockStatement],
        ['ReturnStatement', controlFlowProcessor.processReturnStatement],
        ['EmptyStatement', statementProcessor.processEmptyStatement],
        ['ExpressionStatement', statementProcessor.processExpressionStatement],
    ]);

    return function (nodePath, processStatement) {
        const process = nodePathProcesses.get(nodePath.node.type);
        if (process) {
            const rt = process(nodePath, processStatement, processExpression);
            return rt;
        }

        //return 'Processeor not implemented for : ' + nodePath.node.type + " : " + nodePath.toString();
        throw 'Processeor not implemented for : ' + nodePath.node.type;
    }
})();

exports.processNodePath = processNodePath;
exports.processExpression = processExpression;