package fr.inria.astor.test.repair.approaches.VBAPR.patch;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.astor.util.FileTools;
import fr.inria.main.evolution.VBAPRMain;
import fr.inria.main.test.PatchComparator;
import fr.inria.main.test.TestArgsUtil;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PatchUnifTest {

    String[] projsConsidered = {"Chart", "Math", "Time", "Lang"};//

    public void extractFixFilesTest4Astor() throws Exception {
        String baseDir = "/mnt/workspace/";
        String srcBase = baseDir + "vbaprinfo/d4j_bug_info/src_path/";
        String fixBase = baseDir + "vbaprinfo/d4j_bug_info/fixfiles/";
        TestArgsUtil argsUtil = new TestArgsUtil("");
        VBAPRMain main = new VBAPRMain();
        List<String> projs = FileTools.getDirNames(fixBase);
        for (String proj :projs) {
            List<String> buggyDirs = FileTools.getDirNames(fixBase + proj.toLowerCase() + "/");
            for (String buggyDir : buggyDirs) {
                String version = buggyDir.split("_")[1];
                List<String> srcDirPath = FileTools.readEachLine(srcBase + proj.toLowerCase() + "/" + version + ".txt");
                String subPrefix4bug = srcDirPath.get(0);
                List<String> buggyfilesPath = FileTools.getFilePaths(fixBase + proj.toLowerCase() + "/" + buggyDir, ".java");
                for (String buggyfilePath :buggyfilesPath) {
                    String outputfile = fixBase + "/" + proj.toLowerCase() + "/" + buggyDir + "/" + subPrefix4bug;
//                    if (!FileTools.getFilePaths(outputfile, ".java").isEmpty())
                    String qualifiedName = buggyfilePath.substring(
                            buggyfilePath.indexOf(subPrefix4bug) + subPrefix4bug.length() + 1, buggyfilePath.indexOf(".java"))
                                .replaceAll("/", ".");
                    System.out.println("--------- " + buggyDir);
                    try {
                        main.execute(argsUtil.getArgs(FileTools.firstToUpperCase(proj), version));
                    } catch (AssertionError | Exception e) {
                        System.err.println("something went wrong for " + buggyDir);
                        e.printStackTrace();
                        continue;
                    }
                    CtType ctType = MutationSupporter.getFactory().Type().get(qualifiedName);//
                    PatchComparator.saveSourceCodeOnDisk(MutationSupporter.getFactory(), ctType, outputfile.replace("fixfiles", "fixfiles4Astor"));
                }
            }
        }
    }

    @Test
    public void test() throws Exception {
    }
}
