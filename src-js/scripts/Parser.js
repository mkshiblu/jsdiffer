const babelParser = require('@babel/parser');
const traverse = require('@babel/traverse');
const visitor = require('./parser/Visitor');
const templates = require('./parser/Templates');

function parse(script, asJson) {
    //  console.time('parse');
    const ast = babelParser.parse(script,
        {
            sourceType: 'unambiguous',
            allowImportExportEverywhere: true,
            allowReturnOutsideFunction: true,
            plugins:
                [
                    'jsx'
                    , 'objectRestSpread'
                    , 'exportDefaultFrom'
                    , 'exportNamespaceFrom'
                    , 'classProperties'
                    , 'flow'
                    , 'dynamicImport'
                    , 'decorators-legacy'
                    , 'optionalCatchBinding'
                ]
        });

    // Pass in the program model
    const blockCodeFragment = templates.getBaseCompositeTemplate();
    traverse.default(ast, visitor.containerVisitor, undefined, blockCodeFragment, undefined);
    return asJson ? JSON.stringify(blockCodeFragment) : blockCodeFragment;
};

module.exports.parse = parse;