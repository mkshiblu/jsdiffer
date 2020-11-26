
const nodeProcessor = require('../processors/AstNodeProcessor');
const t = require('@babel/types');

exports.processFunctionBody = function processFunctionBody(bodyPath) {
    const parent = {};
    nodeProcessor.processStatement(bodyPath, parent);

    const functionBody = parent.statements[0];
    return functionBody;
}

