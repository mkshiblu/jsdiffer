package io.jsrminer.parser.js.closurecompiler;

import com.google.javascript.jscomp.jarjar.com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.parsing.parser.trees.Comment;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

public class PrettyPrinter {
    String sourceContent;
    Comment[] comments;
    boolean[] isCommentChar;

    public PrettyPrinter(String sourceContent, ImmutableList<Comment> comments) {
        this.sourceContent = sourceContent;
        init(comments);
    }

    private void init(ImmutableList<Comment> commentsList) {
        isCommentChar = new boolean[sourceContent.length()];
        comments = new Comment[commentsList.size()];
        int commentIndex = 0, start, end;

        for (var comment : commentsList) {
            comments[commentIndex] = comment;
            start = comment.location.start.offset;
            end = comment.location.end.offset;

            for (int i = start; i < end; i++) {
                isCommentChar[i] = true;
            }
            commentIndex++;
        }
    }

    StringBuilder getTextWithoutCommentsAndWhitespaces(SourceRange location) {
        StringBuilder sb = new StringBuilder();
        char previousAppendedChar = '\0', currentChar;

        for (int i = location.start.offset; i < location.end.offset; i++) {
            currentChar = sourceContent.charAt(i);
            if (!isCommentChar[i] && !(previousAppendedChar == ' ' && currentChar == ' ')) {
                sb.append(currentChar);
                previousAppendedChar = currentChar;
            }
        }

        trim(sb);
        return sb;
    }

    private void trim(StringBuilder builder) {
        if (builder.charAt(builder.length() - 1) == ' ') {
            builder.deleteCharAt(builder.length() - 1);
        }
    }

//    Compiler compiler;
//    SourceFile dummyExtern;
//    CompilerOptions options;
//
//    public PrettyPrinter() {
//        compiler = new Compiler();
//        options = new CompilerOptions();
//        options.setPrettyPrint(true);
//        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT_2020);
//        options.setStrictModeInput(false);
//        options.setEmitUseStrict(false);
//        options.setWrapGoogModulesForWhitespaceOnly(false);
//        options.setResolveSourceMapAnnotations(false);
//        options.setEnableModuleRewriting(false);
//        CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(
//                options);
//        dummyExtern = SourceFile.fromCode("externs.js",
//                "");
//    }
//
//    public String prettify(String jsCode, boolean isPretty) {
//        String output = jsCode;
//        options.setPrettyPrint(isPretty);
//        try {
//            var result = compiler.compile(dummyExtern, SourceFile.fromCode("unknown", jsCode), options);
//            if (result.success) {
//                output = compiler.toSource();
//                //output = filter(output);
//            }
//        } catch (Exception ex) {
//            return output;
//        }
//        return output;
//    }
//
//    private String filter(String compiledCode) {
//        return compiledCode.substring(14, compiledCode.length() - 1);
//    }
//
////    public String prettyFromAst(Node n, SourceMap sourceMap, boolean isStrict) {
////        CodePrinter.Builder builder = new CodePrinter.Builder(n);
////        builder.setCompilerOptions(options);
////        builder.setSourceMap(sourceMap);
////        builder.setTagAsTypeSummary(!n.isFromExterns() && options.shouldGenerateTypedExterns());
////        builder.setTagAsStrict(isStrict);
////        return builder.build();
////    }
}
