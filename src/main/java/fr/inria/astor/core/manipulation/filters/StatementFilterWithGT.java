package fr.inria.astor.core.manipulation.filters;

import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.code.CtStatement;
import spoon.reflect.visitor.Filter;
import spoon.support.QueueProcessingManager;

public class StatementFilterWithGT implements Filter<CtStatement> {
    private String location;

    public StatementFilterWithGT(String location) {
        this.location = location;
    }
    @Override
    public boolean matches(CtStatement element) {
        GTVariableProcessor processor = new GTVariableProcessor(location);//x
        QueueProcessingManager processingManager = new QueueProcessingManager(MutationSupporter.getFactory());
        processingManager.addProcessor(processor);
        processingManager.process(element);
        if (processor.varList.size() == 0)
            return false;
        return false;
    }
}
