const { identifier } = require("@babel/types");

const variableDeclaratorProcessor = require('./VariableDeclarator');
const declarationProcessor = require('../processors/DeclarationProcessor');
const astUtil = require('./AstUtil');
const nodePorcessor = require('../processors/AstNodeProcessor');

exports.processFunctionBody = function processFunctionBody(bodyPath) {
    let functionBody = {};
    const bodyNodes = bodyPath.get('body');
    for (let i = 0; i < bodyNodes.length; i++) {
        const nodePath = bodyNodes[i];
        processStatement(nodePath, functionBody);
    }
    return functionBody;
}

function processStatement(path, parent) {
    if (process) {
        const statement = nodePorcessor.processNodePath(path, processStatement);
        statement.sourceLocation = astUtil.getFormattedLocation(path.node);
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
