package fr.inria.main.test;

import fr.inria.astor.util.ReadFileUtil;
import fr.inria.main.CommandSummary;

import java.util.ArrayList;
import java.util.List;

public class TestArgsUtil {

    private CommandSummary cs = null;
    private String srcPathDir = ReadFileUtil.baseDir + "src_path/";//proj/version.txt
    private String ftsDir = ReadFileUtil.baseDir + "failed_tests/";//proj/version.txt
    private String locationDir = ReadFileUtil.outputSrc + "Defects4jProjs/";//proj_version/  //must be absolute path
    private String outputDir = ReadFileUtil.outputSrc;

    public TestArgsUtil() {
        this.cs = new CommandSummary();
        cs.command.put("-customengine", "fr.inria.astor.approaches.jgenprog.extension.VBAPR");
        cs.command.put("-maxgen", "500");
//        cs.command.put("-population", "5");
//        cs.command.put("-scope", "file");
        cs.command.put("-skipfaultlocalization", "true");
//        cs.command.put("-jvm4testexecution", "/usr/lib/jvm/jdk1.7.0_80/bin");
//        cs.command.put("-applyCrossover", "true");
        cs.command.put("-populationcontroller", "fr.inria.astor.core.solutionsearch.population.DiffBasedFitnessPopulationController");
//        cs.command.put("-faultlocalization", "fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.GTBRepairOperatorSpace");//ReplaceExpOperatorSpace
        cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy");
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
        cs.command.put("-out", outputDir + proj + "/");
        String[] compilanceLevel = getCompilanceLevel(proj);
        cs.command.put("-javacompliancelevel", compilanceLevel[0]);
        cs.command.put("-alternativecompliancelevel", compilanceLevel[1]);
        return cs.flat();
    }

    private String[] getCompilanceLevel(String proj) {
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
            case "JacksonCore":
            case "Gson":
                levels[0] = "6";
                break;
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
        List<String> list = ReadFileUtil.readFileByLineToList(srcPathDir + proj.toLowerCase() + "/" + verison + ".txt");
        assert list.size() == 4;
        args.add("/" + list.get(0) + "/");
        args.add("/" + list.get(1) + "/");
        args.add("/" + list.get(2) + "/");
        args.add("/" + list.get(3) + "/");
        args.add(locationDir + proj + "/" + proj + "_" + verison + "/");
        args.add(locationDir + proj + "/lib/");
        list = ReadFileUtil.readFileByLineToList(ftsDir + proj.toLowerCase() + "/" + verison + ".txt");
        assert list.size() >= 1;
        for (String str :list) {
            str = str.substring(0, str.indexOf("::"));
            if (!args.contains(str))
                args.add(str);
        }
        return args;
    }

}
