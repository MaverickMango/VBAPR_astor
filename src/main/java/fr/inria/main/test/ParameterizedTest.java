package fr.inria.main.test;

import fr.inria.astor.approaches.jgenprog.extension.VBAPR;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.astor.core.solutionsearch.EvolutionarySearchEngine;
import fr.inria.astor.util.ReadFileUtil;
import fr.inria.main.evolution.VBAPRMain;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import spoon.support.StandardEnvironment;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ParameterizedTest{

    public static Logger log = Logger.getLogger(Thread.currentThread().getName());
    static VBAPRMain main;
    static TestArgsUtil argsUtil;
    static String fileName = "";
    private String proj;
    private String version;

    public ParameterizedTest(String proj, String version) {
        this.proj = proj;
        this.version = version;
    }

    @Parameterized.Parameters
    public static Collection<String[]> data() {
        String[][] data = {
                {"Math" , "58"}
//                {"Lang", "34"}
//                ,{"Cli", "25"}
//                ,{"Cli", "32"}
//                ,{"Math", "26"}
//                ,{"Math", "72"}
//                ,{"Math", "46"}
//                ,{"Math", "67"}
//                ,{"Codec", "1"}
//                ,{"Codec", "2"}
//                ,{"Codec", "8"}
//                ,{"Jsoup", "43"}
//                ,{"Jsoup", "62"}
//                ,{"Jsoup", "88"}
//                ,{"JacksonDatabind", "37"}
//                ,{"JacksonDatabind", "70"}
//                ,{"JacksonDatabind", "16"}
//                ,{"JacksonDatabind", "102"}
//                ,{"JacksonCore", "5"}

//                ,{"Lang", "6"}
//                ,{"Math", "33"}
//                ,{"Cli", "5"}
//                , {"Chart", "10"}
        };
        return Arrays.asList(data);

//        String fileName = ReadFileUtil.outputSrc + "part2.txt";

//        return readPVInfos(fileName);
    }


    @Before
    public void setUp() throws Exception {
//        for (Object o : System.getProperties().keySet()) {
//            System.out.println(String.valueOf(o) + "->" + System.getProperties().get(o));
//        }
//        String logfile = System.getProperty("logFileName");
//        if (logfile == null) {
//            logfile = proj + version;
//            System.setProperty("logFileName", logfile);
//        }
//        LogManager.getRootLogger().setLevel(Level.ERROR);
//        LogManager.getLogger(VBAPR.class.getSimpleName()).setLevel(Level.INFO);
//        LogManager.getLogger("DetailLog").setLevel(Level.INFO);
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        main = new VBAPRMain();
        argsUtil = new TestArgsUtil();
    }

    @Test
    public void testSoulutionFound() throws Exception {
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
                if (tempString.equals(""))
                    break;
                String[] split = tempString.substring(tempString.indexOf(":") + 1).split(",");
                String proj = tempString.substring(0, tempString.indexOf(":"));
                for (String id :split) {
                    list.add(new String[]{proj, id});
                    stringBuilder.append(counter++).append(",");
                    stringBuilder.append(proj).append(",").append(id);
                    stringBuilder.append("\n");
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
