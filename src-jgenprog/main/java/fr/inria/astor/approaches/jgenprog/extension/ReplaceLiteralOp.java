package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.StatementOperatorInstance;
import spoon.support.reflect.code.CtLiteralImpl;

public class ReplaceLiteralOp extends ReplaceOp {

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        return point.getCodeElement() instanceof CtLiteralImpl;
    }

    @Override
    public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {//todo
        StatementOperatorInstance stmtoperator = (StatementOperatorInstance) operation;
        boolean successful = false;
        CtLiteralImpl ctst = (CtLiteralImpl) operation.getOriginal();

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
        CtLiteralImpl ctst = (CtLiteralImpl) operation.getOriginal();
        CtLiteralImpl fix = (CtLiteralImpl) operation.getModified();
        fix.replace(ctst);
        return true;
    }
}
