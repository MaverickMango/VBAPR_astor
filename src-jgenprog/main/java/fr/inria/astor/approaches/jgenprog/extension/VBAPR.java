package fr.inria.astor.approaches.jgenprog.extension;

import com.martiansoftware.jsap.JSAPException;
import fr.inria.astor.approaches.jgenprog.JGenProg;
import fr.inria.astor.approaches.jgenprog.jGenProgSpace;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.solutionsearch.spaces.operators.IngredientBasedOperator;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSelectionStrategy;
import fr.inria.astor.core.solutionsearch.spaces.operators.OperatorSpace;
import fr.inria.astor.util.ReadGT;
import fr.inria.main.evolution.ExtensionPoints;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class VBAPR  extends JGenProg {
    protected static Logger log = Logger.getLogger(VBAPR.class.getSimpleName());

    public VBAPR(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade) throws JSAPException {
        super(mutatorExecutor, projFacade);
        ReadGT.getGTs(ReadGT.getInfos());
    }

    @Override
    public OperatorInstance createOperatorInstanceForPoint(ModificationPoint modificationPoint) throws IllegalAccessException {

        SuspiciousModificationPoint suspModificationPoint = (SuspiciousModificationPoint) modificationPoint;

        AstorOperator operatorSelected = operatorSelectionStrategy.getNextOperator(suspModificationPoint);

        if (operatorSelected == null) {
            log.debug("Operation Null");
            return null;
        }

        List<OperatorInstance> operatorInstances = null;
        if (operatorSelected.canBeAppliedToPoint(modificationPoint)) {
            if (operatorSelected.needIngredient()) {
                IngredientBasedOperator ingbasedapproach = (IngredientBasedOperator) operatorSelected;

                Ingredient ingredient = this.ingredientSearchStrategy.getFixIngredient(modificationPoint,
                        operatorSelected);

                if (ingredient == null) {
                    return null;
                }
                operatorInstances = ingbasedapproach.createOperatorInstances(modificationPoint, ingredient,
                        this.ingredientTransformationStrategy);

            } else {
                operatorInstances = operatorSelected.createOperatorInstances(modificationPoint);
            }

            return selectRandomly(operatorInstances);
        } else {
            return null;
        }
    }

    @Override
    public List<SuspiciousCode> calculateSuspicious() throws Exception {
        long inittime = System.currentTimeMillis();

        // Find tests:
        String regressionTC = ConfigurationProperties.getProperty("regressiontestcases4fl");
        List<String> regressionTestForFaultLocalization = null;
        if (regressionTC != null && !regressionTC.trim().isEmpty()) {
            regressionTestForFaultLocalization = Arrays.asList(regressionTC.split(File.pathSeparator));
        } else {

            regressionTestForFaultLocalization = this.getFaultLocalization().findTestCasesToExecute(projectFacade);
            projectFacade.getProperties().setRegressionCases(regressionTestForFaultLocalization);

            log.info("Test retrieved from classes: " + regressionTestForFaultLocalization.size());
        }

        List<SuspiciousCode> susp = this.getFaultLocalization()
                .searchSuspicious(getProjectFacade(), regressionTestForFaultLocalization).getCandidates();

        long endtime = System.currentTimeMillis();
        // milliseconds
        Long diffTime = (endtime - inittime);

        log.debug("Executing time Fault localization: " + diffTime / 1000 + " sec");

        if (ConfigurationProperties.getPropertyBool("overridemaxtime")) {
            Long newMaxtime = diffTime * ConfigurationProperties.getPropertyInt("maxtimefactor");
            log.info("Setting up the max to " + newMaxtime + " milliseconds (" + newMaxtime / 1000 + " sec)");
            ConfigurationProperties.setProperty("tmax2", newMaxtime.toString());
        }
        return susp;
    }

    @Override
    public void loadOperatorSpaceDefinition() throws Exception {

        super.loadOperatorSpaceDefinition();
        if (this.getOperatorSpace() == null) {
            this.setOperatorSpace(new VBAPRSpace());
        }
    }
}
