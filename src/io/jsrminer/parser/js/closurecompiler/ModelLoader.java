package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.CodeFragment;
import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.sourcetree.SourceLocation;
import io.rminer.core.api.IContainer;
import io.rminer.core.entities.Container;
import org.eclipse.jgit.annotations.NonNull;

import java.util.List;

/**
 * Populates a SourceModel Using the ProgramTree
 */
public class ModelLoader {

//    private String fileContent;
//
//    /**
//     * File content is needed to get the Text from the source since GCC stores only the position not the text
//     *
//     * @param fileContent
//     */
//    ModelLoader(String fileContent) {
//        this.fileContent = fileContent;
//    }

    /**
     * Loads the sourceFile model using the Ast Tree
     *
     * @param container SourceFile to be populated
     */
    public void loadFromAst(@NonNull ProgramTree programTree, @NonNull Container container) {
        // Create a dummy Block parent for the file
        CodeFragment dummyBodyBlock = new BlockStatement();
        // Set the source location of the block parent to the end of the file
        SourceRange lastElementLocation = programTree.sourceElements.get(programTree.sourceElements.size() - 1).location;
        dummyBodyBlock.setText("{");

        if (container.getSourceLocation() == null) {
            container.setSourceLocation(new SourceLocation(programTree.location.start.source.name
                    , 0, 0
                    , lastElementLocation.end.line, lastElementLocation.end.column
                    , 0, lastElementLocation.end.offset));
        }

        dummyBodyBlock.setSourceLocation(container.getSourceLocation());
        process(programTree.sourceElements, dummyBodyBlock, container);
    }

    private void process(List<? extends ParseTree> trees, CodeFragment parent, IContainer container) {
        for (ParseTree tree : trees) {
            Visitor.visit(tree, parent, container);
        }
    }

    static SingleStatement createSingleStatement(String text
            , SourceLocation sourceLocation
            , int positionIndexInParent
            , int depth) {
        var singleStatement = new SingleStatement();
        singleStatement.setText(text);
        singleStatement.setSourceLocation(sourceLocation);
        singleStatement.setPositionIndexInParent(positionIndexInParent);
        singleStatement.setDepth(depth);
        return singleStatement;
    }
}
