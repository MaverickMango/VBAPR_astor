package fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch;

import fr.inria.astor.approaches.jgenprog.extension.ReplaceInvocationOp;
import fr.inria.astor.approaches.jgenprog.operators.InsertAfterOp;
import fr.inria.astor.approaches.jgenprog.operators.InsertBeforeOp;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.RandomManager;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.stats.Stats;
import fr.inria.astor.util.CodeLineCollector;
import fr.inria.astor.util.StringUtil;
import org.apache.log4j.Logger;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtVariableReference;
import spoon.support.reflect.code.CtInvocationImpl;

import java.util.ArrayList;
import java.util.List;

public class GTBSelectionIngredientSearchStrategy extends SimpleRandomSelectionIngredientStrategy {

    private static final Boolean DESACTIVATE_CACHE = ConfigurationProperties
            .getPropertyBool("desactivateingredientcache");
    protected Logger log = Logger.getLogger(this.getClass().getName());

    public GTBSelectionIngredientSearchStrategy(IngredientPool space) {
        super(space);
    }

    protected Ingredient getRandomVarFromContext(List<Ingredient> fixSpace, List<CtVariable> context) {
        if (context == null || context.size() == 0)
            return null;
        int size = context.size();
        int index = RandomManager.nextInt(size);
        Ingredient ingredient = fixSpace.get(RandomManager.nextInt(fixSpace.size()));
        ingredient.setCode(context.get(index).getReference());
        return ingredient;

    }
    protected Ingredient getRandomFromSpace(List<Ingredient> fixSpace) {
        if (fixSpace == null)
            return null;
        int size = fixSpace.size();
        int index = RandomManager.nextInt(size);
        return fixSpace.get(index);

    }

    List<Ingredient> getstmts(List<Ingredient> base, AstorOperator operationType) {
        List<Ingredient> stmts = new ArrayList<>();
        for (Ingredient in :base) {
            if (operationType instanceof InsertBeforeOp && in.getCode() instanceof CtReturn)
                continue;
            if (in.getCode() instanceof CtStatement
                    && !(in.getCode() instanceof CtInvocation) && !(in.getCode() instanceof CtLocalVariable)) {
                stmts.add(in);
            }
        }
        return stmts;
    }

    List<Ingredient> getInvocations(List<Ingredient> base, ModificationPoint point) {
        String name = ((CtInvocationImpl)point.getCodeElement()).getExecutable().getSimpleName();
        List<Ingredient> stmts = new ArrayList<>();
        for (Ingredient in :base) {
            CtInvocationImpl invocation = (CtInvocationImpl) in.getCode();
            String change = invocation.getExecutable().getSimpleName();
            if (change.equals(name)) {
                stmts.add(in);
            }
        }
        return stmts;
    }

    /**
     * Method that returns an Ingredient from the ingredient space given a
     * modification point and a Operator
     *
     * @param modificationPoint point to be modified using an ingredient
     * @param operationType     operation applied to the modif point
     * @return an ingredient
     */
    @Override
    public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {

        int attemptsBaseIngredients = 0;

        List<Ingredient> baseElements = geIngredientsFromSpace(modificationPoint, operationType);

        if (operationType instanceof InsertBeforeOp || operationType instanceof InsertAfterOp)
            baseElements = getstmts(baseElements, operationType);

        if (operationType instanceof ReplaceInvocationOp)
            baseElements = getInvocations(baseElements, modificationPoint);

        if (baseElements == null || baseElements.isEmpty()) {
            log.debug("Any element available for mp " + modificationPoint);
            return null;
        }

        int elementsFromFixSpace = baseElements.size();
        log.debug("Templates availables" + elementsFromFixSpace);

        Stats.currentStat.getIngredientsStats().addSize(Stats.currentStat.getIngredientsStats().ingredientSpaceSize,
                baseElements.size());

        while (attemptsBaseIngredients < elementsFromFixSpace) {

            attemptsBaseIngredients++;
            log.debug(String.format("Attempts Base Ingredients  %d total %d", attemptsBaseIngredients,
                    elementsFromFixSpace));

            Ingredient baseIngredient = getRandomFromSpace(baseElements);

            String newingredientkey = getKey(modificationPoint, operationType);

            if (baseIngredient != null && baseIngredient.getCode() != null) {

                // check if the element was already used
                if (DESACTIVATE_CACHE || !this.cache.containsKey(newingredientkey)
                        || !this.cache.get(newingredientkey).contains(baseIngredient.getChacheCodeString())) {
                    this.cache.add(newingredientkey, baseIngredient.getChacheCodeString());
                    return baseIngredient;
                }

            }

        } // End while

        log.debug("--- no mutation left to apply in element "
                + StringUtil.trunc(modificationPoint.getCodeElement().getShortRepresentation())
                + ", search space size: " + elementsFromFixSpace);
        return null;
    }
}
