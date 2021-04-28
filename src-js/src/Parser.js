const babelParser = require('@babel/parser');
const generator = require('@babel/generator');

exports.parseAndMakeAst = function parseAndMakeAst(content) {
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
};

exports.formatNode = function format(node) {
  return generator.default(node, {
    comments: false,
    //concise: true,
  }).code;
};
