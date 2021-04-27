import * as parser from './src/Parser';

let content = `
//dsa
function f1(){

return /*dsad*/ keyCodes.indexOf(eventKeyCode) === -1
}
()=> 'hy';

(function(){

})()
var d = function x(){
    
}


`;

parser.parse(content);
console.log('hi');
// //content = require('fs').readFileSync('E:\\PROJECTS_REPO\\vue.js', 'UTF-8');
// //content = require('fs').readFileSync('../resources/real-projects/vue/src1/vue_common.js', 'utf-8');
// //content = require('fs').readFileSync('../test-resources/ExtractOrInlineFunction/src1/vue_runtime_common.js', 'utf-8');
// const sourceModel = parse(content);
// const json = JSON.stringify(sourceModel);
// console.log(json);

// require('child_process').child_process.exec(
//   'compilesrc.cmd',
//   function (error, stdout, stderr) {
//     console.log(stdout);
//   },
// );
