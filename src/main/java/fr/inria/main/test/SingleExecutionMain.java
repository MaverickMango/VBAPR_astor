package fr.inria.main.test;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.main.evolution.VBAPRMain;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SingleExecutionMain {
    static VBAPRMain main;
    static TestArgsUtil argsUtil;
    private static String proj;
    private static String version;

    public static void main(String[] args) throws Exception {
        proj = args[0];
        version = args[1];
        argsUtil = new TestArgsUtil("exhaustedGA");
        main = new VBAPRMain();
        main.execute(argsUtil.getArgs(proj, version));
        AstorCoreEngine engine = main.getEngine();
        List<ProgramVariant> solutions = engine.getSolutions();
        assertEquals(true, solutions.size() > 0);
    }
}
