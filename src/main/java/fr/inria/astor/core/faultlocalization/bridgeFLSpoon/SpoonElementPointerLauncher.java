package fr.inria.astor.core.faultlocalization.bridgeFLSpoon;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

import java.util.ArrayList;
import java.util.List;

public class SpoonElementPointerLauncher extends SpoonLauncher{
    public SpoonElementPointerLauncher(Factory factory) throws Exception {
        super(factory);
    }


    public List<CtElement> run(CtElement ctelement) {
        this.addProcessor(SpoonElementPointer.class.getName());
        SpoonElementPointer.children.clear();
        this.process(ctelement);
        return new ArrayList<>(SpoonElementPointer.children);
    }
}
