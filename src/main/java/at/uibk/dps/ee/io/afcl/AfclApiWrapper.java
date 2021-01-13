package at.uibk.dps.ee.io.afcl;

import java.util.ArrayList;
import java.util.List;

import at.uibk.dps.afcl.Function;
import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.afcl.functions.AtomicFunction;
import at.uibk.dps.afcl.functions.IfThenElse;
import at.uibk.dps.afcl.functions.Parallel;
import at.uibk.dps.afcl.functions.Sequence;
import at.uibk.dps.afcl.functions.objects.ACondition;
import at.uibk.dps.afcl.functions.objects.DataIns;
import at.uibk.dps.afcl.functions.objects.DataOuts;
import at.uibk.dps.afcl.functions.objects.DataOutsAtomic;
import at.uibk.dps.afcl.functions.objects.Section;

/**
 * Class offering static methods to apply additional checks when accessing data
 * from the AFCL Java API objects.
 * 
 * @author Fedor Smirnov
 *
 */
public final class AfclApiWrapper {

	private AfclApiWrapper() {
	}

	public static boolean hasConstraints(DataIns dataIn) {
		return !(dataIn.getConstraints() == null || dataIn.getConstraints().isEmpty());
	}

	/**
	 * Returns true if the given src string points to an input of the given
	 * non-atomic function. Returns false if it points to an output. Throws an
	 * exception in all other cases.
	 * 
	 * @param sourceString
	 * @param func
	 * @return
	 */
	public static boolean pointsToInput(String sourceString, Function func) {
		String funcName = UtilsAfcl.getProducerId(sourceString);
		String dataId = UtilsAfcl.getDataId(sourceString);

		if (func instanceof AtomicFunction) {
			throw new IllegalArgumentException("Intended to be used on non-atomics");
		}
		if (!funcName.equals(func.getName())) {
			throw new IllegalArgumentException("Given function is not the src");
		}

		for (DataIns dIn : getDataIns(func)) {
			if (dataId.equals(dIn.getName())) {
				return true;
			}
		}
		for (DataOuts dOut : getDataOuts(func)) {
			if (dataId.equals(dOut.getName())) {
				return false;
			}
		}
		throw new IllegalStateException(
				"Source " + sourceString + " neither data out nor data in of function " + func.getName());
	}

	public static Function getFunction(Workflow wf, String name) {
		for (Function func : wf.getWorkflowBody()) {
			Function inside = searchInsideFunction(func, name);
			if (inside != null) {
				return inside;
			}
		}
		throw new IllegalStateException("Function " + name + " not found in WF " + wf.getName());
	}

	public static String getDataInSrc(Function func, String dInName) {
		for (DataIns dIn : getDataIns(func)) {
			if (dIn.getName().equals(dInName)) {
				return dIn.getSource();
			}
		}
		throw new IllegalArgumentException(
				"Function " + func.getName() + " does not have a data in with name " + dInName);
	}

	public static String getDataOutSrc(Function func, String dOutName) {
		for (DataOuts dOut : getDataOuts(func)) {
			if (dOut.getName().equals(dOutName)) {
				return dOut.getSource();
			}
		}
		throw new IllegalArgumentException(
				"Function " + func.getName() + " does not have a data in with name " + dOutName);
	}

	protected static Function searchInsideFunction(Function function, String name) {
		if (function.getName().equals(name)) {
			return function;
		}
		if (function instanceof AtomicFunction) {
			return null;
		} else if (function instanceof Sequence) {
			Sequence seq = (Sequence) function;
			for (Function f : seq.getSequenceBody()) {
				if (f.getName().equals(name)) {
					return f;
				}
				Function inside = searchInsideFunction(f, name);
				if (inside != null) {
					return inside;
				}
			}
			return null;
		} else if (function instanceof Parallel) {
			Parallel par = (Parallel) function;
			for (Section sec : par.getParallelBody()) {
				for (Function f : sec.getSection()) {
					Function inside = searchInsideFunction(f, name);
					if (inside != null) {
						return inside;
					}
				}
			}
			return null;
		} else {
			throw new IllegalStateException("Unknown compound");
		}
	}

	public static String getName(IfThenElse ifCompound) {
		if (ifCompound.getName() == null) {
			throw new IllegalArgumentException("Name not set for if compound");
		}
		return ifCompound.getName();
	}

	public static String getName(DataIns dataIn) {
		if (dataIn.getName() == null) {
			throw new IllegalArgumentException("Name not set for data in");
		}
		return dataIn.getName();
	}

	public static String getSource(DataIns dataIn) {
		if (dataIn.getSource() == null) {
			throw new IllegalArgumentException("Source not set for data in " + getName(dataIn));
		}
		return dataIn.getSource();
	}

	public static String getSource(DataOuts dataOut) {
		if (dataOut.getSource() == null) {
			throw new IllegalArgumentException("Source not set for data in " + getName(dataOut));
		}
		return dataOut.getSource();
	}

	public static String getName(DataOuts dataOut) {
		if (dataOut.getName() == null) {
			throw new IllegalArgumentException("Name not set for data out");
		}
		return dataOut.getName();
	}

	public static String getName(DataOutsAtomic dataOut) {
		if (dataOut.getName() == null) {
			throw new IllegalArgumentException("Name not set for data out");
		}
		return dataOut.getName();
	}

	public static List<DataIns> getDataIns(Workflow wf) {
		if (wf.getDataIns() == null) {
			return new ArrayList<>();
		} else {
			return wf.getDataIns();
		}
	}

	public static String getName(Workflow wf) {
		if (wf.getName() == null) {
			throw new IllegalArgumentException("No name set for workflow");
		}
		return wf.getName();
	}

	public static List<DataOuts> getDataOuts(Workflow wf) {
		if (wf.getDataOuts() == null) {
			return new ArrayList<>();
		} else {
			return wf.getDataOuts();
		}
	}

	public static boolean getNegation(ACondition condition) {
		if (condition.getNegation() == null) {
			return false;
		}
		String negString = condition.getNegation();
		if (negString.equals(ConstantsAfcl.afclTrue)) {
			return true;
		} else if (negString.equals(ConstantsAfcl.afclFalse)) {
			return false;
		} else {
			throw new IllegalArgumentException("Unknown negation string " + negString);
		}
	}

	public static List<DataIns> getDataIns(Function func) {
		if (func instanceof AtomicFunction) {
			return getDataIns((AtomicFunction) func);
		} else if (func instanceof Parallel) {
			return getDataIns((Parallel) func);
		} else if (func instanceof Sequence) {
			return getDataIns((Sequence) func);
		} else if (func instanceof IfThenElse) {
			return getDataIns((IfThenElse) func);
		} else {
			throw new IllegalStateException("Not yet implemented.");
		}
	}

	public static List<DataOuts> getDataOuts(Function func) {
		if (func instanceof Parallel) {
			return getDataOuts((Parallel) func);
		} else if (func instanceof Sequence) {
			return getDataOuts((Sequence) func);
		} else if (func instanceof IfThenElse) {
			return getDataOuts((IfThenElse) func);
		} else {
			throw new IllegalStateException("Not yet implemented.");
		}
	}

	public static List<DataIns> getDataIns(IfThenElse ifCompound) {
		if (ifCompound.getDataIns() == null) {
			return new ArrayList<>();
		}
		return ifCompound.getDataIns();
	}

	public static List<DataIns> getDataIns(AtomicFunction atomFunc) {
		if (atomFunc.getDataIns() == null) {
			return new ArrayList<>();
		} else {
			return atomFunc.getDataIns();
		}
	}

	public static List<DataIns> getDataIns(Parallel parallel) {
		if (parallel.getDataIns() == null) {
			return new ArrayList<>();
		} else {
			return parallel.getDataIns();
		}
	}

	public static List<DataIns> getDataIns(Sequence seq) {
		if (seq.getDataIns() == null) {
			return new ArrayList<>();
		} else {
			return seq.getDataIns();
		}
	}

	public static List<DataOutsAtomic> getDataOuts(AtomicFunction atomFunc) {
		if (atomFunc.getDataOuts() == null) {
			return new ArrayList<>();
		} else {
			return atomFunc.getDataOuts();
		}
	}

	public static List<DataOuts> getDataOuts(Parallel par) {
		if (par.getDataOuts() == null) {
			return new ArrayList<>();
		} else {
			return par.getDataOuts();
		}
	}

	public static List<DataOuts> getDataOuts(Sequence seq) {
		if (seq.getDataOuts() == null) {
			return new ArrayList<>();
		} else {
			return seq.getDataOuts();
		}
	}

	public static List<DataOuts> getDataOuts(IfThenElse ifCompound) {
		if (ifCompound.getDataOuts() == null) {
			return new ArrayList<>();
		} else {
			return ifCompound.getDataOuts();
		}
	}
}
