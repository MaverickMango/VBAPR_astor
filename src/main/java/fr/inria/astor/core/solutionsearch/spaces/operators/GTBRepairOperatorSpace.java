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

    public List<AstorOperator> getNextOperator(int type) {
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
                operators.add(getNextOperator("RemoveOp"));
                break;
            case 4:
                operators.add(getNextOperator("ReplaceTypeOp"));
                break;
            default:break;
        }
        return operators;
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
        List<AstorOperator> operators = new ArrayList<>();
        if (element instanceof CtStatement) {
            operators.addAll(this.getNextOperator(1));
        }
        if ((((element instanceof CtInvocation || element instanceof CtAssignment)
                && element.getParent() instanceof CtBlock))
                    || element instanceof CtIf || element instanceof CtWhile || element instanceof CtFor) {
            operators.addAll(this.getNextOperator(3));
        }
        if (element instanceof CtExpression || element instanceof CtBreak) {
            operators.addAll(this.getNextOperator(2));
        }
        if (element instanceof CtLocalVariable) {
            operators.addAll(this.getNextOperator(4));
        }
        return operators.isEmpty() ? null : operators.get(RandomManager.nextInt(operators.size()));
    }
}
