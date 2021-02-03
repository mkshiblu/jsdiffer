package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import io.rminerx.core.api.ICodeFragment;

public abstract class NodeProcessor<R, T extends ParseTree, C extends ICodeFragment> implements INodeProcessor<R, T, C> {
}
