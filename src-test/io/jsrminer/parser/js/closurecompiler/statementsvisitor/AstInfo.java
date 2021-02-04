package io.jsrminer.parser.js.closurecompiler.statementsvisitor;

import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import io.jsrminer.sourcetree.BlockStatement;
import io.rminerx.core.entities.SourceFile;

public class AstInfo {
    final ProgramTree programTree;
    final SourceFile container;
    final BlockStatement bodyBlock;

    AstInfo(StatementsDataProvider provider) {
        programTree = provider.getProgramTree();
        container = provider.getContainer();
        bodyBlock = provider.getDummyBodyBlock();
    }
}
