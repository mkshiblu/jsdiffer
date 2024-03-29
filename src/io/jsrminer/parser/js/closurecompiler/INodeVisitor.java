package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;

public interface INodeVisitor<R, T extends ParseTree, C extends ICodeFragment> {
    R visit(T tree, C parent, IContainer container);

//    default R process(T tree, SingleStatement singleStatement, IContainer container) {
//        throw new NotImplementedException();
//    }
//
//    default R process(T tree, BlockStatement blockStatement, IContainer container) {
//        throw new NotImplementedException();
//    }
//
//    default R process(T tree, Statement statement, IContainer container) {
//        throw new NotImplementedException();
//    }
//
//    default R process(T tree, Expression expression, IContainer container) {
//        throw new NotImplementedException();
//    }
//
//
//
//    default R process(T tree, Expression expression) {
//        throw new NotImplementedException();
//    }
//
//    default R process(T tree, SingleStatement singleStatement) {
//        throw new NotImplementedException();
//    }
//
//    default R process(T tree) {
//        throw new NotImplementedException();
//    }
//
//    default R process(T tree, CodeFragment codeFragment) {
//        throw new NotImplementedException();
//    }
//
//    default R process(T tree, IContainer container) {
//        throw new NotImplementedException();
//    }
}
