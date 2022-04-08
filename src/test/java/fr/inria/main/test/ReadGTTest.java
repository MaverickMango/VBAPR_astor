package fr.inria.main.test;

import fr.inria.astor.util.CodeLineCollector;
import fr.inria.astor.util.GroundTruth;
import fr.inria.astor.util.ReadGT;
import fr.inria.main.CommandSummary;
import fr.inria.main.evolution.AstorMain;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReadGTTest {

    @Test
    void test1() throws IOException {
        String file = "/home/liu/Desktop/projIDs";
        ReadGT.removeRepetition(file, "");
    }

    @Test
    void test() throws IOException {
        String file = "/home/liu/Desktop/projIDs";
        String result = "/home/liu/Desktop/VBAPRResult/filtered";
        String out = "/home/liu/Desktop/VBAPRResult/simpleResult";
        ReadGT.sumResult(file, result, out);
    }

    @Test
    void testotal() {
        String result = "/home/liu/Desktop/simpleResult";
        ReadGT.sumResult(result);
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
            ReadGT.outputFiltered(stringBuilder.toString());
        } catch (IOException e) {
            System.out.println(stringBuilder.toString());
        }
    }

    @Test
    void testgetInfos() {
        String[] info = ReadGT.getInfos();
        System.out.println(info[0]);
        System.out.println(info[1]);
    }

    @Test
    void testgetExps() {
        List<GroundTruth> gts =  ReadGT.getGTs("/home/liu/Desktop/groundtruth/Math.csv", 66);
        for (GroundTruth gt :gts) {
            System.out.println(gt);
        }
    }

    @Test
    void testparseClass() {
        String base = "/home/liu/Desktop/VBAPRResult/";
        List<String> ids = ReadGT.readIDs("/home/liu/Desktop/projs");
        StringBuilder b1 = new StringBuilder("Chart:"), b2 = new StringBuilder("Math:");
        int i = 0, j = 0;
        for (String id:ids) {
            String proj = id.split("-")[0];
            String dir = id.replace("-", "_");
            String status = ReadGT.getStatus(base + proj + "/" + dir + "/astor_output.json");
            if (!status.equals("\"ERROR\""))
                continue;
            if (proj.equals("Chart")) {
                b1.append(id.split("-")[1]).append(",");
                i++;
            }
            if (proj.equals("Math")) {
                b2.append(id.split("-")[1]).append(",");
                j++;
            }
        }
        System.out.println(ids.size());
        System.out.println("total " + i + b1.toString());
        System.out.println("total " + j + b2.toString());
    }
}