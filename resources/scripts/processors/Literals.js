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

// interface NullLiteral <: Literal {
//   type: "NullLiteral";
// }
//  * @param {*} path
//  */
function processNullLiteral(path, { nullLiterals = [] }) {
  nullLiterals.push(path.toString());
}

// interface RegExpLiteral <: Literal {
//   type: "RegExpLiteral";
//   pattern: string;
//   flags: string;
// }

function processRegExpLiteral(path, { stringLiterals = [] }) {
  stringLiterals.push(path.toString());
}

exports.processRegExpLiteral = processRegExpLiteral;
exports.processStringLiteral = processStringLiteral;
exports.processNumericLiteral = processNumericLiteral;
exports.processNullLiteral = processNullLiteral;