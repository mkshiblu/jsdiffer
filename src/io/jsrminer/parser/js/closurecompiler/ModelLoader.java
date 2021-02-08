package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeElementType;
import io.jsrminer.sourcetree.SourceLocation;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.entities.Container;
import org.eclipse.jgit.annotations.NonNull;

import java.util.List;

/**
 * Populates a SourceModel Using the ProgramTree
 */
public class ModelLoader {
    /**
     * Loads the sourceFile model using the Ast Tree
     *
     * @param container SourceFile to be populated
     */
    public void loadFromAst(@NonNull ProgramTree programTree, @NonNull Container container) {
        // Create a dummy Block parent for the file
        BlockStatement dummyBodyBlock = new BlockStatement();
        // Set the source location of the block parent to the end of the file
        SourceRange lastElementLocation = programTree.sourceElements.get(programTree.sourceElements.size() - 1).location;
        dummyBodyBlock.setText("");
        dummyBodyBlock.setType(CodeElementType.BLOCK_STATEMENT);
        //dummyBodyBlock.setDepth(-1);

        if (container.getSourceLocation() == null) {
            container.setSourceLocation(new SourceLocation(programTree.location.start.source.name
                    , 0, 0
                    , lastElementLocation.end.line, lastElementLocation.end.column
                    , 0, lastElementLocation.end.offset));
        }

        dummyBodyBlock.setSourceLocation(container.getSourceLocation());
        process(programTree.sourceElements, dummyBodyBlock, container);

        container.getStatements().addAll(dummyBodyBlock.getStatements());
        //container.getFunctionDeclarations().addAll(dummyBodyBlock.getFunctionDeclarations());
        container.getAnonymousFunctionDeclarations().addAll(dummyBodyBlock.getAllAnonymousFunctionDeclarations());
    }

    private void process(List<? extends ParseTree> trees, BlockStatement parent, IContainer container) {
        for (ParseTree tree : trees) {
            Visitor.visitStatement(tree, parent, container);
        }
    }
}
