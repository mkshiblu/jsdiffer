exports.getFormattedLocation = function getFormattedLocation(node) {
    const sourceLocation = node.loc;
    return {
        start: node.start,
        end: node.end,

        startLine: sourceLocation.start.line,
        endLine: sourceLocation.end.line,

        startColumn: sourceLocation.start.column,
        endColumn: sourceLocation.end.column,
    }
}