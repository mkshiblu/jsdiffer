package io.jsrminer.parser.js.babel;

import io.jsrminer.sourcetree.BlockStatement;
import io.rminerx.core.api.ICodeFragment;
import io.rminerx.core.api.IContainer;
import io.rminerx.core.api.ILeafFragment;
import io.rminerx.core.entities.SourceFile;
import org.apache.commons.lang3.NotImplementedException;

import java.util.EnumMap;

import static io.jsrminer.parser.js.babel.BabelNodeType.*;

public class Visitor {
    private final String filename;
    private final BabelNodeUtil nodeUtil;
    private final DeclarationVisitor declarationVisitor = new DeclarationVisitor(this);
    private final LiteralVisitor literalVisitor = new LiteralVisitor(this);
    private final StatementVisitor statementVisitor = new StatementVisitor(this);
    private final ExpressionVisitor expressionVisitor = new ExpressionVisitor(this);
    private final InvocationVisitor invocationVisitor = new InvocationVisitor(this);
    private final ControlFlowStatementVisitor controlFlowStatementVisitor = new ControlFlowStatementVisitor(this);
    private final LoopStatementVisitor loopStatementVisitor = new LoopStatementVisitor(this);
    private final ExceptionVisitor exceptionVisitor = new ExceptionVisitor(this);
    private final ChoiceStatementVisitor choiceVisitor = new ChoiceStatementVisitor(this);

    private final EnumMap<BabelNodeType, BabelNodeVisitor<ICodeFragment, Object>> visitMethodsMap;

    public Visitor(String filename, String fileContent) {
        this.nodeUtil = new BabelNodeUtil(filename, fileContent);
        this.filename = filename;
        this.visitMethodsMap = new EnumMap(BabelNodeType.class) {{
            // Declarations;
            put(VARIABLE_DECLARATION, declarationVisitor.variableDeclarationVisitor);
            put(FUNCTION_DECLARATION, declarationVisitor.functionDeclarationVisitor);
            put(FUNCTION_EXPRESSION, declarationVisitor.functionExpressionVisitor);

            // Expressions
            put(ASSIGNMENT_EXPRESSION, expressionVisitor.assignmentExpressionVisitor);
            put(MEMBER_EXPRESSION, expressionVisitor.memberExpressionVisitor);
            put(IDENTIFIER, expressionVisitor.identifierVisitor);
            put(BINARY_EXPRESSION, expressionVisitor.binaryExpressionVisitor);
            put(LOGICAL_EXPRESSION, expressionVisitor.logicalExpressionVisitor);
            put(ARRAY_EXPRESSION, expressionVisitor.arrayExpressionVisitor);
            put(UNARY_EXPRESSION, expressionVisitor.unaryExpressionVisitor);
            put(THIS_EXPRESSION, expressionVisitor.thisExpressionVisitor);
            put(UPDATE_EXPRESSION, expressionVisitor.updateExpressionVisitor);
            put(CONDITIONAL_EXPRESSION, expressionVisitor.conditionalExpressionVisitor);

            // Invocations
            put(NEW_EXPRESSION, invocationVisitor.newExpressionVisitor);
            put(CALL_EXPRESSION, invocationVisitor.callExpressionVisitor);

            // Control Flow
            put(RETURN_STATEMENT, controlFlowStatementVisitor.returnStatementVisitor);
            put(BREAK_STATEMENT, controlFlowStatementVisitor.breakStatementVisitor);
            put(CONTINUE_STATEMENT, controlFlowStatementVisitor.continueStatementVisitor);
            put(LABELLED_STATEMENT, controlFlowStatementVisitor.lablelledStatementVisitor);

            // Loops
            put(FOR_STATEMENT, loopStatementVisitor.forStatementVisitor);

            // Literals
            put(NUMERIC_LITERAL, literalVisitor.numericLiteralVisitor);
            put(STRING_LITERAL, literalVisitor.stringLiteralVisitor);
            put(BOOLEAN_LITERAL, literalVisitor.booleanLiteralVisitor);
            put(NULL_LITERAL, literalVisitor.nullLiteralVisitor);

            // Statements
            put(BLOCK_STATEMENT, statementVisitor.blockStatementVisitor);
            put(EXPRESSION_STATEMENT, statementVisitor.expressionStatementVisitor);

            // Exceptions
            put(TRY_STATEMENT, exceptionVisitor.tryStatementVisitor);
            put(CATCH_CLAUSE, exceptionVisitor.catchClausetVisitor);

            // Choice
            put(IF_STATEMENT, choiceVisitor.ifStatementVisitor);
            put(SWITCH_STATEMENT, choiceVisitor.switchStatementVisitor);
            put(SWITCH_CASE, choiceVisitor.switchCaseVisitor);
        }};
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
        var visitor = visitMethodsMap.get(node.getType());
        if (visitor == null) {
            if (!isIgnored(node.getType()))
                throw new NotImplementedException("Processor not implemented for " + node.getType());
        } else {
            Object result = visitor.visit(node, parent, container);
            return result;
        }
        return null;
    }

    public boolean isIgnored(BabelNodeType type) {
        return BabelParserConfig.ignoredNodeTypes.contains(type);
    }

    public BabelNodeUtil getNodeUtil() {
        return nodeUtil;
    }
}
