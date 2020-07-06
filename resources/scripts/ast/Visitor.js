const babelParser = require('@babel/parser');
const t = require('@babel/types');
const processor = require('./FunctionBodyProcessor');

const functionDeclarations = [];

// A visitor for FunctionDeclaration
function FunctionDeclarationVisitor(/*namespace*/) {

    this.FunctionDeclaration = (path) => {
        const fd = path.node;
        const statements = fd.body;
        const name = fd.id.name;
        const namespace = concatScopes(path);
        const qualifiedName = namespace == null ? name : namespace + '.' + name;

        // Pass the path instead of the body because path has the string repreentation?
        const funcBody = processor.processFunctionBody(fd.body)
        saveFunctionDeclaration(fd, qualifiedName);
    };

    this.FunctionExpression = (path) => {
        const fe = path.node;

        // If it's a named functionExpression process it as declaration since it is subject to rename
        if (fe.id != null) {
            const body = fe.body;
            const name = fe.id.name;
            const namespace = concatScopes(path);
            const qualifiedName = namespace == null ? name : namespace + '.' + name;
            saveFunctionDeclaration(fe, qualifiedName);
        } else {

            // This is an unmamed function expression. TODO handle
        }
    };

    // Could be a declaration or declaration expression
    this.Function = (path) => {

    }
};

function saveFunctionDeclaration(node, qualifiedName) {
    const loc = node.loc;
    functionDeclarations.push({
        qualifiedName: qualifiedName
        , body: JSON.stringify(node.body)
        , params: node.params.map(id => id.name)
        , location: {
            startLine: loc.start.line,
            startColumn: loc.start.column,
            endLine: loc.end.line,
            endColumn: loc.end.column
        }
    });
}

/**
 * Concats the function name with its parent scopes to 
 * create the namespace for the function
 * @param {Function Declaration node or a function expression node} fd 
 * TODO cache outer scopes so that whenver a leaf scope is found it can
 * automatically get the outer scopes chain
 */
function concatScopes(path) {
    let namespace = '';
    let scope = path.scope.parent;
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


//print("FunctCount: " + functionDeclarations.size());
exports.FunctionDeclarationVisitor = FunctionDeclarationVisitor;
exports.getFunctionDeclarations = () => functionDeclarations.filter(fd => !fd.qualifiedName.includes("$|$"));