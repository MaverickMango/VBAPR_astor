package fr.inria.astor.approaches.jgenprog.extension;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.AbstractFilter;

public class ExpressionFilter extends AbstractFilter<CtElement> {
    private String _name;

    public ExpressionFilter() {
    }
    public ExpressionFilter(String name) {
        set_name(name);
    }


    public void set_name(String name) {
        this._name = compactStr(name);
    }

    public String get_name() {
        return _name;
    }

    boolean compare(String str) {
        String name = _name;
        if (name.startsWith("(") && name.endsWith(")"))
            name = name.substring(1, name.length() - 1);
        String comp = compactStr(str);
        if (comp.startsWith("(") && comp.endsWith(")"))
            comp = comp.substring(1, comp.length() - 1);
//        if (name.equals(comp)){
//            return true;
//        }
//        return _name.equals(compactStr(str));
        return name.equals(comp);
    }

    String compactStr(String str) {
        return str.trim().replace(" ", "")
                .replace("\n", "")
                .replace("\t", "");
    }

    @Override
    public boolean matches(CtElement element) {
        if (element instanceof CtExpression || element instanceof CtVariableReference) {
            if (element instanceof CtThisAccess || element instanceof CtSuperAccess
            )//|| element instanceof CtTypeAccess
                return false;
            String str = "";
            try {
                str = element.getOriginalSourceFragment().getSourceCode();
            } catch (Exception e) {
                str = element.toString();
//                System.err.println(str);
            }
            return compare(str);
        }
        return false;
    }
}