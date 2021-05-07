package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeEntity;
import io.jsrminer.sourcetree.SingleStatement;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.entities.SourceFile;
import org.apache.commons.lang3.NotImplementedException;

import java.util.AbstractMap;
import java.util.EnumMap;
import java.util.Map;

import static io.jsrminer.parser.js.babel.BabelNodeType.VARIABLE_DECLARATION;

public class Visitor {
    private final String filename;
    private final String fileContent;
    private final DeclarationVisitor declarationVisitor = new DeclarationVisitor(this);

    private final EnumMap<BabelNodeType, BabelNodeVisitor<CodeEntity, ICodeFragment>> nodeProcessors
            = new EnumMap(BabelNodeType.class) {{
        put(VARIABLE_DECLARATION, declarationVisitor.variableDeclarationProcessor);
      //  put(VARIABLE_DECLARATION, declarationVisitor.variableDeclarationProcessor);
    }};

    /**
     * An expression statement such as x = "4";
     */
    public final BabelNodeVisitor<SingleStatement, BlockStatement> expressionStatementProcessor
            = (node, parent, container) -> null;


    private final Map<String, BabelNodeVisitor<? extends CodeEntity, ? extends ICodeFragment>> nodeProcessors2 = Map.ofEntries(
            new AbstractMap.SimpleImmutableEntry<String, BabelNodeVisitor<SingleStatement, BlockStatement>>(
                    "dsad", expressionStatementProcessor
            )
    );

    public Visitor(String filename, String fileContent) {
        this.filename = filename;
        this.fileContent = fileContent;
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
        var nodeType = BabelNodeType.fromTitleCase(type);
        var processor = nodeProcessors.get(nodeType);
        switch (type){
            case "VariableDeclaration":
                declarationVisitor.variableDeclarationProcessor.visit(node, parent, container)
                break;
        }

        if (processor == null) {
            if (!isIgnored(type))
                throw new NotImplementedException("Processor not implemented for " + type);
        } else {
            Object result = processor.visit(node, parent, container);
            return result;
        }
        return null;
    }


    public boolean isIgnored(String type) {
        return BabelParserConfig.ignoredNodeTypes.contains(type);
    }
}
