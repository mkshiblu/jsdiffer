const t = require('@babel/types');
const literals = require('./Literals');
const astUtil = require('../parser/AstUtil');
const objects = require('./ObjectExpression');
const processor = require('./AstNodeProcessor');

const processes = new Map([
  ['BinaryExpression', processBinaryExpression],
  ['LogicalExpression', processLogicalExpression],
  ['UnaryExpression', processUnaryExpression],
  ['Identifier', processIdentifier],

  ['NumericLiteral', literals.processNumericLiteral],
  ['StringLiteral', literals.processStringLiteral],
  ['NullLiteral', literals.processNullLiteral],
  ['RegExpLiteral', literals.processRegExpLiteral],
  ['BooleanLiteral', literals.processBooleanLiteral],

  ['FunctionExpression', processFunctionExpression],
  ['NewExpression', processNewExpression],
  ['CallExpression', processCallExpression],
  ['AssignmentExpression', processAssignmentExpression],
  ['MemberExpression', processMemberExpression],
  ['ArrayExpression', processArrayExpression],
  ['UpdateExpression', processUpdateExpression],
  ['ConditionalExpression', processConditionalExpression],
  ['ObjectExpression', objects.processObjectExpression],
  ['SequenceExpression', processSequenceExpression],
  ['ThisExpression', processThisExpression],
]);

/**
 * Any expression node.
 * Since the left-hand side of an assignment may be any expression in general,
 * an expression can also be a pattern.
 * @param {*} node
 */
function processExpression(path, expressionResult, statement) {
  t.removeComments(path.node);
  const process = processes.get(path.node.type);
  if (process) {
    expressionResult.loc = astUtil.getFormattedLocation(path.node);
    process(path, expressionResult, statement);
    return expressionResult;
  } else {
    if (!t.isTypeAlias(path) && !t.isTypeCastExpression(path))
      throw (
        'Processeor not implemented for : ' +
        String(path.node.type) +
        ' ' +
        String(path.toString())
      );
  }
}

// interface ArrayExpression<: Expression {
//     type: "ArrayExpression";
//     elements: [Expression | SpreadElement | null];
// }
function processArrayExpression(path, expressionResult, statement) {
  // This can also be like setting a value
  const isEmptyArrayCreation =
    path.node.elements && path.node.elements.length == 0;

  if (isEmptyArrayCreation) {
    const objectCreation = astUtil.getFormattedObjectCreation(path);
    //objectCreation.typeName = "EMPTY_ARRAY_LITERAL";
    objectCreation.isInitializerEmptyArray = true;
    expressionResult.objectCreations.push(objectCreation);
  } else {
    path.get('elements').forEach((elementPath) => {
      processExpression(elementPath, expressionResult, statement);
    });
  }

  return {
    type: path.node.type,
    text: path.toString(),
  };
}

/* interface CallExpression<: Expression {
    type: "CallExpression";
    callee: Expression | Super | Import;
    arguments: [Expression | SpreadElement];
    optional: boolean | null;
} */
function processCallExpression(path, expressionResult, statement) {
  const node = path.node;
  const callee = path.node.callee;
  let name;
  let expressionText;

  const result = {
    text: path.toString(),
    type: node.type,
    functionName: '',
    arguments: [],
    loc: astUtil.getFormattedLocation(node),
  };

  expressionResult.functionInvocations.push(result);

  if (t.isIdentifier(callee)) {
    name = callee.name;
  } else if (t.isMemberExpression(callee)) {
    // If the callee has expressions it could be a member expression (a[i].f() , a.f() etc.)
    name = callee.property.name;

    if (!t.isIdentifier(callee.object)) {
      //            console.log("non-identifier calllee" + String(path.toString()));
    }

    expressionText = path.get('callee').get('object').toString();
    processExpression(
      path.get('callee').get('object'),
      expressionResult,
      statement,
    );
    // Todo find chain method calls
    // TODO handle arguments
  } else if (t.isCallExpression(callee)) {
    //name =  callee.callee
    //console.log("Unsupported callee: " + node.loc);
    // TODO chain call
    processCallExpression(path.get('callee'), expressionResult, statement);
  } else if (t.isFunctionExpression(callee)) {
    processExpression(path.get('callee'), expressionResult, statement);
  } else {
    throw 'Unsupported callee: ' + String(path.toString());
  }

  result.functionName = name;

  if (expressionText) {
    result.expressionText = expressionText;
  }

  path.get('arguments').forEach((argumentPath) => {
    processArgument(argumentPath, statement);
    result.arguments.push(argumentPath.toString());
    processExpression(argumentPath, expressionResult, statement);
  });
}

/* interface NewExpression<: CallExpression {
    type: "NewExpression";
    optional: boolean | null;
} */
function processNewExpression(path, expressionResult, statement) {
  const node = path.node;
  const callee = node.callee;

  let name;
  let expressionText;

  if (t.isIdentifier(callee)) {
    name = node.callee.name;
  } else if (t.isMemberExpression(callee)) {
    // If the callee has expressions it could be a member expression (a[i].f() , a.f() etc.)
    name = callee.property.name;
    expressionText = path.get('callee').get('object').toString();
    processExpression(
      path.get('callee').get('object'),
      expressionResult,
      statement,
    );
    // Todo find chain method calls
    // TODO handle arguments
  } else if (t.isFunctionExpression(callee)) {
    // For handling nodes like
    // const PORTS = new function () {
    // }
    processExpression(path.get('callee'), expressionResult, statement);
  } else {
    throw 'Unsupported callee: ' + path.get('callee').toString();
  }

  const result = {
    typeName: name,
    arguments: [],
    text: path.toString(),
    type: node.type,
    loc: astUtil.getFormattedLocation(path.node),
  };

  if (expressionText) {
    result.expressionText = expressionText;
  }

  path.get('arguments').forEach((argumentPath) => {
    processArgument(argumentPath, statement);
    result.arguments.push(argumentPath.toString());
    processExpression(argumentPath, expressionResult, statement);
    // if (t.isIdentifier(argument)) {
    //     result.arguments.push(argument.name)
    // } else if (t.isStringLiteral(argument)) {
    //     result.arguments.push(argument.value);
    // } else {
    //     throw "Unsupported argument type : " + argument.type;
    // }
  });

  expressionResult.objectCreations.push(result);
}

// TODO remove duplication in newexp and callexp and check arguments type
function processArgument(argumentPath, statement) {
  if (
    (statement &&
      (t.isCallExpression(argumentPath.node) ||
        t.isNewExpression(argumentPath.node) ||
        t.isIdentifier(argumentPath.node))) ||
    t.isMemberExpression(argumentPath.node) ||
    t.isLiteral(argumentPath.node) ||
    t.isObjectExpression(argumentPath.node) ||
    t.isFunction(argumentPath.node) ||
    t.isClass(argumentPath.node)
  ) {
    return;
  }

  if (statement) {
    if (!statement.arguments) statement.arguments = [];

    statement.arguments.push(argumentPath.toString());
  }
}

/**
interface BinaryExpression<: Expression {
    type: "BinaryExpression";
    operator: BinaryOperator;
    left: Expression;
    right: Expression;
}
 */
function processBinaryExpression(path, expressionResult, statement) {
  const node = path.node;
  const left = node.left;
  const operator = node.operator;
  const right = node.right;
  expressionResult.infixOperators.push(operator);
  processExpression(path.get('left'), expressionResult, statement);
  processExpression(path.get('right'), expressionResult, statement);
}

// LogicalExpression
// interface LogicalExpression <: Expression {
//   type: "LogicalExpression";
//   operator: LogicalOperator;
//   left: Expression;
//   right: Expression;
// }
// A logical operator expression.

// LogicalOperator
// enum LogicalOperator {
//   "||" | "&&" | "??"
// }
// A logical operator token.
function processLogicalExpression(path, expressionResult, statement) {
  const node = path.node;
  const operator = node.operator;
  expressionResult.infixOperators.push(operator);
  processExpression(path.get('left'), expressionResult, statement);
  processExpression(path.get('right'), expressionResult, statement);
}

// interface ThisExpression<: Expression {
//     type: "ThisExpression";
// }

function processThisExpression(path, expressionResult, statement) {
  const node = path.node;
  // TODO how to determine if this is used as a params
  if (path.toString() !== 'this') {
    throw 'Not supported yet: ' + path.toString();
  }
  expressionResult.identifiers.push('this');
}

// interface UnaryExpression <: Expression {
//     type: "UnaryExpression";
//     operator: UnaryOperator;
//     prefix: boolean;
//     argument: Expression;
//   }
function processUnaryExpression(path, expressionResult, statement) {
  const node = path.node;
  const isPrefix = node.prefix;
  const operator = node.operator;

  if (isPrefix) {
    expressionResult.prefixExpressions.push(path.toString());
  } else {
    expressionResult.postfixExpressions.push(path.toString());
  }
  processExpression(path.get('argument'), expressionResult, statement);
}

// interface ConditionalExpression<: Expression {
//     type: "ConditionalExpression";
//     test: Expression;
//     alternate: Expression;
//     consequent: Expression;
// }
// A conditional expression, i.e., a ternary ? /: expression.

function processConditionalExpression(path, expressionResult, statement) {
  const node = path.node;

  const test = processor.processExpression(path.get('test'), statement);
  const consequent = processor.processExpression(
    path.get('consequent'),
    statement,
  );
  const alternate = processor.processExpression(
    path.get('alternate'),
    statement,
  );

  const ternaryExpression = {
    text: path.toString(),
    condition: test,
    then: consequent,
    else: alternate,
  };

  astUtil.mergeArrayProperties(expressionResult, test, consequent, alternate);
  expressionResult.ternaryExpressions.push(ternaryExpression);
}

/* interface AssignmentExpression<: Expression {
    type: "AssignmentExpression";
    operator: AssignmentOperator;
    left: Pattern | Expression;
    right: Expression;
}
An assignment operator expression.
 
    AssignmentOperator
enum AssignmentOperator {
    "=" | "+=" | "-=" | "*=" | "/=" | "%="
        | "<<=" | ">>=" | ">>>="
        | "|=" | "^=" | "&="
}
An assignment operator token. */
function processAssignmentExpression(path, expressionResult, statement) {
  const node = path.node;
  const operator = node.operator;
  expressionResult.infixOperators.push(operator);
  processExpression(path.get('left'), expressionResult, statement);
  processExpression(path.get('right'), expressionResult, statement);
}

/* interface MemberExpression<: Expression, Pattern {
    type: "MemberExpression";
    object: Expression | Super;
    property: Expression;
    computed: boolean;
    optional: boolean | null;
}
A member expression.If computed is true, the node corresponds to a computed(a[b])
 member expression and property is an Expression.If computed is false, the node 
 corresponds to a static(a.b) member expression and property is an Identifier.
 The optional flags indicates that the member expression can be called even if 
 the object is null or undefined.If this is the object value(null / undefined) 
 should be returned. */
function processMemberExpression(path, expressionResult, statement) {
  const node = path.node;
  if (node.computed) {
    //console.log("Member is computed" + String(path.toString()));
  }

  processExpression(path.get('object'), expressionResult, statement);
  processIdentifier(path.get('property'), expressionResult, statement);
}

/**
 * interface Identifier <: Expression, Pattern {
  type: "Identifier";
  name: string;
}
An identifier. Note that an identifier may be an expression or a destructuring pattern
 * @param {*} path 
 */
function processIdentifier(path, { identifiers = [] }) {
  const name = path.node.name;
  identifiers.push(name);
}

// interface SequenceExpression <: Expression {
//     type: "SequenceExpression";
//     expressions: [ Expression ];
//   }

function processSequenceExpression(path, expressionResult, statement) {
  const name = path.node.name;

  path.get('expressions').forEach((expressionPath) => {
    processExpression(expressionPath, expressionResult, statement);
  });
}

/** An ++ or -- after or befor and expression */
// interface UpdateExpression <: Expression {
//     type: "UpdateExpression";
//     operator: UpdateOperator;
//     argument: Expression;
//     prefix: boolean;
//   }

function processUpdateExpression(path, expressionResult, statement) {
  const node = path.node;

  // Extract operator (++/ --)
  if (node.prefix) {
    expressionResult.prefixExpressions.push(node.operator);
  } else {
    expressionResult.postfixExpressions.push(node.operator);
  }
  processExpression(path.get('argument'), expressionResult, statement);
}

// interface FunctionExpression <: Function, Expression {
//     type: "FunctionExpression";
//   }
//   A function expression.

// function [name]([param1[, param2[, ..., paramN]]]) {
//     statements
//  }

function processFunctionExpression(path, expressionResult, statement) {
  const node = path.node;

  // Body is a block statmeent
  const name = node.id ? node.id.name : undefined;
  const functionDeclarationStatement = {
    type: node.type,
    name,
    text: path.toString(),
    params: astUtil.processfunctionParameters(path),
    loc: astUtil.getFormattedLocation(path.node),
  };

  processor.processStatement(path.get('body'), functionDeclarationStatement);

  expressionResult.functionDeclarations = [functionDeclarationStatement];
}

exports.processExpression = processExpression;
