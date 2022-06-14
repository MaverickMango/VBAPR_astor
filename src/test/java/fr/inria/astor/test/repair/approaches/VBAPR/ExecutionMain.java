package fr.inria.astor.test.repair.approaches.VBAPR;

import org.junit.runner.JUnitCore;

public class ExecutionMain {

    public static void main(String[] args) {
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
