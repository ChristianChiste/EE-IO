package at.uibk.dps.ee.io;

import at.uibk.dps.ee.model.graph.EnactmentGraph;

/**
 * Interface for the classes used for the generation of the
 * {@link EnactmentGraph}.
 * 
 * @author Fedor Smirnov
 *
 */
public interface EnactmentGraphProvider {

	/**
	 * Returns the {@link EnactmentGraph} modeling the enactment process.
	 * 
	 * @return the {@link EnactmentGraph} modeling the enactment process
	 */
	EnactmentGraph getEnactmentGraph();
}
