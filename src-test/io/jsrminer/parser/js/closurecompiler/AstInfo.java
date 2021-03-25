package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import io.jsrminer.sourcetree.BlockStatement;
import io.rminerx.core.entities.SourceFile;

public class AstInfo {
    public final ProgramTree programTree;
    public final SourceFile container;
    public final BlockStatement bodyBlock;

    public AstInfo(StatementsDataProvider provider) {
        programTree = provider.getProgramTree();
        container = provider.getContainer();
        bodyBlock = provider.getDummyBodyBlock();
    }
}
