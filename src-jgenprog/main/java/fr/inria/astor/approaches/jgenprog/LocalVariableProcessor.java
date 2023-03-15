package fr.inria.astor.approaches.jgenprog;

import fr.inria.astor.util.GroundTruth;
import fr.inria.astor.util.FileTools;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtLocalVariable;

import java.util.HashSet;
import java.util.Set;

public class LocalVariableProcessor extends AbstractProcessor<CtLocalVariable> {
    public Set<String> varList = new HashSet<>();
    String location = "";
    public LocalVariableProcessor(String location) {
        this.location = location;
    }

    @Override
    public void process(CtLocalVariable ctLocalVariable) {
        for (GroundTruth gt : FileTools.GTs) {
            if (!gt.getLocation().endsWith(location))
                continue;
            String var = gt.getName();
            if (var.equals(ctLocalVariable.getReference().toString())) {
                System.out.println(ctLocalVariable + "[decl]");//total declaration
                System.out.println(ctLocalVariable.getReference() + "[ref]");
                varList.add(ctLocalVariable.getReference().toString());
                break;
            }
        }
        System.out.println(ctLocalVariable.getAssignment() + "[asg]");
    }
}
