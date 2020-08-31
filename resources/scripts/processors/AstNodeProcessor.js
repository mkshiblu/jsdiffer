const declarationProcessor = require('./DeclarationProcessor');
const controlFlowProcessor = require('./ControlFlowProcessor');
const statementProcessor = require('./StatementProcessor');
const expressionProcessor = require('./ExpressionProcessor');


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

    function processExpression(path) {
        const expressionInfo = {
            identifiers: [],
            numericLiterals: [],
            variableDeclarations: [],
            binaryOperators: []
        };
        
        const expression = expressionProcessor.processExpression(path, expressionInfo);
        return expression;
    }

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