package fr.inria.main.test;

import fr.inria.astor.util.ReadFileUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.List;

public class TestExecutionListener extends RunListener {
    Logger infoLog = LogManager.getLogger("InfoLog");
    TestResultRecorder recorder;
    MethodInfo methodInfo;
    List<MethodInfo> list;

    public void testRunStarted(Description description) throws Exception {
        infoLog.info("--------- START ----------");
        recorder = new TestResultRecorder();
        list = new ArrayList<>();
    }

    public void testRunFinished(Result result) throws Exception {
        recorder.setResult(result.wasSuccessful());
        recorder.setList(list);
        infoLog.info("--------- END ----------");
        infoLog.info("whole result : " + result.wasSuccessful());
        infoLog.info("total time : " + result.getRunTime());
        infoLog.info("total tests : " + result.getRunCount());
        infoLog.info("failed tests : " + result.getFailureCount());
        ParameterizedTest.writeInfo(recorder.toString(), ReadFileUtil.outputSrc + "testResults");
    }

    public void testStarted(Description description) throws Exception {
        recorder.setScript_name(description.getClassName());
        infoLog.info("--------------------------" + description.getMethodName() + " begin");
        methodInfo = new MethodInfo();
        String name = description.getMethodName();
        methodInfo.setMethod_id(name.substring(name.indexOf("[") + 1, name.indexOf("]")));
    }

    public void testFinished(Description description) throws Exception {
        infoLog.info("--------------------------" + description.getMethodName() + " end");
        list.add(methodInfo);
    }

    public void testFailure(Failure failure) throws Exception {
        methodInfo.setResult(false);
        methodInfo.setError_msg(failure.getMessage());
    }

}
