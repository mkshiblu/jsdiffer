exports.processReturnStatement = function (path) {
    return {
        type: path.node.type,
        text: path.toString()
    }
}