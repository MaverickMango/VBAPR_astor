package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.StatementOperatorInstance;
import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtVariableReference;
import spoon.support.reflect.code.CtBlockImpl;

public class ReplaceExpressionOp extends ExpressionIngredientOperator implements IExpressionLevelOperator {

    /**
     * Method that applies the changes in the model (i.e., the spoon
     * representation of the program) according to the operator.
     *
     * @param operation Instance of the operator to be applied in the model
     * @param p          program variant to modified
     * @return true if the changes were applied successfully
     */
    @Override
    public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {
        ExpressionOperatorInstance expop = (ExpressionOperatorInstance) operation;
        boolean successful = false;
        CtElement ori = expop.getOriginal();
        CtElement modi = expop.getModified();
        try {
            ori.replace(modi);
            successful = true;
            operation.setSuccessfulyApplied(successful);
        } catch (Exception ex) {
            log.error("Error applying an operation, exception: " + ex.getMessage());
            operation.setExceptionAtApplied(ex);
            operation.setSuccessfulyApplied(false);
        }
        return successful;
    }

    /**
     * Method that undo the changes applies by this operator.
     *
     * @param operation Instance of the operator to be applied in the model
     * @param p          program variant to modified
     * @return true if the changes were applied successfully
     */
    @Override
    public boolean undoChangesInModel(OperatorInstance operation, ProgramVariant p) {
        ExpressionOperatorInstance expop = (ExpressionOperatorInstance) operation;
        CtElement ori = expop.getOriginal();
        CtElement fix = expop.getModified();
        try {
            fix.replace(ori);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Some operators add or remove modification points from a program variant.
     * for instance, if a oprator removes statement S at moment T, then this
     * statement is not available for applying an operation at T+1.
     *
     * @param opInstance
     * @param p
     * @return
     */
    @Override
    public boolean updateProgramVariant(OperatorInstance opInstance, ProgramVariant p) {
        boolean sucess = true;
        sucess &= removePoint(p, opInstance);
        sucess &= addPoint(p, opInstance);
        assert sucess;
        return sucess;
    }

    /**
     * Indicates whether the operator can be applied in the ModificationPoint
     * passed as argument.
     * <p>
     * By default, we consider that an operator works at the level of
     * CtStatement.
     *
     * @param point location to modify
     * @return
     */
    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        return (point.getCodeElement() instanceof CtExpression) || point.getCodeElement() instanceof CtBreak;
    }
}
