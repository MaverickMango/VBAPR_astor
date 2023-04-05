package fr.inria.astor.test.repair.approaches.VBAPR;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.inria.astor.approaches.jgenprog.extension.VBAPR;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.astor.core.stats.PatchHunkStats;
import fr.inria.astor.test.repair.core.BaseEvolutionaryTest;
import fr.inria.main.CommandSummary;
import fr.inria.main.evolution.VBAPRMain;
import fr.inria.main.test.TestArgsUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class ConsistenceTest extends BaseEvolutionaryTest {
    File out = null;

    public ConsistenceTest() {
        out = new File(ConfigurationProperties.getProperty("workingDirectory"));
    }

    void setConfig(CommandSummary cs){
        cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR4Exhausted");
        cs.command.put("-maxgen", "1");
        cs.command.put("-skipValidation", "true");
        cs.command.put("-skipfaultlocalization", "true");
        cs.command.put("-useVariableEdit", "true");
        cs.command.put("-populationcontroller", "fr.inria.astor.core.solutionsearch.population.DiffBasedFitnessPopulationController");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-modificationpointnavigation","fr.inria.astor.core.solutionsearch.navigation.ForceOrderSuspiciousNavitation");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.GTBRepairOperatorSpace");//ReplaceExpOperatorSpace Type
        cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelecIngreSearchStrategy4Exhausted");
        cs.command.put("-ingredienttransformstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.transformations.GTBIngredientTransformationStrategy");
    }

    @Test
    public void testChart20() throws Exception {
        CommandSummary cs = new CommandSummary();
        setConfig(cs);
        TestArgsUtil testArgsUtil = new TestArgsUtil(cs);
        String[] args = testArgsUtil.getArgs("Chart", "20");
        VBAPRMain main = new VBAPRMain();
        main.execute(args);
        AstorCoreEngine engine = main.getEngine();
        Assert.assertEquals(6, engine.getSolutions().size());
    }

    @Test
    public void testMath11() throws Exception {
        CommandSummary cs = new CommandSummary();
        setConfig(cs);
        TestArgsUtil testArgsUtil = new TestArgsUtil(cs);
        String[] args = testArgsUtil.getArgs("Math", "11");
        VBAPRMain main = new VBAPRMain();
        main.execute(args);
        AstorCoreEngine engine = main.getEngine();
        PatchHunkStats hunkSolution = getHunkSolution(engine.getPatchInfo(), "((double) (dim))", "CtVariableReadImpl|CtUnaryOperatorImpl");
        Assert.assertNotNull(hunkSolution);
        Assert.assertEquals(78, engine.getSolutions().size());
    }
}
