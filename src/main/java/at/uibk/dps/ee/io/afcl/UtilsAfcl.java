package at.uibk.dps.ee.io.afcl;

import at.uibk.dps.afcl.Function;
import at.uibk.dps.afcl.functions.AtomicFunction;
import at.uibk.dps.afcl.functions.objects.PropertyConstraint;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.FunctionType;

/**
 * Convenience methods for working with the AFCL classes and syntax.
 * 
 * @author Fedor Smirnov
 *
 */
public final class UtilsAfcl {

	/**
	 * No constructor
	 */
	private UtilsAfcl() {
	}

	/**
	 * Enum for the different compounds used in AFCL.
	 * 
	 * @author Fedor Smirnov
	 *
	 */
	public enum CompoundType {
		Atomic
	}

	/**
	 * Returns the compound type of the provided function
	 * 
	 * @param function the provided function
	 * @return the compound type of the provided function
	 */
	public static CompoundType getCompoundType(Function function) {
		if (function instanceof AtomicFunction) {
			return CompoundType.Atomic;
		} else {
			throw new IllegalArgumentException(
					"The function " + function.getName() + " is a compound of an unknown type.");
		}
	}

	/**
	 * Returns the enum used by the property service based on the string used in the
	 * afcl file.
	 * 
	 * @param afclString the string used in the afcl file
	 * @return the enum used by the property service based on the string used in the
	 *         afcl file
	 */
	public static DataType getDataTypeForString(String afclString) {
		return switch (afclString) {
		case ConstantsAfcl.typeStringBoolean: {
			yield DataType.Boolean;
		}
		case ConstantsAfcl.typeStringNumber: {
			yield DataType.Number;
		}
		case ConstantsAfcl.typeStringObject: {
			yield DataType.Object;
		}
		case ConstantsAfcl.typeStringCollection: {
			yield DataType.Collection;
		}
		case ConstantsAfcl.typeStringString: {
			yield DataType.String;
		}
		default:
			throw new IllegalArgumentException("Data type string: " + afclString);
		};
	}

	/**
	 * Returns the type of the enactable associated with the provided string.
	 * 
	 * @param afclString the string used as the type of the function in the afcl
	 *                   file
	 * @return the type of the enactable associated with the provided string
	 */
	public static FunctionType getFunctionTypeForString(String afclString) {
		return switch (afclString) {
		case ConstantsAfcl.functionTypeStringLocal: {
			yield FunctionType.Local;
		}
		case ConstantsAfcl.functionTypeStringServerless: {
			yield FunctionType.Serverless;
		}
		default:
			throw new IllegalArgumentException("Unexpected function type value: " + afclString);
		};
	}

	/**
	 * Returns true if a resource is set for the given function.
	 * 
	 * @param atomFunc the given function.
	 * @return true if a resource is set for the given function
	 */
	public static boolean isResourceSetAtomFunc(AtomicFunction atomFunc) {
		if (atomFunc.getProperties() == null) {
			return false;
		}
		for (PropertyConstraint propConstraint : atomFunc.getProperties()) {
			if (propConstraint.getName().equals(ConstantsAfcl.propertyConstraintResourceLink)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the resource link string from the given function.
	 * 
	 * @param atomFunc the afcl atomic function
	 * @return the resource link string from the given function
	 */
	public static String getResLinkAtomicFunction(AtomicFunction atomFunc) {
		if (!isResourceSetAtomFunc(atomFunc)) {
			throw new IllegalArgumentException("No resource annotated for atomic function " + atomFunc.getName());
		}
		for (PropertyConstraint propConstraint : atomFunc.getProperties()) {
			if (propConstraint.getName().equals(ConstantsAfcl.propertyConstraintResourceLink)) {
				return propConstraint.getValue();
			}
		}
		throw new IllegalArgumentException("No resource annotated for atomic function " + atomFunc.getName());
	}

	/**
	 * Returns the ID of a data object which is created by the producer with the
	 * producer ID and is named with the dataID
	 * 
	 * @param producerId the id of the producer (function or the WF)
	 * @param dataId     the name of the data
	 * @return the ID of a data object which is created by the producer with the
	 *         producer ID and is named with the dataID
	 */
	public static String getDataNodeId(String producerId, String dataId) {
		return producerId + ConstantsAfcl.SourceAffix + dataId;
	}

	/**
	 * Reads the producer ID from the given srcString
	 * 
	 * @param srcString the given srcString
	 * @return the producer ID from the given srcString
	 */
	public static String getProducerId(String srcString) {
		return getSrcSubString(srcString, true);
	}

	/**
	 * Reads the data ID from the given srcString
	 * 
	 * @param srcString the given srcString
	 * @return the data ID from the given srcString
	 */
	public static String getDataId(String srcString) {
		return getSrcSubString(srcString, false);
	}

	/**
	 * Returns a substring of the given src string correspopnding to the producer id
	 * (true) or the data id (false).
	 * 
	 * @param srcString the srcString
	 * @param producer  true if asking for the producer, false for the data id
	 * @return a substring of the given src string correspopnding to the producer id
	 *         (true) or the data id (false)
	 */
	protected static String getSrcSubString(String srcString, boolean producer) {
		return producer ? srcString.split(ConstantsAfcl.SourceAffix)[0] : srcString.split(ConstantsAfcl.SourceAffix)[1];
	}
}
