const babelParser = require('@babel/parser');
const generator = require('@babel/generator');

exports.parse = function parse(content, asJson = false) {
  const ast = parseAndMakeAst(content);

  return asJson ? JSON.stringify(ast) : ast;
};

function parseAndMakeAst(content) {
  return babelParser.parse(content, {
    sourceType: 'unambiguous',
    allowImportExportEverywhere: true,
    allowReturnOutsideFunction: true,
    plugins: [
      'jsx',
      'objectRestSpread',
      'exportDefaultFrom',
      'exportNamespaceFrom',
      'classProperties',
      'flow',
      'dynamicImport',
      'decorators-legacy',
      'optionalCatchBinding',
    ],
  });
}

exports.format = function format(node, appendSemicolon = false) {
  return generator.default(node, {
    comments: false,
    concise: true,
    shouldPrintComment : ()=> false,
  }).code;
};
