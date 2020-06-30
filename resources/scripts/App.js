
const parser = require('./ast/Parser');
const fs = require('fs');
const content = fs.readFileSync('F:\\Research\\test\\vue.js', 'UTF-8');

parser.parse('function x() { var d = function namedExp() {};  if (d == \'1\') { d = 2; } function y(s) { } d = function() {}; }');
//parser.parse(content);
