package fr.inria.astor.core.manipulation.filters;

import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.visitor.Filter;
import spoon.support.QueueProcessingManager;
import spoon.support.reflect.code.CtLocalVariableImpl;

public class StatementFilterWithGT implements Filter<CtStatement> {
    private String location;

    public StatementFilterWithGT(String location) {
        this.location = location;
    }
    public StatementFilterWithGT() {

    }
    @Override
    public boolean matches(CtStatement element) {
        GTVariableProcessor processor = new GTVariableProcessor();//x
        QueueProcessingManager processingManager = new QueueProcessingManager(MutationSupporter.getFactory());
        processingManager.addProcessor(processor);
        if (element instanceof CtLocalVariable)
            processingManager.process(((CtLocalVariable<?>) element).getReference());
        else
            processingManager.process(element);
        return processor.varList.size() != 0;
    }
}
