package fr.inria.main.test;

import fr.inria.astor.util.GroundTruth;
import fr.inria.astor.util.ReadFileUtil;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ReadFileUtilTest {

    @Test
    void test1() throws IOException {
        String file = "/home/liu/Desktop/projIDs";
        ReadFileUtil.removeRepetition(file, "");
    }

    @Test
    void test() throws IOException {
        String file = "/home/liu/Desktop/projIDs";
        String result = "/home/liu/Desktop/VBAPRResult/filtered";
        String out = "/home/liu/Desktop/VBAPRResult/simpleResult";
        ReadFileUtil.sumResult(file, result, out);
    }

    @Test
    void testotal() {
        String result = "/home/liu/Desktop/VBAPRResult/filtered";
        ReadFileUtil.sumResult(result);
    }

    @Test
    void testoutput() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nbug: chart_1\n");
        stringBuilder.append("orginal solutions: 1\n");
        stringBuilder.append("filtered numbers: 0\n");
        stringBuilder.append("filtered:");
        stringBuilder.append("\n");
        try {
            ReadFileUtil.outputFiltered(stringBuilder.toString());
        } catch (IOException e) {
            System.out.println(stringBuilder.toString());
        }
    }

    @Test
    void testgetInfos() {
        String[] info = ReadFileUtil.getInfos();
        System.out.println(info[0]);
        System.out.println(info[1]);
    }

    @Test
    void testgetExps() {
        List<GroundTruth> gts =  ReadFileUtil.getGTs("/home/liu/Desktop/groundtruth/Math.csv", 66);
        for (GroundTruth gt :gts) {
            System.out.println(gt);
        }
    }

    @Test
    void testgetStatus() {//VBAPRResult
        String base = "/home/liu/Desktop/jGenProgResult/";
        List<String> lines = readAllLines(base + "BugStatus");
        Map<String, List<String>> statusMap = new HashMap<>();
        Map<String, String[]> timeMap = new HashMap<>();
        int i = 0, j = 0;
        for (String line: lines) {
            String[] proj_v = line.split(":")[0].split("_");
            proj_v[0] = proj_v[0].substring(0, 1).toUpperCase() + proj_v[0].substring(1);
            String P_v = proj_v[0] + "_" + proj_v[1];
            String[] data = line.split(":")[1].split(",");
            data[1] = data[1].replace(";", "");
            if (!timeMap.containsKey(P_v)) {
                timeMap.put(P_v, data);
            }
            String status = ReadFileUtil.getStatus(base + proj_v[0] + "/" + P_v + "/astor_output.json");
            if (!statusMap.containsKey(status)) {
                statusMap.put(status, new ArrayList<>());
            }
            statusMap.get(status).add(P_v);
        }
        for (String key :statusMap.keySet()) {
            System.out.println(key + ": " + statusMap.get(key));
        }
        int tryTime = 0;
        double totalTime = 0;
        List<String> keys = statusMap.get("\"STOP_BY_PATCH_FOUND\"");
        for (String key: timeMap.keySet()) {
            String[] data = timeMap.get(key);
            if (keys.contains(key)) {
                tryTime += Integer.parseInt(data[0]);
                totalTime += Double.parseDouble(data[1]);
            }
        }
        System.out.println("total patch: " + keys.size());
        System.out.println("average runtime: " + totalTime / keys.size());
        System.out.println("average try: " + tryTime / keys.size());
    }

    public List<String> readAllLines(String filePath) {
        File file = new File(filePath);
        BufferedReader reader = null;
        List<String> list = new ArrayList<>();
        String tempString = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            while ((tempString = reader.readLine()) != null) {
                tempString.replace('\r', ' ');
                list.add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}