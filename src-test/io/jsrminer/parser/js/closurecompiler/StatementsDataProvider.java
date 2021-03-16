package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import io.jsrminer.BaseTest;
import io.jsrminer.io.FileUtil;
import io.jsrminer.sourcetree.BlockStatement;
import io.jsrminer.sourcetree.SourceLocation;
import io.rminerx.core.entities.SourceFile;

public enum StatementsDataProvider {
    LOOPS("loops.js"),
    VARIABLE_DECLARATIONS("variable_declarations.js"),
    OBJECT_EXPRESSIONS("objects.js"),
    CLASS_DECLARATIONS("class_declarations.js");
    ProgramTree programTree;
    SourceFile container;
    BlockStatement dummyBodyBlock;

    private StatementsDataProvider(String fileName) {
        container = new SourceFile(fileName);
        var parser = new ClosureCompilerParser();
        var parseResult = parser.parseAndMakeAst(fileName, FileUtil.readFileContent(BaseTest.getRootResourceDirectory() + "parser/" + fileName),
                false);
        programTree = parseResult.getProgramAST();
        dummyBodyBlock = new BlockStatement();

        // Set the source location of the block parent to the end of the file
        SourceRange lastElementLocation = programTree.sourceElements.get(programTree.sourceElements.size() - 1).location;
        dummyBodyBlock.setSourceLocation(new SourceLocation(lastElementLocation.start.source.name, 0, 0, lastElementLocation.end.line, lastElementLocation.end.column, 0, lastElementLocation.end.offset));
        dummyBodyBlock.setText("{");
        container.setSourceLocation(dummyBodyBlock.getSourceLocation());
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