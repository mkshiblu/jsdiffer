const parser = require('./scripts/Parser');
const types = require('@babel/types');

let content = `
return keyCodes.indexOf(eventKeyCode) === -1
`;
//content = require('fs').readFileSync('E:\\PROJECTS_REPO\\vue.js', 'UTF-8');
//content = require('fs').readFileSync('../resources/real-projects/vue/src1/vue_common.js', 'utf-8');
//content = require('fs').readFileSync('../test-resources/ExtractOrInlineFunction/src1/vue_runtime_common.js', 'utf-8');
const sourceModel = parser.parse(content);
const json = JSON.stringify(sourceModel);
require('fs').writeFileSync("E:\\functions.json", json);
console.log(json);
