package fr.inria.astor.test.repair.approaches.VBAPR;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.astor.test.repair.core.BaseEvolutionaryTest;
import fr.inria.astor.util.ReadFileUtil;
import fr.inria.main.evolution.VBAPRMain;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ParameterizedTest extends BaseEvolutionaryTest {

    File out;
    VBAPRMain main;
    TestArgsUtil argsUtil;
    private String proj;
    private String version;

    public ParameterizedTest(String proj, String version) {
//        out = new File(ConfigurationProperties.getProperty("workingDirectory"));
        out = new File(ReadFileUtil.outputSrc + proj);
        this.proj = proj;
        this.version = version;
    }

    @Parameterized.Parameters
    public static Collection<String[]> data() {
        String[][] data = {
                {"Math", "98"}
        };
        return Arrays.asList(data);
//        String fileName = ReadFileUtil.outputSrc + "part2.txt";
//        return readPVInfos(fileName);
    }

    @Before
    public void setUp() {
        main = new VBAPRMain();
        argsUtil = new TestArgsUtil();
    }

    @Test
    public void testSoulutionFound() throws Exception {
        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.OFF);
        main.execute(argsUtil.getArgs(proj, version));
        AstorCoreEngine engine = main.getEngine();
        List<ProgramVariant> solutions = engine.getSolutions();
        assertEquals(true, solutions.size() > 0);
    }

    public static List<String[]> readPVInfos(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        List<String[]> list = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        int counter = 0;

        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while((tempString = reader.readLine()) != null) {
                tempString = tempString.replace("\r", "");
                String[] split = tempString.substring(tempString.indexOf(":") + 1).split(",");
                String proj = tempString.substring(0, tempString.indexOf(":"));
                for (String id :split) {
                    list.add(new String[]{proj, id});
                    stringBuilder.append(counter++).append(",");
                    stringBuilder.append(proj).append(",").append(id);
                }
            }
            writeInfo(stringBuilder.toString(), ReadFileUtil.mapping);
            reader.close();
        } catch (IOException var13) {
            var13.printStackTrace();
        }

        return list;
    }

    public static void writeInfo(String content, String filepath) {
        try {
            BufferedOutputStream buff = null;
            buff = new BufferedOutputStream(new FileOutputStream(filepath, true));
            buff.write(content.getBytes(StandardCharsets.UTF_8));
            buff.flush();
            buff.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
