package fr.inria.astor.approaches.jgenprog;
import fr.inria.astor.util.GroundTruth;
import fr.inria.astor.util.ReadGT;
import spoon.processing.AbstractProcessor;
import spoon.reflect.reference.CtVariableReference;

import java.util.HashSet;
import java.util.Set;

public class VariableReferenceProcessor extends AbstractProcessor<CtVariableReference> {
    String location = "";
    public Set<String> varList = new HashSet<>();
    public VariableReferenceProcessor(String location) {
        this.location = location;
    }
    public VariableReferenceProcessor() {}

    @Override
    public void process(CtVariableReference ctVariableReference) {
        for (GroundTruth gt : ReadGT.GTs) {
            if (!gt.getLocation().endsWith(location))
                continue;
            String var = gt.getName();
            if (var.equals(ctVariableReference.toString())) {
//                System.out.println(ctVariableReference);
                varList.add(ctVariableReference.toString());
            }
        }
    }
}
