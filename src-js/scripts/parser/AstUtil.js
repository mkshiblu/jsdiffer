/**
 * Find the namespace of a function. For example, if A() is declared inside B() and C() is 
 * declared inside A(), it will return A.B for path of C()
 */
function getFunctionNamespace(functionDeclarationPath) {
    let namespace = '';
    let scope = functionDeclarationPath.scope.parent;
    while (scope.parent != null) {
        if (scope.block.id) {
            namespace += scope.block.id.name;
        } else {
            namespace += "$|$"; // TODO handle this
            // e.g; $|$$|$.get
            // if (inBrowser) {
            //     try {
            //         var opts = {};
            //         Object.defineProperty(opts, 'passive', ({
            //             get: function get() {
            //                 /* istanbul ignore next */
            //                 supportsPassive = true;
            //             }
            //         })); // https://github.com/facebook/flow/issues/285
            //         window.addEventListener('test-passive', null, opts);
            //     } catch (e) { }
            // }

        }
        scope = scope.parent;
    }
    return namespace == '' ? null : namespace;
}

exports.getFunctionQualifiedName = (functionDeclarationPath) => {
    // TODO Handle function Expressions when id could be null
    const name = functionDeclarationPath.node.id.name;
    const namespace = getFunctionNamespace(functionDeclarationPath);
    return namespace == null ? name : namespace + '.' + name;
}

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
    objectsToBeMerged.forEach((obj2)=> {
        for (const property in obj2) {
            if (Array.isArray(obj2[property])) {
                obj[property] = [ ...obj2[property], ...obj[property] || [],];
            }
        }
    });

    return obj;
}


exports.getFormattedLocation = getFormattedLocation;
exports.getFunctionNamespace = getFunctionNamespace;