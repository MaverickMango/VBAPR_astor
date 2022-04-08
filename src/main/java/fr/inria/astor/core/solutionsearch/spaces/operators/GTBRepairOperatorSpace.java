package fr.inria.astor.core.solutionsearch.spaces.operators;

import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.setup.RandomManager;
import fr.inria.astor.util.ReadGT;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
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
        if (ReadGT.hasExp()) {
            operators.add(getNextOperator("ReplaceOp"));
            operators.add(getNextOperator("BinaryExpressionMutOp"));
        } else {
            operators.add(getNextOperator("InsertAfterOp"));
            operators.add(getNextOperator("InsertBeforeOp"));
            operators.add(getNextOperator("RemoveOp"));
            operators.add(getNextOperator("ReplaceOp"));
        }
        return operators.get(RandomManager.nextInt(operators.size()));
    }

    public AstorOperator getNextOperator(String name) {
        List<AstorOperator> operators = new ArrayList<>();
        for (AstorOperator op :getOperatorSpace().values()) {
            if (op.name().startsWith(name)) {
                return op;
            }
        }
        return getNextOperator();
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
        if (element instanceof CtStatement) {
            return this.getNextOperator();
        } else if (element instanceof CtVariableAccess) {
            return this.getNextOperator("ReplaceVarOp");
        }
        return null;
    }
}
