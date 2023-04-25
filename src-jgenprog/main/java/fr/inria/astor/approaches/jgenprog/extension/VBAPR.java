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
import fr.inria.astor.util.PatchDiffCalculator;
import fr.inria.astor.util.FileTools;
import fr.inria.astor.util.StringUtil;
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

public class VBAPR  extends JGenProg {
    protected static Logger log = Logger.getLogger(VBAPR.class.getSimpleName());
    protected static Logger detailLog = Logger.getLogger("DetailLog");
    protected static Logger fitASim = Logger.getLogger("FitASim");

    private Map<String, List<OperatorInstance>> cache;

    public VBAPR(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade) throws JSAPException, FileNotFoundException {
        super(mutatorExecutor, projFacade);
        setPropertyIfNotDefined(ExtensionPoints.OPERATORS_SPACE.identifier, "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        setPropertyIfNotDefined(ExtensionPoints.TARGET_CODE_PROCESSOR.identifier, "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cache = new HashMap<>();
//        log.error("vbapr error log");
    }

    private String getKey(ModificationPoint modifPoint, AstorOperator astorOperator) {
        return modifPoint.identified + "-" + modifPoint.getCodeElement().toString() + "-" + astorOperator.toString();
    }

    boolean isOpInstanceUsed(OperatorInstance operatorInstance) {
        String key = getKey(operatorInstance.getModificationPoint(), operatorInstance.getOperationApplied());
        if (!cache.containsKey(key)) {
            cache.put(key, new ArrayList<>());
        }
        boolean flag = cache.get(key).contains(operatorInstance);
        if (!flag) {
            cache.get(key).add(operatorInstance);
        }
        return flag;
    }

    boolean isOpInstanceUsed(ModificationPoint modifPoint, AstorOperator astorOperator) {
        String key = getKey(modifPoint, astorOperator);
        if (!cache.containsKey(key)) {
            cache.put(key, new ArrayList<>());
        }
        boolean flag = cache.get(key).contains(new OperatorInstance(modifPoint, astorOperator, null, null));
        if (!flag) {
            cache.get(key).add(new OperatorInstance(modifPoint, astorOperator, null, null));
        }
        return flag;
    }

    boolean isOpInstanceUsed(ModificationPoint modifPoint, AstorOperator astorOperator, CtElement modified) {
        boolean flag = false;
        if (this.ingredientSearchStrategy instanceof SimpleRandomSelectionIngredientStrategy) {
            String key = ((SimpleRandomSelectionIngredientStrategy) this.ingredientSearchStrategy).getKey(modifPoint, astorOperator);
            String value = astorOperator.name();
            if (astorOperator.needIngredient()) {
                value = modified.toString();
            }
            if (!((SimpleRandomSelectionIngredientStrategy) this.ingredientSearchStrategy).cache.containsKey(key)) {
                ((SimpleRandomSelectionIngredientStrategy) this.ingredientSearchStrategy).cache.put(key, new ArrayList<>());
            } else if (((SimpleRandomSelectionIngredientStrategy) this.ingredientSearchStrategy).cache.get(key).contains(value)){
                flag = true;
            }
            ((SimpleRandomSelectionIngredientStrategy) this.ingredientSearchStrategy).cache.get(key).add(value);
        }
        return flag;
    }

    @Override
    public OperatorInstance createOperatorInstanceForPoint(ModificationPoint modificationPoint) throws IllegalAccessException {

        SuspiciousModificationPoint suspModificationPoint = (SuspiciousModificationPoint) modificationPoint;
        AstorOperator operatorSelected = null;
        List<OperatorInstance> operatorInstances = null;
        for (int i = 0; i < operatorSpace.size(); i ++) {
            operatorSelected = operatorSelectionStrategy.getNextOperator(suspModificationPoint);
            if (operatorSelected == null)
                continue;
            if (operatorSelected.canBeAppliedToPoint(modificationPoint)) {
                if (!operatorSelected.needIngredient()) {
                    if (isOpInstanceUsed(modificationPoint, operatorSelected))
                        continue;
                    operatorInstances = operatorSelected.createOperatorInstances(modificationPoint);
                } else {
                    IngredientBasedOperator ingbasedapproach = (IngredientBasedOperator) operatorSelected;

                    Ingredient ingredient = this.ingredientSearchStrategy.getFixIngredient(modificationPoint,
                            operatorSelected);
                    if (ingredient == null)
                        continue;

                    List<OperatorInstance> instances = ingbasedapproach.createOperatorInstances(modificationPoint, ingredient,
                            this.ingredientTransformationStrategy);
                    operatorInstances = new ArrayList<>();
                    for (OperatorInstance op : instances) {
                        if (!isOpInstanceUsed(op))
                            operatorInstances.add(op);
                    }
                }
                if (operatorInstances != null && !operatorInstances.isEmpty())
                    break;
            }
        }
        if (operatorInstances != null && !operatorInstances.isEmpty())
            return selectRandomly(operatorInstances);
        else {
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

        beforeGenerate(generation);
        detailLog.info("----------------- Generation " + generation);
        log.info("after apply crossover, we got " + variants.size() + " variants to mutate.");
        logProgramVariant(variants, generation, false);

        for (int i = 0; i < variants.size(); i++) {
            ProgramVariant parentVariant = variants.get(i);

            log.debug("**Parent Variant: " + parentVariant);

            this.saveOriginalVariant(parentVariant);
            ProgramVariant newVariant = null;
//            if (generation == 1) {
//                newVariant = createNewProgramVariantInitial(parentVariant);
//            } else
                newVariant = createNewProgramVariant(parentVariant, generation, i);

            if (newVariant == null) {
                continue;
            }
            this.saveModifVariant(newVariant);//

            if (!ConfigurationProperties.getPropertyBool("skipCompilation")){
                boolean solution = false;

                if (ConfigurationProperties.getPropertyBool("antipattern")) {
                    if (!AntiPattern.isAntiPattern(newVariant, generation)) {
                        temporalInstances.add(newVariant);
                        solution = processCreatedVariant(newVariant, generation);
                    }
                } else {
                    solution = processCreatedVariant(newVariant, generation);
                    temporalInstances.add(newVariant);
                    if (newVariant.getFitness() == Double.MAX_VALUE) {
                        detailLog.debug("variant can not compile or an error happened in testing process(such as do not terminate within wait time or out of memory");
                    }
                }

                HashMap<String, List<String>> affectedMap = null;
                if (ConfigurationProperties.getPropertyBool("addSimilarityComparasion")) {
//                setSimilarityForPV(newVariant);//ForPV
                    affectedMap = newVariant.computeAffectedStringOfClassesAndBlocks(false);
                }

                for (OperatorInstance op :newVariant.getOperations(generation)) {
                    log.info("mod_point " + op.getModificationPoint());
                    log.info("-->op: " + op);
                }

                boolean succ = false;
                if (solution) {
                    foundSolution = true;
                    newVariant.setBornDate(new Date());
                    succ = saveVariantWithCheck(newVariant);
                }
                foundOneVariant = true;
                // Finally, reverse the changes done by the child
                reverseOperationInModel(newVariant, generation);
                boolean validation = this.validateReversedOriginalVariant(newVariant);
                assert validation;

                if (affectedMap != null) {
                    setSimilarityForSnippets(newVariant, affectedMap);
                    fitASim.info(newVariant.getFitness() + "," + newVariant.getSimilarity());
                }

                if (solution && succ) {
//                    temporalInstances.add(newVariant);
                    this.savePatch(newVariant);
                }
            } else {
                boolean succ = saveVariantWithCheck(newVariant);
                reverseOperationInModel(newVariant, generation);
                boolean validation = this.validateReversedOriginalVariant(newVariant);
                assert validation;
                if (!succ)
                    continue;
                temporalInstances.add(newVariant);
            }

            if (foundSolution && ConfigurationProperties.getPropertyBool("stopfirst")) {
                break;
            }

        }
        log.info("after generation " + generation + ", we got " + temporalInstances.size() + " children");
        logProgramVariant(temporalInstances, generation, true);
        prepareNextGeneration(temporalInstances, generation);

        if (!foundOneVariant)
            this.nrGenerationWithoutModificatedVariant++;
        else {
            this.nrGenerationWithoutModificatedVariant = 0;
        }

        return foundSolution;
    }

    protected ProgramVariant createNewProgramVariant(ProgramVariant parentVariant, int generation, int mpidx) throws Exception {
        // This is the copy of the original program
        ProgramVariant childVariant = variantFactory.createProgramVariantFromAnother(parentVariant, generation);
        log.debug("\n--Child created id: " + childVariant.getId());

        // Apply previous operations (i.e., from previous operators)
        applyPreviousOperationsToVariantModel(childVariant, generation);

        boolean isChildMutatedInThisGeneration = modifyProgramVariant(childVariant, generation, mpidx);

        if (!isChildMutatedInThisGeneration) {
            log.debug("--Not Operation generated in child variant: " + childVariant);
            reverseOperationInModel(childVariant, generation);
            return null;
        }

        boolean appliedOperations = applyNewOperationsToVariantModel(childVariant, generation);

        if (!appliedOperations) {
            log.debug("---Not Operation applied in child variant:" + childVariant);
            reverseOperationInModel(childVariant, generation);
            return null;
        }

        return childVariant;
    }


    public boolean modifyProgramVariant(ProgramVariant variant, int generation, int mpsidx) throws Exception {

        log.debug("--Creating new operations for variant " + variant);
        boolean oneOperationCreated = false;
        int genMutated = 0, notmut = 0, notapplied = 0;
        int nroGen = 0;

        this.currentStat.getIngredientsStats().sizeSpaceOfVariant.clear();

        // We retrieve the list of modification point ready to be navigated
        // sorted a criterion
        List<ModificationPoint> modificationPointsToProcess = null;
        if (this.suspiciousNavigationStrategy instanceof ForceOrderSuspiciousNavitation) {
            modificationPointsToProcess = ((ForceOrderSuspiciousNavitation)this.suspiciousNavigationStrategy)
                    .getSortedModificationPointsList(variant.getModificationPoints(), mpsidx);
        } else {
            modificationPointsToProcess = this.suspiciousNavigationStrategy
                    .getSortedModificationPointsList(variant.getModificationPoints());
        }

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

                //set the op type
                variant.setLastOp(modificationInstance.getOperationApplied().name()
                        + "-" + (modificationInstance.getOriginal() == null ? "" : modificationInstance.getOriginal().getClass().getSimpleName())
                        + "-" + (modificationInstance.getModified() == null ? "" : modificationInstance.getModified().getClass().getSimpleName()));

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
            if (programVariant.isSolution()) {// && programVariant.getPatchDiff().getFormattedDiff() != null
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
        }//
        crossoverSize = found.size();
        if (crossoverSize < 2) {
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
            v1 = variantFactory.createProgramVariantFromAnother(v1, generation);
            v2 = variantFactory.createProgramVariantFromAnother(v2, generation);

            randomlyChangeOperations(v1, v2);
            // update each fitness of variants
            updatePVAfterCrossover(v1);
            updatePVAfterCrossover(v2);
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

        int gen1startidx = RandomManager.nextInt(gens1.length);
        int gen1endidx = RandomManager.nextInt(gens1.length);
        if (gen1endidx < gen1startidx)
            gen1endidx = gen1startidx;
        int gen2startidx = RandomManager.nextInt(gens2.length);
        int gen2endidx = RandomManager.nextInt(gens2.length);
        if (gen2endidx < gen2startidx)
            gen2endidx = gen2startidx;

        //select the order of op
        int op1startidx = RandomManager.nextInt(v1.getOperations((Integer) gens1[gen1startidx]).size());
        int op1endidx = RandomManager.nextInt(v1.getOperations((Integer) gens1[gen1endidx]).size());
        if (gen1startidx == gen1endidx && op1endidx < op1startidx)
            op1endidx = op1startidx;
        int op2startidx = RandomManager.nextInt(v2.getOperations((Integer) gens2[gen2startidx]).size());
        int op2endidx = RandomManager.nextInt(v2.getOperations((Integer) gens2[gen2endidx]).size());
        if (gen2startidx == gen2endidx && op2endidx < op2startidx)
            op2endidx = op2startidx;

        //crossover: change all ops before op1index and op2index
//        Map<Integer, List<OperatorInstance>> preorder1 = getPreorderOps(v1, gens1, gen1startidx, op1startidx);
//        Map<Integer, List<OperatorInstance>> preorder2 = getPreorderOps(v2, gens2, gen2startidx, op2startidx);
        Map<Integer, List<OperatorInstance>> preorder1 = getPreorderOps(v1, gens1,
                gen1startidx, gen1endidx, op1startidx, op1endidx);
        Map<Integer, List<OperatorInstance>> preorder2 = getPreorderOps(v2, gens2,
                gen2startidx, gen2endidx, op2startidx, op2endidx);


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
        for (int i = 0; i <= opidx; i++) {//ops should never be empty!
            OperatorInstance op = ops.get(i);
            preorder.get(gen).add(op);
            pv.getOperations(gen).remove(op);//
        }
        removeEmptyOps(pv, gen);
        return preorder;
    }


    private Map<Integer, List<OperatorInstance>> getPreorderOps(ProgramVariant pv, Object[] gens,
                        int genstartidx, int genendidx, int opstartidx, int opendidx) {
        Map<Integer, List<OperatorInstance>> preorder = new HashMap<>();
        List<OperatorInstance> parentops = pv.getOperations().remove((Integer) gens[genstartidx]);
        List<OperatorInstance> childpreops = new ArrayList<>();
        for (int j = opstartidx; j < parentops.size(); j++) {
            childpreops.add(parentops.remove(j));
        }
        pv.getOperations().put((Integer) gens[genstartidx], parentops);
        removeEmptyOps(pv, (Integer) gens[genstartidx]);
        preorder.put((Integer)gens[genstartidx], childpreops);
        for (int i = genstartidx + 1; i < genendidx; i++) {
            parentops = pv.getOperations().remove((Integer) gens[i]);
            preorder.put((int)gens[i], parentops);
        }
        if (genstartidx == genendidx)
            return preorder;
        parentops = pv.getOperations().remove((Integer) gens[genendidx]);
        childpreops = new ArrayList<>();
        for (int j = 0; j < opendidx; j++) {
            childpreops.add(parentops.remove(j));
        }
        pv.getOperations().put((Integer) gens[genendidx], parentops);
        removeEmptyOps(pv, (Integer) gens[genendidx]);
        preorder.put((Integer)gens[genendidx], childpreops);
        return preorder;
    }

    private void removeEmptyOps(ProgramVariant pv, int gen) {
        if (pv.getOperations(gen).isEmpty()) {
            pv.getOperations().remove(gen);
        }
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

    private void updatePVAfterCrossover(ProgramVariant variant) {
        if (!ConfigurationProperties.getPropertyBool("skipValidation")) {
            setFitnessForVariant(variant);
        }
//        variant.setModificationPoints(originalVariant.getModificationPoints());
//        for (Integer gen : variant.getOperations().keySet()) {
//            updateVariantGenList(variant, gen);
//        }
    }

    public void logProgramVariant(List<ProgramVariant> pvs, int generation, boolean logopinfo) {
        for (ProgramVariant pv :pvs) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(pv + " fitness: " + pv.getFitness());
            if (!logopinfo) {
                detailLog.info(stringBuilder.toString());
                continue;
            }
            Map<Integer, List<OperatorInstance>> genops = pv.getOperations();
            if (!genops.get(generation).isEmpty()) {
                stringBuilder.append("\n").append("operation instances at generation: " + generation);
                for (OperatorInstance op : genops.get(generation)) {
                    stringBuilder.append("\n").append("modification point: " + op.getModificationPoint());
                    stringBuilder.append("\n").append("operator type: " + op.getOperationApplied().name());
                    stringBuilder.append("\n").append("original: " + op.getOriginal().toString().replaceAll("\\n", ""));
                    stringBuilder.append("\n").append("modified: " + (op.getModified() == null ? "null" : op.getModified().toString().replaceAll("\\n", "")));
                }
            }
            detailLog.info(stringBuilder.toString());
        }
    }


    private void setSimilarityForPV(ProgramVariant newVariant) {
        EditDistanceWithTokens editor = new EditDistanceWithTokens();
        Map<Integer, List<OperatorInstance>> genops = newVariant.getOperations();
        double res = 1d;
        StringBuilder originalsb = new StringBuilder(), modifiedsb = new StringBuilder();
        for (Integer gen : genops.keySet()) {
            List<OperatorInstance> ops = genops.get(gen);
            for (OperatorInstance op :ops) {
                CtElement original = op.getOriginal();
                originalsb.append(" ").append(original.toString().replaceAll("\n", ""));
                CtElement modified = op.getModified();
                modifiedsb.append(" ").append(modified == null ? " " : modified.toString().replaceAll("\n", ""));
            }
        }
        String ed = editor.calEditDisctance(originalsb, modifiedsb);
        res = Double.parseDouble(ed);
        newVariant.setSimilarity(res);
    }

    private void setSimilarityForSnippets(ProgramVariant newVariant, HashMap<String, List<String>> affectedMap) {
        EditDistanceWithTokens editor = new EditDistanceWithTokens();
        double res = 1d;
        HashMap<String, List<String>> origianl = newVariant.computeAffectedStringOfClassesAndBlocks(true);
        assert origianl.values().size() == affectedMap.values().size();
        StringBuilder originalsb = getSnippets(origianl);
        StringBuilder modifiedsb = getSnippets(affectedMap);
        String ed = editor.calEditDisctance(originalsb, modifiedsb);
        res = Double.parseDouble(ed);
        newVariant.setSimilarity(res);
    }

    private StringBuilder getSnippets(HashMap<String, List<String>> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (List<String> blocks :map.values()) {
            for (String block :blocks) {
                stringBuilder.append(" ").append(block);
            }
        }
        return stringBuilder;
    }

    List<String> solutions_f = new ArrayList<>();

    public boolean saveVariantWithCheck(ProgramVariant programVariant) throws Exception {
        final boolean codeFormated = true;
        savePatchDiff(programVariant, codeFormated);
        savePatchDiff(programVariant, !codeFormated);
        return computePatchDiff(new PatchDiffCalculator(), programVariant, solutions_f);
    }

    private boolean computePatchDiff(PatchDiffCalculator cdiff, ProgramVariant solutionVariant, List<String> solutions_f) throws Exception {
        if (solutionVariant.getPatchDiff() != null) {
            return true;
        }
        this.solutions.add(solutionVariant);

        PatchDiff pdiff = new PatchDiff();
        boolean format = false;

        String diffPatchOriginalAlign = cdiff.getDiff(getProjectFacade(), this.solutions, this.solutions.indexOf(solutionVariant),
                    this.mutatorSupporter, format, solutions_f);

        pdiff.setOriginalStatementAlignmentDiff(diffPatchOriginalAlign);

        format = true;

        String diffPatchFormated = cdiff.getDiff(getProjectFacade(), this.solutions, this.solutions.indexOf(solutionVariant),
                    this.mutatorSupporter, format, solutions_f);
        if (diffPatchFormated == null) {
            this.solutions.remove(solutionVariant);
            return false;
        }

        pdiff.setFormattedDiff(diffPatchFormated);

        solutionVariant.setPatchDiff(pdiff);
        this.solutions.remove(solutionVariant);
        return true;
    }

    @Override
    public void atEnd() {
        Logger infolog = LogManager.getLogger("InfoLog");
        long engineStartT = dateEngineCreation.getTime();
        long startT = dateInitEvolution.getTime();
        long endT = System.currentTimeMillis();
        infolog.info("Time Engine Creation(s): " + (startT - engineStartT) / 1000d);
        currentStat.getGeneralStats().put(Stats.GeneralStatEnum.ENGINE_CREATION_TIME, (startT - engineStartT) / 1000d);
        infolog.info("Time Repair Loop (s): " + (endT - startT) / 1000d);
        currentStat.getGeneralStats().put(Stats.GeneralStatEnum.TOTAL_TIME, ((endT - startT)) / 1000d);
        infolog.info("generationsexecuted: " + this.generationsExecuted);

        currentStat.getGeneralStats().put(Stats.GeneralStatEnum.OUTPUT_STATUS, this.getOutputStatus());
//		currentStat.getGeneralStats().put(GeneralStatEnum.EXECUTION_IDENTIFIER,
//				ConfigurationProperties.getProperty("projectIdentifier"));
        //
        if (!ConfigurationProperties.getPropertyBool("skipfaultlocalization"))
            currentStat.getGeneralStats().put(Stats.GeneralStatEnum.FAULT_LOCALIZATION,
                    ConfigurationProperties.getProperty("faultlocalization").toString());

        this.printFinalStatus();


        boolean flag = true;
        List<ProgramVariant> outputs = new ArrayList<>();
        if (!ConfigurationProperties.getPropertyBool("skipValidation")) {
            flag = this.solutions.size() > 0;
            outputs = this.solutions;
            this.sortPatches();
        } else {
            outputs = this.solutions;
//			try {
//
//				this.computePatchDiff(outputs);
//
//			} catch (Exception e) {
//				log.error("Problem at computing diff" + e);
//			}
        }
        if (flag) {
            log.info(this.getSolutionData(outputs, this.generationsExecuted) + "\n");
            patchInfo = createStatsForPatches(outputs, generationsExecuted, dateInitEvolution);
        } else {
            patchInfo = new ArrayList<>();
        }
        // Reporting results
        String output = this.projectFacade.getProperties().getWorkingDirRoot();
        if (ConfigurationProperties.getPropertyBool("removeworkingfolder")) {
            String[] deldirs = new String[2];
            deldirs[0] = output + File.separator + "bin";
            deldirs[1] = output + File.separator + "src" + File.separator + ProgramVariant.DEFAULT_ORIGINAL_VARIANT;
            for (String deldir: deldirs) {
                File fout = new File(deldir);
                try {
                    FileUtils.deleteDirectory(fout);
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error(e);
                }
            }
        }
        for (ReportResults out : this.getOutputResults()) {
            out.produceOutput(patchInfo, this.currentStat.getGeneralStats(), output);
        }
        try {
            List<SuspiciousCode> susp = new ArrayList<>();
            for (ModificationPoint mpi : originalVariant.getModificationPoints()) {
                SuspiciousModificationPoint smpi = (SuspiciousModificationPoint) mpi;
                if (!susp.contains(smpi.getSuspicious())) {
                    susp.add(smpi.getSuspicious());
                    String noout = (ConfigurationProperties.hasProperty("outfl")
                            ? ConfigurationProperties.getProperty("outfl")
                            : output);
                    File f = (new File(noout));
                    if (!f.exists()) {
                        f.mkdirs();
                    }

                    FileWriter fw = new FileWriter(noout + File.separator + "suspicious_"
                            + this.projectFacade.getProperties().getFixid() + ".json");
                    for (SuspiciousCode suspiciousCode : susp) {
                        fw.append(suspiciousCode.toString());
                        fw.append("\n");
                    }
                    fw.flush();
                    fw.close();
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
