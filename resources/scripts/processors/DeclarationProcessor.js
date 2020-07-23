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

    // return {
    //     statement: {
    //         type: node.type,
    //         //qualifiedName,
    //         name,
    //         params: node.params.map(id => id.name)
    //     },
    //     bodyPaths = functionDeclarationPath.get('body')
    // };
    return statement;
}

exports.processVariableDeclaration = (variableDeclarationPath) => {
    const variableDeclaration = variableDeclarationPath.node;
    // const declarationNodes = variableDeclaration.declarations;
    // let temp = [];
    // for (let i = 0; i < declarationNodes.length; i++) {
    //     const declaration = declarationNodes[i];
    //     switch (declaration.type) {

    //         case 'VariableDeclarator':
    //             temp.push(variableDeclaratorProcessor.toStatement(declaration));
    //             break;
    //     }
    // }

    // //return temp;
    return {
        type: variableDeclaration.type,
        text: variableDeclarationPath.toString(),
    };
}