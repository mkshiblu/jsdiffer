const processes = new Map([
    ['BinaryExpression', processBinaryExpression],
    ['Identifier', processIdentifier],
]);

/**
 * Any expression node. 
 * Since the left-hand side of an assignment may be any expression in general, 
 * an expression can also be a pattern.
 * @param {*} node 
 */
function processExpression(path) {
    const node = path.node;
    const process = processes.get(nodePath.node.type);
    if (process) {
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
function processBinaryExpression(path) {
    const node = path.node;
    const left = node.left;
    const operator = node.operator;
    const right = node.right;
    processExpression(left);
    processExpression(right);
}

/**
 * interface Identifier <: Expression, Pattern {
  type: "Identifier";
  name: string;
}
An identifier. Note that an identifier may be an expression or a destructuring pattern
 * @param {*} path 
 */
function processIdentifier(path) {
    const name = path.node.name;
}

exports.processExpression = processExpression;