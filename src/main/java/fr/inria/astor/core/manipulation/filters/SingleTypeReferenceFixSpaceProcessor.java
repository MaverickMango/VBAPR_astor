package fr.inria.astor.core.manipulation.filters;

import org.apache.log4j.Logger;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.reference.CtTypeReference;

public class SingleTypeReferenceFixSpaceProcessor extends TargetTypeReferenceElementProcessor<CtTypeReference>{
    private Logger logger = Logger.getLogger(this.getClass().getName());
    @Override
    public void process(CtTypeReference element) {
        if (element.getParent() != null && element.getParent() instanceof CtLocalVariable) {
            add(element);
        }
    }
}
