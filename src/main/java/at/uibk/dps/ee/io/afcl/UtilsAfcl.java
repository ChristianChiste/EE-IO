package at.uibk.dps.ee.io.afcl;

import at.uibk.dps.afcl.Function;
import at.uibk.dps.afcl.functions.AtomicFunction;
import at.uibk.dps.afcl.functions.IfThenElse;
import at.uibk.dps.afcl.functions.Parallel;
import at.uibk.dps.afcl.functions.Sequence;
import at.uibk.dps.afcl.functions.objects.PropertyConstraint;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.objects.Condition.Operator;
import at.uibk.dps.ee.model.objects.SubCollection;
import at.uibk.dps.ee.model.objects.SubCollectionElement;
import at.uibk.dps.ee.model.objects.SubCollectionStartEndStride;
import at.uibk.dps.ee.model.objects.SubCollections;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.FunctionType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtilityCondition.Summary;

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
		Atomic, Sequence, Parallel, If
	}

	/**
	 * Returns the subcollections object for the given subcollections string.
	 * 
	 * @param subcollectionsString the given subcollections string
	 * @return the subcollections object for the given subcollections string
	 */
	public static SubCollections getSubcollectionsForString(String subcollectionsString) {
		SubCollections result = new SubCollections();
		// remove the white spaces
		subcollectionsString = subcollectionsString.replaceAll("\\s+", "");
		if (subcollectionsString.contains(ConstantsEEModel.ElementIndexValueSeparatorExternal)) {
			// multiple comma-separated values
			for (String subString : subcollectionsString.split(ConstantsEEModel.ElementIndexValueSeparatorExternal)) {
				result.add(getSubCollectionForString(subString));
			}
		} else {
			// only one number
			result.add(getSubCollectionForString(subcollectionsString));
		}
		return result;
	}

	/**
	 * Returns the subcollection for the given string.
	 * 
	 * @param subcollectionString the given string
	 * @return the subcollection for the given string
	 */
	protected static SubCollection getSubCollectionForString(String subcollectionString) {
		if (subcollectionString.contains(ConstantsEEModel.ElementIndexValueSeparatorInternal)) {
			// start end stride
			int numberSeparators = subcollectionString.split(ConstantsEEModel.ElementIndexValueSeparatorInternal).length
					- 1;
			if (numberSeparators == 1) {
				String startString = subcollectionString.split(ConstantsEEModel.ElementIndexValueSeparatorInternal)[0];
				String endString = subcollectionString.split(ConstantsEEModel.ElementIndexValueSeparatorInternal)[1];
				int start = startString.length() == 0 ? -1 : readElemendIdxInt(startString);
				int end = endString.length() == 0 ? -1 : readElemendIdxInt(endString);
				int stride = -1;
				return new SubCollectionStartEndStride(start, end, stride);
			}else if (numberSeparators == 2) {
				String startString = subcollectionString.split(ConstantsEEModel.ElementIndexValueSeparatorInternal)[0];
				String endString = subcollectionString.split(ConstantsEEModel.ElementIndexValueSeparatorInternal)[1];
				String strideString = subcollectionString.split(ConstantsEEModel.ElementIndexValueSeparatorInternal)[2];
				int start = startString.length() == 0 ? -1 : readElemendIdxInt(startString);
				int end = endString.length() == 0 ? -1 : readElemendIdxInt(endString);
				int stride = strideString.length() == 0 ? -1 : readElemendIdxInt(strideString);
				return new SubCollectionStartEndStride(start, end, stride);
			}else {
				throw new IllegalArgumentException("Too many internal element index separators.");
			}
		} else {
			// element
			int idx = readElemendIdxInt(subcollectionString);
			return new SubCollectionElement(idx);
		}
	}

	/**
	 * Interprets the given string as int.
	 * 
	 * @param intString the given string
	 * @return the given string as int
	 */
	protected static int readElemendIdxInt(String intString) {
		try {
			return Integer.parseInt(intString);
		} catch (NumberFormatException exc) {
			throw new IllegalArgumentException("Incorrect num string for element idx: " + intString);
		}
	}

	/**
	 * Returns true if the given element idx value maps to a value.
	 * 
	 * @param elementIdxValue the given element idx value
	 * @return true if the given element idx value maps to a value
	 */
	public static boolean doesElementIdxValueMapToOneValue(String elementIdxValue) {
		if (elementIdxValue.contains(ConstantsEEModel.ElementIndexValueSeparatorExternal)
				|| elementIdxValue.contains(ConstantsEEModel.ElementIndexValueSeparatorInternal)) {
			return false;
		}
		try {
			Integer.parseInt(elementIdxValue);
			return true;
		} catch (NumberFormatException exc) {
			return false;
		}
	}

	/**
	 * Returns the compound type of the provided function
	 * 
	 * @param function the provided function
	 * @return the compound type of the provided function
	 */
	public static CompoundType getCompoundType(final Function function) {
		if (function instanceof AtomicFunction) {
			return CompoundType.Atomic;
		} else if (function instanceof Sequence) {
			return CompoundType.Sequence;
		} else if (function instanceof Parallel) {
			return CompoundType.Parallel;
		} else if (function instanceof IfThenElse) {
			return CompoundType.If;
		} else {
			throw new IllegalArgumentException(
					"The function " + function.getName() + " is a compound of an unknown type.");
		}
	}

	/**
	 * Returns the operator object for the given afcl string.
	 * 
	 * @param afclOperatorString the given afcl string
	 * @return the {@link Operator} object
	 */
	public static Operator getOperatorForString(final String afclOperatorString) {
		switch (afclOperatorString) {
		case ConstantsAfcl.operatorStringContains: {
			return Operator.CONTAINS;
		}
		case ConstantsAfcl.operatorStringEndsWith: {
			return Operator.ENDS_WITH;
		}
		case ConstantsAfcl.operatorStringStartsWith: {
			return Operator.STARTS_WITH;
		}
		case ConstantsAfcl.operatorStringEqual: {
			return Operator.EQUAL;
		}
		case ConstantsAfcl.operatorStringUnequal: {
			return Operator.UNEQUAL;
		}
		case ConstantsAfcl.operatorStringLess: {
			return Operator.LESS;
		}
		case ConstantsAfcl.operatorStringLessEqual: {
			return Operator.LESS_EQUAL;
		}
		case ConstantsAfcl.operatorStringGreater: {
			return Operator.GREATER;
		}
		case ConstantsAfcl.operatorStringGreaterEqual: {
			return Operator.GREATER_EQUAL;
		}
		case ConstantsAfcl.operatorStringAnd: {
			return Operator.AND;
		}
		case ConstantsAfcl.operatorStringOr: {
			return Operator.OR;
		}
		default:
			throw new IllegalArgumentException("Unknown operator string: " + afclOperatorString);
		}
	}

	/**
	 * Returns the summary object for the given afcl string.
	 * 
	 * @param summaryString the afcl string
	 * @return the summary object for the given afcl string
	 */
	public static Summary getSummaryForString(final String summaryString) {
		switch (summaryString) {
		case ConstantsAfcl.summaryStringAnd:
			return Summary.AND;

		case ConstantsAfcl.summaryStringOr:
			return Summary.OR;

		default:
			throw new IllegalArgumentException("Unknown summary string: " + summaryString);
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
	public static DataType getDataTypeForString(final String afclString) {
		switch (afclString) {
		case ConstantsAfcl.typeStringBoolean: {
			return DataType.Boolean;
		}
		case ConstantsAfcl.typeStringNumber: {
			return DataType.Number;
		}
		case ConstantsAfcl.typeStringObject: {
			return DataType.Object;
		}
		case ConstantsAfcl.typeStringCollection: {
			return DataType.Collection;
		}
		case ConstantsAfcl.typeStringString: {
			return DataType.String;
		}
		default:
			throw new IllegalArgumentException("Unknown data type string: " + afclString);
		}
	}

	/**
	 * Returns the type of the enactable associated with the provided string.
	 * 
	 * @param afclString the string used as the type of the function in the afcl
	 *                   file
	 * @return the type of the enactable associated with the provided string
	 */
	public static FunctionType getFunctionTypeForString(final String afclString) {
		switch (afclString) {
		case ConstantsAfcl.functionTypeStringLocal: {
			return FunctionType.Local;
		}
		case ConstantsAfcl.functionTypeStringServerless: {
			return FunctionType.Serverless;
		}
		default:
			throw new IllegalArgumentException("Unexpected function type value: " + afclString);
		}
	}

	/**
	 * Returns true if a resource is set for the given function.
	 * 
	 * @param atomFunc the given function.
	 * @return true if a resource is set for the given function
	 */
	public static boolean isResourceSetAtomFunc(final AtomicFunction atomFunc) {
		if (atomFunc.getProperties() == null) {
			return false;
		}
		for (final PropertyConstraint propConstraint : atomFunc.getProperties()) {
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
	public static String getResLinkAtomicFunction(final AtomicFunction atomFunc) {
		if (!isResourceSetAtomFunc(atomFunc)) {
			throw new IllegalArgumentException("No resource annotated for atomic function " + atomFunc.getName());
		}
		for (final PropertyConstraint propConstraint : atomFunc.getProperties()) {
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
	public static String getDataNodeId(final String producerId, final String dataId) {
		return producerId + ConstantsAfcl.SourceAffix + dataId;
	}

	/**
	 * Returns true iff the provided string describes data produced by a producer.
	 * 
	 * @param srcString the string to check
	 * @return true iff the provided string describes data produced by a producer
	 */
	public static boolean isSrcString(final String srcString) {
		final String affix = ConstantsAfcl.SourceAffix;
		return srcString.contains(affix) && !srcString.startsWith(affix) && !srcString.endsWith(affix);
	}

	/**
	 * Reads the producer ID from the given srcString
	 * 
	 * @param srcString the given srcString
	 * @return the producer ID from the given srcString
	 */
	public static String getProducerId(final String srcString) {
		return getSrcSubString(srcString, true);
	}

	/**
	 * Reads the data ID from the given srcString
	 * 
	 * @param srcString the given srcString
	 * @return the data ID from the given srcString
	 */
	public static String getDataId(final String srcString) {
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
	protected static String getSrcSubString(final String srcString, final boolean producer) {
		return producer ? srcString.split(ConstantsAfcl.SourceAffix)[0] : srcString.split(ConstantsAfcl.SourceAffix)[1];
	}

	/**
	 * Returns true iff the given string describes the src of a data out of an if
	 * compound.
	 * 
	 * @param srcString the src string
	 * @return true iff the given string describes the src of a data out of an if
	 *         compound
	 */
	public static boolean isIfOutSrc(final String srcString) {
		return srcString.contains(ConstantsAfcl.IfFuncSeparator);
	}

	/**
	 * Returns the substring of the first function read from the given if src
	 * string.
	 * 
	 * @param srcString the if src string
	 * @return the substring of the first function read from the given if src string
	 */
	public static String getFirstSubStringIfOut(final String srcString) {
		return getIfOutSubString(srcString, true);
	}

	/**
	 * Returns the substring of the second function read from the given if src
	 * string.
	 * 
	 * @param srcString the if src string
	 * @return the substring of the second function read from the given if src
	 *         string
	 */
	public static String getSecondSubStringIfOut(final String srcString) {
		return getIfOutSubString(srcString, false);
	}

	/**
	 * Returns either the first of the second substring of the given src string of
	 * an if out.
	 * 
	 * @param srcString the given string
	 * @param first     true iff asking for the first string
	 * @return either the first of the second substring of the given src string of
	 *         an if out
	 */
	protected static String getIfOutSubString(final String srcString, final boolean first) {
		return first ? srcString.split(ConstantsAfcl.IfFuncSeparator)[0]
				: srcString.split(ConstantsAfcl.IfFuncSeparator)[1];
	}
}
