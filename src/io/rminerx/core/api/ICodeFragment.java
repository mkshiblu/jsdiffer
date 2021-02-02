package io.rminerx.core.api;

import java.util.List;

public interface ICodeFragment {
    //    List<String> getStringLiterals();
//    List<String> getAnonnymousClassDeclarations();
//    List<String> getNullLiterals();
//    List<String> getNullLiterals();
//    List<String> getBooleanLiterals();
//    List<String> getNumericLiterals();
    List<? extends IFunctionDeclaration> getFunctionDeclarations();
}
