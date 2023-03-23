package fr.inria.astor.core.solutionsearch.spaces.ingredients.transformations;

import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.manipulation.filters.StatementFilterWithGT;
import fr.inria.astor.core.manipulation.sourcecode.VarCombinationForIngredient;
import fr.inria.astor.core.manipulation.sourcecode.VarMapping;
import fr.inria.astor.core.manipulation.sourcecode.VariableResolver;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.util.FileTools;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtVariable;
import spoon.support.reflect.code.CtBlockImpl;
import spoon.support.reflect.code.CtStatementImpl;

import java.util.ArrayList;
import java.util.List;

public class GTBIngredientTransformationStrategy extends RandomTransformationStrategy{

    @Override
    public List<Ingredient> transform(ModificationPoint modificationPoint, Ingredient baseIngredient) {

        if (this.alreadyTransformed(modificationPoint, baseIngredient)) {
            return getCachedTransformations(modificationPoint, baseIngredient);
        }

        List<Ingredient> result = new ArrayList<>();

        CtCodeElement codeElementToModifyFromBase = (CtCodeElement) baseIngredient.getCode();

        if (modificationPoint.getContextOfModificationPoint().isEmpty()) {
            logger.debug("The modification point  has not any var in scope");
        }

        List<CtVariable> contextVariables = modificationPoint.getContextOfModificationPoint();

        VarMapping mapping = VariableResolver.mapVariablesFromContext(contextVariables,
                codeElementToModifyFromBase);
        // if we map all variables
        if (mapping.getNotMappedVariables().isEmpty()) {
            if (mapping.getMappedVariables().isEmpty()) {
                if ((codeElementToModifyFromBase instanceof CtStatementImpl && codeElementToModifyFromBase.getParent() instanceof CtBlockImpl)
                        && ConfigurationProperties.getPropertyBool("useVariableEdit")) {
                    //if ingredient does not affect variables in gt, just drop it
                    boolean isInHasGTVars = !codeElementToModifyFromBase.getElements(new StatementFilterWithGT()).isEmpty();
                    if (!isInHasGTVars)
                        return result;
                }
                // nothing to transform, accept the ingredient
                logger.debug("Any transf sucessful: The var Mapping is empty, we keep the ingredient");
                result.add(new Ingredient(codeElementToModifyFromBase, baseIngredient.getScope(), baseIngredient.getDerivedFrom()));

            } else if (ConfigurationProperties.getPropertyBool("useVariableEdit")){// We have mappings between variables
                if ((codeElementToModifyFromBase instanceof CtStatementImpl && codeElementToModifyFromBase.getParent() instanceof CtBlockImpl)
                        && ConfigurationProperties.getPropertyBool("skipStmtVariableEdit")) {
                    return result;
                }
                logger.debug("Ingredient before transformation: " + baseIngredient);

                List<VarCombinationForIngredient> allCombinations = findAllVarMappingCombinationUsingRandom(
                        mapping.getMappedVariables(), modificationPoint);

                if (allCombinations.size() > 0) {

                    for (VarCombinationForIngredient varCombinationForIngredient : allCombinations) {

                        DynamicIngredient ding = new DynamicIngredient(varCombinationForIngredient, mapping,
                                codeElementToModifyFromBase);
                        ding.setScope(baseIngredient.getScope());
                        result.add(ding);
                    }
                }
            }
        } else {
            logger.debug("Any transformation was sucessful: Vars not mapped: " + mapping.getNotMappedVariables());
        }

        this.storingIngredients(modificationPoint, baseIngredient, result);

        return result;
    }
}
