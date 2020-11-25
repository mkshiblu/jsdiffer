// /**
// interface BinaryExpression<: Expression {
//     type: "BinaryExpression";
//     operator: BinaryOperator;
//     left: Expression;
//     right: Expression;
// }
//  */
// function processBinaryExpression(path, expressionResult, statement) {
//     const node = path.node;
//     const left = node.left;
//     const operator = node.operator;
//     const right = node.right;
//     expressionResult.infixOperators.push(operator);
//     processExpression(path.get('left'), expressionResult, statement);
//     processExpression(path.get('right'), expressionResult, statement);
// }