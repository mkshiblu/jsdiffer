const expressions = require('./ExpressionProcessor');

const propertyProcesses = new Map([["ObjectProperty", parseObjectProperty]
    , ["ObjectMethod", parseObjectMethod]
    , ["SpreadElement", parseSpreadElement]]);

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
        // extract properties
        path.get('properties').forEach(propPath => {
            const func = propertyProcesses.get(propPath.node.type);
            func(propPath, expressionResult, statement);
        });
    }
}

// interface ObjectProperty<: ObjectMember {
//     type: "ObjectProperty";
//     shorthand: boolean;
//     value: Expression;
// }

// Has a key property?
function parseObjectProperty(path, expressionResult, statement) {

    const node = path.node;

    if (node.shorthand) {
        console.log("Shorthand not implemented yet" + path.node.loc);
    }

    expressions.processExpression(path.get('key'), expressionResult, statement);
    expressions.processExpression(path.get('value'), expressionResult, statement);

}

// interface ObjectMethod<: ObjectMember, Function {
//     type: "ObjectMethod";
//     kind: "get" | "set" | "method";
// }

function parseObjectMethod(path, expressionResult, statement) {
    console.log("Not implemented yet: " + path.node.loc);
}

function parseSpreadElement(path) {
    console.log("Not implemented yet: " + path.node.loc);
}

exports.processObjectExpression = processObjectExpression;