const { parse } = require('@babel/parser');
const parser = require('./babel_parser');

let content = `vnode.isRootInsert = !nested;
`;

const ast = parser.parse(content);
const formattedCode = parser.format(ast, true);
//const formattedAst = parser.parse(formattedCode);
console.log(formattedCode);
