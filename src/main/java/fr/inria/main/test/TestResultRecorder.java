package fr.inria.main.test;

import java.util.List;

public class TestResultRecorder {

    String script_name;
    List<MethodInfo> list;
    Boolean result;

    public String getScript_name() {
        return script_name;
    }

    public void setScript_name(String script_name) {
        this.script_name = script_name;
    }

    public List<MethodInfo> getList() {
        return list;
    }

    public void setList(List<MethodInfo> list) {
        this.list = list;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    @Override
    public String toString() {
        StringBuilder suite = new StringBuilder("Recorder\n{\n\t\"script_name\": " + script_name + ",\n\t\"tests_list\": {\n");
        for (int i = 0; i < list.size(); i++) {
            MethodInfo info = list.get(i);
            suite.append("\t\t\"").append(i).append("\"").append(": ");
            suite.append(info).append("\n");
        }
        suite.append("\t},\n\t\"result\": ").append(result).append("\n}");
        return suite.toString();
    }
}
