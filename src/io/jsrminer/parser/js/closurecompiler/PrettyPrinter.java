package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;

public class PrettyPrinter {
    Compiler compiler;
    SourceFile dummyExtern;
    CompilerOptions options;

    public PrettyPrinter() {
        compiler = new Compiler();
        options = new CompilerOptions();
        options.setPrettyPrint(true);
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT_2020);
        options.setStrictModeInput(false);
        options.setEmitUseStrict(false);
        options.setWrapGoogModulesForWhitespaceOnly(false);
        options.setResolveSourceMapAnnotations(false);
        options.setEnableModuleRewriting(false);
        CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(
                options);
        dummyExtern = SourceFile.fromCode("externs.js",
                "");
    }

    public String prettify(String jsCode, boolean isPretty) {
        String output = jsCode;
        options.setPrettyPrint(isPretty);
        try {
            var result = compiler.compile(dummyExtern, SourceFile.fromCode("unknown", jsCode), options);
            if (result.success) {
                output = compiler.toSource();
                //output = filter(output);
            }
        } catch (Exception ex) {
            return output;
        }
        return output;
    }

    private String filter(String compiledCode) {
        return compiledCode.substring(14, compiledCode.length() - 1);
    }

//    public String prettyFromAst(Node n, SourceMap sourceMap, boolean isStrict) {
//        CodePrinter.Builder builder = new CodePrinter.Builder(n);
//        builder.setCompilerOptions(options);
//        builder.setSourceMap(sourceMap);
//        builder.setTagAsTypeSummary(!n.isFromExterns() && options.shouldGenerateTypedExterns());
//        builder.setTagAsStrict(isStrict);
//        return builder.build();
//    }
}
