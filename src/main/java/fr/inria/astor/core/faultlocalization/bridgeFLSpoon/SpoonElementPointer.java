package fr.inria.astor.core.faultlocalization.bridgeFLSpoon;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtTypedElement;

import java.util.ArrayList;
import java.util.List;

public class SpoonElementPointer extends AbstractProcessor<CtTypedElement> {
    public static List<CtElement> children = new ArrayList<>();


    public void process(CtTypedElement element) {
        if (element instanceof CtNewArray || element instanceof CtTypeAccess
                || element instanceof CtSuperAccess || element instanceof CtAnnotation
                //|| element instanceof CtThisAccess //|| element instanceof CtFieldAccess//add field&this
                || element instanceof CtVariableWrite)//|| element instanceof CtLiteral
            return;
        if (element.getType() != null) {
//            if (element instanceof CtLiteralImpl && !(element.getType().toString().equals("boolean")))
//                return;
            children.add(element);
        }
    }
}
