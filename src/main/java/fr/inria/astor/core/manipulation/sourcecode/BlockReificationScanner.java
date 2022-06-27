package fr.inria.astor.core.manipulation.sourcecode;

import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Reification of Blocks. It creates a new parent
 * of type block for all statements which parents are not blocks
 * (for the moment only if)
 * @author matias
 *
 * add while & if itself
 */
public class BlockReificationScanner extends CtScanner {

	@Override
	public void visitCtIf(CtIf element) {
		super.visitCtIf(element);
		if(!(element.getThenStatement() instanceof CtBlock)){
			CtStatement c = element.getThenStatement() ;
			CtBlock nBlock = MutationSupporter.getFactory().Core().createBlock();
			nBlock.addStatement(c);
			element.setThenStatement(nBlock);
		}
		
		if( element.getElseStatement() != null && !(element.getElseStatement() instanceof CtBlock)){
			CtStatement c = element.getElseStatement() ;
			CtBlock nBlock = MutationSupporter.getFactory().Core().createBlock();
			nBlock.addStatement(c);
			element.setElseStatement(nBlock);
		}


		addParentBlock(element);
	}

	@Override
	public void visitCtWhile(CtWhile element) {
		super.visitCtWhile(element);
		addParentBlock(element);
	}

	@Override
	public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> element) {
		super.visitCtAssignment(element);
		addParentBlock(element);
	}

	@Override
	public <T> void visitCtInvocation(CtInvocation<T> element) {
		super.visitCtInvocation(element);
		addParentBlock(element);
	}

	@Override
	public <R> void visitCtReturn(CtReturn<R> element) {
		super.visitCtReturn(element);
		addParentBlock(element);
	}

	private void addParentBlock(CtElement element) {
		if (!(element.getParent() instanceof CtBlock) && CtRole.STATEMENT.equals(element.getRoleInParent())) {
			List<CtStatement> ctElementList = element.getParent().getValueByRole(CtRole.STATEMENT);
			CtBlock nBlock = MutationSupporter.getFactory().Core().createBlock();
			for (CtStatement e :ctElementList) {
				CtStatement c = MutationSupporter.getFactory().Core().clone(e);
				nBlock.addStatement(c);
				c.setParent(nBlock);
			}
			ctElementList = new ArrayList<>();
			ctElementList.add(nBlock);
			element.getParent().setValueByRole(CtRole.STATEMENT, ctElementList);
		}
	}
}
