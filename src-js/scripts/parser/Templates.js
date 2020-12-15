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
        //variableDeclarations: [],
        classDeclarations: [],
        //leafStatements: [],
        statements: [],
    };
};

exports.getBaseExpressionInfo = (path) => {
    return {
        text: path.toString(),
        identifiers: [],
        numericLiterals: [],
        stringLiterals: [],
        nullLiterals: [],
        booleanLiterals: [],
        infixOperators: [],
        prefixOperators: [],
        postfixOperators: [],
        variableDeclarations: [],
        functionInvocations: [],
        constructorInvocations: [],
        objectCreations: [],
        functionExpressions: [],
        functionDeclarations: [],
        arguments: [],
        loc: {}
    };
};