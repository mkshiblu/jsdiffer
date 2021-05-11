const { parse } = require('@babel/parser');
const parser = require('./babel_parser');

let content = `let x,y,    z=1`;
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
const formattedCode = parser.format(ast, true);
//const formattedAst = parser.parse(formattedCode);
console.log(formattedCode);
