const astProcessor = require("../processors/AstNodeProcessor");
const templates = require("../parser/Templates");
const types = require('@babel/types');

// interface ReturnStatement <: Statement {
//     type: "ReturnStatement";
//     argument: Expression | null;
//   }
exports.processReturnStatement = function (path) {
    let statement = {
        type: path.node.type,
        text: path.toString()
    }

    if (path.node.argument != null) {
        const expression = astProcessor.processExpression(path.get('argument'), statement);
        statement = Object.assign(expression, statement);
    }

    return statement;
}

// ContinueStatement
// interface ContinueStatement<: Statement {
//     type: "ContinueStatement";
//     label: Identifier | null;
// }
// A continue statement.

exports.processContinueStatement = function (path) {
    let statement = templates.getStatementTemplate(path);

    if (path.node.label != null) {
       // throw "Labeled statements are not supported yet: " + path;
    }

    return statement;
}

/**
 * BreakStatement
interface BreakStatement <: Statement {
  type: "BreakStatement";
  label: Identifier | null;
}

The label is identifier of a coode block
 */

exports.processBreakStatement = function (path) {
    let statement = templates.getStatementTemplate(path);

    if (path.node.label != null) {
       // throw "Labeled statements are not supported yet: " + path;
    }

    return statement;
}

// interface LabeledStatement<: Statement {
//     type: "LabeledStatement";
//     label: Identifier;
//     body: Statement;
// }
exports.processLabeledStatement = function (path, processStatement) {
    const result = astProcessor.processNodePath(path.get('body'), processStatement);

    if (types.isClassDeclaration(path.node.body) || types.isFunctionDeclaration(path.node.body)) {
        console.log('Labled function/class declarations are being considered as statement at line. ' + String(path.node.loc.start.line));
        //return {}
    }
    return result;
}
