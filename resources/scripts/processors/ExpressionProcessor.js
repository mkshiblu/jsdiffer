const t = require('@babel/types');
const literals = require("./Literals");
const astUtil = require('../parser/AstUtil');

const processes = new Map([
    ['BinaryExpression', processBinaryExpression],
    ['Identifier', processIdentifier],
    ['NumericLiteral', literals.processNumericLiteral],
    ['StringLiteral', literals.processStringLiteral],
    ['NewExpression', processNewExpression],
    ['CallExpression', processCallExpression],
    ['AssignmentExpression', processAssignmentExpression],
    ['MemberExpression', processMemberExpression],
    ['ArrayExpression', processArrayExpression],
    ['UpdateExpression', processUpdateExpression],
]);

/**
 * Any expression node. 
 * Since the left-hand side of an assignment may be any expression in general, 
 * an expression can also be a pattern.
 * @param {*} node 
 */
function processExpression(path, expressionResult, statement) {
    const node = path.node;
    const process = processes.get(path.node.type);
    if (process) {
        expressionResult.loc = astUtil.getFormattedLocation(path.node);
        process(path, expressionResult, statement);
        return expressionResult;
    } else {
        throw 'Processeor not implemented for : ' + path.node.type;
    }
}

function processArrayExpression(path, expressionResult) {
    // This can also be like setting a value
    const isEmptyArrayCreation = path.node.elements && path.node.elements.length == 0;

    if (isEmptyArrayCreation) {
        const objectCreation = astUtil.getFormattedObjectCreation(path);
        //objectCreation.typeName = "EMPTY_ARRAY_LITERAL";
        objectCreation.isInitializerEmptyArray = true;
        expressionResult.objectCreations.push(objectCreation);
    }

    return {
        type: path.node.type,
        text: path.toString()
    }
}

/* interface CallExpression<: Expression {
    type: "CallExpression";
    callee: Expression | Super | Import;
    arguments: [Expression | SpreadElement];
    optional: boolean | null;
} */
function processCallExpression(path, expressionResult, statement) {
    const node = path.node;
    const callee = path.node.callee;
    let name;
    let expressionText;

    if (t.isIdentifier(callee)) {
        name = callee.name;
    } else if (t.isMemberExpression(callee)) {
        // If the callee has expressions it could be a member expression (a[i].f() , a.f() etc.)
        name = callee.property.name;
        expressionText = path.get('callee').get('object').toString();
        processExpression(path.get('callee').get('object'), expressionResult, statement);
        // Todo find chain method calls
        // TODO handle arguments
    } else {
        throw "Unsupported callee: " + node.callee.type;
    }

    const result = {
        text: path.toString(),
        type: node.type,
        functionName: name,
        arguments: [],
        loc: astUtil.getFormattedLocation(path.node)
    };


    if (expressionText) {
        result.expressionText = expressionText;
    }

    path.get('arguments')
        .forEach((argumentPath) => {
            processArgument(argumentPath, statement);
            result.arguments.push(argumentPath.toString());
            processExpression(argumentPath, expressionResult, statement);
        });

    expressionResult.functionInvocations.push(result);
}

/* interface NewExpression<: CallExpression {
    type: "NewExpression";
    optional: boolean | null;
} */
function processNewExpression(path, expressionResult, statement) {

    const node = path.node;
    let name;
    let expressionText;

    if (t.isIdentifier(node.callee)) {
        name = node.callee.name;
    } else if (t.isMemberExpression(callee)) {
        // If the callee has expressions it could be a member expression (a[i].f() , a.f() etc.)
        name = callee.property.name;
        expressionText = path.get('callee').get('object').toString();
        processExpression(path.get('callee').get('object'), expressionResult, statement);
        // Todo find chain method calls
        // TODO handle arguments
    } else {
        throw "Unsupported callee: " + node.callee.type;
    }

    const result = {
        typeName: name,
        arguments: [],
        text: path.toString(),
        type: node.type,
        loc: astUtil.getFormattedLocation(path.node)
    };

    if (expressionText) {
        result.expressionText = expressionText;
    }

    path.get('arguments')
        .forEach((argumentPath) => {
            processArgument(argumentPath, statement);
            result.arguments.push(argumentPath.toString());
            processExpression(argumentPath, expressionResult, statement);
            // if (t.isIdentifier(argument)) {
            //     result.arguments.push(argument.name)
            // } else if (t.isStringLiteral(argument)) {
            //     result.arguments.push(argument.value);
            // } else {
            //     throw "Unsupported argument type : " + argument.type;
            // }

        });

    expressionResult.objectCreations.push(result);
}

// TODO remove duplication in newexp and callexp and check arguments type
function processArgument(argumentPath, statement) {
    if (statement && (t.isCallExpression(argumentPath.node) || t.isNewExpression(argumentPath.node)
        || t.isIdentifier(argumentPath.node))) {
        if (!statement.argumentsWithIdentifier)
            statement.argumentsWithIdentifier = [];

        statement.argumentsWithIdentifier.push(argumentPath.toString());
    }
}


/**
interface BinaryExpression<: Expression {
    type: "BinaryExpression";
    operator: BinaryOperator;
    left: Expression;
    right: Expression;
}
 */
function processBinaryExpression(path, expressionResult, statement) {
    const node = path.node;
    const left = node.left;
    const operator = node.operator;
    const right = node.right;
    expressionResult.infixOperators.push(operator);
    processExpression(path.get('left'), expressionResult, statement);
    processExpression(path.get('right'), expressionResult, statement);
}

/* interface AssignmentExpression<: Expression {
    type: "AssignmentExpression";
    operator: AssignmentOperator;
    left: Pattern | Expression;
    right: Expression;
}
An assignment operator expression.

    AssignmentOperator
enum AssignmentOperator {
    "=" | "+=" | "-=" | "*=" | "/=" | "%="
        | "<<=" | ">>=" | ">>>="
        | "|=" | "^=" | "&="
}
An assignment operator token. */
function processAssignmentExpression(path, expressionResult, statement) {
    const node = path.node;
    const operator = node.operator;
    expressionResult.infixOperators.push(operator);
    processExpression(path.get('left'), expressionResult, statement);
    processExpression(path.get('right'), expressionResult, statement);
}

/* interface MemberExpression<: Expression, Pattern {
    type: "MemberExpression";
    object: Expression | Super;
    property: Expression;
    computed: boolean;
    optional: boolean | null;
}
A member expression.If computed is true, the node corresponds to a computed(a[b])
 member expression and property is an Expression.If computed is false, the node 
 corresponds to a static(a.b) member expression and property is an Identifier.
 The optional flags indicates that the member expression can be called even if 
 the object is null or undefined.If this is the object value(null / undefined) 
 should be returned. */
function processMemberExpression(path, expressionResult, statement) {
    const node = path.node;
    processIdentifier(path.get('object'), expressionResult, statement);
    processIdentifier(path.get('property'), expressionResult, statement);
}

/**
 * interface Identifier <: Expression, Pattern {
  type: "Identifier";
  name: string;
}
An identifier. Note that an identifier may be an expression or a destructuring pattern
 * @param {*} path 
 */
function processIdentifier(path, { identifiers = [] }) {
    const name = path.node.name;
    identifiers.push(name);
}

/** An ++ or -- after or befor and expression */
// interface UpdateExpression <: Expression {
//     type: "UpdateExpression";
//     operator: UpdateOperator;
//     argument: Expression;
//     prefix: boolean;
//   }

function processUpdateExpression(path, expressionResult, statement) {

    const node = path.node;
    
    // Extract operator (++/ --)
    if (node.prefix) {
        expressionResult.prefixOperators.push(node.operator);
    } else {
        expressionResult.postfixOperators.push(node.operator);
    }
    processExpression(path.get('argument'), expressionResult, statement);
}

exports.processExpression = processExpression;