package io.jsrminer.parser.js.babel;

import com.eclipsesource.v8.V8Object;
import io.rminerx.core.entities.SourceFile;
import org.w3c.dom.Node;

public class ModelBuilder {

    private final SourceFile sourceFile;

    public ModelBuilder(SourceFile file) {
        this.sourceFile = file;
    }

    void processVariableDeclaration(JV8 node){

    }

    public void visit(JV8 astNode) {

    }

    public void loadFromAst(JV8 programAST) {
        var body = programAST.get("body");

        for (int i = 0; i < body.size(); i++) {
            var member = body.get(i);
            visit(member);
        }
    }
}
