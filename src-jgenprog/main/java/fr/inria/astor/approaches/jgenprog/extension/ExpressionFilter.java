package fr.inria.astor.approaches.jgenprog.extension;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.support.reflect.code.CtVariableReadImpl;

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
        String comp = compactStr(str);
        if (name.equals(comp)){
            return true;
        }
        if (name.startsWith("(") && name.endsWith(")"))
            name = name.substring(1, name.length() - 1);
        if (name.equals(comp)){
            return true;
        }
        if (comp.startsWith("(") && comp.endsWith(")"))
            comp = comp.substring(1, comp.length() - 1);
        return name.equals(comp);
    }

    String compactStr(String str) {
        return str.trim().replace(" ", "")
                .replace("\n", "")
                .replace("\t", "");
    }

    @Override
    public boolean matches(CtElement element) {
        if (element instanceof CtExpression || element instanceof CtVariable) {
            if (element instanceof CtSuperAccess
            )//|| element instanceof CtTypeAccess element instanceof CtThisAccess ||
                return false;
            String str = "";
            if (element instanceof CtLocalVariable)
                str = ((CtLocalVariable<?>) element).getSimpleName();
            else if(element instanceof CtVariableRead) {
                str = ((CtVariableRead) element).getVariable().getSimpleName();
            } else {
                try {
                    str = element.getOriginalSourceFragment().getSourceCode();
                } catch (Exception e) {
                    str = element.toString();
//                System.err.println(str);
                }
            }
            return compare(str);
        }
        return false;
    }
}