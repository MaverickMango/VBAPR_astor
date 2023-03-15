package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.approaches.jmutrepair.MutantCtElement;
import fr.inria.astor.approaches.jmutrepair.operators.*;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.declaration.CtElement;

import java.util.List;

public abstract class ExpressionMutOp extends ExpresionMutOp {

    public ExpressionMutOp() {
        super();
        this.mutatorBinary.getMutators().add(new NegationUnaryOperator4Neg(MutationSupporter.getFactory()));
    }

}
