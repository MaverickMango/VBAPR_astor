package fr.inria.astor.approaches.jgenprog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.ingredientbased.IngredientBasedEvolutionaryRepairApproachImpl;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.setup.RandomManager;
import fr.inria.astor.util.FileTools;
import fr.inria.main.evolution.ExtensionPoints;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.support.QueueProcessingManager;
import spoon.support.reflect.code.CtAssignmentImpl;
import spoon.support.reflect.code.CtBlockImpl;
import spoon.support.reflect.code.CtIfImpl;

/**
 * Core repair approach based on reuse of ingredients.
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class JGenProg extends IngredientBasedEvolutionaryRepairApproachImpl {

	public JGenProg(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade) throws JSAPException {
		super(mutatorExecutor, projFacade);
		FileTools.getInfos();
		setPropertyIfNotDefined(ExtensionPoints.OPERATORS_SPACE.identifier, "irr-statements");
		setPropertyIfNotDefined(ExtensionPoints.TARGET_CODE_PROCESSOR.identifier, "statements");
	}

	@Override
	public void filterSolutions() {
		super.filterSolutions();
		String[] info = FileTools.getInfos();
//		List<GroundTruth> gts = ReadFileUtil.getGTs(info[0], Integer.parseInt(info[1]));
//		List<ProgramVariant> filteredSolutions = new ArrayList<>();
		List<Integer> filteredSolutions = new ArrayList<>();
		for (ProgramVariant solutionVariant : this.solutions) {
			boolean filtered = true;
			int gen = 0;
			for (int i = 1; i <= this.generationsExecuted; i++) {
				List<OperatorInstance> genOperationInstances = solutionVariant.getOperations().get(i);
				if (genOperationInstances == null)
					continue;

				for (OperatorInstance genOperationInstance : genOperationInstances) {
					String location = genOperationInstance.getModificationPoint().getCtClass().getQualifiedName();
					LocalVariableProcessor localVariableProcessor = new LocalVariableProcessor(location);
					VariableReferenceProcessor referenceProcessor = new VariableReferenceProcessor(location);
					Set<String> oriVars;
					Set<String> modiVars;
					String op = genOperationInstance.getOperationApplied().toString();
					CtElement original = genOperationInstance.getOriginal();
					if (op.equals("RemoveOp")) {
						if (original instanceof CtIfImpl) {
							CtStatement thenStmt = ((CtIfImpl) original).getThenStatement();
							assert thenStmt instanceof CtBlockImpl;
							for (Object obj :((CtBlockImpl)thenStmt).getStatements()) {
								CtStatement stmt = (CtStatement) obj;
								if (!(stmt instanceof CtAssignmentImpl)) {
									continue;
								}
								//
								filtered = FileTools.considerVariableReference(location, original, this.mutatorSupporter.getFactory());
							}
							continue;
						}
						if (!original.getClass().getSimpleName().equals("CtAssignmentImpl"))
							continue;
						//
						filtered = FileTools.considerVariableReference(location, original, this.mutatorSupporter.getFactory());
					}
					if (op.equals("InsertStatementOp")) {
						if (genOperationInstance.getModified() != null) {
							CtElement modified = genOperationInstance.getModified();
							//
							filtered = FileTools.considerVariableReference(location, modified, this.mutatorSupporter.getFactory());
						}
					}
					if (op.equals("ReplaceOp")) {
						if (genOperationInstance.getModified() != null) {
							ProcessingManager processingManager = new QueueProcessingManager(this.mutatorSupporter.getFactory());
							CtElement modified = genOperationInstance.getModified();
							processingManager.addProcessor(localVariableProcessor);
							processingManager.addProcessor(referenceProcessor);
							processingManager.process(original);
							oriVars = new HashSet<>(localVariableProcessor.varList);
							oriVars.addAll(referenceProcessor.varList);
							localVariableProcessor.varList.clear();
							referenceProcessor.varList.clear();
							processingManager.process(modified);
							modiVars = new HashSet<>(localVariableProcessor.varList);
							modiVars.addAll(referenceProcessor.varList);
							filtered = FileTools.filtered(oriVars, modiVars);
						}
					}
				}
			}
			if (filtered) {
//				filteredSolutions.add(solutionVariant);
				filteredSolutions.add(solutionVariant.getId());
			}
		}
//		this.solutions = filteredSolutions;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\nbug:" + info[0] + "-" + info[1] + "\n");
		stringBuilder.append("original solutions: " + this.solutions.size() + "\n");
		stringBuilder.append("filtered numbers: " + filteredSolutions.size() + "\n");
		stringBuilder.append("filtered:");
		for (Integer id :filteredSolutions) {
			stringBuilder.append(" " + id);
		}
		stringBuilder.append("\n");
		try {
			FileTools.outputFiltered(stringBuilder.toString());
		} catch (IOException e) {
			System.out.println(stringBuilder.toString());
		}
	}

	@Override
	public void prepareNextGeneration(List<ProgramVariant> temporalInstances, int generation) {

		super.prepareNextGeneration(temporalInstances, generation);

		if (ConfigurationProperties.getPropertyBool("applyCrossover")) {
			applyCrossover(generation);
		}
	}

	@Override
	public void loadOperatorSpaceDefinition() throws Exception {

		super.loadOperatorSpaceDefinition();

		if (this.getOperatorSpace() == null) {

			this.setOperatorSpace(new jGenProgSpace());
		}

	}

	private void applyCrossover(int generation) {
		int numberVariants = this.variants.size();
		if (numberVariants <= 1) {
			log.debug("CO|Not Enough variants to apply Crossover");
			return;
		}

		// We randomly choose the two variants to crossover
		ProgramVariant v1 = this.variants.get(RandomManager.nextInt(numberVariants));
		ProgramVariant v2 = this.variants.get(RandomManager.nextInt(numberVariants));
		// Same instance
		if (v1 == v2) {
			log.debug("CO|randomless chosen the same variant to apply crossover");
			return;
		}

		if (v1.getOperations().isEmpty() || v2.getOperations().isEmpty()) {
			log.debug("CO|Not Enough ops to apply Crossover");
			return;
		}
		// we randomly select the generations to apply
		int rgen1index = RandomManager.nextInt(v1.getOperations().keySet().size()) + 1;
		int rgen2index = RandomManager.nextInt(v2.getOperations().keySet().size()) + 1;

		List<OperatorInstance> ops1 = v1.getOperations((int) v1.getOperations().keySet().toArray()[rgen1index]);
		List<OperatorInstance> ops2 = v2.getOperations((int) v2.getOperations().keySet().toArray()[rgen2index]);

		OperatorInstance opinst1 = ops1.remove((int) RandomManager.nextInt(ops1.size()));
		OperatorInstance opinst2 = ops2.remove((int) RandomManager.nextInt(ops2.size()));

		if (opinst1 == null || opinst2 == null) {
			log.debug("CO|We could not retrieve a operator");
			return;
		}

		// The generation of both new operators is the Last one.
		// In the first variant we put the operator taken from the 2 one.
		v1.putModificationInstance(generation, opinst2);
		// In the second variant we put the operator taken from the 1 one.
		v2.putModificationInstance(generation, opinst1);
		//
	}

}
