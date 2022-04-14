package fr.inria.astor.approaches.jgenprog.extension;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.support.reflect.code.CtStatementImpl;

import java.util.List;

public class StatementFilter extends AbstractFilter<CtStatement> {
    private List<Integer> _positions;

    public void set_positions(List<Integer> positions) {
        _positions = positions;
    }

    public boolean compare(int startLine, int endLine) {
        for (int position : _positions) {
            if (startLine <= position && endLine >= position) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(CtStatement element) {
        if ((element instanceof CtStatementImpl && !(element instanceof CtBlock))
                || (element instanceof CtInvocation && element.getPosition() != null &&
                !(element.getPosition() instanceof NoSourcePosition))) {//
            int start = 0;
            int end = 0;
            try {
                start = element.getPosition().getLine();
                end = element.getPosition().getEndLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return compare(start, end);
        }
        return false;
    }
}