package io.jsrminer.parser;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import io.jsrminer.sourcetree.OperationInvocation;
import org.junit.jupiter.api.Test;

public class JsonCodeDeserializerTest {

    @Test
    public void testChainedCall() {
        JsonCodeDeserializer parser = new JsonCodeDeserializer();

        String json = "{\"text\": \"m1().a.m2().m3(232).m4()\",\"type\": \"CallExpression\",\"functionName\": \"m4\",\"arguments\": [],\"loc\": {\"start\": 1,\"end\": 25,\"startLine\": 2,\"endLine\": 2,\"startColumn\": 0,\"endColumn\": 24},\"expressionText\": \"m1().a.m2().m3(232)\"}";
        Any invocationAny = JsonIterator.deserialize(json);
        OperationInvocation invocation = parser.createOperationInvocation(invocationAny, null);
        //invocation.getArguments();
    }
}
