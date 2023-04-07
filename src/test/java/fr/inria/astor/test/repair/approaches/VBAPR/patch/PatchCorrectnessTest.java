package fr.inria.astor.test.repair.approaches.VBAPR.patch;

import fr.inria.astor.util.FileTools;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import org.junit.Test;

import java.io.File;
import java.util.*;

public class PatchCorrectnessTest {
    String[] projsConsidered = {"Chart", "Math", "Time", "Lang"};

    @Test
    public void test() throws Exception {
        String baseDir = "/home/liumengjiao/Desktop/";
        String srcBase = baseDir + "vbaprinfo/d4j_bug_info/src_path/";
        String groundtruthBase = baseDir + "VBAPRResult/Defects4jProjs_fix/";
        String buggyBase = baseDir + "VBAPRResult/Defects4jProjs/";
        String repairBase = baseDir + "VBAPRResult/";
        List<String> projs = FileTools.getDirNames(groundtruthBase);
        Map<String, String> sameFixList = new HashMap<>();
        Map<String, Integer> proj_version = new HashMap<>();
        try {
            for (String proj : projs) {
                if (!Arrays.asList(projsConsidered).contains(proj))
                    continue;
                System.out.println("---------- Check for Project " + proj);
                List<String> repairDirs = FileTools.getDirNames(repairBase + proj + "/");
                for (String repairDir : repairDirs) {
                    if (repairDir.split(proj).length < 2)
                        continue;
                    String version = repairDir.split(proj)[1].split("_")[1];
                    if (!proj_version.containsKey(proj + "_" + version)) {
                        proj_version.put(proj + "_" + version, 0);
                    }
                    System.out.println("     ----- Check for version " + version);
                    List<String> fixFilesPath = FileTools.getFilePaths(srcBase + "../fixfiles4spoon/" + proj.toLowerCase() + "/" + proj.toLowerCase() + "_" + version, ".java");
                    if (fixFilesPath.isEmpty()) {
                        System.err.println("Fix version file does not exist.");
                        continue;
                    }
                    boolean hasSameFix = false;
                    List<String> patchesDir = FileTools.getDirNames(repairBase + proj + "/" + repairDir + "/src");
                    patchesDir.sort(new Comparator<String>() {
                        public int getID(String patchDir) {
                            if (!patchDir.startsWith("variant"))
                                return -1;
                            return Integer.parseInt(patchDir.split("-")[1].split("_")[0]);
                        }
                        @Override
                        public int compare(String o1, String o2) {
                            return Integer.compare(getID(o1), getID(o2));
                        }
                    });
                    for (String patchDir : patchesDir) {
                        if (hasSameFix)
                            break;
                        if (!patchDir.startsWith("variant") || !patchDir.endsWith("_f"))
                            continue;
                        System.out.println("           " + patchDir);
                        proj_version.put(proj + "_" + version, proj_version.get(proj + "_" + version) + 1);
                        String prefix4patch = repairBase + proj + "/" + repairDir + "/src/" + patchDir;
                        List<String> patchFiles = FileTools.getFilePaths(prefix4patch, ".java");
                        for (String patchFilePath : patchFiles) {
                            if (hasSameFix)
                                break;
                            String buggyFilePostfix = patchFilePath.substring(patchFilePath.lastIndexOf("/"));
                            for (String fixFilePath : fixFilesPath) {
                                String fixFlePathPostfix = fixFilePath.substring(fixFilePath.lastIndexOf("/"));
                                if (!buggyFilePostfix.equals(fixFlePathPostfix))
                                    continue;
                                Diff result = new AstComparator().compare(new File(fixFilePath), new File(patchFilePath));
                                if (result.getRootOperations().isEmpty()) {
                                    hasSameFix = true;
                                    System.out.println("           " + patchDir + ": Solution Found!");
                                    sameFixList.put(proj + "_" + version, patchDir);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("---------- Finished.");
        StringBuilder stringBuilder = new StringBuilder("Total bugs: ");
        stringBuilder.append(proj_version.size()).append("\n")
                .append("Average pataches: ").append(proj_version.values().stream().mapToInt(o -> o).average()).append("\n");
        if (!sameFixList.isEmpty()) {
            stringBuilder.append("Solutions Found: ").append(sameFixList.size()).append("\n")
                    .append(FileTools.getMap2String(sameFixList));
//            FileTools.writeToFile(stringBuilder.toString(), outputFile, true);
        } else {
            stringBuilder.append("No Solution Found.");
        }
        System.out.println(stringBuilder);
    }
}
