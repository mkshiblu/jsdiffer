const parser = require('./babel_parser');

let content = 'var x = function() {  }';
const ast = parser.parse(content);
const formattedCode = parser.format(ast, true);
const formattedAst = parser.parse(formattedCode);
console.log(formattedAst);
