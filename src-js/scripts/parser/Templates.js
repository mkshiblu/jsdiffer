const astUtil = require("./AstUtil");

exports.getStatementTemplate = (path) => {
    return {
        type: path.node.type,
        text: path.toString(),
        loc: {}//astUtil.getFormattedLocation(path.node),
    };
}