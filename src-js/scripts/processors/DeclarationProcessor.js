const astProcessor = require("../processors/AstNodeProcessor");
const astUtil = require('../parser/AstUtil');

/** Process for a AST functionDeclarationNodePath */
exports.processFunctionDeclaration = (path, processStatement) => {
    const node = path.node;
    //const qualifiedName = astUtil.getFunctionQualifiedName(functionDeclarationPath);
    const name = node.id.name

    const statement = {
        type: node.type,
        text: path.toString(),
        name,
        params: node.params.map(id => id.name)
    };
    processStatement(path.get('body'), statement);
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
    let initializer;

    const statement = {
        type: node.type,
        text: path.toString(),
        identifiers: [],
        // functionInvocations: [],
        // objectCreations: []
        variableDeclarations: []
    };

    // Extract initilaizer which is an expression
    const declarators = path.get("declarations");

    // TODO remove if no such things found
    declarators.forEach(declaratorPath => {
        const variableDeclaration = processVariableDeclarator(declaratorPath, kind, statement);
        statement.variableDeclarations.push(variableDeclaration);
        statement.identifiers.push(variableDeclaration.variableName);

        // Add info from initializers
        const initializer = variableDeclaration.initializer;
        if (initializer) {
            //statement.identifiers.push(...initializer.identifiers);
            //statement.functionInvocations = initializer.functionInvocations;
            //statement.objectCreations = initializer.objectCreations;
            astUtil.mergeArrayProperties(statement, initializer);
        }
    });

    return statement;
}

/**
 * interface VariableDeclarator <: Node {
  type: "VariableDeclarator";
  id: Pattern;
  init: Expression | null;
}
 * @param {declaratorPath} path 
 */
function processVariableDeclarator(path, kind, statement) {

    const declaratorNode = path.node;
    const variableName = declaratorNode.id.name;

    const variableDeclaration = {
        kind,
        variableName,
        text: path.toString(),
    };

    if (declaratorNode.init) {
        initializer = astProcessor.processExpression(path.get("init"), statement);
        variableDeclaration.initializer = initializer;
    }

    return variableDeclaration;
}