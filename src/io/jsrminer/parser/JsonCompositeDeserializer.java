package io.jsrminer.parser;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.FunctionBody;
import io.jsrminer.sourcetree.FunctionDeclaration;
import io.jsrminer.sourcetree.SourceLocation;
import io.jsrminer.uml.UMLParameter;
import io.rminer.core.entities.SourceFile;

import java.util.List;

/**
 * A deserializer for the program which is in a composite json format
 */
public class JsonCompositeDeserializer {

    public static SourceFile parseSourceFile(final String containerJson) {
        //JsonCompositeParser.createBlockStatement(containerBodyJson);
        Any any = JsonIterator.deserialize(containerJson);
        SourceFile sourceFile = new SourceFile(null);
        //List<Any> statementAnys = any.get("statements").asList();


//        for (Any statementAny : statementAnys) {
//            JsonCompositeParser.createBlockStatement(sta)
//        }

        BlockStatement blockBody = JsonCompositeParser.createBlockStatement(any);
        sourceFile.getStatements().addAll(blockBody.getStatements());

        List<Any> functionDeclarationAnys = any.get("functionDeclarations").asList();
        for (Any functionDeclarationAny : functionDeclarationAnys) {
            FunctionDeclaration functionDeclaration = parseFunctionDeclaration(functionDeclarationAny);
            sourceFile.getFunctionDeclarations().add(functionDeclaration);
        }
        return sourceFile;
    }

    public static FunctionDeclaration parseFunctionDeclaration(Any any) {

        // Extract fds info
        String qualifiedName = any.toString("qualifiedName");
        FunctionDeclaration function = new FunctionDeclaration(qualifiedName, true);

        // Location
        SourceLocation location = any.get("loc").as(SourceLocation.class);
        function.setSourceLocation(location);

        // Params
        for (Any paramAny : any.get("params").asList()) {
            UMLParameter parameter = parseUMLParameter(paramAny);
            function.getParameters().put(parameter.name, parameter);
        }

        // Body Statements
        Any body = any.get("statements").asList().get(0);
        BlockStatement bodyBlock = JsonCompositeParser.createBlockStatement(body);
        function.setBody(new FunctionBody(bodyBlock));

        return function;
    }

    static UMLParameter parseUMLParameter(Any parameterAny) {
        return new UMLParameter(parameterAny.toString());
    }

    /**
     * Parses a json and returns a BlockStatement
     *
     * @param json
     */
    public void parseBlockStatement(final String json) {

    }

    /**
     * Helper for parsing anynodes
     *
     * @param any
     */
    private void parse(Any any) {

    }
}
