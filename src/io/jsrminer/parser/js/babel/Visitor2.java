package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.BlockStatement;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.entities.SourceFile;
import org.apache.commons.lang3.NotImplementedException;


public class Visitor2 {
    private final String filename;
    private final BabelNodeUtil nodeUtil;
    private final DeclarationVisitor declarationVisitor = new DeclarationVisitor(this);
    private final LiteralVisitor literalVisitor = new LiteralVisitor(this);
    private final StatementVisitor statementVisitor = new StatementVisitor(this);
    private final ExpressionVisitor expressionVisitor = new ExpressionVisitor(this);
    private final InvocationVisitor invocationVisitor = new InvocationVisitor(this);
    private final ControlFlowStatementVisitor controlFlowStatementVisitor = new ControlFlowStatementVisitor(this);
    private final LoopStatementVisitor loopStatementVisitor = new LoopStatementVisitor(this);

    public Visitor2(String filename, String fileContent) {
        this.nodeUtil = new BabelNodeUtil(filename, fileContent);
        this.filename = filename;
    }

    public SourceFile loadFromAst(BabelNode programAST) {
        var container = new SourceFile(filename);
        container.setSourceLocation(programAST.getSourceLocation());

        BlockStatement dummyBodyBlock = new BlockStatement();
        dummyBodyBlock.setText("");
        dummyBodyBlock.setSourceLocation(container.getSourceLocation());
        //var path = new NodePath(dummyBodyBlock, container);
        BabelNode body = programAST.get("body");
        for (int i = 0; i < body.size(); i++) {
            var member = body.get(i);
            visitStatement(member, dummyBodyBlock, container);
        }

        container.getStatements().addAll(dummyBodyBlock.getStatements());

        return container;
    }

    void visitExpression(BabelNode node, ILeafFragment leaf, IContainer container) {
        visit(node, leaf, container);
    }

    Object visitStatement(BabelNode node, BlockStatement parent, IContainer container) {
        return visit(node, parent, container);
    }

    private Object visit(BabelNode node, ICodeFragment parent, IContainer container) {
        var type = node.getType();
        switch (type) {
            case VARIABLE_DECLARATION:
                declarationVisitor.visitVariableDeclaration(node, parent, container);
                break;
            case NUMERIC_LITERAL:
                literalVisitor.visitNumericLiteral(node, (ILeafFragment) parent, container);
                break;

            case FUNCTION_DECLARATION:
                declarationVisitor.visitFunctionDeclaration(node, (BlockStatement) parent, container);
                break;

            case FUNCTION_EXPRESSION:
                declarationVisitor.visitFunctionExpression(node, (ILeafFragment) parent, container);
                break;
            case EXPRESSION_STATEMENT:
                statementVisitor.visitExpressionStatement(node, (BlockStatement) parent, container);
                break;

            case ASSIGNMENT_EXPRESSION:
                expressionVisitor.visitAssignmentExpression(node, (ILeafFragment) parent, container);
                break;

            case MEMBER_EXPRESSION:
                expressionVisitor.visitMemberExpression(node, (ILeafFragment) parent, container);
                break;

            case UNARY_EXPRESSION:
                expressionVisitor.visitUnaryExpression(node, (ILeafFragment) parent, container);
                break;

            case UPDATE_EXPRESSION:
                expressionVisitor.visitUpdateExpression(node, (ILeafFragment) parent, container);
                break;

            case NEW_EXPRESSION:
                invocationVisitor.visitNewExpression(node, (ILeafFragment) parent, container);
                break;

            case THIS_EXPRESSION:
                expressionVisitor.visitThisExpression(node, (ILeafFragment) parent, container);
                break;

            case IDENTIFIER:
                expressionVisitor.visitIdentifier(node, (ILeafFragment) parent, container);
                break;
            case EMPTY_STATEMENT:
                break;
            case RETURN_STATEMENT:
                controlFlowStatementVisitor.visitReturnStatement(node, (BlockStatement) parent, container);
                break;
            case FOR_STATEMENT:
                loopStatementVisitor.visitForStatement(node, (BlockStatement) parent, container);
                break;

            case BINARY_EXPRESSION:
                expressionVisitor.visitBinaryExpression(node, (ILeafFragment) parent, container);
                break;

            case BLOCK_STATEMENT:
                statementVisitor.visitBlockStatement(node, (BlockStatement) parent, container);
                break;
            default:
                throw new NotImplementedException(type.toString());
        }

        return null;
    }

    public boolean isIgnored(String type) {
        return BabelParserConfig.ignoredNodeTypes.contains(type);
    }

    public BabelNodeUtil getNodeUtil() {
        return nodeUtil;
    }
}
