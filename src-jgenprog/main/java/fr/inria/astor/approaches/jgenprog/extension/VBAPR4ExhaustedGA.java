package fr.inria.astor.approaches.jgenprog.extension;

import com.martiansoftware.jsap.JSAPException;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.stats.Stats;
import fr.inria.astor.util.FileTools;
import fr.inria.main.AstorOutputStatus;
import fr.inria.main.test.PatchComparator;
import fr.inria.main.test.TestArgsUtil;
import org.apache.log4j.Logger;
import spoon.reflect.declaration.CtType;

import java.io.FileNotFoundException;
import java.util.*;

public class VBAPR4ExhaustedGA extends VBAPR4Exhausted {
    protected static Logger log = Logger.getLogger(VBAPR4Exhausted.class.getSimpleName());
    protected static Logger detailLog = Logger.getLogger("DetailLog");
    protected static Logger fitASim = Logger.getLogger("FitASim");
    protected static List<CtType> fixedCtType;

    public VBAPR4ExhaustedGA(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade) throws JSAPException, FileNotFoundException {
        super(mutatorExecutor, projFacade);
        String fixFileDir = TestArgsUtil.baseDir + "/fixfiles4Astor/" + FileTools.proj.toLowerCase() + "/" + FileTools.proj.toLowerCase() + "_" + FileTools.version;
        List<String> paths = FileTools.getFilePaths(fixFileDir, ".java");
        fixedCtType = new ArrayList<>();
        for (String fixedPath :paths) {
            CtType ctType = FileTools.getCtTypeFromFile(fixedPath);
            assert ctType != null;
            fixedCtType.add(ctType);
        }
    }

    public void startEvolution() throws Exception {

        log.info("----Starting Evolutionary Solution Search");

        generationsExecuted = 0;
        nrGenerationWithoutModificatedVariant = 0;
        boolean stopSearch = false;

        dateInitEvolution = new Date();

        int maxMinutes = ConfigurationProperties.getPropertyInt("maxtime");

        while (!stopSearch) {

            if (!(generationsExecuted < ConfigurationProperties.getPropertyInt("maxGeneration"))) {
                log.debug("\n Max generation reached " + generationsExecuted);
                this.outputStatus = AstorOutputStatus.MAX_GENERATION;
                break;
            }

            if (!(belowMaxTime(dateInitEvolution, maxMinutes))) {
                log.debug("\n Max time reached " + generationsExecuted);
                this.outputStatus = AstorOutputStatus.TIME_OUT;
                break;
            }

            generationsExecuted++;
            // warning level to keep Travis CI log aliive
            log.warn("----------Running generation: " + generationsExecuted + ", population size: "
                    + this.variants.size());
            try {
                boolean solutionFound = processGenerations(generationsExecuted);

                if (solutionFound) {
                    stopSearch =
                            // one solution
                            (ConfigurationProperties.getPropertyBool("stopfirst")
                                    // or nr solutions are greater than max allowed
                                    || (this.solutions.size() >= ConfigurationProperties
                                    .getPropertyInt("maxnumbersolutions")));

                    if (stopSearch) {
                        log.debug("Max Solution found " + this.solutions.size());
                        this.outputStatus = AstorOutputStatus.STOP_BY_PATCH_FOUND;
                    }
                }
            } catch (Throwable e) {
                log.error("Error at generation " + generationsExecuted + "\n" + e.getMessage());
                e.printStackTrace();
                this.outputStatus = AstorOutputStatus.ERROR;
                break;
            }

            if (this.nrGenerationWithoutModificatedVariant >= ConfigurationProperties
                    .getPropertyInt("nomodificationconvergence")) {
                log.error(String.format("Stopping main loop at %d generation", generationsExecuted));
                this.outputStatus = AstorOutputStatus.CONVERGED;
                break;
            }
        }
    }

    @Override
    public boolean processGenerations(int generation) throws Exception {
        log.debug("\n***** Generation " + generation + " : " + this.nrGenerationWithoutModificatedVariant);
        currentStat.increment(Stats.GeneralStatEnum.NR_GENERATIONS);
//        beforeGenerate(generation);

        detailLog.info("----------------- Generation " + generation);
        detailLog.info("after apply crossover, we got " + variants.size() + " variants to mutate.");
        logProgramVariant(variants, generation, false);
        boolean solutionFound = false;
        boolean foundOneVariant = false;
        int maxMinutes = ConfigurationProperties.getPropertyInt("maxtime");
        int v = 0;
        List<ProgramVariant> variantsForOneGen = new ArrayList<>();
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
                        return solutionFound;
                    }

                    try {
                        log.info("mod_point " + modifPoint);
                        log.info("-->op: " + pointOperation);

                        // We validate the variant after applying the operator
                        ProgramVariant solutionVariant = variantFactory.createProgramVariantFromAnother(parentVariant,
                                generationsExecuted);
                        solutionVariant.getOperations().put(generationsExecuted, Arrays.asList(pointOperation));

                        applyNewMutationOperationToSpoonElement(pointOperation);

                        boolean solution = false;

                        if (!ConfigurationProperties.getPropertyBool("skipCompilation")) {
                            solution = processCreatedVariant(solutionVariant, generationsExecuted);
                        }

                        if (solution) {
                            variantsForOneGen.add(solutionVariant);
//                            currentStat.increment(Stats.GeneralStatEnum.TOTAL_VARIANTS_COMPILED);
//                            solutionFound = compareWithFix(solutionVariant);
                            solutionFound = solution;
                            saveVariant(solutionVariant);
                            this.solutions.add(solutionVariant);

                            if (solutionFound && ConfigurationProperties.getPropertyBool("stopfirst")) {
                                this.setOutputStatus(AstorOutputStatus.STOP_BY_PATCH_FOUND);
                                return solutionFound;
                            }
                        }
                    } catch (Exception e) {
                        log.error(e);
                    }

                    foundOneVariant = true;
                    // We undo the operator (for try the next one)
                    undoOperationToSpoonElement(pointOperation);

                    if (!belowMaxTime(dateInitEvolution, maxMinutes)) {

                        this.setOutputStatus(AstorOutputStatus.TIME_OUT);
                        log.debug("Max time reached");
                        return solutionFound;
                    }
                }
            }
        }
        this.variants = variantsForOneGen;
//        prepareNextGeneration(variantsForOneGen, generation);

        if (!foundOneVariant)
            this.nrGenerationWithoutModificatedVariant++;
        else {
            this.nrGenerationWithoutModificatedVariant = 0;
        }

        return solutionFound;
    }

    public boolean compareWithFix(ProgramVariant solutionVariant) {
        boolean solutionFound = false;
        Set<CtType> modifiedTypes = new HashSet<>(solutionVariant.getClassesAffectedByOperators());
//        for (OperatorInstance op :solutionVariant.getAllOperations()) {
//            CtType ctType = op.getModificationPoint().getCodeElement().getParent(CtType.class);
//            modifiedTypes.add(ctType);
//        }
        if (modifiedTypes.size() != fixedCtType.size())
            return solutionFound;
        for (CtType modified: modifiedTypes) {
            for (CtType fixed :fixedCtType) {
                if (!modified.getSimpleName().equals(fixed.getSimpleName()))
                    continue;
                solutionFound = solutionFound || PatchComparator.sameComparedByStr(fixed, modified);
            }
        }
        return solutionFound;
    }

}
