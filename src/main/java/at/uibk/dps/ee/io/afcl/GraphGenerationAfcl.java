package at.uibk.dps.ee.io.afcl;

import java.util.List;

import at.uibk.dps.afcl.Function;
import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.afcl.functions.objects.DataIns;
import at.uibk.dps.afcl.functions.objects.DataOuts;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Task;

/**
 * Static container for the generation of the {@link EnactmentGraph} from a
 * {@link Workflow}.
 * 
 * @author Fedor Smirnov
 *
 */
public final class GraphGenerationAfcl {

	/**
	 * Static container => no constructor
	 */
	private GraphGenerationAfcl() {
	}

	/**
	 * Generates and returns the {@link EnactmentGraph} based on the provided
	 * {@link Workflow}.
	 * 
	 * @param afclWorkflow the {@link Workflow} object created from an .afcl/.cfcl
	 *                     file
	 * @return the {@link EnactmentGraph} modeling the enactment of the workflow
	 */
	public static EnactmentGraph generateEnactmentGraph(Workflow afclWorkflow) {
		EnactmentGraph result = new EnactmentGraph();
		addWfInputNodes(result, AfclApiWrapper.getDataIns(afclWorkflow), AfclApiWrapper.getName(afclWorkflow));
		addWfFunctions(result, afclWorkflow);
		annotateWfOutputs(result, AfclApiWrapper.getDataOuts(afclWorkflow));
		return result;
	}

	/**
	 * Adds the functions contained within the given workflow to the graph to the
	 * enactment graph
	 * 
	 * @param graph        the enactment graph
	 * @param afclWorkflow the given workflow
	 */
	protected static void addWfFunctions(EnactmentGraph graph, Workflow afclWorkflow) {
		for (Function function : afclWorkflow.getWorkflowBody()) {
			CompoundConstructionAfcl.addFunctionCompound(graph, function);
		}
	}

	/**
	 * Annotates the outputs of the workflow. At this point, the nodes have to be in
	 * the graph already. Otherwise, an exception is thrown.
	 * 
	 * @param graph    the enactment graph
	 * @param dataOuts the list of afcl data outs
	 */
	protected static void annotateWfOutputs(EnactmentGraph graph, List<DataOuts> dataOuts) {
		for (DataOuts dataOut : dataOuts) {
			annotateWfOutput(graph, dataOut);
		}
	}

	/**
	 * Annotates the wf output specified by the given dataOut.
	 * 
	 * @param graph   the enactment graph
	 * @param dataOut the data out
	 */
	protected static void annotateWfOutput(EnactmentGraph graph, DataOuts dataOut) {
		String source = dataOut.getSource();
		if (graph.getVertex(source) == null) {
			throw new IllegalStateException(
					"The source of the dataOut " + AfclApiWrapper.getName(dataOut) + " is not in the graph.");
		}
		Task leafNode = graph.getVertex(source);
		PropertyServiceData.makeLeaf(leafNode);
		String jsonKey = dataOut.getName();
		DataType dataType = UtilsAfcl.getDataTypeForString(dataOut.getType());
		PropertyServiceData.setDataType(leafNode, dataType);
		PropertyServiceData.setJsonKey(leafNode, jsonKey);
	}

	/**
	 * Adds the data nodes defined by the provided data ins to the provided
	 * enactment graph.
	 * 
	 * @param graph   the enactment graph
	 * @param dataIns the list of data ins of the workflow
	 * @param wfName  the name of the workflow
	 */
	protected static void addWfInputNodes(EnactmentGraph graph, List<DataIns> dataIns, String wfName) {
		for (DataIns dataIn : dataIns) {
			graph.addVertex(generateWfInputDataNode(dataIn, wfName));
		}
	}

	/**
	 * Generates the data node modeling the provided input of the WF.
	 * 
	 * @param dataIn the data in
	 * @param wfName the name of the wf
	 * @return the data node modeling the provided input of the WF
	 */
	protected static Task generateWfInputDataNode(DataIns dataIn, String wfName) {
		String dataId = dataIn.getName();
		String nodeId = UtilsAfcl.getDataNodeId(wfName, dataId);
		String jsonKey = AfclApiWrapper.getSource(dataIn);
		DataType dataType = UtilsAfcl.getDataTypeForString(dataIn.getType());
		Task result = new Communication(nodeId);
		PropertyServiceData.setDataType(result, dataType);
		PropertyServiceData.makeRoot(result);
		PropertyServiceData.setJsonKey(result, jsonKey);
		return result;
	}
}
