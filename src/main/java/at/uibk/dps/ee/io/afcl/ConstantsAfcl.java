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
  public static final String IfFuncSeparator = ",";

  // boolean strings
  public static final String afclTrue = "true";
  public static final String afclFalse = "false";

  // Strings defining the data types
  public static final String typeStringNumber = "number";
  public static final String typeStringString = "string";
  public static final String typeStringCollection = "collection";
  public static final String typeStringObject = "object";
  public static final String typeStringBoolean = "bool";

  // Strings defining the conditional operators
  public static final String operatorStringEqual = "==";
  public static final String operatorStringLess = "<";
  public static final String operatorStringGreater = ">";
  public static final String operatorStringLessEqual = "<=";
  public static final String operatorStringGreaterEqual = ">=";
  public static final String operatorStringUnequal = "!=";
  public static final String operatorStringContains = "contains";
  public static final String operatorStringStartsWith = "startsWith";
  public static final String operatorStringEndsWith = "endsWith";
  public static final String operatorStringAnd = "AND";
  public static final String operatorStringOr = "OR";

  // String defining the conidition summary
  public static final String summaryStringAnd = "and";
  public static final String summaryStringOr = "or";

  // String defining different function types
  public static final String functionTypeStringServerless = "serverless";
  public static final String functionTypeStringLocal = "local";

  // Strings describing certain properties
  public static final String propertyConstraintResourceLink = "resource";

  // Constraint properties
  public static final String constraintNameElementIndex = "element-index";
  public static final String constraintSeparatorEIdxOuter = ",";
  public static final String constraintSeparatorEIdxInner = ":";
  public static final String constraintNameBlock = "block";
  public static final String constraintNameSplit = "split";
  public static final String constraintSeparatorBlock = ",";
  public static final String constraintNameReplicate = "replicate";


  /**
   * No constructor.
   */
  private ConstantsAfcl() {}
}
