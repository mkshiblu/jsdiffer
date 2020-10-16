const parser = require('./Parser');

let content ='function x() { var d = 1; if (d  == 1) { return 5; } function y(p) { }; return 6; }';
//content = require('fs').readFileSync('E:\\PROJECTS_REPO\\vue.js', 'UTF-8');
content = require('fs').readFileSync('E:\\PROJECTS_REPO\\toy_js\\HazelCast.js', 'utf-8');
const sourceModel = parser.parse(content);
const json = JSON.stringify(sourceModel);
console.log(json);