package fr.inria.astor.approaches.jgenprog.extension;

import com.martiansoftware.jsap.JSAPException;
import fr.inria.astor.approaches.jgenprog.JGenProg;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor;
import fr.inria.astor.core.manipulation.filters.SingleStatementFixSpaceProcessor;
import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.population.ProgramVariantFactory;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.solutionsearch.spaces.operators.IngredientBasedOperator;
import fr.inria.astor.util.ReadFileUtil;
import fr.inria.main.evolution.ExtensionPoints;
import org.apache.log4j.Logger;
import spoon.processing.AbstractProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VBAPR  extends JGenProg {
    protected static Logger log = Logger.getLogger(VBAPR.class.getSimpleName());

    public VBAPR(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade) throws JSAPException, FileNotFoundException {
        super(mutatorExecutor, projFacade);
        ReadFileUtil.getGTs(ReadFileUtil.getInfos());
        ReadFileUtil.setGTElements();
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

    protected List<AbstractProcessor<?>> targetElementProcessors = null;

    protected void loadTargetElements() throws Exception {

        ExtensionPoints epoint = ExtensionPoints.TARGET_CODE_PROCESSOR;

        List<TargetElementProcessor<?>> loadedTargetElementProcessors = new ArrayList<TargetElementProcessor<?>>();

        String ingrProcessors = ConfigurationProperties.getProperty(epoint.identifier);
        String[] in = ingrProcessors.split(File.pathSeparator);
        for (String processor : in) {
//            TargetElementProcessor proc_i = (TargetElementProcessor) PlugInLoader.loadPlugin(processor,
//                    epoint._class);
            loadedTargetElementProcessors.add(new SingleExpressionFixSpaceProcessor());
            loadedTargetElementProcessors.add(new SingleStatementFixSpaceProcessor());
//					loadedTargetElementProcessors.add(new SingleTypeReferenceFixSpaceProcessor());
//            loadedTargetElementProcessors.add(proc_i);
        }
        this.setTargetElementProcessors(loadedTargetElementProcessors);
        this.setVariantFactory(new ProgramVariantFactory(this.getTargetElementProcessors()));
    }
}
