const { parse } = require('@babel/parser');
const parser = require('./babel_parser');

let content = `var m = function () {
  var t, z = 5;
};
`;

const ast = parser.parse(content);
const formattedCode = parser.format(ast, true);
//const formattedAst = parser.parse(formattedCode);
console.log(formattedCode);
