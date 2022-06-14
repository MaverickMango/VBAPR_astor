package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.solutionsearch.spaces.operators.IngredientBasedOperator;

public abstract class ExpressionIngredientOperator extends IngredientBasedOperator {
    @Override
    protected OperatorInstance createOperatorInstance(ModificationPoint mp) {
        OperatorInstance operation = new ExpressionOperatorInstance(mp, this, mp.getCodeElement(), null);
        return operation;
    }
}
