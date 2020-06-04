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
        saveFunctionDeclaration(fd);
    };

    this.FunctionExpression = (path) => {
        const fd = path.node;
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

exports.FunctionDeclarationVisitor = FunctionDeclarationVisitor;
exports.getFunctionDeclarations = () => functionDeclarations;