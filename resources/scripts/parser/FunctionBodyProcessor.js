const astUtil = require('./AstUtil');
const nodePorcessor = require('../processors/AstNodeProcessor');

exports.processFunctionBody = function processFunctionBody(bodyPath) {
    const parent = {};
    processStatement(bodyPath, parent);
    const functionBody = parent.statements[0];
    return functionBody;
}

function processStatement(path, parent) {
        const statement = nodePorcessor.processNodePath(path, processStatement);
        statement.loc = astUtil.getFormattedLocation(path.node);
        addStatement(parent, statement);
}

function addStatement(parent, childStatement) {
    if (!parent.statements) {
        parent.statements = [];
    }
    parent.statements.push(childStatement);
}
