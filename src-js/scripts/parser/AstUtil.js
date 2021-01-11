exports.processfunctionParameters = (functionPath) => {
    const parameters = functionPath.get('params').map(path => {
        return {
            name: path.node.name,
            loc: getFormattedLocation(path.node)
        }
    });

    return parameters;
};

function getFormattedLocation(node) {
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

exports.getFormattedObjectCreation = (path) => {
    const objectCreation = {
        typeName: null,
        isInitializerEmptyArray: false,
        arguments: [],
        text: path.toString(),
        type: path.node.type,
        loc: getFormattedLocation(path.node)//getgetFormattedLocation(path.node)
    };

    return objectCreation;
}

/**
 * Mergess array properties from obj2 to obj1 and returns obj1
 */
exports.mergeArrayProperties = (obj, ...objectsToBeMerged) => {
    objectsToBeMerged.forEach((obj2) => {
        for (const property in obj2) {
            if (Array.isArray(obj2[property])) {
                obj[property] = [...obj2[property], ...obj[property] || [],];
            }
        }
    });

    return obj;
}


exports.getFormattedLocation = getFormattedLocation;
//exports.getFunctionNamespace = getFunctionNamespace;