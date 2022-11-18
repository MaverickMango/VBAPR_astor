package fr.inria.astor.core.validation.extensions;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.validation.VariantValidationResult;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.validation.ProgramVariantValidator;
import org.apache.log4j.Logger;

import java.util.List;

public class Defects4jValidator extends ProgramVariantValidator {
    protected Logger log = Logger.getLogger(Thread.currentThread().getName());
    @Override
    public VariantValidationResult validate(ProgramVariant variant, ProjectRepairFacade projectFacade) {
        return null;
    }

    @Override
    public List<String> findTestCasesToExecute(ProjectRepairFacade projectFacade) {
        return null;
    }
}
