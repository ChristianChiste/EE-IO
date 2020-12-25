package at.uibk.dps.ee.io.afcl;

/**
 * Constants from the AFCL syntax.
 * 
 * @author Fedor Smirnov
 *
 */
public final class ConstantsAfcl {

	// The string used to create the source name out of the function/compound name
	// and the data in/out name
	public static final String SourceAffix = "/";

	// String defining the data types
	public static final String typeStringNumber = "number";
	public static final String typeStringString = "string";
	public static final String typeStringCollection = "collection";
	public static final String typeStringObject = "object";
	public static final String typeStringBoolean = "bool";
	
	// String defining different function types
	public static final String functionTypeStringServerless = "serverless";
	public static final String functionTypeStringLocal = "local";

	// Strings describing certain properties
	public static final String propertyConstraintResourceLink = "resource";
	
	/**
	 * No constructor.
	 */
	private ConstantsAfcl() {
	}
}
