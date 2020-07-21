
const parser = require('./ast/Parser');
const fs = require('fs');
const content = fs.readFileSync('E:\\PROJECTS_REPO\\vue.js', 'UTF-8');

parser.parse('function x() { var d = 1; if (d  == 1) { return 5; } return 6; }');
//parser.parse(content);
