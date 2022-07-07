package fr.inria.astor.core.validation.junit;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.InitializationError;

import java.util.HashSet;
import java.util.Set;

/**
 * This class runs a JUnit test suite i.e., a set of test cases.
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class JUnitNologExternalExecutor extends JUnitExternalExecutor {

	@Override
	public String createOutput(Result r) {
//		String out = "[";
//		int nr_failures = 0;
		Set<String> failures = new HashSet<>();
		try {
			for (Failure f : r.getFailures()) {
				String s = failureMessage(f);
				if (!s.startsWith("warning")) {//warning
//					failures++;
					failures.add(f.getTestHeader());
				}
			}
		} catch (Exception e) {
			// We do not care about this exception,
		}
//		out = out + "]";
		return (OUTSEP + r.getRunCount() + OUTSEP + failures.size() + OUTSEP + "" + OUTSEP);
	}

	public static void main(String[] arg) throws Exception, InitializationError {

		JUnitNologExternalExecutor re = new JUnitNologExternalExecutor();

		Result result = re.run(arg);
		// This sysout is necessary for the communication between process...
		System.out.println(re.createOutput(result));

		System.exit(0);
	}

}
