const astProcessor = require("../processors/AstNodeProcessor");

/** Process for a AST functionDeclarationNodePath */
exports.processFunctionDeclaration = (functionDeclarationPath, processStatement) => {
    const node = functionDeclarationPath.node;
    //const qualifiedName = astUtil.getFunctionQualifiedName(functionDeclarationPath);
    const name = node.id.name

    const statement = {
        type: node.type,
        //qualifiedName,
        name,
        params: node.params.map(id => id.name)
    };
    processStatement(functionDeclarationPath.get('body'), statement);
    return statement;
}

/**
* 
* interface VariableDeclaration <: Declaration {
*     type: "VariableDeclaration";
*    declarations: [ VariableDeclarator ];
*    kind: "var" | "let" | "const";
* }

interface VariableDeclarator <: Node {
  type: "VariableDeclarator";
  id: Pattern;
  init: Expression | null;
}
 * @param {*} path variableDeclaration path
 */
exports.processVariableDeclaration = (path) => {
    const node = path.node;
    const kind = node.kind;
    let initializer = {};

    // Extract initilaizer which is an expression
    const declarators = path.get("declarations");

    // TODO remove if no such things found
    if (declarators.length > 1) {
        throw "not supported yet multi-length initializers" + variableDeclarationPath;
    }

    const declaratorPath = declarators[0];
    const declaratorNode = declaratorPath.node;
    const variableName = declaratorNode.id.name;
    if (declaratorNode.init) {
        initializer = astProcessor.processExpression(declaratorPath.get("init"));
    }

    const statement = {
        type: node.type,
        text: path.toString(),
        identifiers: [variableName, ...initializer.identifiers],
        variableDeclarations: [{
            text: path.toString(),
            variableName,
            kind,
            initializer,
        }]
    };

    return statement;
}
