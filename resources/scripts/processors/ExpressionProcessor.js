/**
 * Any expression node. 
 * Since the left-hand side of an assignment may be any expression in general, 
 * an expression can also be a pattern.
 * @param {*} node 
 */
function processExpression(node) {
    switch (condition.type) {
        case 'BinaryExpression':
            const left = condition.left;
            const operator = condition.operator;
            const right = condition.right;
            processExpression(right);
            break;
    }
}

exports.processExpression = processExpression;