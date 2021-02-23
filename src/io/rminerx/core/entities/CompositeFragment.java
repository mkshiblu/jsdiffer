package io.rminerx.core.entities;

import io.jsrminer.sourcetree.FunctionDeclaration;
import io.rminerx.core.api.ICompositeFragment;
import io.rminerx.core.api.IVariableDeclaration;

import java.util.List;

public class CompositeFragment extends Fragment implements ICompositeFragment {
    private List<Fragment> statementList;
    private List<ExpressionFragment> expressionList;
    private List<IVariableDeclaration> variableDeclarations;
    private List<FunctionDeclaration> functionDeclarations;

//    @Override
//    public List<IFunctionDeclaration> getFunctionDeclarations() {
//        return null;
//    }
}
