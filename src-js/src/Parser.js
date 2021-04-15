const babelParser = require('@babel/parser');
const traverse = require('@babel/traverse');
const visitor = require('./parser/Visitor');
const templates = require('./parser/Templates');

function parse(script, asJson) {
     console.time("parse");
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

        console.timeEnd("parse");
    // Pass in the program model
    const blockCodeFragment = templates.getBaseCompositeTemplate();
    traverse.default(ast, visitor.containerVisitor, undefined, blockCodeFragment, undefined);

    console.time("JSON.stringify");
    const result =asJson ? JSON.stringify(blockCodeFragment) : blockCodeFragment;
    console.timeEnd("JSON.stringify");

    return result;
};

module.exports.parse = parse;