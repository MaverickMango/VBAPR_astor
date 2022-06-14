package fr.inria.astor.core.solutionsearch.spaces.operators;

import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.setup.RandomManager;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSelectionStrategy;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;

import java.util.ArrayList;
import java.util.List;

public class ReplaceExpOperatorSpace extends OperatorSelectionStrategy {
    public ReplaceExpOperatorSpace(OperatorSpace space) {
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
                operators.add(getNextOperator("ReplaceExpressionOp"));
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
        if (element instanceof CtExpression && !(element instanceof CtAssignment)) {//CtAssignment CtInvocation are both exp and stmt
            return this.getNextOperator(1);
        }
        return null;
    }
}
