package at.uibk.dps.ee.io.afcl;

import java.util.ArrayList;
import java.util.List;

import at.uibk.dps.afcl.Function;
import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.afcl.functions.AtomicFunction;
import at.uibk.dps.afcl.functions.Parallel;
import at.uibk.dps.afcl.functions.Sequence;
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

	public static Function getFunction(Workflow wf, String name) {
		for (Function func : wf.getWorkflowBody()) {
			Function inside = searchInsideFunction(func, name);
			if (inside != null) {
				return inside;
			}
		}
		throw new IllegalStateException("Function " + name + " not found in WF " + wf.getName());
	}

	protected static Function searchInsideFunction(Function function, String name) {
		if (function.getName().equals(name)) {
			return function;
		}
		if (function instanceof AtomicFunction) {
			return null;
		}else if (function instanceof Sequence) {
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
		}else if (function instanceof Parallel) {
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
		}else {
			throw new IllegalStateException("Unknown compound");
		}
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

	public static List<DataIns> getDataIns(AtomicFunction atomFunc) {
		if (atomFunc.getDataIns() == null) {
			return new ArrayList<>();
		} else {
			return atomFunc.getDataIns();
		}
	}

	public static List<DataOutsAtomic> getDataOuts(AtomicFunction atomFunc) {
		if (atomFunc.getDataOuts() == null) {
			return new ArrayList<>();
		} else {
			return atomFunc.getDataOuts();
		}
	}
}
