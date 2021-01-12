/**
 * Returns the text for a composite by appending the expressions and its name
 * @param  compositeStatement a filled up composite statement in json format
 */
exports.getTextWithExpressions = function (compositeStatement) {
    if (compositeStatement.expressions.length > 0) {
        const expressionsText = compositeStatement.expressions.map(expression => expression.text);
        return compositeStatement.text + "(" + expressionsText.join(',') + ")";
    }
    return compositeStatement.text;
}