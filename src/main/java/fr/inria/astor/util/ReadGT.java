package fr.inria.astor.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.inria.astor.approaches.jgenprog.LocalVariableProcessor;
import fr.inria.astor.approaches.jgenprog.VariableReferenceProcessor;
import fr.inria.astor.core.setup.ConfigurationProperties;
import org.json.simple.JSONObject;
import spoon.processing.ProcessingManager;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.support.QueueProcessingManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReadGT {
    public static String outputSrc = "/home/liu/Desktop/VBAPRResult/filtered";
    public static List<GroundTruth> GTs = null;
    static String fileBase = "/home/liu/Desktop/groundtruth/";

    public static boolean hasThisVar(String name) {
        for (GroundTruth gt :GTs) {
            if (!gt.isExp() && gt.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasThisExp(String exp) {
        for (GroundTruth gt :GTs) {
            if (gt.isExp() && gt.getName().startsWith("(") && gt.getName().endsWith(")")) {
                String temp = gt.getName().replace(" ", "")
                        .substring(1, gt.getName().length() - 1);
                if (temp.equals(exp)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasExp() {
        for (GroundTruth gt :GTs) {
            if (gt.isExp())
                return true;
        }
        return false;
    }
    public static boolean hasVar() {
        for (GroundTruth gt :GTs) {
            if (gt.isExp())
                return false;
        }
        return true;
    }

    public static boolean considerVariableReference(String location, CtElement element, Factory factory) {
        LocalVariableProcessor localVariableProcessor = new LocalVariableProcessor(location);
        VariableReferenceProcessor referenceProcessor = new VariableReferenceProcessor(location);
        ProcessingManager processingManager = new QueueProcessingManager(factory);
        processingManager.addProcessor(localVariableProcessor);
        processingManager.addProcessor(referenceProcessor);
        processingManager.process(element);
        Set<String> oriVars = localVariableProcessor.varList;
        oriVars.addAll(referenceProcessor.varList);
        return oriVars.size() == 0;
    }

    public static boolean filtered(Set<String> A, Set<String> B) {
        boolean allAInB = true;
        for (String var : A) {
            if (!B.contains(var)) {
                allAInB = false;
                break;
            }
        }
        if (!allAInB) {
            return false;
        } else {
            return B.size() <= A.size();
        }
    }

    public static void getGTs(String[] info) {
        GTs = getGTs(fileBase + info[0] +".csv", Integer.parseInt(info[1]));
        System.out.println();
    }

    public static String[] getInfos() {
//        ConfigurationProperties.setProperty("location", "/home/liu/Desktop/astor/examples/chart_3/");
        String location = ConfigurationProperties.getProperty("location");
        String temp = location.substring(0, location.length()-1);
        String[] info = temp.substring(temp.lastIndexOf("/") + 1).split("_");
        assert info.length == 2;
//        info[0] = fileBase + info[0] +".csv";
        return info;
    }

    public static List<GroundTruth> getGTs(String filePath, int version) {
        String line = getLine(filePath, version);
        return splitExps(line);
    }

    private static String getLine(String filePath, int version){
        File file = new File(filePath);
        BufferedReader reader = null;
        String tempString = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            int idx = 0;
            while ((tempString = reader.readLine()) != null) {
                idx ++;
                if (idx <= version) {
                    continue;
                }
                tempString.replace('\r', ' ');
                break;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempString;
    }

    private static List<GroundTruth> splitExps(String line) {
        List<GroundTruth> exps = new ArrayList<>();
        String[] oneFile = line.split(":/");
        assert oneFile.length >= 1;
        for(int i = 1; i < oneFile.length; i ++) {
            String temp = oneFile[i];
            String[] gtsWithFile = temp.split("#");
            if (gtsWithFile.length > 1) {
                String path = gtsWithFile[0].replace("/", ".");
//                String path = gtsWithFile[0];
                String[] gts = gtsWithFile[1].split(";");//<var-[start,end]>
                String regex1 = "-\\[[0-9]+,[0-9]+\\]";
                String regex2 = "-\\[[0-9]+\\]";
                for (String gt :gts) {
                    GroundTruth groundTruth = new GroundTruth(path);
                    String name = "";
                    Pattern pattern1 = Pattern.compile(regex1);
                    Matcher matcher1 = pattern1.matcher(gt);
                    while (matcher1.find()) {
                        name = gt.substring(0, matcher1.start());
                        String[] lineNumbers = matcher1.group()
                                .replace("-[", "")
                                .replace("]", "").split(",");
                        assert lineNumbers.length == 2;
                        groundTruth.setName(name);
                        groundTruth.setStartLineNumber(Integer.parseInt(lineNumbers[0]));
                        groundTruth.setEndLineNumber(Integer.parseInt(lineNumbers[1]));
                        exps.add(groundTruth);
                        continue;
                    }
                    Pattern pattern2 = Pattern.compile(regex2);
                    Matcher matcher2 = pattern2.matcher(gt);
                    while (matcher2.find()) {
                        name = gt.substring(0, matcher2.start());
                        String lineNumber = matcher2.group()
                                .replace("-[", "")
                                .replace("]", "");
                        groundTruth.setName(name);
                        groundTruth.setLinenumber(Integer.parseInt(lineNumber));
                        exps.add(groundTruth);
                    }
                }
            }
        }
        return exps;
    }

    public static void outputFiltered(String appendStr, String outputSrc) throws IOException {
        File file = new File(outputSrc);
        if (!file.exists()) {
            file.createNewFile();
        }
        try (FileOutputStream fos = new FileOutputStream(file,true);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            writer.write(appendStr);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void outputFiltered(String appendStr) throws IOException {
        File file = new File(outputSrc);
        if (!file.exists()) {
            file.createNewFile();
        }
        try (FileOutputStream fos = new FileOutputStream(file,true);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            writer.write(appendStr);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> getFiltered(String resultFile) {
        if (resultFile == null || resultFile.equals("")) {
            return null;
        }
        File file = new File(resultFile);
        BufferedReader reader = null;
        String tempString = null;
        List<String[]> sum = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String[] one = new String[3];
            while ((tempString = reader.readLine()) != null) {
                tempString.replace('\r', ' ');
                if (tempString.startsWith("bug:")) {
                    one[0] = tempString.split(":")[1];
                }
                if (tempString.startsWith("filtered numbers:")) {
                    one[1] = tempString.split(":")[1];
                }
                if (tempString.startsWith("filtered:")) {
                    one[2] = tempString.split(":").length > 1 ? tempString.split(":")[1] : "";
                }
                if (one[0] != null && one[1] != null && one[2] != null) {
                    sum.add(one);
                    one = new String[3];
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sum;
    }

    public static List<String> readIDs(String fileName) {
        List<String> ids = new ArrayList<>();
        File file = new File(fileName);
        BufferedReader reader = null;
        String tempString = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            while ((tempString = reader.readLine()) != null) {
                tempString.replace('\r', ' ');
                String proj = tempString.split(":")[0];
                String[] versions = tempString.split(":")[1].split(",");
                for (String version :versions) {
                    ids.add(proj + "-" + version);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public static Map<String, List<String>> removeRepetition(String projIDs, String result) throws IOException {
        String base = "/home/liu/Desktop/FilterResult/";
        List<String> ids = readIDs(projIDs);
        Map<String, List<String>> map = new HashMap<>();
        String proj, version, dirPath;
        File file;
        for (String id :ids) {
            List<String> variantDir = new ArrayList<>();
            proj = id.split("-")[0];
            version = id.split("-")[1];
            dirPath = base + proj + "/" + proj + "_" + version + "/src/";
            file = new File(dirPath);
            File[] list = file.listFiles();
            for (File dir :list) {
                if (dir.isDirectory() && dir.getName().startsWith("variant") && !dir.getName().endsWith("_f")) {
                    variantDir.add(dir.getName());
                }
            }
            if (variantDir.size() == 0) {
                continue;
            }
            int diffFile = variantDir.size();
            List<Integer> iterator = new ArrayList<>();
            for (int i = 0; i < variantDir.size(); i++) {
                iterator.add(i);
            }
            List<Integer> diffVars = new ArrayList<>();
            different(variantDir, dirPath, diffFile, iterator, diffVars);
            List<String> variants = new ArrayList<>();
            for (Integer i :diffVars) {
                String s = variantDir.get(i).split("-")[1].split("_")[0];
                variants.add(s);
            }
            map.put(id, variants);
        }
        return map;
    }

    public static int different(List<String> variantDir, String dirPath, int diffFileNums, List<Integer> iterator, List<Integer> diffVars) throws IOException {
        if (iterator.size() != 0) {
            diffVars.add(iterator.get(0));
        }
        if (iterator.size() < 2) {
            return diffFileNums;
        }
        List<Integer> nextDiff = new ArrayList<>();
        String patch = "/patch.diff";
        boolean isSame = false;
        int i = iterator.get(0);
        String variant1 = dirPath + variantDir.get(i) + patch;
        for (int j = iterator.get(iterator.indexOf(i)+1); ; j = iterator.get(iterator.indexOf(j)+1)) {
            String compare = dirPath + variantDir.get(j) + patch;
            isSame = compareTwo(variant1, compare);
            if (isSame) {
                diffFileNums --;
            } else {
                nextDiff.add(j);
            }
            if (iterator.indexOf(j) >= iterator.size() - 1)
                break;
        }
        return different(variantDir, dirPath, diffFileNums, nextDiff, diffVars);
    }

    public static boolean compareTwo(String one, String another) throws IOException {
        List<String> list1 =  Files.readAllLines(Paths.get(one));
        List<String> list2 =  Files.readAllLines(Paths.get(another));

        List<String> finalList = list2.stream().filter(line ->
                list1.stream().filter(line2 -> line2.equals(line)).count() == 0
        ).collect(Collectors.toList());
        if (finalList.size() == 0) {
            return true;
        }else{
            return false;
        }
    }

    public static void sumResult(String projIDs, String result, String output) throws IOException {
        List<String[]> filtered = getFiltered(result);
        Map<String, List<String>> map = removeRepetition(projIDs, result);
        StringBuilder stringBuilder = new StringBuilder();
        for (String[] infos: filtered) {
            if (map.containsKey(infos[0])) {
                stringBuilder.append("\n");
                List<String> solutions = map.get(infos[0]);
                stringBuilder.append("bug: ").append(infos[0]).append("\n")
                        .append("orginal solutions: ").append(solutions.size()).append("\n")
                        .append("solutions:");
                for (String solution :solutions) {
                    stringBuilder.append(" ").append(solution);
                }
                String[] filteredVs = infos[2].split(" ");
                infos[2] = "";
                int nums = 0;
                for (String v :filteredVs) {
                    if (solutions.contains(v)) {
                        infos[2] += " " + v;
                        nums ++;
                    }
                }
                stringBuilder.append("\n")
                        .append("filtered numbers: ").append(nums).append("\n")
                        .append("filtered:").append(infos[2]).append("\n");
            }
        }
        outputFiltered(stringBuilder.toString(), output);
    }

    public static void sumResult(String resultFile) {
        File file = new File(resultFile);
        BufferedReader reader = null;
        String tempString = null;
        String bug = "";
        int total = 0;
        int filtered = 0;
        String fl = "";
        try {
            reader = new BufferedReader(new FileReader(file));
            while ((tempString = reader.readLine()) != null) {
                tempString.replace('\r', ' ');
//                if (tempString.startsWith("bug:")) {
//                    bug = tempString.split(":")[1];
//                }
                if (tempString.startsWith("orginal solutions:")) {
                    total += Integer.parseInt(tempString.split(":")[1].trim());
                }
                if (tempString.startsWith("filtered numbers:")) {
                    filtered += Integer.parseInt(tempString.split(":")[1].trim());
                }
//                if (tempString.startsWith("filtered:")) {
//                    fl = tempString;
//                }
            }
            System.out.println("filtered/total: " + filtered + "/" + total);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                tempString.replace('\r', ' ');
                sb.append(tempString);
//                System.out.println(tempString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return sb.toString();
    }

    public static String getStatus(String filepath) {
        //从json文件中获得patch对应的project和version信息
        String jsonFile = readFileByLines(filepath);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonFile).getAsJsonObject();
        String status = jsonObject.get("general")
                .getAsJsonObject().get("OUTPUT_STATUS").toString();
        return status;
    }

}
