package fr.inria.astor.core.solutionsearch.spaces.operators;

import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.setup.RandomManager;
import fr.inria.astor.util.ReadGT;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.support.reflect.code.CtLiteralImpl;

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
            case 2:
                operators.add(getNextOperator("ReplaceVarOp"));
                operators.add(getNextOperator("BinaryExpressionMutOp"));
                break;
            case 1:
                operators.add(getNextOperator("InsertAfterOp"));
                operators.add(getNextOperator("InsertBeforeOp"));
//                operators.add(getNextOperator("RemoveOp"));
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
        if (element instanceof CtInvocation) {
            return this.getNextOperator("ReplaceInvocationOp");
        } else if (element instanceof CtTypeReference) {
            return this.getNextOperator("ReplaceTypeInLocalVariableOp");
        } else if (element instanceof CtStatement) {
            return this.getNextOperator(1);
        } else if (element instanceof CtVariableAccess || element instanceof CtVariableReference) {
            return this.getNextOperator("ReplaceVarOp");
        } else if (element instanceof CtLiteralImpl){
            return this.getNextOperator("ReplaceLiteralOp");
        } else if (element instanceof CtExpression) {
            return this.getNextOperator(2);
        }
        return null;
    }
}
