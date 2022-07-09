package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.RandomManager;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CodeAddFactory {
    static String[] _types = {"double", "float", "long", "int", "short", "short", "byte"};

    public static CtBinaryOperator createCondition(CtElement root, CtElement newPart) {
        CtExpression left = (CtExpression) MutationSupporter.getFactory().Core().clone(root);
        CtExpression right = (CtExpression) MutationSupporter.getFactory().Core().clone(newPart);
        int selection = RandomManager.nextInt(1);
        CtBinaryOperator newCond = MutationSupporter.getFactory().Core().createBinaryOperator();
        newCond.setLeftHandOperand(left);
        newCond.setRightHandOperand(right);
        newCond.setKind(selection == 0? BinaryOperatorKind.AND : BinaryOperatorKind.OR);
        left.setParent(newCond);
        right.setParent(newCond);
        newCond.setParent(root.getParent());
        try {
            newCond.toString();
        } catch (Exception e) {
            return null;
        }
        return newCond;
    }

    public static CtElement deleteCondition(CtBinaryOperator old) {
        int selection = RandomManager.nextInt(1);
        CtExpression newCond = MutationSupporter.getFactory().Core().clone(selection == 0? old.getLeftHandOperand(): old.getRightHandOperand());
        newCond.setParent(old.getParent());
        return newCond;
    }

    public static CtElement createBinaryOperator(CtBinaryOperator old) {
        CtExpression left = null;
        CtExpression right = null;
        CtExpression single = null;
        CtBinaryOperator newOne = MutationSupporter.getFactory().Core().createBinaryOperator();
        CtBinaryOperator wholeExp = MutationSupporter.getFactory().Core().createBinaryOperator();
        if (old.getRightHandOperand() instanceof CtBinaryOperator) {
            left = MutationSupporter.getFactory().Core().clone(((CtBinaryOperator<?>) old.getRightHandOperand()).getLeftHandOperand());
            right = MutationSupporter.getFactory().Core().clone(((CtBinaryOperator<?>) old.getRightHandOperand()).getRightHandOperand());
            single = MutationSupporter.getFactory().Core().clone(old.getLeftHandOperand());
            newOne.setLeftHandOperand(single);
            newOne.setKind(old.getKind());
            newOne.setRightHandOperand(left);
            single.setParent(newOne);
            left.setParent(newOne);

            wholeExp.setLeftHandOperand(newOne);
            wholeExp.setKind(((CtBinaryOperator<?>) old.getRightHandOperand()).getKind());
            wholeExp.setRightHandOperand(right);
            newOne.setParent(wholeExp);
            right.setParent(wholeExp);
            wholeExp.setParent(old.getParent());
        } else if (old.getLeftHandOperand() instanceof CtBinaryOperator) {
            left = MutationSupporter.getFactory().Core().clone(((CtBinaryOperator<?>) old.getLeftHandOperand()).getLeftHandOperand());
            right = MutationSupporter.getFactory().Core().clone(((CtBinaryOperator<?>) old.getLeftHandOperand()).getRightHandOperand());
            single = MutationSupporter.getFactory().Core().clone(old.getRightHandOperand());
            newOne.setLeftHandOperand(right);
            newOne.setKind(old.getKind());
            newOne.setRightHandOperand(single);
            single.setParent(newOne);
            right.setParent(newOne);

            wholeExp.setLeftHandOperand(left);
            wholeExp.setKind(((CtBinaryOperator<?>) old.getLeftHandOperand()).getKind());
            wholeExp.setRightHandOperand(newOne);
            newOne.setParent(wholeExp);
            left.setParent(wholeExp);
            wholeExp.setParent(old.getParent());
        }
        return wholeExp;
    }

    public static CtContinue createContinue() {
        return MutationSupporter.getFactory().Core().createContinue();
    }

    public static CtVariableRead createVariableRead(CtVariable old) {
        CtVariableReference oldVariable = MutationSupporter.getFactory().Core().clone(old.getReference());
        CtVariableRead newElement =  MutationSupporter.getFactory().Core().createVariableRead();
        newElement.setVariable(oldVariable);
        newElement.setType(oldVariable.getType());
        newElement.setParent(old);
        return newElement;
    }

    public static CtVariableRead createTypeCast(CtVariableRead root, CtTypeReference typeReference) {
        CtVariableRead newExp = MutationSupporter.getFactory().Core().createVariableRead();
        newExp.setVariable(MutationSupporter.getFactory().Core().clone(root.getVariable()));
        newExp.setType(MutationSupporter.getFactory().Core().clone(root.getType()));

        CtTypeReference typecast = MutationSupporter.getFactory().Core().createTypeReference();
        typecast.setDeclaringType(MutationSupporter.getFactory().Core().clone(typeReference));
        newExp.addTypeCast(typecast);
        typecast.setParent(newExp);
        newExp.setParent(root.getParent());
        return newExp;
    }

    public static CtUnaryOperator createTypeCast(CtUnaryOperator root, CtTypeReference typeReference) {
        CtUnaryOperator newExp = MutationSupporter.getFactory().Core().createUnaryOperator();
        CtUnaryOperator newroot = MutationSupporter.getFactory().Core().clone(root);
        newExp.setKind(newroot.getKind());
        newExp.setOperand(newroot.getOperand());
        newExp.setType(newroot.getType());

        CtTypeReference typecast = MutationSupporter.getFactory().Core().createTypeReference();
        typecast.setDeclaringType(typeReference);
        newExp.addTypeCast(typecast);
        typecast.setParent(newExp);
        newExp.setParent(root.getParent());
        return newExp;
    }

    public static CtConditional createTypeCast(CtConditional root, CtTypeReference typeReference) {
        CtConditional newExp = MutationSupporter.getFactory().Core().createConditional();
        CtConditional newroot = MutationSupporter.getFactory().Core().clone(root);
        newExp.setCondition(newroot.getCondition());
        newExp.setType(newroot.getType());

        CtTypeReference typecast = MutationSupporter.getFactory().Core().createTypeReference();
        typecast.setDeclaringType(typeReference);
        newExp.addTypeCast(typecast);
        typecast.setParent(newExp);
        newExp.setParent(root.getParent());
        return newExp;
    }

    public static CtBinaryOperator createTypeCast(CtBinaryOperator root, CtTypeReference typeReference) {
        CtBinaryOperator newExp = MutationSupporter.getFactory().Core().createBinaryOperator();
        CtBinaryOperator newroot = MutationSupporter.getFactory().Core().clone(root);
        newExp.setKind(newroot.getKind());
        newExp.setType(newroot.getType());
        newExp.setLeftHandOperand(newroot.getLeftHandOperand());
        newExp.setRightHandOperand(newroot.getRightHandOperand());

        CtTypeReference typecast = MutationSupporter.getFactory().Core().createTypeReference();
        typecast.setDeclaringType(typeReference);
        newExp.addTypeCast(typecast);
        typecast.setParent(newExp);
        newExp.setParent(root.getParent());
        return newExp;
    }

    public static CtLiteral createTypeCast(CtLiteral root, CtTypeReference typeReference) {
        CtLiteral newExp = MutationSupporter.getFactory().Core().createLiteral();
        CtLiteral newroot = MutationSupporter.getFactory().Core().clone(root);
        newExp.setBase(newroot.getBase());
        newExp.setType(newroot.getType());
        newExp.setValue(newroot.getValue());

        CtTypeReference typecast = MutationSupporter.getFactory().Core().createTypeReference();
        typecast.setDeclaringType(typeReference);
        newExp.addTypeCast(typecast);
        typecast.setParent(newExp);
        newExp.setParent(root.getParent());
        return newExp;
    }

    public static CtInvocation createTypeCast(CtInvocation root, CtTypeReference typeReference) {
        CtInvocation newExp = MutationSupporter.getFactory().Core().createInvocation();
        CtInvocation newroot = MutationSupporter.getFactory().Core().clone(root);
        newExp.setExecutable(newroot.getExecutable());
        newExp.setType(newroot.getType());
        newExp.setArguments(newroot.getArguments());

        CtTypeReference typecast = MutationSupporter.getFactory().Core().createTypeReference();
        typecast.setDeclaringType(typeReference);
        newExp.addTypeCast(typecast);
        typecast.setParent(newExp);
        newExp.setParent(root.getParent());
        return newExp;
    }

    public static CtExpression createTypeCast(CtExpression root, CtTypeReference typeReference) {//problem
        CtExpression newExp = MutationSupporter.getFactory().Core().clone(root);

        CtTypeReference typecast = MutationSupporter.getFactory().Core().createTypeReference();
        typecast.setDeclaringType(typeReference);
        newExp.addTypeCast(typecast);
        typecast.setParent(newExp);
        newExp.setParent(root.getParent());
        return newExp;
    }

    public static CtInvocation createInvocationSameName(CtInvocation old, CtInvocation change) {
        CtInvocation newExp = MutationSupporter.getFactory().Core().createInvocation();
        if (old.getTarget() != null && old.getTarget() instanceof CtVariableRead) {
            CtExpression target = MutationSupporter.getFactory().Core().clone(old.getTarget());
            newExp.setTarget(target);
            target.setParent(newExp);
        }
        CtExecutableReference exe = MutationSupporter.getFactory().Core().clone(old.getExecutable());
        newExp.setExecutable(exe);
        exe.setParent(newExp);
        newExp.setType(exe.getType());
        List<CtExpression<?>> args_copy = new ArrayList<>();
        List<CtExpression<?>> args = change.getArguments();
        for (CtExpression exp :args) {
            CtExpression copy = MutationSupporter.getFactory().Core().clone(exp);
            args_copy.add(copy);
            copy.setParent(newExp);
        }
        newExp.setArguments(args_copy);
        newExp.setParent(old.getParent());
        try {
            newExp.toString();
        } catch (Exception e) {
            return null;
        }
        return newExp;
    }

    public static CtInvocation createInvocationSameArgs(CtInvocation old, CtExecutableReference change) {
        CtInvocation newExp = MutationSupporter.getFactory().Core().createInvocation();
        if (old.getTarget() != null && old.getTarget() instanceof CtVariableRead) {
            CtExpression target = MutationSupporter.getFactory().Core().clone(old.getTarget());
            newExp.setTarget(target);
            target.setParent(newExp);
        }
        CtExecutableReference exe = MutationSupporter.getFactory().Core().clone(change);
        newExp.setExecutable(exe);
        exe.setParent(newExp);
        newExp.setType(exe.getType());
        List<CtExpression<?>> args_copy = new ArrayList<>();
        List<CtExpression<?>> args = old.getArguments();
        for (CtExpression exp :args) {
            CtExpression copy = MutationSupporter.getFactory().Core().clone(exp);
            args_copy.add(copy);
            copy.setParent(newExp);
        }
        newExp.setArguments(args_copy);
        newExp.setParent(old.getParent());
        try {
            newExp.toString();
        } catch (Exception e) {
            return null;
        }
        return newExp;
    }

    public static List<Ingredient> createTypeChangeInredients(CtLocalVariable point, Map<String, Set<CtTypeReference>> base) {
        List<Ingredient> list = new ArrayList<>();
        CtTypeReference oldType = point.getType();

        if (oldType.isPrimitive()) {
            //need to compare the accuracy of type. only high to low.
            for (int i = 0; i < _types.length; i++) {
                if (oldType.getSimpleName().equalsIgnoreCase(_types[i])) {
                    for (int j = i - 1; j >= 0; j--) {
                        CtTypeReference newType = MutationSupporter.getFactory().Core().createTypeReference();
                        CtLocalVariable newPoint = MutationSupporter.getFactory().Core().clone(point);
                        newType.setSimpleName(_types[j]);
                        newPoint.setType(newType);
                        newPoint.setParent(point.getParent());
                        list.add(new Ingredient(newPoint));
                    }
                    break;
                }
            }
        } else {
            CtTypeReference newType = MutationSupporter.getFactory().Core().createTypeReference();
            CtLocalVariable newPoint = MutationSupporter.getFactory().Core().clone(point);
            newType.setSimpleName("Object");
            newPoint.setType(newType);
            newPoint.setParent(point.getParent());
            list.add(new Ingredient(newPoint));
        }
        return list;
    }

}
