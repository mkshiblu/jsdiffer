const declarationProcessor = require('./DeclarationProcessor');
const controlFlowProcessor = require('./ControlFlowProcessor');
const choice = require("./Choice");
const statementProcessor = require('./StatementProcessor');
const expressionProcessor = require('./ExpressionProcessor');
const loopsProcessor = require('./Loops');
const exceptions = require('./Exceptions');
const astUtil = require('../parser/AstUtil');
const types = require('@babel/types');

/**
 * Could be called by each node recursively if they have child to be processed
 */
function processStatement(path, parent) {
    try {
        const statement = processNodePath(path, processStatement);
        statement.loc = astUtil.getFormattedLocation(path.node);
        // Add children
        addStatement(parent, statement, path);
    } catch (ex) {
        console.error(ex, path.node.loc.start.toString());
    }
}

function addStatement(parent, childStatement, childPath) {

    if (types.isFunctionDeclaration(childPath.node)) {
        if (!parent.functionDeclarations) {
            parent.functionDeclarations = [];
        }

        parent.functionDeclarations.push(childStatement);
        return;
    }

    if (!parent.statements) {
        parent.statements = [];
    }
    parent.statements.push(childStatement);
}

function createBaseExpressionInfo(path) {
    return {
        text: path.toString(),
        identifiers: [],
        numericLiterals: [],
        stringLiterals: [],
        nullLiterals: [],
        booleanLiterals: [],
        infixOperators: [],
        prefixExpressions: [],
        postfixExpressions: [],
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
    var nodePathProcesses = new Map([
        ['FunctionDeclaration', declarationProcessor.processFunctionDeclaration],
        ['VariableDeclaration', declarationProcessor.processVariableDeclaration],

        ['IfStatement', choice.processIfStatement],
        ['SwitchStatement', choice.processSwitchStatement],
        ['SwitchCase', choice.processSwitchCase],

        ['BlockStatement', statementProcessor.processBlockStatement],

        ['ReturnStatement', controlFlowProcessor.processReturnStatement],
        ['BreakStatement', controlFlowProcessor.processBreakStatement],
        ['ContinueStatement', controlFlowProcessor.processContinueStatement],

        ['EmptyStatement', statementProcessor.processEmptyStatement],
        ['ExpressionStatement', statementProcessor.processExpressionStatement],

        ['ForStatement', loopsProcessor.processForStatement],
        ['ForInStatement', loopsProcessor.processForInStatement],
        ['WhileStatement', loopsProcessor.processWhileStatement],

        ['TryStatement', exceptions.processTryStatement],
        ['ThrowStatement', exceptions.processThrowStatement],
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

exports.processStatement = processStatement;
exports.processNodePath = processNodePath;
exports.processExpression = processExpression;
exports.createBaseExpressionInfo = createBaseExpressionInfo;