package fr.inria.main.test;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.astor.util.FileTools;
import fr.inria.main.evolution.VBAPRMain;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ParameterizedTest{
//    static {
//        System.setProperty("log.base", "./log");
//    }
//    public static Logger log = Logger.getLogger(Thread.currentThread().getName());
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
//        String[][] data = {
//                {"Lang" , "26"}//
//        };
//        return Arrays.asList(data);

        String fileName = FileTools.baseDir + "../" + "bugs4.txt";
        return readPVInfos(fileName);
//
//        List<String> mapping = FileTools.readFileByLineToList(FileTools.mapping);
//        List<String> success = Arrays.asList(FileTools.readFileByLines(FileTools.outputSrc + "/success_bugs").split(","));
//        List<String[]> proj_ids = new ArrayList<>();
//        for (String map :mapping) {
//            String[] temp = map.split(",");
//            if (success.contains(temp[0]))//successful bugs condition: success.contains(temp[0])；failed bugs condition: !success.contains(temp[0])
//                proj_ids.add(new String[]{temp[1],temp[2]});
//        }
//        proj_ids = proj_ids.subList(22, proj_ids.size());
//        return proj_ids;
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        argsUtil = new TestArgsUtil("exhaustedGA");
    }

    @Test(timeout = 10800000)
    public void testSoulutionFound() throws Exception {
        String[] args = argsUtil.getArgs(proj, version);
        main = new VBAPRMain();
        main.execute(args);
        AstorCoreEngine engine = main.getEngine();
        assertEquals(true, engine.getOriginalVariant().getModificationPoints().size() > 0);
//        List<ProgramVariant> solutions = engine.getVariants();
//        assertEquals(true, solutions.size() > 0);
    }

//    @Test
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
            writeInfo(stringBuilder.toString(), FileTools.mapping);
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
