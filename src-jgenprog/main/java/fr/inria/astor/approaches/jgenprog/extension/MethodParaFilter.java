package fr.inria.astor.approaches.jgenprog.extension;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.support.reflect.code.CtStatementImpl;

import java.util.List;

public class MethodParaFilter extends AbstractFilter<CtMethod> {
    private List<Integer> _positions;
    private String _name;

    public void set_positions(List<Integer> positions) {
        _positions = positions;
    }
    public void set_name(String name) {
        this._name = name;
    }

    public boolean compare(int startLine, int endLine) {
        for (int position : _positions) {
            if (startLine == position && endLine >= position) {
                return true;
            }
            if (startLine <= position && endLine == position) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(CtMethod element) {
        if (!element.getParameters().isEmpty() && element.getPosition().getLine() == _positions.get(0)) {
            List paras = element.getParameters();
            for (Object para :paras) {
                if (((CtParameter) para).getSimpleName().equals(_name))
                    return true;
            }
        }
        return false;
    }
}