import * as types from '@babel/types';
import { NodePath } from '@babel/traverse';
import * as declarationProcessor from './processors/DeclarationProcessor';

var nodePathProcesses = new Map([
  ['FunctionDeclaration', declarationProcessor.processFunctionDeclaration],
  //['VariableDeclaration', declarationProcessor.processVariableDeclaration],

  // ['IfStatement', choice.processIfStatement],
  // ['SwitchStatement', choice.processSwitchStatement],
  // ['SwitchCase', choice.processSwitchCase],

  // ['BlockStatement', statementProcessor.processBlockStatement],

  // ['ReturnStatement', controlFlowProcessor.processReturnStatement],
  // ['BreakStatement', controlFlowProcessor.processBreakStatement],
  // ['LabeledStatement', controlFlowProcessor.processLabeledStatement],
  // ['ContinueStatement', controlFlowProcessor.processContinueStatement],

  // ['EmptyStatement', statementProcessor.processEmptyStatement],
  // ['ExpressionStatement', statementProcessor.processExpressionStatement],

  // ['ForStatement', loopsProcessor.processForStatement],
  // ['ForInStatement', loopsProcessor.processForInStatement],
  // ['WhileStatement', loopsProcessor.processWhileStatement],
  // ['DoWhileStatement', loopsProcessor.processDoWhileStatement],

  // ['TryStatement', exceptions.processTryStatement],
  // ['ThrowStatement', exceptions.processThrowStatement],
]);

export function createProgramVisitor() {
  return {
    Program(path: NodePath, parent) {
      for (let childPath in path.get('body')) {
        process(childPath, parent);
      }
      path.skip();
    },
  };
}

function process(path: NodePath, parent) {
  const process = nodePathProcesses.get(path.node.type);
  process(path, parent);
}

function processFunctionDeclaration(path: NodePath, parent) {}

const containerVisitor = {
  Program(path, parent) {
    types.removeComments(path.node);
    path.skip();
  },

  Statement(path, parent) {
    types.removeComments(path.node);
    //nodeProcessors.processStatement(path, parent);
    path.skip();
  },
  Expression(path, parent) {
    types.removeComments(path.node);
    throw 'Expression not handled yet';
    path.skip();
  },
  FunctionDeclaration(path, parent) {
    types.removeComments(path.node);
    //  nodeProcessors.processStatement(path, parent);
    path.skip();
  },
  FunctionExpression(path, parent) {
    types.removeComments(path.node);
    throw 'Function Expression not handled yet';
    path.skip();
  },
};

export { containerVisitor };
