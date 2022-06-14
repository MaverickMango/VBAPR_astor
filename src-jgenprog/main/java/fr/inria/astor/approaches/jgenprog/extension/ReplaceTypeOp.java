package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.StatementOperatorInstance;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.reference.CtTypeReference;

public class ReplaceTypeOp extends ReplaceOp {

    @Override
    public boolean canBeAppliedToPoint(ModificationPoint point) {
        return point.getCodeElement() instanceof CtLocalVariable;
//                && ((CtLocalVariable<?>) point.getCodeElement()).getType().isPrimitive();
    }

    @Override
    public boolean applyChangesInModel(OperatorInstance operation, ProgramVariant p) {
        StatementOperatorInstance stmtoperator = (StatementOperatorInstance) operation;
        boolean successful = false;
        CtLocalVariable ori = (CtLocalVariable) stmtoperator.getOriginal();
        CtLocalVariable fix = (CtLocalVariable) stmtoperator.getModified();

        try {
            ori.replace(fix);
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
        CtLocalVariable ori = (CtLocalVariable) stmtoperator.getOriginal();
        CtLocalVariable fix = (CtLocalVariable) stmtoperator.getModified();
        fix.replace(ori);
        return true;
    }
}
