package at.uibk.dps.ee.io.afcl;

import at.uibk.dps.afcl.Workflow;

/**
 * Static method container with methods used to flatten AFCL's compound
 * hierarchy while eliminating the redundant data description.
 * 
 * @author Fedor Smirnov
 */
public final class HierarchyLevellingAfcl {

	/**
	 * No constructor
	 */
	private HierarchyLevellingAfcl() {
	}

	/**
	 * Returns the data id corresponding to the provided source string
	 * 
	 * @param afclSource the src string in the afcl file
	 * @param workflow the workflow built based on the afcl file 
	 * @return the data id in the flattened graph
	 */
	public static String getDataId(String afclSource, Workflow workflow) {
		throw new IllegalStateException("Not yet implemented");
	}
}
