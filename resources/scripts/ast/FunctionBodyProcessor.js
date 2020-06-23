const { identifier } = require("@babel/types");

exports.processFunctionBody = function processFunctionBody(functionBody) {

    const nodes = functionBody.body;
    for (let i = 0; i < nodes.length; i++) {

        const node = nodes[i];

        switch (node.type) {
            case 'VariableDeclaration':
                processVariableDeclaration(node);
                break;
            case 'FunctionDeclaration':
                processFunctionDeclaration(node);
                break;
            case 'ExpressionStatement':
                processExpressionStatement(node);
                break;
            default:
                break;
        }
    }
}

function processVariableDeclaration(variableDeclarationNode) {
    const declarationNodes = variableDeclarationNode.declarations;
    for (let i = 0; i < declarationNodes.length; i++) {
        const declaration = declarationNodes[i];

        switch(declaration.type) {
            
            case 'VariableDeclarator':
                processsVariableDeclarator(declaration)
                break;
        }
    }
}


function processsVariableDeclarator(variableDeclarator) {
    
    let vd = {};
    switch(variableDeclarator.id.type){
        case 'Identifier':
            vd.name = variableDeclarator.id.name;
            break;
    }
}

function processFunctionDeclaration(functionDeclaration) {

}

function processExpressionStatement(expressionStatement) {

}
