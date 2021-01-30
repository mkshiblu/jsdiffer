package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;
import io.jsrminer.sourcetree.CodeEntity;
import io.jsrminer.sourcetree.CodeFragment;
import io.rminer.core.api.IContainer;
import org.apache.commons.lang3.NotImplementedException;

import java.util.EnumMap;

import static com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType.*;

public class Visitor {
    private final static EnumMap<ParseTreeType, INodeProcessor<CodeEntity, ParseTree, CodeFragment>> nodeProcessors
            = new EnumMap(ParseTreeType.class) {{
        put(FUNCTION_DECLARATION, DeclarationsVisitor.functionDeclarationProcessor);
        put(VARIABLE_STATEMENT, StatementsVisitor.variableStatementProcessor);
        put(IDENTIFIER_EXPRESSION, ExpressionsVisitor.identifierProcessor);
        put(LITERAL_EXPRESSION, ExpressionsVisitor.literalExpressionProcessor);
    }};


    static void visit(ParseTree tree, CodeFragment parent, IContainer container) {
        var processor = nodeProcessors.get(tree.type);

        if (processor == null) {
            throw new NotImplementedException("Processor not implemented for " + tree.type);
        }

        enter(tree, parent, container);
        processor.process(tree, parent, container);
        exit(tree, parent, container);
    }

    static void enter(ParseTree tree, CodeFragment parent, IContainer container) {

    }

    static void exit(ParseTree tree, CodeFragment parent, IContainer container) {

    }
}
