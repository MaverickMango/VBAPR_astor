package fr.inria.astor.test.repair.approaches.VBAPR;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.inria.astor.approaches.jgenprog.extension.VBAPR;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.astor.core.stats.PatchHunkStats;
import fr.inria.astor.test.repair.core.BaseEvolutionaryTest;
import fr.inria.main.CommandSummary;
import fr.inria.main.evolution.VBAPRMain;
import fr.inria.main.test.TestArgsUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ConsistenceTest extends BaseEvolutionaryTest {
    File out = null;
    static VBAPRMain main;
    static TestArgsUtil argsUtil;
    static String fileName = "";
    private String proj;
    private String version;

    public ConsistenceTest(String proj, String version) {
        this.proj = proj;
        this.version = version;
        out = new File(ConfigurationProperties.getProperty("workingDirectory"));
    }

    @Parameterized.Parameters
    public static Collection<String[]> data() {
        String[][] data = {
                {"Chart", "20"},
                {"Math", "11"},
                {"Math", "58"},
                {"Math", "85"},
                {"Lang", "27"}
        };
        return Arrays.asList(data);
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        argsUtil = new TestArgsUtil("VBAPR");
    }

    @Test(timeout = 10800000)
    public void testSoulutionFound() throws Exception {
        String[] args = argsUtil.getArgs(proj, version);
        main = new VBAPRMain();
        main.execute(args);
        AstorCoreEngine engine = main.getEngine();
        assertEquals(true, engine.getOriginalVariant().getModificationPoints().size() > 0);
        List<ProgramVariant> solutions = engine.getVariants();
        assertEquals(true, solutions.size() > 0);
        testResult4exhausted_Edit4Exp_compiled_extractStmt(engine);
    }

    boolean testResult4exhausted_Edit4Exp_compiled_extractStmt(AstorCoreEngine engine) {
        if (proj.equals("Lang") && version.equals("27")) {
            PatchHunkStats hunkSolution = getHunkSolution(engine.getPatchInfo(), "(expPos < decPos) || (str.length() > 0)", "CtBinaryOperatorImpl|CtIfImpl");
            Assert.assertNotNull(hunkSolution);
            Assert.assertEquals(1177, engine.getSolutions().size());
        }
        if (proj.equals("Chart") && version.equals("20")) {
            Assert.assertEquals(6, engine.getSolutions().size());
        }
        if (proj.equals("Math") && version.equals("58")) {
            PatchHunkStats hunkSolution = getHunkSolution(engine.getPatchInfo(), "fit(guess)", "CtInvocationImpl|CtReturnImpl");
            Assert.assertNotNull(hunkSolution);
//            Assert.assertEquals(5, engine.getSolutions().size());
        }
        if (proj.equals("Math") && version.equals("11")) {
            PatchHunkStats hunkSolution = getHunkSolution(engine.getPatchInfo(), "((double) (dim))", "CtVariableReadImpl|CtUnaryOperatorImpl");
            Assert.assertNotNull(hunkSolution);
            Assert.assertEquals(78, engine.getSolutions().size());
        }
        if (proj.equals("Math") && version.equals("85")) {
            PatchHunkStats hunkSolution = getHunkSolution(engine.getPatchInfo(), "(fa * fb) > 0.0", "CtBinaryOperatorImpl|CtIfImpl");
            Assert.assertNotNull(hunkSolution);
            Assert.assertEquals(235, engine.getSolutions().size());
        }
        return true;
    }

}
