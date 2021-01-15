package io.rminer.core.api;

import io.jsrminer.sourcetree.FunctionBody;
import io.jsrminer.uml.UMLParameter;

import java.util.List;

public interface IFunctionDeclaration extends IDeclarationContainer{
    FunctionBody getBody();
    List<UMLParameter> getParameters();
    String getName();
    boolean isConstructor();
    List<String> getParameterNameList();
}
