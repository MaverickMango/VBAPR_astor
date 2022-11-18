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
import java.util.concurrent.TimeUnit;

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
                {"Math" , "5"}
        };
        return Arrays.asList(data);

//        String fileName = ReadFileUtil.outputSrc + "part2.txt";

//        return readPVInfos(fileName);
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

    @Test
    public void testCommand() throws IOException, InterruptedException {
        StringBuilder stringBuilder = new StringBuilder();
//        String cmd = "cd /home/liu/Desktop/VBAPRResult/Defects4jProjs/Chart/Chart_8 && /home/liu/Desktop/defects4j/major/bin/ant -f /home/liu/Desktop/defects4j/framework/projects/defects4j.build.xml -Dd4j.home=/home/liu/Desktop/defects4j -Dd4j.dir.projects=/home/liu/Desktop/defects4j/framework/projects -Dbasedir=/home/liu/Desktop/VBAPRResult/Defects4jProjs/Chart/Chart_8 -Dbuild.compiler=javac1.7 -DOUTFILE=/home/liu/Desktop/VBAPRResult/Defects4jProjs/Chart/Chart_8/failing_tests -Dtest.entry.class=org.jfree.data.time.junit.TimeSeriesCollectionTests -Dtest.entry.method=testGetSurroundingItems run.dev.tests";
//        String cmd = "java -XX:ReservedCodeCacheSize=256M -XX:MaxPermSize=1G -Djava.awt.headless=true -Xbootclasspath/a:/home/liu/Desktop/defects4j/major/bin/../config/config.jar -jar /home/liu/Desktop/defects4j/major/bin/../lib/ant-launcher.jar -f /home/liu/Desktop/defects4j/framework/projects/defects4j.build.xml -Dd4j.home=/home/liu/Desktop/defects4j -Dd4j.dir.projects=/home/liu/Desktop/defects4j/framework/projects -Dbasedir=/home/liu/Desktop/VBAPRResult/Defects4jProjs/Chart/Chart_8 -Dbuild.compiler=javac1.7 -DOUTFILE=/home/liu/Desktop/VBAPRResult/Defects4jProjs/Chart/Chart_8/failing_tests -Dtest.entry.class=org.jfree.data.time.junit.TimeSeriesCollectionTests -Dtest.entry.method=testGetSurroundingItems run.dev.tests";
        String cmd = "cd /home/liu/Desktop/VBAPRResult/Defects4jProjs/Chart/Chart_8 && /home/liu/Desktop/defects4j/framework/bin/defects4j test";
        stringBuilder.append(cmd);
        String[] strings = new String[] {"/bin/bash", "-c", stringBuilder.toString()};
        System.out.println(execute(strings));
//        ProcessBuilder pb = new ProcessBuilder(strings);
//        Process p = pb.start();
//        //
//        if (!p.waitFor(6000, TimeUnit.MILLISECONDS)) {
//
//        }
    }

    public static List<String> execute(String[] command) {
        Process process = null;
        final List<String> message = new ArrayList<String>();
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            process = builder.start();
            final InputStream inputStream = process.getInputStream();

            Thread processReader = new Thread(){
                public void run() {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    try {
                        while((line = reader.readLine()) != null) {
                            message.add(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            processReader.start();
            try {
                processReader.join();
                process.waitFor();
            } catch (InterruptedException e) {
                return new LinkedList<>();
            }
        } catch (IOException e) {
        } finally {
            if (process != null) {
                process.destroy();
            }
            process = null;
        }

        return message;
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
