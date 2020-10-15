package io.jsrminer.sourcetree;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A block statement, i.e., a sequence of statements surrounded by braces {}.
 * May contain other block statements or statements (i.e. composite statements)
 */
public class BlockStatement extends Statement {
    protected List<Statement> statements = new ArrayList<>();
    protected List<Expression> expressions = new ArrayList<>();
    // exp
    // vd

    public BlockStatement() {
    }

    public void addStatement(Statement statement) {
        this.statements.add(statement);
    }

    public void addExpression(Expression expression) {
        this.expressions.add(expression);
    }


    /**
     * Returns all the single statements including children's of children in a bottom up fashion
     * (i.e. lowest level children will be the first elements of the list sorted by their index position in parent)
     */
    public List<SingleStatement> getAllLeafStatementsIncludingNested() {
        final List<SingleStatement> leaves = new ArrayList<>();
        for (Statement statement : this.statements) {
            if (statement instanceof BlockStatement) {
                leaves.addAll(((BlockStatement) statement).getAllLeafStatementsIncludingNested());
            } else {
                leaves.add((SingleStatement) statement);
            }
        }
        return leaves;
    }

//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append(this.type.titleCase);
//        if (expressions.size() > 0) {
//            sb.append("(");
//            for (int i = 0; i < expressions.size() - 1; i++) {
//                sb.append(expressions.get(i).toString()).append("; ");
//            }
//            sb.append(expressions.get(expressions.size() - 1).toString());
//            sb.append(")");
//        }
//        return sb.toString();
//    }

    public String getTextWithExpressions() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.type.titleCase);
        if (expressions.size() > 0) {
            sb.append("(");
            for (int i = 0; i < expressions.size() - 1; i++) {
                sb.append(expressions.get(i).text).append("; ");
            }
            sb.append(expressions.get(expressions.size() - 1).toString());
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * Similar to getInnerNodes of RM
     *
     * @return
     */
    public Set<BlockStatement> getAllBlockStatementsIncludingNested() {
        final Set<BlockStatement> innerNodes = new LinkedHashSet<>();
        for (Statement statement : this.statements) {
            if (statement instanceof BlockStatement) {
                BlockStatement composite = (BlockStatement) statement;
                innerNodes.addAll(composite.getAllBlockStatementsIncludingNested());
            }
        }
        innerNodes.add(this);
        return innerNodes;
    }

    public List<Statement> getStatements() {
        return this.statements;
    }

    public void setCodeElementType(CodeElementType type) {
        this.type = type;
    }
}
