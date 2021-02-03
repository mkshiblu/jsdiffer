package io.jsrminer.parser.js.closurecompiler.statementsvisitor;

import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import io.jsrminer.BaseTest;
import io.jsrminer.sourcetree.BlockStatement;
import io.rminerx.core.entities.SourceFile;

public class StatementsVisitorTest extends BaseTest {
    static ProgramTree programTree = StatementsDataProvider.INSTANCE.getProgramTree();
    static SourceFile container = StatementsDataProvider.INSTANCE.getContainer();
    static BlockStatement bodyBlock = StatementsDataProvider.INSTANCE.getDummyBodyBlock();
}
