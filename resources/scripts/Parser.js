const babelParser = require('@babel/parser');
const traverse = require('@babel/traverse');
const visitor = require('./parser/Visitor');

function parse(script) {
    console.time('parse');
    const ast = babelParser.parse(script,
        {
            sourceType: 'unambiguous',
            allowImportExportEverywhere: true,
            allowReturnOutsideFunction: true,
            plugins: ['jsx', 'objectRestSpread', 'exportDefaultFrom', 'exportNamespaceFrom', 'classProperties', 'flow', 'dynamicImport', 'decorators-legacy', 'optionalCatchBinding']
        });

    console.timeEnd('parse');
    
    traverse.default(ast, new visitor.Visitor());
    const functionDeclarations = visitor.getFunctionDeclarations();

    // return JSON.stringify(functionDeclarations);
    return functionDeclarations;
};

module.exports.parse = parse;