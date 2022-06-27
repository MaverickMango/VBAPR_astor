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
        cs.command.put("-population", "5");
//        cs.command.put("-scope", "file");
        cs.command.put("-skipfaultlocalization", "true");
        cs.command.put("-populationcontroller", "fr.inria.astor.core.solutionsearch.population.DiffBasedFitnessPopulationController");
//        cs.command.put("-faultlocalization", "fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT");
        cs.command.put("-operatorspace", "fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace");
        cs.command.put("-targetelementprocessor", "fr.inria.astor.core.manipulation.filters.SingleExpressionFixSpaceProcessor");
        cs.command.put("-opselectionstrategy", "fr.inria.astor.core.solutionsearch.spaces.operators.ReplaceExpOperatorSpace");//GTBRepairOperatorSpace
        cs.command.put("-ingredientstrategy", "fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy");
    }

    public String[] getArgs(String proj, String version) {
        List<String> args = readArgs(proj, version);
        assert args.size() >= 6;
        cs.command.put("-srcjavafolder", args.get(0));
        cs.command.put("-binjavafolder", args.get(1));
        cs.command.put("-srctestfolder", args.get(2));
        cs.command.put("-bintestfolder", args.get(3));
        cs.command.put("-location", args.get(4));
        cs.command.put("-dependencies", args.get(4) + "/lib");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 5; i < args.size(); i++) {
            stringBuilder.append(args.get(i)).append(":");
        }
        cs.command.put("-failing", stringBuilder.toString().substring(0, stringBuilder.length() - 1));
        cs.command.put("-out", outputDir + proj + "/");
        return cs.flat();
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
