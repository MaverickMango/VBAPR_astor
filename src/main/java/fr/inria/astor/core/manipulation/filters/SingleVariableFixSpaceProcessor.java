package fr.inria.astor.core.manipulation.filters;

import fr.inria.astor.util.ReadGT;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtVariable;

public class SingleVariableFixSpaceProcessor extends TargetElementProcessor<CtVariableAccess> {
    @Override
    public void process(CtVariableAccess ctVariableAccess) {
        if (!ReadGT.hasVar())
            return;
        if (ctVariableAccess instanceof CtSuperAccess)
            return;
        add(ctVariableAccess);
    }
}
