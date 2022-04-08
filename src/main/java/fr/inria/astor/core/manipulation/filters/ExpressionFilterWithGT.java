package fr.inria.astor.core.manipulation.filters;

import fr.inria.astor.util.GroundTruth;
import fr.inria.astor.util.ReadGT;
import spoon.reflect.code.CtExpression;
import spoon.reflect.visitor.Filter;

public class ExpressionFilterWithGT implements Filter<CtExpression> {
    private String location;

    public ExpressionFilterWithGT(String location) {
        this.location = location;
    }
    @Override
    public boolean matches(CtExpression element) {
        for (GroundTruth gt : ReadGT.GTs) {
            if (!gt.getLocation().endsWith(location))
                continue;
            if (gt.isExp()) {
                String exp = trim(gt.getName());
                if (exp.startsWith("(") && exp.endsWith(")")) {
                    exp = exp.substring(1, exp.length() - 1);
                }
                String code = element.getOriginalSourceFragment().getSourceCode();
                code = trim(code);
                if (code.contains(exp))
                    return true;
            }
        }
        return false;
    }

    public String trim(String str) {
        return str.replace("\r", "")
                .replace("\n", "")
                .replace(" ", "")
                .replace("\t", "");
    }
}
