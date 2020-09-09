/**
 * interface NumericLiteral <: Literal {
  type: "NumericLiteral";
  value: number;
}
 * @param {*} path
 */
function processNumericLiteral(path, { numericLiterals = [] }) {
    numericLiterals.push(path.toString());
}
exports.processNumericLiteral = processNumericLiteral;
/**
 * interface NumericLiteral <: Literal {
  type: "NumericLiteral";
  value: number;
}
 * @param {*} path
 */
function processStringLiteral(path, { stringLiterals = [] }) {
    stringLiterals.push(path.toString());
}
exports.processStringLiteral = processStringLiteral;
