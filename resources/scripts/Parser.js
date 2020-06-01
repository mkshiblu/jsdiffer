const babelParser = require('@babel/parser');
const traverse = require('@babel/traverse');
const visitor = require('./ast/Visitor');

// const path = require('path');
// const content = 'var x;'

// const ast = babelParser.parse(content, {
//     sourceType: 'module',
//     plugins: ['jsx'],
// });

function parse(script) {
    
    const ast = babelParser.parse(script,
        {
            ranges: true,
            tokens: true,
            sourceType: 'unambiguous',
            allowImportExportEverywhere: true,
            allowReturnOutsideFunction: true,
            plugins: ['jsx', 'objectRestSpread', 'exportDefaultFrom', 'exportNamespaceFrom', 'classProperties', 'flow', 'dynamicImport', 'decorators-legacy', 'optionalCatchBinding']
        });

    traverse.default(ast, new visitor.FunctionDeclarationVisitor());
    const functionDeclarations = visitor.getFunctionDeclarations();

    return JSON.stringify(functionDeclarations);
};

parse('function x() { } ');

module.exports.parse = parse;