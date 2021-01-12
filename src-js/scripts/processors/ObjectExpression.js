const expressions = require('./ExpressionProcessor');
const astProcessor = require('./AstNodeProcessor');
const types = require('@babel/types');

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

        // Create a new Expression to serve as annonmous class
        let objectExpression = {
            properties: []
        };

        // extract properties
        path.get('properties').forEach(propPath => {
            const func = propertyProcesses.get(propPath.node.type);
            let prop = func(propPath, statement);
            objectExpression.properties.push(prop);
        });

        expressionResult.objectExpressions.push(objectExpression);
    }
}

// interface ObjectProperty<: ObjectMember {
//     type: "ObjectProperty";
//     shorthand: boolean;
//     value: Expression;
// }

// Has a key property?
function parseObjectProperty(path, statement) {

    const node = path.node;

    if (node.shorthand) {
        console.log("Shorthand not implemented yet" + path.node.loc);
    }

    if (types.isIdentifier(path.get('key').node)) {

        const result = {
            key: path.get('key').toString(),
            value: null,
        };

        let propValueExpression = astProcessor.createBaseExpressionInfo(path.get('value'));
        expressions.processExpression(path.get('value'), propValueExpression, statement);

        if (types.isFunctionExpression(path.get('value'))) {
            result.value = propValueExpression.functionDeclarations[0];
        } else {
            result.value = propValueExpression;
        }

        return result;
    } else {
        throw "Object key is not an identifier: " + path.node.loc.startLine;
    }
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