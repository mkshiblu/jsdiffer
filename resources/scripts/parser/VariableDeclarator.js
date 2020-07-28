const { variableDeclaration } = require("@babel/types");
const traverse = require('@babel/traverse');

exports.toStatement = function toStatement(variableDeclarator) {
    // Should return a string representation of
    let vd = {};
    switch (variableDeclarator.id.type) {
        case 'Identifier':
            vd.name = variableDeclarator.id.name;
            vd.json = vd.name;
            if (variableDeclarator.init) {
                vd.init = processInit(variableDeclarator.init);
                vd.json += " = " + vd.init;
            }
            break;
        default:
            console.log("Cannot process: " + variableDeclaration);
            break;
    }

    return vd;
}

function processInit(init) {
        
//const x = traverse.default(init);

    let initStatement = {};
    switch (init.type) {
        case 'FunctionExpression':
            initStatement = init.id.name + "()";
            break;
        case 'NumericLiteral':
            initStatement = init.value;
        default:
            console.log("Cannot process: " + init);
            break;
    }

    return initStatement;
}