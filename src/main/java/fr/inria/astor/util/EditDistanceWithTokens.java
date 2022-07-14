package fr.inria.astor.util;

import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtElement;

import java.util.ArrayList;
import java.util.List;

public class EditDistanceWithTokens {
    List<String> targetTokens = null;
    List<String> sourceTokens = null;

    public EditDistanceWithTokens(CtBlock original) {
        sourceTokens = new ArrayList<>();
        targetTokens = new ArrayList<>();
        setSourceTokens(original);
    }

    public EditDistanceWithTokens() {
        sourceTokens = new ArrayList<>();
        targetTokens = new ArrayList<>();
    }

    private void setTokens(StringBuilder source, boolean isSource) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> list = null;
        if (isSource)
            list = sourceTokens;
        else
            list = targetTokens;
        boolean flag = false;
        while (source.length() != 0) {
            String s = source.substring(0, 1);
            source = source.delete(0, 1);
            if (s.equals(" ") && stringBuilder.toString().equals(""))
                continue;
            switch (s.toString()) {
                case "{":
                case "}":
                case "(":
                case ")":
                case " ":
                case ":":
                case ";":
                case ".":
                case "=":
                case "+":
                case "-":
                case "x":
                case "/":
                case "%":
                    flag = true;
                    break;
                default:;
            }
            if (flag) {
                if (!"".equals(stringBuilder.toString()))
                    list.add(stringBuilder.toString());
                stringBuilder.delete(0, stringBuilder.length());
                flag = false;
                if (!(" ".equals(s.toString()) || ";".equals(s.toString())))
                    list.add(s.toString());
            } else
                stringBuilder.append(s);
        }
        if (!"".equals(stringBuilder.toString()) && !" ".equals(stringBuilder.toString()))
            list.add(stringBuilder.toString());
    }

    private void setTargetTokens(CtElement modified) {
        if (modified == null)
            return;
        setTokens(new StringBuilder(modified.toString().replaceAll("\n", "")), false);
    }

    private void setSourceTokens(CtElement original) {
        if (original == null)
            return;
        StringBuilder source = new StringBuilder(original.toString().replaceAll("\n", ""));
        setTokens(source, true);
    }

    public String calEditDisctance(CtElement original, CtElement modified) {
        setSourceTokens(original);
        setTargetTokens(modified);
        return OracleEditDistance.evaluate(sourceTokens, targetTokens);
    }

    public String calEditDisctance(CtElement modified) {
        setTargetTokens(modified);
        return OracleEditDistance.evaluate(sourceTokens, targetTokens);
    }

    public String calEditDisctance(StringBuilder original, StringBuilder modified) {
        setTokens(original, true);
        setTokens(modified, false);
        return OracleEditDistance.evaluate(sourceTokens, targetTokens);
    }

}
