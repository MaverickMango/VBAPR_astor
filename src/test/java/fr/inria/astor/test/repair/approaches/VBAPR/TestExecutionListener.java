package fr.inria.astor.test.repair.approaches.VBAPR;

import fr.inria.astor.util.ReadFileUtil;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.List;

public class TestExecutionListener extends RunListener {
    TestResultRecorder recorder;
    MethodInfo methodInfo;
    List<MethodInfo> list;

    public TestExecutionListener() {
        this.list = new ArrayList<>();
    }

    public void testRunStarted(Description description) throws Exception {
        System.out.println("--------- START ----------");
        recorder = new TestResultRecorder();
    }

    public void testRunFinished(Result result) throws Exception {
        recorder.setResult(result.wasSuccessful());
        recorder.setList(list);
        System.out.println("--------- END ----------");
        System.out.println("whole result : " + result.wasSuccessful());
        System.out.println("total time : " + result.getRunTime());
        System.out.println("total tests : " + result.getRunCount());
        System.out.println("failed tests : " + result.getFailureCount());
        ParameterizedTest.writeInfo(recorder.toString(), ReadFileUtil.outputSrc + "testResults");
    }

    public void testStarted(Description description) throws Exception {
        recorder.setScript_name(description.getClassName());
        System.out.println("--------------------------" + description.getMethodName() + " begin");
        methodInfo = new MethodInfo();
        String name = description.getMethodName();
        methodInfo.setMethod_id(name.substring(name.indexOf("[") + 1, name.indexOf("]")));
    }

    public void testFinished(Description description) throws Exception {
        System.out.println("--------------------------" + description.getMethodName() + " end");
        list.add(methodInfo);
    }

    public void testFailure(Failure failure) throws Exception {
        methodInfo.setResult(false);
        methodInfo.setError_msg(failure.getMessage());
    }

}
