package fr.inria.astor.test.repair.approaches.VBAPR;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.astor.core.solutionsearch.EvolutionarySearchEngine;
import fr.inria.astor.test.repair.core.BaseEvolutionaryTest;
import fr.inria.main.CommandSummary;
import fr.inria.main.evolution.VBAPRMain;
import org.apache.log4j.Level;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperatorTest extends BaseEvolutionaryTest {

    File out = null;

    public OperatorTest() {
        out = new File(ConfigurationProperties.getProperty("workingDirectory"));
    }
    @Test
    public void testVBAPRMath11() throws Exception {
//        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.DEBUG);
        VBAPRMain main1 = new VBAPRMain();
        CommandSummary cs = new CommandSummary();
        cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR");
        cs.command.put("-srcjavafolder", "/src/main/java/");
        cs.command.put("-srctestfolder", "/src/test/");
        cs.command.put("-binjavafolder", "/target/classes/");
        cs.command.put("-bintestfolder", "/target/test-classes/");
        cs.command.put("-location", "/home/liu/Desktop/astor/examples/math_11/");
        cs.command.put("-dependencies", "examples/math_11/lib");
        cs.command.put("-skipfaultlocalization", "true");
//        cs.command.put("-faultlocalization", "fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT");
        cs.command.put("-maxgen", "100");
        cs.command.put("-scope", "file");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.ReplaceExpOperatorSpace");
        cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy");

        main1.execute(cs.flat());
        AstorCoreEngine engine = main1.getEngine();
        List<ProgramVariant> solutions = engine.getSolutions();
        assertEquals(true, solutions.size() > 0);
    }

    @Test
    public void testVBAPRMath33() throws Exception {

        org.apache.log4j.LogManager.getLogger(EvolutionarySearchEngine.class).setLevel(Level.DEBUG);
//        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.DEBUG);
        VBAPRMain main1 = new VBAPRMain();
        CommandSummary cs = new CommandSummary();
        cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR");
        cs.command.put("-srcjavafolder", "/src/main/java/");
        cs.command.put("-srctestfolder", "/src/test/");
        cs.command.put("-binjavafolder", "/target/classes/");
        cs.command.put("-bintestfolder", "/target/test-classes/");
        cs.command.put("-location", "/home/liu/Desktop/astor/examples/math_33/");
        cs.command.put("-dependencies", "examples/math_33/lib");
        cs.command.put("-faultlocalization", "fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT");
        cs.command.put("-maxgen", "100");
        cs.command.put("-scope", "file");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.ReplaceExpOperatorSpace");
        cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy");

        main1.execute(cs.flat());
        AstorCoreEngine engine = main1.getEngine();
        List<ProgramVariant> variants = engine.getVariants();
        assertTrue(variants.size() > 0);
    }



    @Test
    public void testVBAPRMath57() throws Exception {

        org.apache.log4j.LogManager.getLogger(EvolutionarySearchEngine.class).setLevel(Level.DEBUG);
//        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.DEBUG);
        VBAPRMain main1 = new VBAPRMain();
        CommandSummary cs = new CommandSummary();
        cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR");
        cs.command.put("-srcjavafolder", "/src/main/java/");
        cs.command.put("-srctestfolder", "/src/test/");
        cs.command.put("-binjavafolder", "/target/classes/");
        cs.command.put("-bintestfolder", "/target/test-classes/");
        cs.command.put("-location", "/home/liu/Desktop/astor/examples/math_21/");
        cs.command.put("-dependencies", "examples/math_21/lib");
        cs.command.put("-faultlocalization", "fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT");
        cs.command.put("-maxgen", "100");
        cs.command.put("-scope", "file");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.ReplaceTypeOperatorSpace");
        cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy");

        main1.execute(cs.flat());
        AstorCoreEngine engine = main1.getEngine();
        List<ProgramVariant> variants = engine.getVariants();
        assertTrue(variants.size() > 0);
    }

    @Test
    public void testVBAPRChart10() throws Exception {

        org.apache.log4j.LogManager.getLogger(EvolutionarySearchEngine.class).setLevel(Level.DEBUG);
//        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.DEBUG);
        VBAPRMain main1 = new VBAPRMain();
        CommandSummary cs = new CommandSummary();
        cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR");
        cs.command.put("-srcjavafolder", "/source/");
        cs.command.put("-srctestfolder", "/tests/");
        cs.command.put("-binjavafolder", "/build/");
        cs.command.put("-bintestfolder", "/build-tests/");
        cs.command.put("-location", "/home/liu/Desktop/astor/examples/chart_10/");
        cs.command.put("-dependencies", "examples/chart_10/lib");
        cs.command.put("-faultlocalization", "fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT");
        cs.command.put("-maxgen", "200");
//        cs.command.put("-scope", "file");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.ReplaceExpOperatorSpace");
        cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy");

        main1.execute(cs.flat());
        AstorCoreEngine engine = main1.getEngine();
        List<ProgramVariant> variants = engine.getVariants();
        assertTrue(variants.size() > 0);
    }

    @Test
    public void testVBAPRjsoup62() throws Exception {
//        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.DEBUG);
        VBAPRMain main1 = new VBAPRMain();
        CommandSummary cs = new CommandSummary();
        cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR");
        cs.command.put("-srcjavafolder", "/src/main/java/");
        cs.command.put("-srctestfolder", "/src/test/");
        cs.command.put("-binjavafolder", "/target/classes/");
        cs.command.put("-bintestfolder", "/target/test-classes/");
        cs.command.put("-location", "/home/liu/Desktop/astor/examples/jsoup_62/");
//        cs.command.put("-dependencies", "examples/jsoup_62/");
//        cs.command.put("-faultlocalization", "fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT");
        cs.command.put("-maxgen", "100");
//        cs.command.put("-scope", "file");
        cs.command.put("-skipfaultlocalization", "true");
        cs.command.put("-failing", "org.jsoup.parser.HtmlParserTest");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.ReplaceExpOperatorSpace");
        cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy");

        main1.execute(cs.flat());
        AstorCoreEngine engine = main1.getEngine();
        List<ProgramVariant> variants = engine.getVariants();
        assertTrue(variants.size() > 0);
    }

}
