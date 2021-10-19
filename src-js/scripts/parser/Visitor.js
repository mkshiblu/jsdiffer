const babelParser = require('@babel/parser');
const types = require('@babel/types');
const processor = require('./FunctionBodyProcessor');
const astUtil = require('./AstUtil');
const nodeProcessors = require('../processors/AstNodeProcessor');

const containerVisitor = {
  Statement(path, parent) {
    types.removeComments(path.node);
    nodeProcessors.processStatement(path, parent);
    path.skip();
  },
  Expression(path, parent) {
    types.removeComments(path.node);
    throw 'Expression not handled yet';
    path.skip();
  },
  FunctionDeclaration(path, parent) {
    types.removeComments(path.node);
    nodeProcessors.processStatement(path, parent);
    path.skip();
  },
  FunctionExpression(path, parent) {
    types.removeComments(path.node);
    throw 'Function Expression not handled yet';
    path.skip();
  },
};
exports.containerVisitor = containerVisitor;
exports.getFunctionDeclarations = () =>
  functionDeclarations.filter((fd) => fd /*!fd.qualifiedName.includes("$|$")*/);
exports.clearFunctionDeclarations = () => (functionDeclarations.length = 0);
