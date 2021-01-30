package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import io.jsrminer.sourcetree.CodeFragment;

public abstract class NodeProcessor<R, T extends ParseTree, C extends CodeFragment> implements INodeProcessor<R, T, C> {
}
