package fr.inria.main.test;

import fr.inria.astor.util.FileTools;
import fr.inria.main.CommandSummary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestArgsUtil {

    private CommandSummary cs = null;
    public static final String root = "/mnt/workspace/";
    public static final String baseDir = root + "vbaprinfo/d4j_bug_info/";//util files
    public static final String outputSrc = root + "VBAPRResult/";//working dir
    private String srcPathDir = baseDir + "src_path/";//...proj/version.txt
    private String ftsDir = baseDir + "failed_tests/";//...proj/version.txt
    private String locationDir = outputSrc + "Defects4jProjs/";//proj_version/  //must be absolute path

    public TestArgsUtil(CommandSummary cs){
        this.cs = cs;
    }
    public TestArgsUtil(String mode) {
        this.cs = new CommandSummary();
        cs.command.put("-skipfaultlocalization", "true");
        cs.command.put("-useGTsizeAsPopSize", "true");
        cs.command.put("-useVariableEdit", "true");
//        cs.command.put("-scope", "file");
//        cs.command.put("-addSimilarityComparasion", "true");
        cs.command.put("-populationcontroller", "fr.inria.astor.core.solutionsearch.population.DiffBasedFitnessPopulationController");
//        cs.command.put("-faultlocalization", "fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-modificationpointnavigation","fr.inria.astor.core.solutionsearch.navigation.ForceOrderSuspiciousNavitation");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.GTBRepairOperatorSpace");//ReplaceExpOperatorSpace Type
        cs.command.put("-ingredienttransformstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.transformations.GTBIngredientTransformationStrategy");
        if (mode.contains("exhausted")) {
            if (mode.equals("exhaustedGA")) {
                cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR4ExhaustedGA");
                cs.command.put("-maxgen", "2");
                cs.command.put("-skipValidation", "false");
                cs.command.put("-stopfirst", "true");
            }
            if (mode.equals("exhausted")) {
                cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR4Exhausted");
                cs.command.put("-maxgen", "1");
                cs.command.put("-skipValidation", "true");
            }
//            cs.command.put("-saveall", "true");
            cs.command.put("-stopfirst", "false");
            cs.command.put("-useGTsizeAsPopSize", "false");
            cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelecIngreSearchStrategy4Exhausted");
        }
    }

    public String[] getArgs(String proj, String version) {
        List<String> args = readArgs(proj, version);
        assert args.size() >= 7;
        cs.command.put("-srcjavafolder", args.get(0));
        cs.command.put("-binjavafolder", args.get(1));
        cs.command.put("-srctestfolder", args.get(2));
        cs.command.put("-bintestfolder", args.get(3));
        cs.command.put("-location", args.get(4));
        cs.command.put("-dependencies", args.get(5));
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 6; i < args.size(); i++) {
            stringBuilder.append(args.get(i)).append(":");
        }
        cs.command.put("-failing", stringBuilder.toString().substring(0, stringBuilder.length() - 1));
        cs.command.put("-out", FileTools.outputSrc + proj + "/");
        String[] compilanceLevel = getCompilanceLevel(proj);
        cs.command.put("-javacompliancelevel", compilanceLevel[0]);
        cs.command.put("-alternativecompliancelevel", compilanceLevel[1]);

        System.setProperty("log.base", FileTools.outputSrc + proj + "/log/" + version + "/");
        return cs.flat();
    }

    public String[] getCompilanceLevel(String proj) {
        String[] levels = new String[2];
        levels[0] = "8";
        switch (proj) {
            case "JxPath":
            case "Cli":
                levels[0] = "3";
                break;
            case "Chart":
            case "Codec":
                levels[0] = "4";
                break;
            case "Math":
            case "Time":
            case "Mockito":
            case "Compress":
                levels[0] = "5";
                break;
            case "Gson":
                levels[0] = "6";
                break;
            case "JacksonCore":
            case "JacksonXml":
            case "JacksonDatabind":
            case "Csv":
            case "Jsoup":
                levels[0] = "7";
            default:
                break;
        }
        levels[1] = "8";
        switch (proj) {
            case "Lang":
                levels[1] = "3";
                break;
            case "Csv":
            case "Jsoup":
            case "Cli":
                levels[1] = "5";
                break;
            case "JacksonDatabind":
            case "Codec":
                levels[1] = "6";
            default: break;
        }
        return levels;
    }

    private List<String> readArgs(String proj, String verison) {
        List<String> args = new ArrayList<>();
        List<String> list = FileTools.readFileByLineToList(srcPathDir + proj.toLowerCase() + "/" + verison + ".txt");
        assert list.size() == 4;
        args.add("/" + list.get(0) + "/");
        args.add("/" + list.get(1) + "/");
        args.add("/" + list.get(2) + "/");
        args.add("/" + list.get(3) + "/");
        String projPath = locationDir + proj + "/" + proj + "_" + verison + "/";
        args.add(projPath);
        if (isMavenProj(projPath))
            args.add(projPath + "target/dependency/");
        else
            args.add(projPath + "/lib/");
        list = FileTools.readFileByLineToList(ftsDir + proj.toLowerCase() + "/" + verison + ".txt");
        assert list.size() >= 1;
        FileTools.failingActualSize = list.size();
        for (String str :list) {
            str = str.substring(0, str.indexOf("::"));
            if (!args.contains(str))
                args.add(str);
        }
        return args;
    }

    private boolean isMavenProj(String projPath) {
        File dir = new File(projPath);
        File[] files = dir.listFiles();
        for (File f:files) {
            if (f.isFile() && f.getName().equals("pom.xml")) {
                return true;
            }
        }
        return false;
    }
}
