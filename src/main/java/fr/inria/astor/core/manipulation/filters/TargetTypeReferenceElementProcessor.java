package fr.inria.astor.core.manipulation.filters;

import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.solutionsearch.extension.AstorExtensionPoint;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.declaration.CtElement;

import java.util.ArrayList;
import java.util.List;

public abstract class TargetTypeReferenceElementProcessor<T extends CtElement> extends AbstractProcessor<T> implements AstorExtensionPoint {

    public boolean allowsDuplicateIngredients = false;


    public TargetTypeReferenceElementProcessor(){
        allowsDuplicateIngredients = ConfigurationProperties.getPropertyBool("duplicateingredientsinspace");
    }
    /**
     * This list saves the result
     */
    public static List<CtElement> spaceElements = new ArrayList<CtElement>();

    public void add(CtElement st) {

        if(st == null ||st.getParent() == null){
            return;
        }

        if (allowsDuplicateIngredients ||  !contains(st)) {
            CtElement code = st;
            spaceElements.add(code);
        }
    }

    public boolean contains(CtElement st) {
        for (CtElement ce : spaceElements) {
            if (ce.toString().equals(st.toString())) {
                return true;
            }
        }
        return false;
    }


}