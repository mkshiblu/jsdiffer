const processes = new Map([
    ['BinaryExpression', processBinaryExpression],
    ['Identifier', processIdentifier],
    ['NumericLiteral', processNumericLiteral],
]);

/**
 * Any expression node. 
 * Since the left-hand side of an assignment may be any expression in general, 
 * an expression can also be a pattern.
 * @param {*} node 
 */
function processExpression(path, expressionResult) {
    const node = path.node;
    const process = processes.get(path.node.type);
    if (process) {
        process(path, expressionResult);
        return expressionResult;
    } else {
        throw 'Processeor not implemented for : ' + path.node.type;
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
function processBinaryExpression(path, expressionResult) {
    const node = path.node;
    const left = node.left;
    const operator = node.operator;
    const right = node.right;
    expressionResult.binaryOperators.push(operator);
    processExpression(path.get('left'), expressionResult);
    processExpression(path.get('right'), expressionResult);
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

/**
 * interface NumericLiteral <: Literal {
  type: "NumericLiteral";
  value: number;
}
 * @param {*} path 
 */
function processNumericLiteral(path, { numericLiterals = [] }) {
    numericLiterals.push(path.node.value);
}

exports.processExpression = processExpression;