package at.uibk.dps.ee.io.afcl;

import at.uibk.dps.afcl.Function;
import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.afcl.functions.AtomicFunction;
import at.uibk.dps.afcl.functions.Parallel;
import at.uibk.dps.afcl.functions.objects.Section;
import at.uibk.dps.ee.model.graph.EnactmentGraph;

/**
 * Static method container for the methods used when creating the enactment
 * graph parts modeling elements of parallel compounds.
 * 
 * @author Fedor Smirnov
 */
public final class AfclCompoundsParallel {

	/**
	 * No constructor.
	 */
	private AfclCompoundsParallel() {
	}
	
	/**
	 * Adds the nodes modeling the content of the given parallel compound to the
	 * provided enactment graph.
	 * 
	 * @param graph    the enactment graph
	 * @param parallel the parallel compound
	 * @param workflow the afcl workflow object
	 */
	protected static void addParallel(final EnactmentGraph graph, final Parallel parallel, final Workflow workflow) {
		for (final Section section : parallel.getParallelBody()) {
			for (final Function function : section.getSection()) {
				if (function instanceof AtomicFunction) {
					AfclCompoundsAtomic.addAtomicFunctionSubWfLevel(graph, (AtomicFunction) function, workflow);
				} else {
					AfclCompounds.addFunctionCompound(graph, function, workflow);
				}
			}
		}
	}
}
