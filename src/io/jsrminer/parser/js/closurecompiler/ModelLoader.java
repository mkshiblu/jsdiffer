package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;
import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeEntity;
import io.jsrminer.sourcetree.CodeFragment;
import io.jsrminer.sourcetree.SourceLocation;
import io.rminer.core.entities.Container;
import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.jgit.annotations.NonNull;

import java.util.EnumMap;
import java.util.List;

import static com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType.FUNCTION_DECLARATION;
import static com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType.IDENTIFIER_EXPRESSION;

/**
 * Populates a SourceModel Using the ProgramTree
 */
public class ModelLoader {
    private final static EnumMap<ParseTreeType, INodeProcessor<CodeEntity, ParseTree>> nodeProcessors
            = new EnumMap(ParseTreeType.class) {{
        put(FUNCTION_DECLARATION, DeclarationProcessor.functionDeclarationProcessor);
        put(IDENTIFIER_EXPRESSION, ExpressionProcessor.identifierProcessor);
    }};

    /**
     * Loads the sourceFile model using the Ast Tree
     *
     * @param container SourceFile to be populated
     */
    public void loadFromAst(@NonNull ProgramTree programTree, @NonNull Container container) {
        CodeFragment parent = new BlockStatement();
        parent.setSourceLocation(new SourceLocation());

        process(programTree.sourceElements, parent, container);
    }

    private void process(List<? extends ParseTree> trees, CodeFragment parent, Container container) {
        for (ParseTree tree : trees) {
            process(tree, parent, container);
        }
    }

    protected void process(ParseTree tree, CodeFragment parent, Container container) {
        var processor = nodeProcessors.get(tree.type);

        if (processor == null) {
            throw new NotImplementedException("Processor not implemented for " + tree.type);
        }

        enter(tree, parent, container);
        processor.process(tree, parent, container);
        exit(tree, parent, container);
    }

    protected void enter(ParseTree tree, CodeFragment parent, Container container) {

    }

    protected void exit(ParseTree tree, CodeFragment parent, Container container) {

    }
}
