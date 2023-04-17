package fr.inria.astor.test.repair.approaches.VBAPR.patch;

import fr.inria.astor.util.FileTools;
import fr.inria.main.test.PatchComparator;
import org.junit.Test;

import java.io.File;
import java.util.*;

public class PatchCorrectnessTest {
    String[] projsConsidered = {"Chart", "Math", "Time", "Lang"};
    String baseDir = "/mnt/workspacd/";

    @Test
    public void testSolutionFound() {
        String srcBase = baseDir + "vbaprinfo/d4j_bug_info/src_path/";
        String groundtruthBase = baseDir + "VBAPRResult/Defects4jProjs_fix/";
        String buggyBase = baseDir + "VBAPRResult/Defects4jProjs/";
        String repairBase = baseDir + "VBAPRResult_exhausted_Edit4Exp_compiled/";
        PatchComparator.cmpAstChange4VBAPR_astorResults(srcBase, groundtruthBase, buggyBase, repairBase, projsConsidered, repairBase + "solutionFound.txt");
    }
}
