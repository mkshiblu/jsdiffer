package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.*;
import io.jsrminer.uml.UMLAttribute;
import io.jsrminer.uml.UMLParameter;
import io.jsrminer.uml.UMLType;
import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    BabelNodeVisitor<BlockStatement, ClassDeclaration> classDeclarationVisitor = (BabelNode node, BlockStatement parent, IContainer container) -> {
        return visitClassDeclaration(node, parent, container);
    };

    BabelNodeVisitor<ILeafFragment, AnonymousClassDeclaration> classExpressionVisitor = (BabelNode node, ILeafFragment parent, IContainer container) -> {
        return visitClassExpression(node, parent, container);
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
        var type = idNode.getType();
        if (type != null) {
            switch (type) {
                case IDENTIFIER:
                    variableName = idNode.get("name").asString();
                    break;
                case OBJECT_PATTERN:
                    // e.g. { file, banner } = output a Destructuring assignment
                    // For now take the first one only
                    var properties = idNode.get("properties");
                    variableName = properties.get(0).getText();
                    break;
                case ARRAY_PATTERN:
                    // NOT HANDLED
                    variableName = "";
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
        } else {
            return null;
        }
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
            if (variableDeclaration != null)
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
     * container
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
     * interface Class <: Node {
     * id: Identifier | null;
     * superClass: Expression | null;
     * body: ClassBody;
     * decorators: [ Decorator ];
     * }
     */
    public ClassDeclaration visitClassDeclaration(BabelNode node, BlockStatement parent, IContainer container) {
        var classDeclaration = new ClassDeclaration();
        container.getClassDeclarations().add(classDeclaration);
        visitor.getNodeUtil().loadClassDeclarationInfo(node, classDeclaration, container);

        //  Super class
        var superClassNode = node.get("superClass");
        if (superClassNode.isDefined()) {
            UMLType superClassType = extractSuperClassTypeObject(superClassNode); // TODO
            classDeclaration.setSuperClass(superClassType);
        }
//         Decorators

        // Body
        var classBodyNode = node.get("body");
        visitClassBody(classDeclaration, classBodyNode);

        return classDeclaration;
    }

    /**
     * interface Class <: Node {
     * id: Identifier | null;
     * superClass: Expression | null;
     * body: ClassBody;
     * decorators: [ Decorator ];
     * }
     * interface ClassExpression <: Class, Expression {
     * type: "ClassExpression";
     * }
     * MetaProperty
     * interface MetaProperty <: Expression {
     * type: "MetaProperty";
     * meta: Identifier;
     * property: Identifier;
     * }
     */
    AnonymousClassDeclaration visitClassExpression(BabelNode node, ILeafFragment leafFragment, IContainer
            container) {
        var anonymousClassDeclaration = new AnonymousClassDeclaration();
        anonymousClassDeclaration.setSourceLocation(node.getSourceLocation());
        String name = visitor.getNodeUtil().generateNameForAnonymousClassDeclaration(container);
        visitor.getNodeUtil().populateContainerNamesAndLocation(anonymousClassDeclaration,
                name, anonymousClassDeclaration.getSourceLocation(), container);
        String text = visitor.getNodeUtil().getTextInSource(node, false);
        anonymousClassDeclaration.setText(text);

        // Body
        var classBodyNode = node.get("body");
        visitClassBody(anonymousClassDeclaration, classBodyNode);

        return anonymousClassDeclaration;
    }

    /**
     * ClassBody
     * interface ClassBody <: Node {
     * type: "ClassBody";
     * body: [ ClassMethod | ClassPrivateMethod | ClassProperty | ClassPrivateProperty ];
     * }
     * interface ClassDeclaration <: Class, Declaration {
     * type: "ClassDeclaration";
     * id: Identifier;
     * }
     */
    private void visitClassBody(ClassDeclaration classDeclaration, BabelNode classBodyNode) {
        // Traverse the body statements
        var blockBodyNodes = classBodyNode.get("body");
        for (int i = 0; i < blockBodyNodes.size(); i++) {
            var propertyNode = blockBodyNodes.get(i);

            switch (propertyNode.getType()) {
                case CLASS_PROPERTY:
                    visitClassProperty(propertyNode, classDeclaration);
                    break;
                case CLASS_METHOD:
                    visitClassMethod(propertyNode, classDeclaration);
                    break;
                case CLASS_PRIVATE_METHOD:
                    visitClassPrivateMethod(propertyNode, classDeclaration);
                    break;
                case CLASS_PRIVATE_PROPERTY:
                    visitClassPrivateProperty(propertyNode, classDeclaration);
                    break;
            }
        }
    }

    /**
     * interface ClassProperty <: Node {
     * type: "ClassProperty";
     * key: Expression;
     * value: Expression;
     * static: boolean;
     * computed: boolean;
     * }
     */
    List<UMLAttribute> visitClassProperty(BabelNode node, ClassDeclaration classDeclaration) {
        return processFieldDeclaration(node, classDeclaration);
    }

    private List<UMLAttribute> processFieldDeclaration(BabelNode node, IClassDeclaration classDeclaration) {
        var keyNode = node.get("key");
        var valueNode = node.get("value");
        boolean isStatic = node.get("static").asBoolean();
        List<UMLAttribute> attributes = new ArrayList<>();

        switch (keyNode.getType()) {
            case IDENTIFIER:
                String fieldName = keyNode.getString("name");
                UMLAttribute attribute = new UMLAttribute(fieldName, node.getSourceLocation());

                var variableDeclaration =
                        createVariableDeclaration(node.getSourceLocation()
                                , fieldName, null, classDeclaration.getSourceLocation());
                variableDeclaration.setType(CodeElementType.FIELD_DECLARATION);
                variableDeclaration.setAttribute(true);

                attribute.setVariableDeclaration(variableDeclaration);
                attribute.setStatic(isStatic);
                attribute.setClassQualifiedName(classDeclaration.getQualifiedName());

                attributes.add(attribute);
                break;

            case SEQUENCE_EXPRESSION:
                break;

            default:
                throw new RuntimeException("Not handled " + keyNode.getSourceLocation());
        }

        // Process Initializer
        if (valueNode.isDefined()) {
            var expression = visitor.getNodeUtil().createBaseExpressionWithRMType(valueNode, CodeElementType.VARIABLE_DECLARATION_INITIALIZER);
            visitor.visitExpression(valueNode, expression, classDeclaration);
            attributes.forEach(attribute -> attribute.getVariableDeclaration().setInitializer(expression));
        }

        // Add to the class
        attributes.forEach(attribute -> classDeclaration.addAttribute(attribute));
        return attributes;
    }

    VariableDeclaration visitClassPrivateProperty(BabelNode node, ClassDeclaration classDeclaration) {
        return null;
    }

    /**
     * interface ClassMethod <: Function {
     * type: "ClassMethod";
     * key: Expression;
     * kind: "constructor" | "method" | "get" | "set";
     * computed: boolean;
     * static: boolean;
     * decorators: [ Decorator ];
     * }
     * <p>
     * interface Function <: Node {
     * id: Identifier | null;
     * params: [ Pattern ];
     * body: BlockStatement;
     * generator: boolean;
     * async: boolean;
     * }
     */
    FunctionDeclaration visitClassMethod(BabelNode node, ClassDeclaration classDeclaration) {
        var function = new FunctionDeclaration();
        classDeclaration.registerFunctionDeclaration(function);
        function.setIsConstructor("constructor".equals(node.get("kind").asString()));
        function.setStatic(node.get("static").asBoolean());

        var keyNode = node.get("key");
        switch (keyNode.getType()) {
            case IDENTIFIER:
                String name = keyNode.get("name").asString();
                visitor.getNodeUtil().populateContainerNamesAndLocation(function, name, node.getSourceLocation(), classDeclaration);
                break;
            default:
                throw new RuntimeException("Not supported " + keyNode.getSourceLocation());
        }

        boolean successFullyParsed = processFunctionParamaterAndBody(node, classDeclaration, function);
        if (!successFullyParsed) {
            classDeclaration.getFunctionDeclarations().remove(function);
        }
        return function;
    }

    FunctionDeclaration visitClassPrivateMethod(BabelNode node, ClassDeclaration classDeclaration) {
        return null;
    }

    private UMLType extractSuperClassTypeObject(BabelNode superClassNode) {

        //UMLType umlType = new UMLType(typeName, typeQualifiedName);
        String typeQualifiedName;
        String typeName;
        switch (superClassNode.getType()) {

            case MEMBER_EXPRESSION:
                typeName = superClassNode.get("property").getString("name");
                typeQualifiedName = superClassNode.get("object").getText() + "." + typeName;
                break;
            case IDENTIFIER:
                typeName = superClassNode.getString("name");
                typeQualifiedName = typeName;
                break;
            default:
                throw new RuntimeException("error super class type " + superClassNode.getSourceLocation());
        }

        if (typeName != null && typeQualifiedName != null)
            return new UMLType(typeName, typeQualifiedName, superClassNode.getSourceLocation());
        return null;
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
    AnonymousFunctionDeclaration visitFunctionExpression(BabelNode node, ILeafFragment leafFragment, IContainer
            container) {
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
    AnonymousFunctionDeclaration visitArrowFunctionExpression(BabelNode node, ILeafFragment
            leafFragment, IContainer container) {
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

    boolean processFunctionParamaterAndBody(BabelNode node, IContainer container, FunctionDeclaration function) {
        extractFunctionParamters(node, function);

        var functionBodyNode = node.get("body");

        BlockStatement bodyBlock = new BlockStatement();
        bodyBlock.setText("{");
        function.setBody(new FunctionBody(bodyBlock));

        switch (functionBodyNode.getType()) {


            case BLOCK_STATEMENT:
                visitor.getNodeUtil().populateBlockStatementData(functionBodyNode, bodyBlock);

                // Traverse the body statements
                var blockBodyNodes = functionBodyNode.get("body");
                for (int i = 0; i < blockBodyNodes.size(); i++) {
                    visitor.visitStatement(blockBodyNodes.get(i), bodyBlock, function);
                }
                break;

            default:
                // TODO load expression as statements
                //var leaf = visitor.getNodeUtil().createSingleStatementPopulateAndAddToParent(functionBodyNode, bodyBlock);
                //visitor.visitStatement(functionBodyNode, null, function);
                break;
            //throw new NotImplementedException("Body Type: " + functionBodyNode.getSourceLocation());
        }

        return true;
    }

    private void extractFunctionParamters(BabelNode node, FunctionDeclaration function) {
        // Load parameters
        var paramterNodes = node.get("params");
        BabelNode parameterNode;
        UMLParameter umlParameter;

        for (int i = 0; i < paramterNodes.size(); i++) {
            parameterNode = paramterNodes.get(i);

            switch (parameterNode.getType()) {
                case IDENTIFIER:
                    umlParameter = visitor.getNodeUtil().createUmlParameter(
                            parameterNode.getString("name")
                            , function, parameterNode.getSourceLocation()
                    );
                    function.registerParameter(umlParameter);
                    break;

                case OBJECT_PATTERN:
                    // e.g. { file, banner } = output //a Destructuring assignment
                    var identifiers = extractIdentifiersFromObjectPatternNode(parameterNode);
                    for (var entry : identifiers.entrySet()) {
                        umlParameter = visitor.getNodeUtil().createUmlParameter(
                                entry.getKey()
                                , function
                                , entry.getValue()
                        );
                        function.registerParameter(umlParameter);
                    }
                    break;
                case REST_ELEMENT:
                    umlParameter = visitor.getNodeUtil().createUmlParameter(
                            parameterNode.get("argument").getText()
                            , function, parameterNode.getSourceLocation()
                    );
                    function.registerParameter(umlParameter);
                    break;

                case ASSIGNMENT_PATTERN:
                    // Parameter with default value
                    var variableNode = parameterNode.get("left");
                    if (variableNode.getType() == BabelNodeType.IDENTIFIER) {
                        umlParameter = visitor.getNodeUtil().createUmlParameter(
                                variableNode.getString("name")
                                , function, parameterNode.getSourceLocation()
                        );
                        function.registerParameter(umlParameter);
                    }
                    break;
                default:
                    visitor.getErrorReporter().reportWarning(function.getSourceLocation(), "Parameter type " + parameterNode.getType().toString() + "not handled");
            }
        }
    }

    private Map<String, SourceLocation> extractIdentifiersFromObjectPatternNode(BabelNode node) {
        Map<String, SourceLocation> identifiers = new HashMap<>();

        var propertiesNode = node.get("properties");
        for (int i = 0; i < propertiesNode.size(); i++) {
            var propertyNode = propertiesNode.get(i);
            String name = null;
            switch (propertyNode.getType()) {
                case OBJECT_PROPERTY:
                    var keyNode = propertyNode.get("key");
                    if (keyNode.getType() == BabelNodeType.IDENTIFIER) {
                        name = keyNode.getString("name");
                    }
                    break;
            }

            if (name != null)
                identifiers.put(name, propertyNode.getSourceLocation());
        }
        return identifiers;
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
        String name = visitor.getNodeUtil().generateNameForAnonymousFunction(container);
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

        if (property.getType() == BabelNodeType.OBJECT_PROPERTY
                || property.getType() == BabelNodeType.OBJECT_METHOD) {
            var keyNode = property.get("key"); // name
            var valueNode = property.get("value");  // initialzier
            //var isShortHand = property.get("shorthand").asBoolean();
            String fieldName = getFieldNameFromObjectMemberKeyNode(keyNode);
            if (fieldName == null){
                return;
            }

            switch (property.getType()) {
                case OBJECT_PROPERTY:
                    switch (valueNode.getType()) {
                        case FUNCTION_DECLARATION:
                            processObjectFunctionDeclaration(valueNode, container, fieldName);
                            break;

                        case FUNCTION_EXPRESSION:
                            prcesssObjectPropertyFunction(property, container);
                            break;
                        default:
                            // Else this is an attribute i.e. field declaration
                            processObjectFieldDeclaration(valueNode, body, container, fieldName);
                    }

                    break;
                case OBJECT_METHOD:
                    processObjectFunctionDeclaration(property, container, fieldName);
                    break;
            }
        } else {
            // Rest element not handled
            // throw new RuntimeException("Rest Object  property at " + property.getSourceLocation() + " not handled");
            this.visitor.getErrorReporter().reportWarning(property.getSourceLocation(), "Could not parse Object  property");
        }
    }

    FunctionDeclaration processObjectFunctionDeclaration(BabelNode tree
            , IContainer container
            , String propertyNameAsFunctionName) {
        var function = new FunctionDeclaration();
        container.getFunctionDeclarations().add(function);

        // Load function info
        visitor.getNodeUtil().populateContainerNamesAndLocation(function, propertyNameAsFunctionName, tree.getSourceLocation(), container);

        processFunctionParamaterAndBody(tree, container, function);
        return function;
    }

    FunctionDeclaration prcesssObjectPropertyFunction(BabelNode propertyNode, IContainer anonymousFunctionDeclaration) {
        var function = new FunctionDeclaration();
        anonymousFunctionDeclaration.registerFunctionDeclaration(function);

        var keyNode = propertyNode.get("key");
        if (keyNode.getType() == BabelNodeType.IDENTIFIER) {
            String name = keyNode.get("name").asString();
            visitor.getNodeUtil()
                    .populateContainerNamesAndLocation(function, name
                            , propertyNode.getSourceLocation(), anonymousFunctionDeclaration);

            boolean successFullyParsed = processFunctionParamaterAndBody(propertyNode.get("value")
                    , anonymousFunctionDeclaration, function);
            if (!successFullyParsed) {
                anonymousFunctionDeclaration.getFunctionDeclarations().remove(function);
            }
        } else {
            this.visitor.getErrorReporter().reportWarning(propertyNode.getSourceLocation(),
                    "Unsupported object key type : "  + keyNode.getType() + " Text: " + propertyNode.getText());
        }
        return function;
    }

    private String getFieldNameFromObjectMemberKeyNode(BabelNode objectMemberNodeKeyNode) {

        String fieldName = null;
        switch (objectMemberNodeKeyNode.getType()) {
            case IDENTIFIER:
                fieldName = objectMemberNodeKeyNode.getString("name");
                break;
            case STRING_LITERAL:
                fieldName = objectMemberNodeKeyNode.getString("value");
                break;
            default:
                this.visitor.getErrorReporter().reportWarning(objectMemberNodeKeyNode.getSourceLocation()
                        , "Unsupported Object member KeyNode type: " +  objectMemberNodeKeyNode.getType()
                + " Text: " + objectMemberNodeKeyNode.getText());
        }

        return fieldName;
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