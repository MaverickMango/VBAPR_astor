package fr.inria.astor.approaches.jgenprog.extension;

import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;

public class ExpressionOperatorInstance extends OperatorInstance {
    private CtElement parent = null;
    private CtRole roleInParent = null;

    public ExpressionOperatorInstance() {
        super();
    }

    /**
     * Creates a modification instance
     *
     * @param modificationPoint
     * @param operationApplied
     * @param original
     * @param modified
     */
    public ExpressionOperatorInstance(ModificationPoint modificationPoint, AstorOperator operationApplied, CtElement original, CtElement modified) {
        super(modificationPoint, operationApplied, original, modified);
        this.setParentInformation(modificationPoint);
    }

    public CtElement getParent() {
        return parent;
    }

    public void setParent(CtElement parent) {
        this.parent = parent;
    }

    public CtRole getRoleInParent() {
        return roleInParent;
    }

    public void setRoleInParent(CtRole roleInParent) {
        this.roleInParent = roleInParent;
    }

    public void setParentInformation(ModificationPoint point) {
        setParent(point.getCodeElement().getParent());
        setRoleInParent(point.getCodeElement().getRoleInParent());
    }
}
