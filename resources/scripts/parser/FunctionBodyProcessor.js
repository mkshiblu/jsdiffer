const astUtil = require('./AstUtil');
const nodeProcessor = require('../processors/AstNodeProcessor');
const t = require('@babel/types');

exports.processFunctionBody = function processFunctionBody(bodyPath) {
    const parent = {};
    processStatement(bodyPath, parent);
    const functionBody = parent.statements[0];
    return functionBody;
}

/**
 * Could be called by each node recursively if they have child to be processed
 */
function processStatement(path, parent) {
    const statement = nodeProcessor.processNodePath(path, processStatement);
    statement.loc = astUtil.getFormattedLocation(path.node);

    // Add children
    addStatement(parent, statement);
}

function addStatement(parent, childStatement) {
    if (!parent.statements) {
        parent.statements = [];
    }
    parent.statements.push(childStatement);
}
