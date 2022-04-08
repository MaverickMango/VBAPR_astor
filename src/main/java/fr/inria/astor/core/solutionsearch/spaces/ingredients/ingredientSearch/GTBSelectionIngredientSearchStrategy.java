package fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch;

import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.stats.Stats;
import fr.inria.astor.util.CodeLineCollector;
import fr.inria.astor.util.StringUtil;
import org.apache.log4j.Logger;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;

import java.util.ArrayList;
import java.util.List;

public class GTBSelectionIngredientSearchStrategy extends SimpleRandomSelectionIngredientStrategy {

    private static final Boolean DESACTIVATE_CACHE = ConfigurationProperties
            .getPropertyBool("desactivateingredientcache");
    protected Logger log = Logger.getLogger(this.getClass().getName());

    public GTBSelectionIngredientSearchStrategy(IngredientPool space) {
        super(space);
    }

    public List<Ingredient> searchFitScope(List<Ingredient> baseElements, int type) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (int i = 0; i < baseElements.size(); i++) {
            Ingredient ingredient = baseElements.get(i);
            if (type == 1 && ingredient.getCode() instanceof CtStatement) {
                ingredients.add(ingredient);
            }
            if (type == 2 && ingredient.getCode() instanceof CtVariableAccess) {
                for (CtVariableAccess va : CodeLineCollector.varElements) {
                    if (va.toString().equals(ingredient.toString())) {
                        ingredients.add(ingredient);
                        break;
                    }
                }
            }
        }
        return ingredients;
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

        //filter the gt scope vars
        CodeLineCollector.getVarsInscope();
        if (modificationPoint.getCodeElement() instanceof CtVariableAccess) {
            baseElements = searchFitScope(baseElements, 2);
        } else {
            baseElements = searchFitScope(baseElements, 1);
        }

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

            Ingredient baseIngredient = getRandomStatementFromSpace(baseElements);

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
