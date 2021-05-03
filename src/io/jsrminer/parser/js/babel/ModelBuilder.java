package io.jsrminer.parser.js.babel;

import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.entities.SourceFile;

public class ModelBuilder {

    private final SourceFile sourceFile;

    public ModelBuilder(SourceFile file) {
        this.sourceFile = file;
    }

    void processVariableDeclaration(JV8 node) {

    }

    public void visit(JV8 astNode) {
        final JV8 elementType = astNode.get("type");
        String type = elementType.asString();
    }

    public void loadFromAst(JV8 programAST) {
        var body = programAST.get("body");

        

        for (int i = 0; i < body.size(); i++) {
            var member = body.get(i);
            visit(member);
        }
    }


    /**
     * Keep tracks of composite traversal relalted info
     */
    private class Path {
        private final IContainer container;
        private final ICodeFragment parent;

        private Path(IContainer container, ICodeFragment parent) {
            this.container = container;
            this.parent = parent;
        }

        public ICodeFragment getParent() {
            return parent;
        }

        public IContainer getContainer() {
            return container;
        }
    }
}
