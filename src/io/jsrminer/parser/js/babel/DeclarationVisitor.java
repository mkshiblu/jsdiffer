package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.*;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.api.INode;
import org.apache.commons.lang3.NotImplementedException;

public class DeclarationVisitor {
    private final Visitor visitor;

    BabelNodeVisitor<ICodeFragment, Object> variableDeclarationVisitor = (BabelNode node, ICodeFragment fragment, IContainer container) -> {
        visitVariableDeclaration(node, fragment, container);
        return null;
    };

    BabelNodeVisitor<BlockStatement, Object> functionDeclarationVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitFunctionDeclaration(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, AnonymousFunctionDeclaration> functionExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitFunctionExpression(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, AnonymousFunctionDeclaration> arrowFunctionExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitArrowFunctionExpression(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, AnonymousFunctionDeclaration> objectExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitObjectExpression(node, parent, container);
    };

    BabelNodeVisitor<BlockStatement, SingleStatement> exportDefaultVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitExportDefaultDeclaration(node, parent, container);
    };

    DeclarationVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    /**
     * interface VariableDeclarator <: Node {
     * type: "VariableDeclarator";
     * id: Pattern;
     * init: Expression | null;
     * }
     *
     * @param {declaratorPath} path
     */
    VariableDeclaration processVariableDeclarator(BabelNode node, VariableDeclarationKind kind, ILeafFragment leaf, IContainer container) {
        var idNode = node.get("id");
        String variableName;

        switch (idNode.getType()) {
            case IDENTIFIER:
                variableName = idNode.get("name").asString();
                break;
            case OBJECT_PATTERN:
                // e.g. { file, banner } = output a Destructuring assignment
                // For now take the first one only
                var properties = idNode.get("properties");
                variableName = properties.get(0).getText();
                break;
            default:
                throw new RuntimeException("Declarator Id of type " + idNode.getType() + " at " + idNode.getSourceLocation() + " not handled");
        }

        VariableDeclaration variableDeclaration = createVariableDeclaration(node.getSourceLocation(), variableName, kind, leaf.getParent().getSourceLocation());
        BabelNode initNode = node.get("init");

        if (initNode != null && initNode.isDefined()) {
            Expression expression = visitor.getNodeUtil().createBaseExpressionWithRMType(initNode, CodeElementType.VARIABLE_DECLARATION_INITIALIZER);
            visitor.visitExpression(initNode, expression, container);
            variableDeclaration.setInitializer(expression);
        }

        return variableDeclaration;
    }

    VariableDeclaration createVariableDeclaration(SourceLocation variableNodeLocation, String variableName
            , VariableDeclarationKind kind
            , SourceLocation scopeNodeLocation) {
        var variableDeclaration = new VariableDeclaration(variableName, kind);
        variableDeclaration.setSourceLocation(variableNodeLocation);

        // Set Scope (TODO set body source location
        variableDeclaration.setScope(visitor.getNodeUtil().createVariableScope(variableDeclaration.getSourceLocation(), scopeNodeLocation));
        return variableDeclaration;
    }

    /**
     * interface VariableDeclaration <: Declaration {
     * type: "VariableDeclaration";
     * declarations: [ VariableDeclarator ];
     * kind: "var" | "let" | "const";
     * }
     */

    void visitVariableDeclaration(BabelNode node, ICodeFragment fragment, IContainer container) {
        String kindStr = node.get("kind").asString();
        var kind = VariableDeclarationKind.fromName(kindStr);
        var declarations = node.get("declarations");
        var isStatement = fragment instanceof BlockStatement;

        ILeafFragment leaf = isStatement
                ? visitor.getNodeUtil().createSingleStatementPopulateAndAddToParent(node, (BlockStatement) fragment)
                : (Expression) fragment;

        for (int i = 0; i < declarations.size(); i++) {
            VariableDeclaration variableDeclaration = processVariableDeclarator(declarations.get(i), kind, leaf, container);
            addVariableDeclarationToParent(leaf, variableDeclaration);
        }
    }

    void addVariableDeclarationToParent(ILeafFragment leaf, VariableDeclaration vd) {
        leaf.getVariableDeclarations().add(vd);
        leaf.getVariables().add(vd.variableName);

        if (vd.getInitializer() != null) {
            visitor.getNodeUtil().copyLeafData(vd.getInitializer(), leaf);
        }
    }

    /**
     * interface FunctionDeclaration <: Function, Declaration {
     * type: "FunctionDeclaration";
     * id: Identifier;
     * }
     * A function declaration. Note that unlike in the parent interface Function,
     * the id cannot be null, except when this is the child of an ExportDefaultDeclaration.
     *
     * @param node
     * @param parent
     * @param container
     */
    public FunctionDeclaration visitFunctionDeclaration(BabelNode node, BlockStatement parent, IContainer container) {
        FunctionDeclaration function = new FunctionDeclaration();
        container.registerFunctionDeclaration(function);
        visitor.getNodeUtil().loadFunctionDeclarationInfo(node, function, container);
        boolean successFullyParsed = processFunctionParamaterAndBody(node, container, function);
        if (!successFullyParsed) {
            container.getFunctionDeclarations().remove(function);
        }
        return function;
    }

    /**
     * // interface FunctionExpression <: Function, Expression {
     * //     type: "FunctionExpression";
     * //   }
     * //   A function expression.
     * <p>
     * // function [name]([param1[, param2[, ..., paramN]]]) {
     * //     statements
     * //  }
     */
    AnonymousFunctionDeclaration visitFunctionExpression(BabelNode node, ILeafFragment leafFragment, IContainer container) {
        var anonymousFunctionDeclaration = new AnonymousFunctionDeclaration();
        leafFragment.registerAnonymousFunctionDeclaration(anonymousFunctionDeclaration);

        anonymousFunctionDeclaration.setText(visitor.getNodeUtil().getTextInSource(node, false));
        visitor.getNodeUtil().loadAnonymousFunctionDeclarationInfo(node, anonymousFunctionDeclaration, container);

        container.getAnonymousFunctionDeclarations().add(anonymousFunctionDeclaration);

        boolean isSuccessFullyParsed = processFunctionParamaterAndBody(node, container, anonymousFunctionDeclaration);
        if (!isSuccessFullyParsed) {
            leafFragment.getAnonymousFunctionDeclarations().remove(anonymousFunctionDeclaration);
            container.getAnonymousFunctionDeclarations().remove(anonymousFunctionDeclaration);
        }

        return anonymousFunctionDeclaration;
    }

    /**
     * interface ArrowFunctionExpression <: Function, Expression {
     * type: "ArrowFunctionExpression";
     * body: BlockStatement | Expression;
     * expression: boolean;
     * }
     * A fat arrow function expression, e.g., let foo = (bar) => { }
     */
    AnonymousFunctionDeclaration visitArrowFunctionExpression(BabelNode node, ILeafFragment leafFragment, IContainer container) {
        var anonymousFunctionDeclaration = new AnonymousFunctionDeclaration();
        leafFragment.registerAnonymousFunctionDeclaration(anonymousFunctionDeclaration);
        anonymousFunctionDeclaration.setText(visitor.getNodeUtil().getTextInSource(node, false));
        visitor.getNodeUtil().loadAnonymousFunctionDeclarationInfo(node, anonymousFunctionDeclaration, container);
        container.getAnonymousFunctionDeclarations().add(anonymousFunctionDeclaration);

//        boolean isSuccessFullyParsed = processFunctionParamaterAndBody(node, container, anonymousFunctionDeclaration);
//        if (!isSuccessFullyParsed) {
//            leafFragment.getAnonymousFunctionDeclarations().remove(anonymousFunctionDeclaration);
//            container.getAnonymousFunctionDeclarations().remove(anonymousFunctionDeclaration);
//        }

        return anonymousFunctionDeclaration;
    }

    boolean processFunctionParamaterAndBody(BabelNode node, IContainer container, FunctionDeclaration function) {
        extractFunctionParamters(node, function);

        var functionBodyNode = node.get("body");
        switch (functionBodyNode.getType()) {
            case BLOCK_STATEMENT:
                BlockStatement bodyBlock = new BlockStatement();
                bodyBlock.setText("{");
                function.setBody(new FunctionBody(bodyBlock));
                visitor.getNodeUtil().populateBlockStatementData(functionBodyNode, bodyBlock);

                // Traverse the body statements
                var blockBodyNodes = functionBodyNode.get("body");
                for (int i = 0; i < blockBodyNodes.size(); i++) {
                    visitor.visitStatement(blockBodyNodes.get(i), bodyBlock, function);
                }
                break;

            default:
                throw new NotImplementedException("Body Type: " + functionBodyNode.getSourceLocation());
        }

        return true;
    }

    private void extractFunctionParamters(BabelNode node, FunctionDeclaration function) {
        // Load parameters
        var paramterNodes = node.get("params");
        BabelNode parameterNode;
        for (int i = 0; i < paramterNodes.size(); i++) {
            parameterNode = paramterNodes.get(i);

            switch (parameterNode.getType()) {
                case IDENTIFIER:
                    var umlParamter = visitor.getNodeUtil().createUmlParameter(parameterNode.getString("name"), function, parameterNode.getSourceLocation());
                    function.registerParameter(umlParamter);
                    break;

                default:
                    throw new NotImplementedException("paramter type not handled: " + function.getSourceLocation());
            }
        }
    }

    /**
     * interface ObjectExpression <: Expression {
     * type: "ObjectExpression";
     * properties: [ ObjectProperty | ObjectMethod | SpreadElement ];
     * }
     * An object expression.
     */
    AnonymousFunctionDeclaration visitObjectExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        var anonymousFunctionDeclaration = new AnonymousFunctionDeclaration();
        anonymousFunctionDeclaration.setSourceLocation(node.getSourceLocation());
        String name = visitor.getNodeUtil().generateNameForAnonymousContainer(container);
        visitor.getNodeUtil().populateContainerNamesAndLocation(anonymousFunctionDeclaration,
                name, anonymousFunctionDeclaration.getSourceLocation(), container);
        String text = visitor.getNodeUtil().getTextInSource(node, false);
        anonymousFunctionDeclaration.setText(text);

        BlockStatement blockStatement = new BlockStatement();
        blockStatement.setSourceLocation(node.getSourceLocation());
        blockStatement.setText("{");
        blockStatement.setCodeElementType(CodeElementType.BLOCK_STATEMENT);

        anonymousFunctionDeclaration.setBody(new FunctionBody(blockStatement));
        leaf.registerAnonymousFunctionDeclaration(anonymousFunctionDeclaration);


        var properties = node.get("properties");

        for (int i = 0; i < properties.size(); i++) {
            processProperty(properties.get(i), blockStatement, anonymousFunctionDeclaration);
        }
        return anonymousFunctionDeclaration;
    }

    /**
     * Property of a class or object can be  of [ ObjectProperty | ObjectMethod | SpreadElement ];
     */
    void processProperty(BabelNode property, BlockStatement body, IContainer container) {

        switch (property.getType()) {
            case OBJECT_PROPERTY:

                var keyNode = property.get("key"); // name
                var valueNode = property.get("value");  // initialzier
                var isShortHand = property.get("shorthand").asBoolean();


                String fieldName = null;
                switch (keyNode.getType()) {
                    case IDENTIFIER:
                        fieldName = keyNode.getString("name");
                        break;
                    case STRING_LITERAL:
                        fieldName = keyNode.getString("value");
                        break;
                    default:
                        throw new RuntimeException("KeyNode type not handled at " + keyNode.getSourceLocation().toString());
                }

                switch (valueNode.getType()) {
                    case FUNCTION_DECLARATION:
                        processObjectFunctionDeclaration(valueNode, body, container, fieldName);
                        break;
                    default:
                        // Else this is an attribute i.e. field declaration
                        processObjectFieldDeclaration(valueNode, body, container, fieldName);
                }

                break;
            case SPREAD_ELEMENT:
                var argument = property.get("argument");
                // TODO not handled
                break;
            default:
                throw new RuntimeException("Object  property at " + property.getSourceLocation() + " not handled");
        }
    }

    /**
     * interface ObjectMember <: Node {
     * key: Expression;
     * computed: boolean;
     * decorators: [ Decorator ];
     * }
     * <p>
     * interface ObjectProperty <: ObjectMember {
     * type: "ObjectProperty";
     * shorthand: boolean;
     * value: Expression;
     * }
     */
    VariableDeclaration processObjectFieldDeclaration(BabelNode node
            , BlockStatement body
            , IContainer container
            , String fieldName) {

        // Create a single statement
        var leaf = new SingleStatement();
        leaf.setSourceLocation(node.getSourceLocation());
        leaf.setType(CodeElementType.VARIABLE_DECLARATION_STATEMENT);
        leaf.setText(fieldName + " = " + visitor.getNodeUtil().getTextInSource(node, true));
        visitor.getNodeUtil().addStatement(leaf, body);

        var variableDeclaration = createVariableDeclaration(node.getSourceLocation(), fieldName, null, body.getSourceLocation());

        // Process initializer
        Expression expression = visitor.getNodeUtil().createBaseExpressionWithRMType(node, CodeElementType.VARIABLE_DECLARATION_INITIALIZER);
        visitor.visitExpression(node, expression, container);
        variableDeclaration.setInitializer(expression);

        // Keep track of all the vd
        addVariableDeclarationToParent(leaf, variableDeclaration);

        return variableDeclaration;
    }

    FunctionDeclaration processObjectFunctionDeclaration(BabelNode tree
            , CodeFragment fragment
            , IContainer container
            , String propertyNameAsFunctionName) {
        var function = new FunctionDeclaration();
        container.getFunctionDeclarations().add(function);

        // Load function info
        visitor.getNodeUtil().populateContainerNamesAndLocation(function, propertyNameAsFunctionName, tree.getSourceLocation(), container);

        processFunctionParamaterAndBody(tree, container, function);
        return function;
    }


    /**
     * interface OptFunctionDeclaration <: FunctionDeclaration {
     * id: Identifier | null;
     * }
     * <p>
     * interface OptClasDeclaration <: ClassDeclaration {
     * id: Identifier | null;
     * }
     * <p>
     * interface ExportDefaultDeclaration <: ModuleDeclaration {
     * type: "ExportDefaultDeclaration";
     * declaration: OptFunctionDeclaration | OptClassDeclaration | Expression;
     * }
     */
    SingleStatement visitExportDefaultDeclaration(BabelNode node, BlockStatement parent, IContainer container) {
        var leaf = visitor.getNodeUtil().createSingleStatementPopulateAndAddToParent(node, parent);

        var declaration = node.get("declaration");
        if (declaration.getType() == BabelNodeType.OPT_CLASS_DECLARATION ||
                declaration.getType() == BabelNodeType.OPT_FUNCTION_DECLARATION) {
            var id = declaration.get("id");
            if (id.isDefined()) {
                visitor.visitExpression(id, leaf, container);
            }
        } else {
            visitor.visitExpression(declaration, leaf, container);
        }

        return leaf;
    }
}