package fr.inria.astor.test.repair.approaches.VBAPR;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.astor.core.solutionsearch.EvolutionarySearchEngine;
import fr.inria.astor.test.repair.core.BaseEvolutionaryTest;
import fr.inria.main.CommandSummary;
import fr.inria.main.evolution.AstorMain;
import fr.inria.main.evolution.VBAPRMain;
import org.apache.log4j.Level;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class VBAPRTest extends BaseEvolutionaryTest {
    File out = null;

    public VBAPRTest() {
        out = new File(ConfigurationProperties.getProperty("workingDirectory"));
    }

    @Test
    public void testGenprogOri() throws Exception {
        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.DEBUG);
        AstorMain main1 = new AstorMain();
        CommandSummary cs = new CommandSummary();
        cs.command.put("-mode", "jGenprog");
        cs.command.put("-srcjavafolder", "/src/main/java/");
        cs.command.put("-srctestfolder", "/src/test/");
        cs.command.put("-binjavafolder", "/target/classes/");
        cs.command.put("-bintestfolder", "/target/test-classes/");
        cs.command.put("-location", "/home/liu/Desktop/astor/examples/math_73/");
        cs.command.put("-dependencies", "examples/math_73/lib");
        cs.command.put("-maxgen", "100");

        main1.execute(cs.flat());
        AstorCoreEngine engine = main1.getEngine();
        List<ProgramVariant> variants = engine.getVariants();
        assertTrue(variants.size() > 0);

    }

    @Test
    public void testVBAPRMath() throws Exception {
        org.apache.log4j.LogManager.getLogger(EvolutionarySearchEngine.class).setLevel(Level.DEBUG);
//        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.DEBUG);
        VBAPRMain main1 = new VBAPRMain();
        CommandSummary cs = new CommandSummary();
        cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR");
        cs.command.put("-srcjavafolder", "/src/main/java/");
        cs.command.put("-srctestfolder", "/src/test/");
        cs.command.put("-binjavafolder", "/target/classes/");
        cs.command.put("-bintestfolder", "/target/test-classes/");
        cs.command.put("-location", "/home/liu/Desktop/astor/examples/math_1/");
        cs.command.put("-dependencies", "examples/math_1/lib");
        cs.command.put("-faultlocalization", "fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT");
        cs.command.put("-maxgen", "1");
//        cs.command.put("-population", "2");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.GTBRepairOperatorSpace");
        cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy");

        main1.execute(cs.flat());
        AstorCoreEngine engine = main1.getEngine();
        List<ProgramVariant> variants = engine.getVariants();
        assertTrue(variants.size() > 0);
    }

    @Test
    public void testVBAPRLang() throws Exception {
        org.apache.log4j.LogManager.getLogger(EvolutionarySearchEngine.class).setLevel(Level.DEBUG);
//        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.DEBUG);
        VBAPRMain main1 = new VBAPRMain();
        CommandSummary cs = new CommandSummary();
        cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR");
        cs.command.put("-srcjavafolder", "/src/main/java/");
        cs.command.put("-srctestfolder", "/src/test/");
        cs.command.put("-binjavafolder", "/target/classes/");
        cs.command.put("-bintestfolder", "/target/tests/");
        cs.command.put("-location", "/home/liu/Desktop/astor/examples/lang_63/");
        cs.command.put("-dependencies", "examples/lang_63/lib");
        cs.command.put("-faultlocalization", "fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT");
        cs.command.put("-maxgen", "1");
//        cs.command.put("-population", "2");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.GTBRepairOperatorSpace");
        cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy");

        main1.execute(cs.flat());
        AstorCoreEngine engine = main1.getEngine();
        List<ProgramVariant> variants = engine.getVariants();
        assertTrue(variants.size() > 0);
    }

    @Test
    public void testVBAPRTime() throws Exception {
        org.apache.log4j.LogManager.getLogger(EvolutionarySearchEngine.class).setLevel(Level.DEBUG);
//        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.DEBUG);
        VBAPRMain main1 = new VBAPRMain();
        CommandSummary cs = new CommandSummary();
        cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR");
        cs.command.put("-srcjavafolder", "/src/main/java/");
        cs.command.put("-srctestfolder", "/src/test/");
        cs.command.put("-binjavafolder", "/target/classes/");
        cs.command.put("-bintestfolder", "/target/test-classes/");
        cs.command.put("-location", "/home/liu/Desktop/astor/examples/time_7/");
        cs.command.put("-dependencies", "examples/time_7/lib");
        cs.command.put("-faultlocalization", "fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT");
        cs.command.put("-maxgen", "1");
//        cs.command.put("-population", "2");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.GTBRepairOperatorSpace");
        cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy");

        main1.execute(cs.flat());
        AstorCoreEngine engine = main1.getEngine();
        List<ProgramVariant> variants = engine.getVariants();
        assertTrue(variants.size() > 0);
    }

    @Test
    public void testVBAPRChart() throws Exception {
        org.apache.log4j.LogManager.getLogger(EvolutionarySearchEngine.class).setLevel(Level.DEBUG);
//        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.DEBUG);
        VBAPRMain main1 = new VBAPRMain();
        CommandSummary cs = new CommandSummary();
        cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR");
        cs.command.put("-srcjavafolder", "/source/");
        cs.command.put("-srctestfolder", "/tests/");
        cs.command.put("-binjavafolder", "/build/");
        cs.command.put("-bintestfolder", "/build-tests/");
        cs.command.put("-location", "/home/liu/Desktop/astor/examples/chart_8/");
        cs.command.put("-dependencies", "examples/chart_8/lib");
        cs.command.put("-faultlocalization", "fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT");
        cs.command.put("-maxgen", "100");
//        cs.command.put("-population", "2");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.GTBRepairOperatorSpace");
        cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy");

        main1.execute(cs.flat());
        AstorCoreEngine engine = main1.getEngine();
        List<ProgramVariant> variants = engine.getVariants();
        assertTrue(variants.size() > 0);
    }
}
