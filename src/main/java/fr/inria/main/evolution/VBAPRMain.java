package fr.inria.main.evolution;

import fr.inria.astor.approaches.cardumen.CardumenApproach;
import fr.inria.astor.approaches.deeprepair.DeepRepairEngine;
import fr.inria.astor.approaches.jgenprog.JGenProg;
import fr.inria.astor.approaches.jgenprog.extension.TibraApproach;
import fr.inria.astor.approaches.jkali.JKaliEngine;
import fr.inria.astor.approaches.jmutrepair.jMutRepairExhaustive;
import fr.inria.astor.approaches.scaffold.ScaffoldRepairEngine;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.ingredientbased.ExhaustiveIngredientBasedEngine;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.astor.util.FileTools;
import fr.inria.main.AbstractMain;
import fr.inria.main.ExecutionMode;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Astor main
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class VBAPRMain extends AbstractMain {

	protected Logger log = Logger.getLogger(VBAPRMain.class.getName());

	protected AstorCoreEngine core = null;

	/**
	 * It creates a repair engine according to an execution mode.
	 * 
	 * 
	 * @param mode
	 * @return
	 * @throws Exception
	 */

	public AstorCoreEngine createEngine(ExecutionMode mode) throws Exception {
		core = null;
		MutationSupporter mutSupporter = new MutationSupporter();

		if (ExecutionMode.DeepRepair.equals(mode)) {
			core = new DeepRepairEngine(mutSupporter, projectFacade);

		} else if (ExecutionMode.CARDUMEN.equals(mode)) {
			core = new CardumenApproach(mutSupporter, projectFacade);

		} else if (ExecutionMode.jKali.equals(mode)) {
			core = new JKaliEngine(mutSupporter, projectFacade);

		} else if (ExecutionMode.jGenProg.equals(mode)) {
			core = new JGenProg(mutSupporter, projectFacade);

		} else if (ExecutionMode.MutRepair.equals(mode)) {
			core = new jMutRepairExhaustive(mutSupporter, projectFacade);

		} else if (ExecutionMode.EXASTOR.equals(mode)) {
			core = new ExhaustiveIngredientBasedEngine(mutSupporter, projectFacade);

		} else if (ExecutionMode.TIBRA.equals(mode)) {
			core = new TibraApproach(mutSupporter, projectFacade);

		} else if (ExecutionMode.SCAFFOLD.equals(mode)) {
			core = new ScaffoldRepairEngine(mutSupporter, projectFacade);

		} else {
			FileTools.getInfos();
			// If the execution mode is any of the predefined, Astor
			// interpretes as
			// a custom engine, where the value corresponds to the class name of
			// the engine class
			String customengine = ConfigurationProperties.getProperty(ExtensionPoints.NAVIGATION_ENGINE.identifier);
			core = createEngineFromArgument(customengine, mutSupporter, projectFacade);

		}

		// Loading extension Points
		core.loadExtensionPoints();

		core.initModel();

		FileTools.getGTs(FileTools.getInfos());
		if (!FileTools.setGTElements())
			return null;

		try {
			if (ConfigurationProperties.getPropertyBool("skipfaultlocalization")) {
				// We dont use FL, so at this point the do not have suspicious
				List<SuspiciousCode> suspicious = new ArrayList<SuspiciousCode>();
				core.initPopulation(suspicious);
			} else {
				List<SuspiciousCode> suspicious = core.calculateSuspicious();

				if (suspicious == null || suspicious.isEmpty()) {
					throw new IllegalStateException("No suspicious line detected by the fault localization");
				}

				core.initPopulation(suspicious);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}

		return core;

	}

	/**
	 * We create an instance of the Engine which name is passed as argument.
	 * 
	 * @param customEngineValue
	 * @param mutSupporter
	 * @param projectFacade
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected AstorCoreEngine createEngineFromArgument(String customEngineValue, MutationSupporter mutSupporter,
			ProjectRepairFacade projectFacade) throws Exception {
		Object object = null;
		try {
			Class classDefinition = Class.forName(customEngineValue);
			object = classDefinition.getConstructor(mutSupporter.getClass(), projectFacade.getClass())
 					.newInstance(mutSupporter, projectFacade);
		} catch (Exception e) {
			log.error("Loading custom engine: " + customEngineValue + " --" + e);
			BufferedOutputStream buff =null;
			try {
				String content = FileTools.proj + "_" + FileTools.version + "\n";
				buff = new BufferedOutputStream(new FileOutputStream(FileTools.loadError, true));
				buff.write(content.getBytes(StandardCharsets.UTF_8));
				buff.flush();
				buff.close();
			} catch (IOException ec) {
				e.printStackTrace();
			}
			throw new Exception("Error Loading Engine: " + e);
		}
		if (object instanceof AstorCoreEngine)
			return (AstorCoreEngine) object;
		else
			throw new Exception(
					"The strategy " + customEngineValue + " does not extend from " + AstorCoreEngine.class.getName());

	}

	@Override
	public void run(String location, String projectName, String dependencies, String packageToInstrument, double thfl,
			String failing) throws Exception {

		initProject(location, projectName, dependencies, packageToInstrument, thfl, failing);

		String mode = ConfigurationProperties.getProperty("mode").toLowerCase();
		String customEngine = ConfigurationProperties.getProperty(ExtensionPoints.NAVIGATION_ENGINE.identifier);

		if (customEngine != null && !customEngine.isEmpty())
			core = createEngine(ExecutionMode.custom);
		else {
			for (ExecutionMode executionMode : ExecutionMode.values()) {
				for (String acceptedName : executionMode.getAcceptedNames()) {
					if (acceptedName.equals(mode)) {
						core = createEngine(executionMode);
						break;
					}
				}
			}

		}
		if (core == null) {
			System.err.println("Unknown mode of execution: '" + mode + "',  modes are: "
					+ Arrays.toString(ExecutionMode.values()));
			return;
		}

		if (!core.isCompilable) {
			System.err.println("error project configuration");
			return;
		}

		ConfigurationProperties.print();

		core.startEvolution();

		core.atEnd();

	}

	/**
	 * @param args
	 * @throws Exception
	 * @throws ParseException
	 */
	public static void main(String[] args) throws Exception {
		VBAPRMain m = new VBAPRMain();
		m.execute(args);
	}

	public void execute(String[] args) throws Exception {
		boolean correct = processArguments(args);

		log.info("Running Astor on a JDK at " + System.getProperty("java.home"));

		if (!correct) {
			System.err.println("Problems with commands arguments");
			return;
		}
		if (isExample(args)) {
			executeExample(args);
			return;
		}

		String dependencies = ConfigurationProperties.getProperty("dependenciespath");
		dependencies += (ConfigurationProperties.hasProperty("extendeddependencies"))
				? (File.pathSeparator + ConfigurationProperties.hasProperty("extendeddependencies"))
				: "";
		String failing = ConfigurationProperties.getProperty("failing");
		String location = ConfigurationProperties.getProperty("location");
		String packageToInstrument = ConfigurationProperties.getProperty("packageToInstrument");
		double thfl = ConfigurationProperties.getPropertyDouble("flthreshold");
		String projectName = ConfigurationProperties.getProperty("projectIdentifier");

		setupLogging();

		run(location, projectName, dependencies, packageToInstrument, thfl, failing);

	}

	public AstorCoreEngine getEngine() {
		return core;
	}

	public void setupLogging() throws IOException {
		// done with log4j2.xml
	}

}
