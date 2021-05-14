package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.BlockStatement;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.entities.SourceFile;
import org.apache.commons.lang3.NotImplementedException;

import java.util.function.Function;

public class Visitor {
    private final String filename;
    private final BabelNodeUtil nodeUtil;
    private final DeclarationVisitor declarationVisitor = new DeclarationVisitor(this);
    private final LiteralVisitor literalVisitor = new LiteralVisitor(this);

    public Visitor(String filename, String fileContent) {
        this.nodeUtil = new BabelNodeUtil(filename, fileContent);
        this.filename = filename;
    }

    public SourceFile loadFromAst(BabelNode programAST) {
        var container = new SourceFile(filename);
        container.setSourceLocation(programAST.getSourceLocation());

        BlockStatement dummyBodyBlock = new BlockStatement();
        dummyBodyBlock.setText("");
        dummyBodyBlock.setSourceLocation(container.getSourceLocation());
        //var path = new NodePath(dummyBodyBlock, container);
        BabelNode body = programAST.get("body");
        for (int i = 0; i < body.size(); i++) {
            var member = body.get(i);
            visitStatement(member, dummyBodyBlock, container);
        }

        container.getStatements().addAll(dummyBodyBlock.getStatements());

        return container;
    }

    void visitExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        visit(node, leaf, container);
    }

    Object visitStatement(BabelNode node, BlockStatement parent, IContainer container) {
        return visit(node, parent, container);
    }

    private Object visit(BabelNode node, ICodeFragment parent, IContainer container) {
        final BabelNode elementType = node.get("type");
        String type = elementType.asString();
        switch (type) {
            case "VariableDeclaration":
                declarationVisitor.visitVariableDeclaration(node, parent, container);
                break;
            case "NumericLiteral":
                literalVisitor.visitNumericLiteral(node, (ILeafFragment) parent, container);
                break;

            case "FunctionDeclaration":
                declarationVisitor.visitFunctionDeclaration(node, (BlockStatement) parent, container);
                break;
            default:
                throw new NotImplementedException(type);
        }

        return null;
    }

    public boolean isIgnored(String type) {
        return BabelParserConfig.ignoredNodeTypes.contains(type);
    }

    public BabelNodeUtil getNodeUtil() {
        return nodeUtil;
    }
}
