package fr.inria.astor.test.repair.approaches.VBAPR;

public class MethodInfo {
    String method_id;
    Boolean result;
    String error_msg;

    public String getMethod_id() {
        return method_id;
    }

    public void setMethod_id(String method_id) {
        this.method_id = method_id;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    @Override
    public String toString() {
        return "{\n\t\t\t\"test\": " + method_id + ",\n\t\t\t\"result\": " + result + ",\n\t\t\t\"error_msg\": " + error_msg + "\n\t\t}";
    }

}