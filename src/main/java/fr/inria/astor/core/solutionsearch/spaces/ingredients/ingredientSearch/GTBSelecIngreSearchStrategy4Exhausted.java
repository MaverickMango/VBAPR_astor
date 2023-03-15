package fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch;

import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.stats.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GTBSelecIngreSearchStrategy4Exhausted extends GTBSelectionIngredientSearchStrategy{

    public GTBSelecIngreSearchStrategy4Exhausted(IngredientPool space) {
        super(space);
    }

    @Override
    public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {
//        List<Ingredient> baseElements = shrinkBaseElements(modificationPoint, operationType);
        return super.getFixIngredient(modificationPoint, operationType);
    }

    public List<Ingredient> getFixIngredients(ModificationPoint modificationPoint, AstorOperator operationType) {
        List<Ingredient> baseElements = shrinkBaseElements(modificationPoint, operationType);

        if (baseElements == null || baseElements.isEmpty()) {
            log.debug("Any element available for mp " + modificationPoint);
            return null;
        }
        String eleKey = getKey(modificationPoint, operationType);
        if (!this.cache.containsKey(eleKey)) {
            this.cache.put(eleKey, new ArrayList<>());
        }
        if (!this.cache.get(eleKey).isEmpty()) {
            removeUsed(baseElements, eleKey);
        }

        int elementsFromFixSpace = baseElements.size();
        log.debug("Templates availables" + elementsFromFixSpace);

        Stats.currentStat.getIngredientsStats().addSize(Stats.currentStat.getIngredientsStats().ingredientSpaceSize,
                baseElements.size());

        if (!baseElements.isEmpty()) {
//            this.cache.get(eleKey).addAll(baseElements.stream().map(Ingredient::getChacheCodeString).collect(Collectors.toList()));
            return baseElements;
        }

        return null;
    }
}
