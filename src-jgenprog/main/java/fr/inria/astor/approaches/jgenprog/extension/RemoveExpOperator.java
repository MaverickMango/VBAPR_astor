package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.spaces.operators.AutonomousOperator;
import fr.inria.astor.util.ReadGT;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.QueueProcessingManager;

import java.util.ArrayList;
import java.util.List;

public class RemoveExpOperator extends AutonomousOperator {
    public List<CtBinaryOperator> expElements = new ArrayList<>();
    int target = -1;

    public OperatorInstance createModificationInstance(ModificationPoint mp, CtElement fixElement) {
        CtElement element = mp.getCodeElement();
        OperatorInstance operation = new OperatorInstance();
        operation.setOriginal(element);
        operation.setOperationApplied(this);
        operation.setModificationPoint(mp);
        operation.setModified(fixElement);

        return operation;
    }

    /**
     * Create a modification instance from this operator
     *
     * @param modificationPoint
     * @return
     */
    @Override
    public List<OperatorInstance> createOperatorInstances(ModificationPoint modificationPoint) {
        List<OperatorInstance> ops = new ArrayList<>();
        assert target != -1;

        CtElement fix = getResult(modificationPoint.getCodeElement());

            OperatorInstance opInstance;
            try {
                opInstance = createModificationInstance(modificationPoint, fix);
                if (opInstance != null)
                    ops.add(opInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }

        return ops;
    }

    public CtElement getResult(CtElement element) {
        CtElement result = null;
        CtElement parent = expElements.get(target).getParent();
        //TODO
        return result;
    }

    /**
     * Method that applies the changes in the model (i.e., the spoon
     * representation of the program) according to the operator.
     *
     * @param opInstance Instance of the operator to be applied in the model
     * @param p          program variant to modified
     * @return true if the changes were applied successfully
     */
    @Override
    public boolean applyChangesInModel(OperatorInstance opInstance, ProgramVariant p) {
        return false;
    }

    /**
     * Method that undo the changes applies by this operator.
     *
     * @param opInstance Instance of the operator to be applied in the model
     * @param p          program variant to modified
     * @return true if the changes were applied successfully
     */
    @Override
    public boolean undoChangesInModel(OperatorInstance opInstance, ProgramVariant p) {
        return false;
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
        return false;
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
        CtElement element = point.getCodeElement();
        if (!ReadGT.hasExp())
            return false;
        BinaryExpProcessor processor = new BinaryExpProcessor();
        QueueProcessingManager processingManager = new QueueProcessingManager(MutationSupporter.getFactory());
        processingManager.addProcessor(processor);
        processingManager.process(element);
        if (processor.expList.size() != 0) {
            expElements = processor.expList;
            for (int i = 0; i < expElements.size(); i ++) {
                CtBinaryOperator bop = expElements.get(i);
                CtExpression expression = bop.getRightHandOperand();
                String exp = "";
                try {
                    exp = expression.getOriginalSourceFragment().getSourceCode();
                    exp = exp.trim().replace(" ", "")
                            .replace("\t", "")
                            .replace("\n", "");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (ReadGT.hasThisExp(exp)) {
                    target = i;
                    return true;
                }
            }
        }
        return false;
    }
}
