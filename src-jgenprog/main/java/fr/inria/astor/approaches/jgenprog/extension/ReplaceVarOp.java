package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.approaches.jgenprog.operators.RemoveOp;
import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;
import fr.inria.astor.approaches.jgenprog.operators.StatementSupporter;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.StatementOperatorInstance;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtVariableReference;

public class ReplaceVarOp extends ReplaceOp {

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        return point.getCodeElement() instanceof CtVariableAccess || point.getCodeElement() instanceof CtVariableReference;
    }

    @Override
    public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {
        StatementOperatorInstance stmtoperator = (StatementOperatorInstance) operation;
        boolean successful = false;
        CtElement ctst = null;
        if (operation.getOriginal() instanceof CtVariableAccess)
            ctst = (CtVariableAccess) operation.getOriginal();
        else
            ctst = (CtVariableReference) operation.getOriginal();

            try {
                ctst.replace(stmtoperator.getModified());
                successful = true;
                operation.setSuccessfulyApplied(successful);
            } catch (Exception ex) {
                log.error("Error applying an operation, exception: " + ex.getMessage());
                operation.setExceptionAtApplied(ex);
                operation.setSuccessfulyApplied(false);
            }
        return successful;
    }

    @Override
    public boolean updateProgramVariant(OperatorInstance opInstance, ProgramVariant p) {
        return super.updateProgramVariant(opInstance, p);
    }

    @Override
    public boolean undoChangesInModel(OperatorInstance operation, ProgramVariant p) {
        StatementOperatorInstance stmtoperator = (StatementOperatorInstance) operation;
        if (operation.getOriginal() instanceof CtVariableAccess) {
            CtVariableAccess ctst = (CtVariableAccess) operation.getOriginal();
            CtVariableAccess fix = (CtVariableAccess) operation.getModified();
            fix.replace(ctst);
        } else {
            CtVariableReference ctst = (CtVariableReference) operation.getOriginal();
            CtVariableReference fix = (CtVariableReference) operation.getModified();
            fix.replace(ctst);
        }
        return true;
    }
}
