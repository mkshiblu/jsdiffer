const astUtil = require("./AstUtil");

exports.getStatementTemplate = (path) => {
    return {
        type: path.node.type,
        text: path.toString(),
        loc: {}//astUtil.getFormattedLocation(path.node),
    };
}

/**
 * Returns the format of a container. If 
 * path is not provided the type is set as File
 */
exports.getBaseContainerTemplate = (path) => {
    return {
        type: path ? path.node.type : 'File',
        functionDeclarations: [],
        variableDeclarations: [],
        classDeclarations: [],
        compositeStatements: [],
    };
};

/**
 * Returns the format of a Block of Code. The type is set as
 * a Block Statement
 */
exports.getBaseCompositeTemplate = () => {
    return {
        type: 'BlockStatement',
        functionDeclarations: [],
        classDeclarations: [],
        statements: [],
    };
};