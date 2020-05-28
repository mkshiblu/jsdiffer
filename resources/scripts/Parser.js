const babelParser = require('@babel/parser');
// const traverse = require('@babel/traverse');
// const path = require('path');
// const content = 'var x;'

// const ast = babelParser.parse(content, {
//     sourceType: 'module',
//     plugins: ['jsx'],
// });

module.exports.parse = function parse(script) {
    return JSON.stringify(
            babelParser.parse(script,
            {
                ranges: true,
                tokens: true,
                sourceType: 'unambiguous',
                allowImportExportEverywhere: true,
                allowReturnOutsideFunction: true,
                plugins: ['jsx', 'objectRestSpread', 'exportDefaultFrom', 'exportNamespaceFrom', 'classProperties', 'flow', 'dynamicImport', 'decorators-legacy', 'optionalCatchBinding']
            })
        );
};

//
//const root = "res";
//const uniqueFilePath = "res." + path.parse(filePath).name;
//traverse.default(ast, new visitor.FunctionDeclarationVisitor(uniqueFilePath));
//
//functionDeclarations.forEach(function (element) {
//    printAsJson(element);
//});
//
//traverse.default(ast, new f.FunctionDeclarationVisitor(uniqueFilePath, functionDeclarations));
//
//function printAsJson(json) {
//    console.log(JSON.stringify(json));
//}
//
//const functionInvocation = functionInvocation.getStateSetterInvocations();
//functionInvocation.forEach(function (element) {
//    printAsJson(element);
//});
