package at.uibk.dps.ee.io.afcl;

import at.uibk.dps.afcl.Function;
import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.afcl.functions.AtomicFunction;
import at.uibk.dps.afcl.functions.Sequence;
import at.uibk.dps.ee.model.graph.EnactmentGraph;

/**
 * Static method container for the methods used when creating the enactment
 * graph parts modeling elements of sequence compounds.
 * 
 * @author Fedor Smirnov
 */
public final class AfclCompoundsSequence {

	/**
	 * No constructor.
	 */
	private AfclCompoundsSequence() {
	}

	/**
	 * Adds the nodes modeling the content of the given sequence compound to the
	 * provided enactment graph.
	 * 
	 * @param graph    the enactment graph
	 * @param sequence the provided sequence compound
	 * @param workflow the afcl workflow object
	 */
	protected static void addSequence(final EnactmentGraph graph, final Sequence sequence, final Workflow workflow) {
		for (final Function function : sequence.getSequenceBody()) {
			if (function instanceof AtomicFunction) {
				AfclCompoundsAtomic.addAtomicFunctionSubWfLevel(graph, (AtomicFunction) function, workflow);
			} else {
				AfclCompounds.addFunctionCompound(graph, function, workflow);
			}
		}
	}
}
