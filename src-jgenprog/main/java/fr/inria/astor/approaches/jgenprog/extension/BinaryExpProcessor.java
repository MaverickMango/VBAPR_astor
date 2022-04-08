package fr.inria.astor.approaches.jgenprog.extension;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;

import java.util.ArrayList;
import java.util.List;

public class BinaryExpProcessor extends AbstractProcessor<CtBinaryOperator> {
    public List<CtBinaryOperator> expList = new ArrayList<>();

    @Override
    public void process(CtBinaryOperator element) {
        if (element.getKind().equals(BinaryOperatorKind.AND) || element.getKind().equals(BinaryOperatorKind.OR)) {
            expList.add(element);
        }
    }
}
