package fr.inria.astor.core.entities;

/**
 * Representation of patches (original and formatted)
 * 
 * @author Matias Martinez
 *
 */
public class PatchDiff {

	String originalStatementAlignmentDiff = null;
	String formattedDiff = null;

	public String getOriginalStatementAlignmentDiff() {
		return originalStatementAlignmentDiff == null ? "" : originalStatementAlignmentDiff;
	}

	public void setOriginalStatementAlignmentDiff(String originalStatementAlignmentDiff) {
		this.originalStatementAlignmentDiff = originalStatementAlignmentDiff;
	}

	public String getFormattedDiff() {
		return formattedDiff == null ? "" : formattedDiff;
	}

	public void setFormattedDiff(String formatedDiff) {
		this.formattedDiff = formatedDiff;
	}
}
