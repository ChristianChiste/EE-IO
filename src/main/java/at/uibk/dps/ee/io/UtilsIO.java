package at.uibk.dps.ee.io;

/**
 * Static method container for general utility methods.
 * 
 * @author Fedor Smirnov
 */
public final class UtilsIO {

	/**
	 * No constructor.
	 */
	private UtilsIO() {
	}

	/**
	 * Returns the int read from the given string.
	 * 
	 * @param intString the given string
	 * @return the int read from the given string
	 */
	public static int readAsInt(final String intString) {
		if (!readableAsInt(intString)) {
			throw new IllegalArgumentException("The string " + intString + " cannot be read as int.");
		}
		return Integer.parseInt(intString);
	}

	/**
	 * Returns true if the given string can be parsed to an int.
	 * 
	 * @param intString the given string
	 * @return true if the given string can be parsed to an int
	 */
	public static boolean readableAsInt(final String intString) {
		try {
			Integer.parseInt(intString);
			return true;
		} catch (NumberFormatException exc) {
			return false;
		}
	}
}
