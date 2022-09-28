//42e622b751d1ad1520b824473f9ad31e7efb75b3 angular

var ARROW_ARG = /^([^(]+?)=>/;
var FN_ARGS = /^[^(]*\(\s*([^)]*)\)/m;
var STRIP_COMMENTS = /((\/\/.*$)|(\/\*[\s\S]*?\*\/))/mg;

function extractArgs(fn) {
  var fnText = Function.prototype.toString.call(fn).replace(STRIP_COMMENTS, ''),
      args = fnText.match(ARROW_ARG) || fnText.match(FN_ARGS);
  return args;
}