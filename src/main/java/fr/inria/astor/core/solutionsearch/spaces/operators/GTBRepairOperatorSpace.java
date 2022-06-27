package fr.inria.astor.core.solutionsearch.spaces.operators;

import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.setup.RandomManager;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;

import java.util.ArrayList;
import java.util.List;

public class GTBRepairOperatorSpace  extends OperatorSelectionStrategy {
    public GTBRepairOperatorSpace(OperatorSpace space) {
        super(space);
    }

    /**
     * Returns an Operator
     *
     * @return
     */
    @Override
    public AstorOperator getNextOperator() {
        List<AstorOperator> operators = new ArrayList<>();
        return operators.get(RandomManager.nextInt(operators.size()));
    }

    public AstorOperator getNextOperator(int type) {
        List<AstorOperator> operators = new ArrayList<>();
        switch (type) {
            case 1:
                operators.add(getNextOperator("InsertAfterOp"));
                operators.add(getNextOperator("InsertBeforeOp"));
                break;
            case 2:
                operators.add(getNextOperator("ReplaceExpressionOp"));
                operators.add(getNextOperator("BinaryExpressionMutOp"));
                break;
            case 3:
                operators.add(getNextOperator("InsertAfterOp"));
                operators.add(getNextOperator("InsertBeforeOp"));
                operators.add(getNextOperator("RemoveOp"));
                break;
            case 4:
                operators.add(getNextOperator("InsertAfterOp"));
                operators.add(getNextOperator("InsertBeforeOp"));
                operators.add(getNextOperator("ReplaceTypeOp"));
                break;
            case 5:
                operators.add(getNextOperator("InsertBeforeOp"));
                operators.add(getNextOperator("ReplaceExpressionOp"));
                break;
            default:break;
        }
        return operators.get(RandomManager.nextInt(operators.size()));
    }

    public AstorOperator getNextOperator(String name) {
        for (AstorOperator op :getOperatorSpace().values()) {
            if (op.name().startsWith(name)) {
                return op;
            }
        }
        return null;
    }

    /**
     * Given a modification point, it retrieves an operator to apply to that
     * point.
     *
     * @param modificationPoint
     * @return
     */
    @Override
    public AstorOperator getNextOperator(SuspiciousModificationPoint modificationPoint) {
        CtElement element = modificationPoint.getCodeElement();
        if ((element instanceof CtInvocation && element.getParent() instanceof CtBlock)
                || element instanceof CtIf || element instanceof CtWhile) {
            return this.getNextOperator(3);
        } else if (element instanceof CtExpression && !(element instanceof CtAssignment)) {//CtAssignment CtInvocation are both exp and stmt
            return this.getNextOperator(2);
        } else if (element instanceof CtLocalVariable) {
            return this.getNextOperator(4);
        } else if (element instanceof CtBreak) {
            return this.getNextOperator(5);
        } else if (element instanceof CtStatement) {
            return this.getNextOperator(1);
        }
        return null;
    }
}
