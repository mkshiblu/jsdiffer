const declarationProcessor = require('./DeclarationProcessor');
const controlFlowProcessor = require('./ControlFlowProcessor');
const statementProcessor = require('./StatementProcessor');

const processNodePath = (function () {
    var nodePathProcesses = new Map([
        ['FunctionDeclaration', declarationProcessor.processFunctionDeclaration],
        ['VariableDeclaration', declarationProcessor.processVariableDeclaration],
        ['IfStatement', controlFlowProcessor.processIfStatement],
        ['BlockStatement', statementProcessor.processBlockStatement],
        ['ReturnStatement', controlFlowProcessor.processReturnStatement],
        ['EmptyStatement', statementProcessor.processEmptyStatement],
        ['ExpressionStatement', statementProcessor.processExpressionStatement],
    ]);


    

    this.VariableDeclaration = (path) => {
        const variableDeclaration = path.node;

        // If variable declarations are passed as state
        if (this.variableDeclarations) {

            // Add the variable declaration to the passed state
            this.variableDeclarations.push({
                // name: 
                // scope: 
            });
        }
    }
    
    return function (nodePath, processStatement, visitor) {
        const process = nodePathProcesses.get(nodePath.node.type);
        if (process) {
            const rt = process(nodePath, processStatement, visitor);
            return rt;
        }

        //return 'Processeor not implemented for : ' + nodePath.node.type + " : " + nodePath.toString();
        throw 'Processeor not implemented for : ' + nodePath.node.type;
    }
})();

exports.processNodePath = processNodePath;