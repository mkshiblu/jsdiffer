const { parse } = require('@babel/parser');

const generator = require('@babel/generator');
const parser = require('./babel_parser');

let content = `
var x = new Function () {  };
`;

const ast = parser.parse(content);
const formattedCode = parser.format(ast, true);
//const formattedAst = parser.parse(formattedCode);
console.log(formattedCode);

exports.format = function format(node, appendSemicolon = false) {
  return generator.default(node, {
    comments: false,
    concise: true,
    //sourceFileName: 'dsa',

    //sourceMaps: true,
    //retainLines: true,
  }).code;
};
