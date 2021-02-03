package io.jsrminer.parser.js.closurecompiler.statementsvisitor;

import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import io.jsrminer.BaseTest;
import io.jsrminer.io.FileUtil;
import io.jsrminer.parser.js.closurecompiler.ClosureCompilerParser;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.SourceLocation;
import io.rminerx.core.entities.SourceFile;

public enum StatementsDataProvider {

    INSTANCE;
    ProgramTree programTree;
    SourceFile container;
    BlockStatement dummyBodyBlock;

    private StatementsDataProvider() {
        container = new SourceFile();
        container.setFilepath("unnamed.js");
        container.setQualifiedName("unnamed.js");
        var parser = new ClosureCompilerParser();
        var parseResult = parser.parseAndMakeAst("test", FileUtil.readFileContent(BaseTest.getRootResourceDirectory() + "parser/variable_declarations.js"),
                false);
        programTree = parseResult.getProgramAST();
        dummyBodyBlock = new BlockStatement();

        // Set the source location of the block parent to the end of the file
        SourceRange lastElementLocation = programTree.sourceElements.get(programTree.sourceElements.size() - 1).location;
        dummyBodyBlock.setSourceLocation(new SourceLocation(lastElementLocation.start.source.name, 0, 0, lastElementLocation.end.line, lastElementLocation.end.column, 0, lastElementLocation.end.offset));
        dummyBodyBlock.setText("{");

    }

    public ProgramTree getProgramTree() {
        return programTree;
    }

    public SourceFile getContainer() {
        return container;
    }

    public BlockStatement getDummyBodyBlock() {
        return dummyBodyBlock;
    }
}