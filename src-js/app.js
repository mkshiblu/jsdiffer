const { parse } = require('@babel/parser');
const parser = require('./src/Parser');

let content = `
//dsa
function f1(){

return /*dsad*/ keyCodes.indexOf(  eventKeyCode) ===   -1
}
()=> 'hy';



(function(){

})()
var d = function x(){
    
}


`;

const ast = parser.parseAndMakeAst(content);
const code = parser.formatNode(ast);
console.log(code);
