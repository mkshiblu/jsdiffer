const babelParser = require('@babel/parser');
//const fs = require('fs');
const t = require('@babel/types');
//const path = require('path');

const functionDeclarations = [];

// A visitor for FunctionDeclaration
function FunctionDeclarationVisitor(/*namespace*/) {

    this.FunctionDeclaration = (path) => {
        const fd = path.node;
        const statements = fd.body;
        const name = fd.id.name;
        //const fullyQualifiedName = namespace + "." + fd.id.name;
        // path.skip();
        //const

        concatScopes(path);
        saveFunctionDeclaration(fd);
    };

    this.FunctionExpression = (path) => {
        const fd = path.node;

        concatScopes(path);
        saveFunctionDeclaration(fd);
    };
};

function saveFunctionDeclaration(node) {
    const loc = node.loc;
    functionDeclarations.push({
        name: node.id.name, body: node.body
        , location: {
            startLine: loc.start.line,
            startColumn: loc.start.column,
            endLine: loc.end.line,
            endColumn: loc.end.column
        }
    });
}

/**
 * Concats the function name with its parent scopes to 
 * create the namespace for the function
 * @param {Function Declaration node or a function expression node} fd 
 * TODO cache outer scopes so that whenver a leaf scope is found it can
 * automatically get the outer scopes chain
 */
function concatScopes(path) {
    let namespace = '';
    let scope = path.scope.parent;
    while (scope.parent != null) {
        namespace += scope.block.id.name;
        scope = scope.parent;
    }
    return scope;
}

exports.FunctionDeclarationVisitor = FunctionDeclarationVisitor;
exports.getFunctionDeclarations = () => functionDeclarations;