const { parse } = require('@babel/parser');
const parser = require('./babel_parser');

let content = `let x = /*fdf**/1;`;
// let content = `
// //dsa
// function f1(){

// return /*dsad*/ keyCodes.indexOf(  eventKeyCode) ===   -1
// }
// ()=> 'hy';

// (function(){

// })()
// var d = function x(){

// }

// `;

const ast = parser.parse(content);
//const formattedCode = parser.formatNode(ast, true);
//const formattedAst = parser.parse(formattedCode);
console.log(formattedAst.code);
