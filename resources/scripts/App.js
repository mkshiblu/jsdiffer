
const parser = require('./Parser');
const fs = require('fs');
const content = fs.readFileSync('F:\\Research\\test\\vue.js', 'UTF-8');

//parser.parse('function x() { var d = function namedExp() {}; function y(s) { } d = function() {}; }');
parser.parse(content);
