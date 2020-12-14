const babelParser = require('@babel/parser');
const traverse = require('@babel/traverse');
const visitor = require('./parser/Visitor');
const templates = require('./parser/Templates');

function parse(script) {
    //  console.time('parse');
    const ast = babelParser.parse(script,
        {
            sourceType: 'unambiguous',
            allowImportExportEverywhere: true,
            allowReturnOutsideFunction: true,
            plugins: ['jsx', 'objectRestSpread', 'exportDefaultFrom', 'exportNamespaceFrom', 'classProperties', 'flow', 'dynamicImport', 'decorators-legacy', 'optionalCatchBinding']
        });
    // Pass in the program model
    const container = templates.getBaseContainerTemplate();
    traverse.default(ast, visitor.containerVisitor, undefined, container, undefined);
    traverse.default(ast, visitor.Visitor);
    const functionDeclarations = visitor.getFunctionDeclarations();
    visitor.clearFunctionDeclarations();
    //    console.timeEnd('parse');
    // return JSON.stringify(functionDeclarations);
    return functionDeclarations;
};

module.exports.parse = parse;