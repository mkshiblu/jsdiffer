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
        console.log("Object Literals Not supported yet " + path.toString() + JSON.stringify(path.node.loc));
        // extract properties
        node.properties.forEach(prop => {

        });
    }
}

// interface ObjectProperty<: ObjectMember {
//     type: "ObjectProperty";
//     shorthand: boolean;
//     value: Expression;
// }
function parseObjectProperty(objectProperty) {

}

// interface ObjectMethod<: ObjectMember, Function {
//     type: "ObjectMethod";
//     kind: "get" | "set" | "method";
// }

function parseObjectMethod(objectMethod) {

}

exports.processObjectExpression = processObjectExpression;