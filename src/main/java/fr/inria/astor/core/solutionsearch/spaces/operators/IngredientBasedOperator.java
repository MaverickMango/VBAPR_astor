package fr.inria.astor.core.solutionsearch.spaces.operators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.jgenprog.extension.CodeAddFactory;
import fr.inria.astor.approaches.jgenprog.operators.InsertStatementOp;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.RandomManager;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.transformations.IngredientTransformationStrategy;
import org.apache.log4j.LogManager;
import org.codehaus.plexus.logging.LoggerManager;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class IngredientBasedOperator extends AstorOperator {

	@Override
	public boolean needIngredient() {
		return true;
	}

	public List<OperatorInstance> createOperatorInstances(ModificationPoint modificationPoint, Ingredient ingredient,
			IngredientTransformationStrategy transformationStrategy) {

		List<OperatorInstance> operatorIntances = new ArrayList<>();

		if (ingredient == null) {
			log.error("The ingredient cannot be null");
			return operatorIntances;
		}

		if (transformationStrategy != null) {

			List<Ingredient> ingredientsAfterTransformation = transformationStrategy.transform(modificationPoint,
					ingredient);

			if (ingredientsAfterTransformation == null) {
				log.debug("Empty transformations mp " + modificationPoint + " " + ingredient);

				if (!(this instanceof InsertStatementOp)) {
					return operatorIntances;
				}

			}

			for (Ingredient ingredientTransformed : ingredientsAfterTransformation) {

				OperatorInstance operatorInstance = this.createOperatorInstance(modificationPoint,
						ingredientTransformed);
				if (operatorInstance != null) {
					operatorIntances.add(operatorInstance);
				}

			}
		} else {// No transformation
			OperatorInstance opInstance = createOperatorInstance(modificationPoint);
			CtElement modifed = MutationSupporter.getFactory().Core().clone(ingredient.getCode());
			try {
				modifed.setParent(ingredient.getCode().getParent());
			}catch (Exception e) {
				e.printStackTrace();
			}
			opInstance.setModified(modifed);
			opInstance.setIngredient(ingredient);
			operatorIntances.add(opInstance);//todo
		}
		return operatorIntances;
	}

	protected OperatorInstance createOperatorInstance(ModificationPoint modificationPoint, Ingredient ingredient) {
		OperatorInstance operatorInstance = this.createOperatorInstance(modificationPoint);
		CtElement modifed = MutationSupporter.getFactory().Core().clone(ingredient.getCode());
		try {
			modifed.setParent(ingredient.getCode().getParent());
		}catch (Exception e) {
			e.printStackTrace();
		}
		operatorInstance.setModified(modifed);
		operatorInstance.setIngredient(ingredient);
		return operatorInstance;

	}

	protected OperatorInstance createOperatorInstance(ModificationPoint mp) {
		OperatorInstance operation = new OperatorInstance();
		operation.setOriginal(mp.getCodeElement());
		operation.setOperationApplied(this);
		operation.setModificationPoint(mp);
		return operation;
	}

	@Override
	public List<OperatorInstance> createOperatorInstances(ModificationPoint modificationPoint) {

		throw new IllegalAccessError(
				"An ingredient-based operator needs an ingredient. This method could never be called.");
	}

}
