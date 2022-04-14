package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.approaches.jgenprog.operators.InsertAfterOp;
import fr.inria.astor.approaches.jgenprog.operators.InsertBeforeOp;
import fr.inria.astor.approaches.jgenprog.operators.RemoveOp;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;

public class VBAPRSpace extends OperatorSpace {

    public VBAPRSpace(){
        super.register(new BinaryExpressionMutOp());
        super.register(new ReplaceVarOp());
        super.register(new InsertAfterOp());
        super.register(new InsertBeforeOp());
        super.register(new ReplaceLiteralOp());
        super.register(new ReplaceInvocationOp());
        super.register(new ReplaceTypeInLocalVariableOp());
//        super.register(new RemoveOp());
    }
}
