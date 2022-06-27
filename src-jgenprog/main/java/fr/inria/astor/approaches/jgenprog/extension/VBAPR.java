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
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.setup.RandomManager;
import fr.inria.astor.core.solutionsearch.population.ProgramVariantFactory;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.solutionsearch.spaces.operators.IngredientBasedOperator;
import fr.inria.astor.core.stats.Stats;
import fr.inria.astor.util.ReadFileUtil;
import fr.inria.astor.util.StringUtil;
import fr.inria.main.evolution.ExtensionPoints;
import org.apache.log4j.Logger;
import spoon.processing.AbstractProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

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
        }else {
            log.debug("Operation Null");
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

        loadedTargetElementProcessors.add(new SingleExpressionFixSpaceProcessor());
        loadedTargetElementProcessors.add(new SingleStatementFixSpaceProcessor());
//        String ingrProcessors = ConfigurationProperties.getProperty(epoint.identifier);
//        String[] in = ingrProcessors.split(File.pathSeparator);
//        for (String processor : in) {
////            TargetElementProcessor proc_i = (TargetElementProcessor) PlugInLoader.loadPlugin(processor,
////                    epoint._class);
////					loadedTargetElementProcessors.add(new SingleTypeReferenceFixSpaceProcessor());
////            loadedTargetElementProcessors.add(proc_i);
//        }
        this.setTargetElementProcessors(loadedTargetElementProcessors);
        this.setVariantFactory(new ProgramVariantFactory(this.getTargetElementProcessors()));
    }


    public boolean processGenerations(int generation) throws Exception {

        log.debug("\n***** Generation " + generation + " : " + this.nrGenerationWithoutModificatedVariant);
        boolean foundSolution = false, foundOneVariant = false;

        List<ProgramVariant> temporalInstances = new ArrayList<ProgramVariant>();

        currentStat.increment(Stats.GeneralStatEnum.NR_GENERATIONS);

//        beforeGenerate(generation);

        for (ProgramVariant parentVariant : variants) {

            log.debug("**Parent Variant: " + parentVariant);

            this.saveOriginalVariant(parentVariant);
            ProgramVariant newVariant = createNewProgramVariant(parentVariant, generation);

            if (newVariant == null) {
                continue;
            }
            this.saveModifVariant(newVariant);//

            boolean solution = false;

            if (ConfigurationProperties.getPropertyBool("antipattern")) {
                if (!AntiPattern.isAntiPattern(newVariant, generation)) {
                    temporalInstances.add(newVariant);
                    solution = processCreatedVariant(newVariant, generation);
                }
            } else {
                temporalInstances.add(newVariant);
                solution = processCreatedVariant(newVariant, generation);
            }

            if (solution) {
                foundSolution = true;
                newVariant.setBornDate(new Date());
            }
            foundOneVariant = true;
            // Finally, reverse the changes done by the child
            reverseOperationInModel(newVariant, generation);
            boolean validation = this.validateReversedOriginalVariant(newVariant);

            if (solution) {
                this.savePatch(newVariant);

            }

            if (foundSolution && ConfigurationProperties.getPropertyBool("stopfirst")) {
                break;
            }

        }
        prepareNextGeneration(temporalInstances, generation);

        if (!foundOneVariant)
            this.nrGenerationWithoutModificatedVariant++;
        else {
            this.nrGenerationWithoutModificatedVariant = 0;
        }

        return foundSolution;
    }


    public boolean modifyProgramVariant(ProgramVariant variant, int generation) throws Exception {

        log.debug("--Creating new operations for variant " + variant);
        boolean oneOperationCreated = false;
        int genMutated = 0, notmut = 0, notapplied = 0;
        int nroGen = 0;

        this.currentStat.getIngredientsStats().sizeSpaceOfVariant.clear();

        // We retrieve the list of modification point ready to be navigated
        // sorted a criterion
        List<ModificationPoint> modificationPointsToProcess = this.suspiciousNavigationStrategy
                .getSortedModificationPointsList(variant.getModificationPoints());

        for (int i = 0; i < modificationPointsToProcess.size(); i ++) {
            ModificationPoint modificationPoint = modificationPointsToProcess.get(i);
            log.debug("---analyzing modificationPoint position: " + modificationPoint.identified);

            // A point can be modified several time in the evolution
            boolean multiPointMutation = ConfigurationProperties.getPropertyBool("multipointmodification");
            if (!multiPointMutation && alreadyModified(modificationPoint, variant.getOperations(), generation))
                continue;

            modificationPoint.setProgramVariant(variant);
            OperatorInstance modificationInstance = createOperatorInstanceForPoint(modificationPoint);

            if (modificationInstance != null) {

                modificationInstance.setModificationPoint(modificationPoint);

                if (ConfigurationProperties.getPropertyBool("uniqueoptogen") && alreadyApplied(modificationInstance)) {
                    log.debug("---Operation already applied to the gen " + modificationInstance);
//                    currentStat.getIngredientsStats().setAlreadyApplied(variant.getId());
                    i --;
                    continue;
                }
                variant.putModificationInstance(generation, modificationInstance);

                oneOperationCreated = true;
                genMutated++;
                // We analyze all gens
                if (!ConfigurationProperties.getPropertyBool("allpoints")) {//problem
                    break;
                }

            } else {// Not gen created
                log.debug("---modifPoint " + (nroGen++) + " not mutation generated in  "
                        + StringUtil.trunc(modificationPoint.getCodeElement().toString()));
                notmut++;
            }
        }

        if (oneOperationCreated && !ConfigurationProperties.getPropertyBool("resetoperations")) {
            updateVariantGenList(variant, generation);
        }
        log.debug("\n--Summary Creation: for variant " + variant + " gen mutated: " + genMutated + " , gen not mut: "
                + notmut + ", gen not applied  " + notapplied);

        currentStat.getIngredientsStats().commitStatsOfTrial();

        return oneOperationCreated;
    }

    public void prepareNextGeneration(List<ProgramVariant> temporalInstances, int generation) {
        // After analyze all variant
        // New population creation:
        // show all and search solutions:

        // We filter the solution from the rest
        String solutionId = "";
        for (ProgramVariant programVariant : temporalInstances) {
            if (programVariant.isSolution()) {
                this.solutions.add(programVariant);
                solutionId += programVariant.getId() + "(SOLUTION)(f=" + programVariant.getFitness() + ")" + ", ";
            }
        }
        log.debug("End analysis generation - Solutions found:" + "--> (" + solutionId + ")");

        variants = populationControler.selectProgramVariantsForNextGeneration(variants, temporalInstances,
                ConfigurationProperties.getPropertyInt("population"), variantFactory, originalVariant, generation);

    }

    public void beforeGenerate(int generation) {
        if (ConfigurationProperties.getPropertyBool("applyCrossover")) {
            applyCrossover(generation);
        }
    }


    private void applyCrossover(int generation) {
        int numberVariants = this.variants.size();
        if (numberVariants <= 1) {
            log.debug("CO|Not Enough variants to apply Crossover");
            return;
        }
        double crossoverProb = ConfigurationProperties.getPropertyDouble("crossoverProb");
        int crossoverSize = (int) Math.ceil(crossoverProb * numberVariants) / 2 * 2;//make sure it is even
        if (crossoverSize < 2) {
            //should not be access. never!
            return;
        }

        //random choose ${crossoverSize} variants
        List<ProgramVariant> remaining = new ArrayList<>(this.variants);
        List<ProgramVariant> found = new ArrayList<>();
        int counter = 0;
        while (found.size() < crossoverSize && counter < remaining.size()) {
            counter ++;
            int idx = RandomManager.nextInt(remaining.size());
            if (remaining.get(idx).getOperations().isEmpty()) {
                log.debug("CO|Not Enough ops to apply Crossover");
                continue;
            }
            ProgramVariant target = remaining.remove(idx);
            found.add(target);
        }
        if (found.size() < crossoverSize) {
            log.debug("CO|Not Enough ops to apply Crossover");
            return;
        }
        if (ConfigurationProperties.getPropertyBool("reserveParentInCrossover")) {
            remaining.addAll(found);
        }

        //for each turn, randomly choose the two variants to crossover. total turns=${crossoverSize}/2
        counter = 0;
        List<ProgramVariant> crossoverParents = new ArrayList<>(found);
        found = new ArrayList<>();
        while (counter ++ < crossoverSize / 2) {
            int idx= RandomManager.nextInt(crossoverParents.size());
            ProgramVariant v1 = crossoverParents.remove(idx);
            idx = RandomManager.nextInt(crossoverParents.size());
            ProgramVariant v2 = crossoverParents.remove(idx);
            found.add(v1);
            found.add(v2);
            if (v1.getOperationsSize() <= 1 && v2.getOperationsSize() <= 1) {
                log.debug("CO|Not Enough ops to apply Crossover");
                continue;
            }

            randomlyChangeOperations(v1, v2);
            // update each fitness of variants
            setFitnessForVariant(v1);
            setFitnessForVariant(v2);
        }
        remaining.addAll(found);
        assert remaining.size() >= this.variants.size();
        this.variants = new ArrayList<>(remaining);
    }

    private void randomlyChangeOperations(ProgramVariant v1, ProgramVariant v2) {
        // we randomly select the generations to apply//+1?
        Object[] gens1 = v1.getOperations().keySet().toArray();
        Object[] gens2 = v2.getOperations().keySet().toArray();
        Arrays.sort(gens1);
        Arrays.sort(gens2);

        int rgen1index = RandomManager.nextInt(gens1.length);
        int rgen2index = RandomManager.nextInt(gens2.length);

        //select the order of op
        int op1index = RandomManager.nextInt(v1.getOperations((int) gens1[rgen1index]).size());
        int op2index = RandomManager.nextInt(v2.getOperations((int) gens2[rgen2index]).size());

        //crossover: change all ops before op1index and op2index
        Map<Integer, List<OperatorInstance>> preorder1 = getPreorderOps(v1, gens1, rgen1index, op1index);
        Map<Integer, List<OperatorInstance>> preorder2 = getPreorderOps(v2, gens2, rgen2index, op2index);

        // The generation of both new operators is the Last one.
        // In the first variant we put the operator taken from the 2 one.
        changeops(v1, preorder2);
        // In the second variant we put the operator taken from the 1 one.
        changeops(v2, preorder1);
    }

    private Map<Integer, List<OperatorInstance>> getPreorderOps(ProgramVariant pv, Object[] gens, int genidx, int opidx) {
        Map<Integer, List<OperatorInstance>> preorder = new HashMap<>();
        for (int i = 0; i < genidx; i++) {
            List<OperatorInstance> parentops1 = pv.getOperations().remove((int) gens[i]);
            preorder.put((int)gens[i], parentops1);
        }
        int gen = (int) gens[genidx];
        preorder.put(gen, new ArrayList<>());
        List<OperatorInstance> ops = new ArrayList<>(pv.getOperations().get(gen));
        assert !ops.isEmpty();
        for (int i = 0; i <= opidx && !ops.isEmpty(); i++) {//problem here, ops should never be empty!
            preorder.get(gen).add(ops.get(i));
            pv.getOperations(gen).remove(i);
        }
//        if (pv.getOperations(gen).isEmpty()) {
//            pv.getOperations().remove(gen);
//        }
        return preorder;
    }

    private void changeops(ProgramVariant pv, Map<Integer, List<OperatorInstance>> preorder) {
        for (Integer key: preorder.keySet()) {
            List<OperatorInstance> preOps = preorder.get(key);
            List<OperatorInstance> leftOps = pv.getOperations().get(key);
            if (leftOps != null) {
                preOps.addAll(leftOps);
            }
            pv.getOperations().put(key, preOps);
        }
    }

}
