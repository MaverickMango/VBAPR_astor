package fr.inria.astor.core.solutionsearch.spaces.ingredients.transformations;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.approaches.jgenprog.extension.CodeAddFactory;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.RandomManager;
import org.apache.log4j.Logger;

import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.manipulation.sourcecode.VariableResolver;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.scopes.IngredientPoolScope;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * Represents the default strategy: it does not apply any code transformation.
 * It only return the ingredient as is in case it fix.
 * 
 * @author Matias Martinez
 *
 */
public class NoIngredientTransformationWithCheck implements IngredientTransformationStrategy {

	protected static Logger log = Logger.getLogger(NoIngredientTransformationWithCheck.class.getName());

	@Override
	public List<Ingredient> transform(ModificationPoint modificationPoint, Ingredient ingredient) {

		List<Ingredient> result = new ArrayList<>();

		CtElement elementFromIngredient = ingredient.getCode();

		boolean fit = VariableResolver.fitInPlace(modificationPoint.getContextOfModificationPoint(),
				elementFromIngredient);

		if (!fit && ConfigurationProperties.getPropertyBool("usevariableedit")) {
			List<CtVariable> remaining = modificationPoint.getContextOfModificationPoint();
			List<CtVariableAccess> temp = new ArrayList<>(VariableResolver._notmapped);
			while (!fit && !remaining.isEmpty()) {
				for (CtVariableAccess old :VariableResolver._notmapped) {
					try {
						CtVariable variable = remaining.remove((int)RandomManager.nextInt(remaining.size()));//typecheck
						if (variable.getReference().getType().equals(old.getType())
								|| (variable.getReference().getType().isPrimitive() && old.getType().isPrimitive())) {
							CtVariableRead repalceone = CodeAddFactory.createVariableRead(variable);
							old.replace(repalceone);
							temp.remove(old);
						}
					} catch (Exception e) {
						log.warn("variable replace error in transform");
						e.printStackTrace();
					}
					if (remaining.isEmpty())
						break;
				}
				if (temp.isEmpty()) {
					fit = VariableResolver.fitInPlace(modificationPoint.getContextOfModificationPoint(),
							elementFromIngredient);
					temp = new ArrayList<>(VariableResolver._notmapped);
				}
			}
		}

		if (fit) {
			IngredientPoolScope scope = VariableResolver.determineIngredientScope(modificationPoint.getCodeElement(),
					elementFromIngredient);

			boolean changeShadow = VariableResolver.changeShadowedVars(modificationPoint.getCodeElement(),
					elementFromIngredient);
			if (changeShadow) {
				log.debug("Transforming shadowed variable in " + elementFromIngredient);
			}

			// Only one ingredient was to be returned (the original)
			result.add(new Ingredient(elementFromIngredient, scope));
		}
		return result;
	}

}
