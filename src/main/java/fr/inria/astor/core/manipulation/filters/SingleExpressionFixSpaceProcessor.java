package fr.inria.astor.core.manipulation.filters;

import org.apache.log4j.Logger;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtAnnotation;

public class SingleExpressionFixSpaceProcessor extends TargetElementProcessor<CtExpression> {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void process(CtExpression element) {
        if (element instanceof CtNewArray || element instanceof CtTypeAccess
                || element instanceof CtSuperAccess || element instanceof CtAnnotation
                //|| element instanceof CtThisAccess //|| element instanceof CtFieldAccess//add field&this
                || element instanceof CtVariableWrite )//|| element instanceof CtLiteral
            return;
        if (element.getType() != null) {
//            if (element instanceof CtLiteralImpl && !(element.getType().toString().equals("boolean")))
//                return;
            this.add(element);
        }
    }
}
