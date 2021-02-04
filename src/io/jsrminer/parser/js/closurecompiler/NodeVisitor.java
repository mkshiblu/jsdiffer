package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import io.rminerx.core.api.ICodeFragment;

public abstract class NodeVisitor<R, T extends ParseTree, C extends ICodeFragment> implements INodeVisitor<R, T, C> {
}
