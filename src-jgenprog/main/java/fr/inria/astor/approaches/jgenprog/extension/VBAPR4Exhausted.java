package fr.inria.astor.approaches.jgenprog.extension;

import com.martiansoftware.jsap.JSAPException;
import fr.inria.astor.approaches.jgenprog.JGenProg;
import fr.inria.astor.core.antipattern.AntiPattern;
import fr.inria.astor.core.entities.*;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor;
import fr.inria.astor.core.manipulation.filters.SingleStatementFixSpaceProcessor;
import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;
import fr.inria.astor.core.output.ReportResults;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.setup.RandomManager;
import fr.inria.astor.core.solutionsearch.navigation.ForceOrderSuspiciousNavitation;
import fr.inria.astor.core.solutionsearch.population.ProgramVariantFactory;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.SimpleRandomSelectionIngredientStrategy;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.solutionsearch.spaces.operators.IngredientBasedOperator;
import fr.inria.astor.core.stats.Stats;
import fr.inria.astor.util.EditDistanceWithTokens;
import fr.inria.astor.util.FileTools;
import fr.inria.astor.util.PatchDiffCalculator;
import fr.inria.astor.util.StringUtil;
import fr.inria.main.AstorOutputStatus;
import fr.inria.main.evolution.ExtensionPoints;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class VBAPR4Exhausted extends VBAPR {
    protected static Logger log = Logger.getLogger(VBAPR4Exhausted.class.getSimpleName());
    protected static Logger detailLog = Logger.getLogger("DetailLog");
    protected static Logger fitASim = Logger.getLogger("FitASim");

    public VBAPR4Exhausted(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade) throws JSAPException, FileNotFoundException {
        super(mutatorExecutor, projFacade);
    }

    public void startEvolution() throws Exception {

        dateInitEvolution = new Date();
        // We don't evolve variants, so the generation is always one.
        generationsExecuted = 1;
        // For each variant (one is enough)
        int maxMinutes = ConfigurationProperties.getPropertyInt("maxtime");

        int v = 0;
        for (ProgramVariant parentVariant : variants) {

            log.debug("\n****\nanalyzing variant #" + (++v) + " out of " + variants.size());
            // We analyze each modifpoint of the variant i.e. suspicious
            // statement
            for (ModificationPoint modifPoint : parentVariant.getModificationPoints()) {
                // We create all operators to apply in the modifpoint
                List<OperatorInstance> operatorInstances = createInstancesOfOperators(
                        (SuspiciousModificationPoint) modifPoint);

                if (operatorInstances.isEmpty())
                    continue;

                for (OperatorInstance pointOperation : operatorInstances) {

                    if (!belowMaxTime(dateInitEvolution, maxMinutes)) {

                        this.setOutputStatus(AstorOutputStatus.TIME_OUT);
                        log.debug("Max time reached");
                        return;
                    }

                    try {
                        log.info("mod_point " + modifPoint);
                        log.info("-->op: " + pointOperation);
                    } catch (Exception e) {
                        log.error(e);
                    }

                    // We validate the variant after applying the operator
                    ProgramVariant solutionVariant = variantFactory.createProgramVariantFromAnother(parentVariant,
                            generationsExecuted);
                    solutionVariant.getOperations().put(generationsExecuted, Arrays.asList(pointOperation));

                    applyNewMutationOperationToSpoonElement(pointOperation);

                    boolean solution = true;

                    if (!ConfigurationProperties.getPropertyBool("skipvalidation")) {
                        solution = processCreatedVariant(solutionVariant, generationsExecuted);
                    }

                    if (solution) {
                        this.solutions.add(solutionVariant);

                        saveVariant(solutionVariant);
                        //this.savePatch(solutionVariant);

                        if (ConfigurationProperties.getPropertyBool("stopfirst")) {
                            this.setOutputStatus(AstorOutputStatus.STOP_BY_PATCH_FOUND);
                            return;
                        }
                    }

                    // We undo the operator (for try the next one)
                    undoOperationToSpoonElement(pointOperation);

                    if (!belowMaxTime(dateInitEvolution, maxMinutes)) {

                        this.setOutputStatus(AstorOutputStatus.TIME_OUT);
                        log.debug("Max time reached");
                        return;
                    }
                }
            }
        }
        this.variants = this.solutions;
        log.debug("End exhaustive navigation");

        this.setOutputStatus(AstorOutputStatus.EXHAUSTIVE_NAVIGATED);
    }

    private List<OperatorInstance> createInstancesOfOperators(SuspiciousModificationPoint modifPoint) {//for exhausted
        List<OperatorInstance> ops = new ArrayList<>();
        AstorOperator[] operators = getOperatorSpace().values();
        for (AstorOperator astorOperator : operators) {
            if (astorOperator.canBeAppliedToPoint(modifPoint)) {
                List<OperatorInstance> instances = null;
                if (!astorOperator.needIngredient()) {
                    instances = astorOperator.createOperatorInstances(modifPoint);

                    //make sure operators without ingredients also do not produce duplicate instances.
                    if (instances != null && !instances.isEmpty()) {
                        if (isOpInstanceUsed(modifPoint, astorOperator, null))
                            ops.addAll(instances);
                    }

                } else {
                    IngredientBasedOperator ingbasedapproach = (IngredientBasedOperator) astorOperator;
                    //[poi]considering all new generations of baseElements
                    List<Ingredient> ingredients = this.ingredientSearchStrategy.getFixIngredients(modifPoint,
                            astorOperator);

                    if (ingredients != null) {
                        instances = new ArrayList<>();
                        for (Ingredient ingredient : ingredients) {
                            instances.addAll(ingbasedapproach.createOperatorInstances(modifPoint, ingredient,
                                    this.ingredientTransformationStrategy));
                        }
                        for (OperatorInstance op :instances) {
                            if (!isOpInstanceUsed(modifPoint, astorOperator, op.getModified()))
                                ops.add(op);
                        }
                    }
                }
            }
        }

        return ops;
    }
}
