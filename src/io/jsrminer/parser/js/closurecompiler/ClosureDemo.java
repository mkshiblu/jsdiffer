package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.*;
import com.google.javascript.rhino.Node;

public class ClosureDemo {

    PrettyPrinter prettyPrinter;// = new PrettyPrinter();

    public class JavaScriptAnalyzer extends NodeTraversal.AbstractShallowCallback implements CompilerPass {

        @Override
        public void visit(NodeTraversal t, Node n, Node parent) {
            if (n.isClass()) {
                System.out.println(n.getFirstChild().getString());
            }
            if (n.isMemberFunctionDef() || n.isGetterDef() || n.isSetterDef()) {
                System.out.println(n.getString());
            }
            if (n.isFunction()) {
                System.out.println(n.getFirstChild().getString());
            }
            // there is more work required to detect all types of methods that
            // has been left out for brevity...
        }

        @Override
        public void process(Node externs, Node root) {

        }
    }

    public void parse1(String filename, String fileContent) {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setPrettyPrint(true);
        var level = CompilationLevel.WHITESPACE_ONLY;
        level.setOptionsForCompilationLevel(options);

        compiler.initOptions(options);
        Node root = new JsAst(SourceFile.fromCode(filename, fileContent))
                .getAstRoot(compiler);
        JavaScriptAnalyzer jsListener = new JavaScriptAnalyzer();
        NodeTraversal.traverse(compiler, root, jsListener);
    }

    public void parse(String filename, String fileContent) {
       // var str = prettyPrinter.prettify(fileContent, false);

    }

    public static void main(String[] args) {
        new ClosureDemo().parse1("source.js", "var x = 1; function y(){}");
    }
}
