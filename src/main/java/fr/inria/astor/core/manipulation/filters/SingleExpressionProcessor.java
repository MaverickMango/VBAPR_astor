package fr.inria.astor.core.manipulation.filters;

import fr.inria.astor.approaches.jgenprog.VariableReferenceProcessor;
import fr.inria.astor.core.manipulation.MutationSupporter;
import org.apache.log4j.Logger;
import spoon.reflect.code.*;
import spoon.support.QueueProcessingManager;

public class SingleExpressionProcessor extends TargetElementProcessor<CtExpression>{

    private Logger logger = Logger.getLogger(this.getClass().getName());

    public SingleExpressionProcessor() {
        super();
    }

    @Override
    public void process(CtExpression ctExpression) {
        if (ctExpression instanceof CtArrayAccess || ctExpression instanceof CtNewArray
                || ctExpression instanceof CtTypeAccess || ctExpression instanceof CtThisAccess
                || ctExpression instanceof CtVariableAccess || ctExpression instanceof CtLiteral)
            return;
        if (ctExpression.getType() != null) {
//            GTVariableProcessor processor = new GTVariableProcessor();
//            QueueProcessingManager processingManager = new QueueProcessingManager(MutationSupporter.getFactory());
//            processingManager.addProcessor(processor);
//            processingManager.process(ctExpression);
//            if (processor.varList.size() != 0)
                this.add(ctExpression);
        }

    }
}
