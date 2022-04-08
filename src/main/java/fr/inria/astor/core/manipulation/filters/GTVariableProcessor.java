package fr.inria.astor.core.manipulation.filters;

import fr.inria.astor.util.GroundTruth;
import fr.inria.astor.util.ReadGT;
import spoon.processing.AbstractProcessor;
import spoon.reflect.reference.CtVariableReference;

import java.util.HashSet;
import java.util.Set;

public class GTVariableProcessor extends AbstractProcessor<CtVariableReference> {
    public Set<String> varList = new HashSet<>();
    private String location;

    public GTVariableProcessor(String location) {
        this.location = location;
    }

    public GTVariableProcessor() {
    }

    @Override
    public void process(CtVariableReference ctVariableReference) {
//        .getElements(new TypeFilter<>(CtStatement.class))
        for (GroundTruth gt : ReadGT.GTs) {
            if (!gt.getLocation().endsWith(location))
                continue;
            if (!gt.isExp()) {
                String var = gt.getName();
                if (var.equals(ctVariableReference.toString())) {
//                System.out.println(ctVariableReference);
                    varList.add(ctVariableReference.toString());
                    return;
                }
            }
        }
    }
}
