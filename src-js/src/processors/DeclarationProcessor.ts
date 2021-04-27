import { NodePath } from '@babel/traverse';
import { functionDeclaration } from '@babel/types';
import { Container, Fragment } from '../RmTypes';
import { createSourceLocation } from './AstInfoExtractor';

export function processFunctionDeclaration(
  path: NodePath,
  container: Container,
  parentFragment: Fragment,
) {
  const name = path.node.id.name;
  const location = createSourceLocation(path);

  const functonDeclaration = {
    name,
    loc: location,
    params: [],
    body: [],
  };

  container.registerFunctionDeclaration(functionDeclaration);
}

export function processClassDeclaration(
  path: NodePath,
  container: Container,
  parentFragment: Fragment,
) {}

export function processFunctionExpression(
  path: NodePath,
  container: Container,
  parentFragment: Fragment,
) {}

// const astProcessor = require("../processors/AstNodeProcessor");
// const astUtil = require("../parser/AstUtil");

// /** Process for a AST functionDeclarationNodePath */
// exports.processFunctionDeclaration = (path, processStatement) => {
//   const node = path.node;
//   //const qualifiedName = astUtil.getFunctionQualifiedName(functionDeclarationPath);
//   const name = node.id.name;

//   const statement = {
//     type: node.type,
//     text: path.toString(),
//     name,
//     params: astUtil.processfunctionParameters(path),
//   };
//   processStatement(path.get("body"), statement);
//   return statement;
// };

// /** Process for a AST class declaration */
// exports.classDeclaration = (path, processStatement) => {
//   const node = path.node;
//   const name = node.id.name;

//   const statement = {
//     type: node.type,
//     text: path.toString(),
//     name,
//     //params: astUtil.processfunctionParameters(path),
//   };
//   processStatement(path.get("body"), statement);
//   return statement;
// };

// /**
// *
// * interface VariableDeclaration <: Declaration {
// *     type: "VariableDeclaration";
// *    declarations: [ VariableDeclarator ];
// *    kind: "var" | "let" | "const";
// * }

// interface VariableDeclarator <: Node {
//   type: "VariableDeclarator";
//   id: Pattern;
//   init: Expression | null;
// }
//  * @param {*} path variableDeclaration path
//  */
// exports.processVariableDeclaration = (path) => {
//   const node = path.node;
//   const kind = node.kind;

//   const statement = {
//     type: node.type,
//     text: path.toString(),
//     identifiers: [],
//     // functionInvocations: [],
//     // objectCreations: []
//     variableDeclarations: [],
//   };

//   // Extract initilaizer which is an expression
//   const declarators = path.get("declarations");

//   // TODO remove if no such things found
//   declarators.forEach((declaratorPath) => {
//     const variableDeclaration = processVariableDeclarator(
//       declaratorPath,
//       kind,
//       statement
//     );
//     statement.variableDeclarations.push(variableDeclaration);
//     statement.identifiers.push(variableDeclaration.variableName);

//     // Add info from initializers
//     const initializer = variableDeclaration.initializer;
//     if (initializer) {
//       //statement.identifiers.push(...initializer.identifiers);
//       //statement.functionInvocations = initializer.functionInvocations;
//       //statement.objectCreations = initializer.objectCreations;
//       astUtil.mergeArrayProperties(statement, initializer);
//     }
//   });

//   return statement;
// };

// /**
//  * interface VariableDeclarator <: Node {
//   type: "VariableDeclarator";
//   id: Pattern;
//   init: Expression | null;
// }
//  * @param {declaratorPath} path
//  */
// function processVariableDeclarator(path, kind, statement) {
//   const declaratorNode = path.node;
//   const variableName = declaratorNode.id.name;

//   const variableDeclaration = {
//     kind,
//     variableName,
//     text: path.toString(),
//     type: "VariableDeclaration",
//     loc: astUtil.getFormattedLocation(path.node),
//   };

//   if (declaratorNode.init) {
//     initializer = astProcessor.processExpression(path.get("init"), statement);
//     variableDeclaration.initializer = initializer;
//   }

//   return variableDeclaration;
// }
