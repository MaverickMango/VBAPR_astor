package fr.inria.astor.core.solutionsearch.spaces.operators;

import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.setup.RandomManager;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;

import java.util.ArrayList;
import java.util.List;

public class RemoveOpSpace extends OperatorSelectionStrategy {

    public RemoveOpSpace(OperatorSpace space) {
        super(space);
    }

    @Override
    public AstorOperator getNextOperator() {
        List<AstorOperator> operators = new ArrayList<>();
        return operators.get(RandomManager.nextInt(operators.size()));
    }

    public AstorOperator getNextOperator(int type) {
        List<AstorOperator> operators = new ArrayList<>();
        switch (type) {
            case 1:
                operators.add(getNextOperator("RemoveOp"));
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
        if (element instanceof CtIf || element instanceof CtWhile || element instanceof CtInvocation) {//CtAssignment CtInvocation are both exp and stmt
            return this.getNextOperator(1);
        }
        return null;
    }
}
