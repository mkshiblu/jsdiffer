const babelParser = require('@babel/parser');
const traverse = require('@babel/traverse');
const visitor = require('./ast/Visitor');

function parse(script) {
    
    const ast = babelParser.parse(script,
        {
            sourceType: 'unambiguous',
            allowImportExportEverywhere: true,
            allowReturnOutsideFunction: true,
            plugins: ['jsx', 'objectRestSpread', 'exportDefaultFrom', 'exportNamespaceFrom', 'classProperties', 'flow', 'dynamicImport', 'decorators-legacy', 'optionalCatchBinding']
        });

    traverse.default(ast, new visitor.FunctionDeclarationVisitor());
    const functionDeclarations = visitor.getFunctionDeclarations();

    return JSON.stringify(functionDeclarations);
};

//parse('function x() { var d = function namedExp() {}; function y(s) { } d = function() {}; }');

module.exports.parse = parse;