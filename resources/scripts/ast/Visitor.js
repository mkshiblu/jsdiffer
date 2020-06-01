const babelParser = require('@babel/parser');
//const fs = require('fs');
const t = require('@babel/types');
//const path = require('path');

const functionDeclarations = [];

// A visitor for FunctionDeclaration
function FunctionDeclarationVisitor(/*namespace*/) {

    this.FunctionDeclaration = (path, ars) => {
        const fd = path.node;
        const statements = fd.body;
        const name = fd.id.name;
        //const fullyQualifiedName = namespace + "." + fd.id.name;
      //  path.get('body').traverse(new StateDeclarationVisitor(fullyQualifiedName));
       // path.skip();
        saveFunctionDeclaration(fd);
    };
};

function saveFunctionDeclaration(node) {
    functionDeclarations.push({ name: node.id.name, body: node.body, loc: node.loc });
}

exports.FunctionDeclarationVisitor = FunctionDeclarationVisitor;
exports.getFunctionDeclarations = () => functionDeclarations;