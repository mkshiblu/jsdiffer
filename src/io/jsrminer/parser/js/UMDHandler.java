package io.jsrminer.parser.js;

import io.jsrminer.sourcetree.SingleStatement;
import io.rminerx.core.api.IAnonymousFunctionDeclaration;
import io.rminerx.core.api.ISourceFile;

public class UMDHandler {

    public void hoistUMDCodeToSourceFileLevel(ISourceFile sourceFile) {
        var statement = (SingleStatement) sourceFile.getStatements().get(0);
        var umdFunction = statement.getAnonymousFunctionDeclarations().get(1);
        //keepUmdAsAnonymous(sourceFile, statement, umdFunction);
        hoistEverythingFromUMd(sourceFile, umdFunction, statement);
    }

    private void hoistEverythingFromUMd(ISourceFile sourceFile, IAnonymousFunctionDeclaration umdFunction, SingleStatement statement) {
        sourceFile.getStatements().clear();
        sourceFile.getFunctionDeclarations().clear();
        sourceFile.getAnonymousFunctionDeclarations().remove(statement.getAnonymousFunctionDeclarations().get(0));
        sourceFile.getAnonymousFunctionDeclarations().remove(umdFunction);

        // Copy UMD
        sourceFile.getStatements().addAll(umdFunction.getBody().blockStatement.getStatements());
        sourceFile.getFunctionDeclarations().addAll(umdFunction.getFunctionDeclarations());
        sourceFile.getAnonymousFunctionDeclarations().addAll(umdFunction.getAnonymousFunctionDeclarations());
    }

    private void keepUmdAsAnonymous(ISourceFile sourceFile, SingleStatement statement, io.rminerx.core.api.IAnonymousFunctionDeclaration umdFunction) {
        if (!sourceFile.getFunctionDeclarations().contains(umdFunction)) {
            sourceFile.getFunctionDeclarations().add(umdFunction);
        }
        sourceFile.getStatements().remove(statement);
        sourceFile.getAnonymousFunctionDeclarations().remove(statement.getAnonymousFunctionDeclarations().get(0));
        sourceFile.getAnonymousFunctionDeclarations().remove(umdFunction);
    }

    public boolean isUMD(ISourceFile sourceFile) {
        boolean hasNoFunctionDeclarations = sourceFile.getFunctionDeclarations().size() == 0;
        if (hasNoFunctionDeclarations && sourceFile.getStatements().size() == 1) {
            var statement = sourceFile.getStatements().get(0);
            if (!(statement instanceof SingleStatement))
                return false;

            var singleStatement = (SingleStatement) statement;
            boolean statementHasTwoFunctionExpressions = singleStatement.getAnonymousFunctionDeclarations().size() == 2;

            if (statementHasTwoFunctionExpressions) {
                // first function has two paremeters and empty second has non
                var ano1 = singleStatement.getAnonymousFunctionDeclarations().get(0);

                if (ano1.getParameters().size() == 2
                        && ano1.getStatements().size() == 0
                        && ano1.getFunctionDeclarations().size() == 0
                        && ano1.getAnonymousFunctionDeclarations().size() == 0) {
                    var ano2 = singleStatement.getAnonymousFunctionDeclarations().get(1);

                    return ano2.getBody().blockStatement.getStatements().size() != 0
                            || ano2.getFunctionDeclarations().size() != 0
                            || ano2.getAnonymousFunctionDeclarations().size() != 0;
                }
            }
        }
        return false;
    }
}
