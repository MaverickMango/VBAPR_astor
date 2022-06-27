package fr.inria.main.test;

import fr.inria.astor.util.ReadFileUtil;
import org.junit.runner.JUnitCore;

public class ExecutionMain {

    static String filename = ReadFileUtil.outputSrc;

    public static void main(String[] args) {
        ParameterizedTest.fileName = filename + args[0];
        run(ParameterizedTest.class);
    }

    private static void run(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            JUnitCore runner = new JUnitCore();
            TestExecutionListener listener = new TestExecutionListener();
            runner.addListener(listener);
            runner.run(clazz);
        }
    }
}
