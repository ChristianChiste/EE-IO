package at.uibk.dps.ee.io.persistance;

import at.uibk.dps.ee.model.graph.EnactmentGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.io.SpecificationReader;
import net.sf.opendse.io.SpecificationWriter;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;

/**
 * The {@link EnactmentGraphIO} is a static method container for read/write
 * methods.
 * 
 * @author Fedor Smirnov
 *
 */
public final class EnactmentGraphIO {

	/**
	 * No constructor.
	 */
	private EnactmentGraphIO() {
	}

	/**
	 * Writes the given enactment graph to the location indicated by the file path.
	 * 
	 * @param graph    the enactment graph to store
	 * @param filePath the filepath indicating the storage location.
	 */
	public static void writeEnactmentGraph(final EnactmentGraph graph, final String filePath) {
		final Specification spec = new Specification(graph, new Architecture<>(), new Mappings<>());
		final SpecificationWriter writer = new SpecificationWriter();
		writer.write(spec, filePath);
	}

	/**
	 * Reads and returns the enactment graph stored at the indicated position.
	 * 
	 * @param filePath the filepath where the graph is stored
	 * @return the enactment graph stored at the indicated position
	 */
	public static EnactmentGraph readEnactmentGraph(final String filePath) {
		final SpecificationReader reader = new SpecificationReader();
		final Specification spec = reader.read(filePath);
		return application2EnactmentGraph(spec.getApplication());
	}

	/**
	 * Creates an enactment graph corresponding to the given application.
	 * 
	 * @param application the given application
	 * @return an enactment graph corresponding to the given application
	 */
	protected static EnactmentGraph application2EnactmentGraph(final Application<Task, Dependency> application) {
		final EnactmentGraph result = new EnactmentGraph();
		// add the nodes
		for (final Task task : application) {
			result.addVertex(task);
		}
		// add the edges
		for (final Dependency dep : application.getEdges()) {
			final Task src = application.getEndpoints(dep).getFirst();
			final Task dst = application.getEndpoints(dep).getSecond();
			result.addEdge(dep, src, dst, EdgeType.DIRECTED);
		}
		return result;
	}
}
