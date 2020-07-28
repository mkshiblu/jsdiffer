const parser = require('./Parser');

const content ='function x() { var d = 1; if (d  == 1) { return 5; } function y(p) { }; return 6; }';
//content = require('fs').fs.readFileSync('E:\\PROJECTS_REPO\\vue.js', 'UTF-8');
parser.parse(content);
