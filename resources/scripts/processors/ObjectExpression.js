// interface ObjectExpression<: Expression {
//     type: "ObjectExpression";
//     properties: [ObjectProperty | ObjectMethod | SpreadElement];
// }

function processObjectExpression(path, expressionResult, statement) {
    const node = path.node;
    const isEmptyLiteral = node.properties.length == 0;

    if (isEmptyLiteral) {

        if (expressionResult.objectLiterals) {
            expressionResult.objectLiterals.push(path.toString());
        } else {
            expressionResult.objectLiterals = [path.toString()];
        }
    } else {
        throw "Not supported yet " + path.toString();
    }
}

exports.processObjectExpression = processObjectExpression;