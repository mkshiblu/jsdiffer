
const parser = require('./ast/Parser');
const fs = require('fs');
const content = fs.readFileSync('F:\\Research\\test\\vue.js', 'UTF-8');

parser.parse('function x() { var d = 1; if (d  == 1) { return 5; } else return 6; }');
//parser.parse(content);
