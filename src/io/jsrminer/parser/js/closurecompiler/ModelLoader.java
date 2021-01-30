package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import io.jsrminer.sourcetree.*;
import io.rminer.core.entities.Container;
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
        CodeFragment parent = new BlockStatement();
        parent.setSourceLocation(new SourceLocation());

        process(programTree.sourceElements, parent, container);
    }

    private void process(List<? extends ParseTree> trees, CodeFragment parent, Container container) {
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
