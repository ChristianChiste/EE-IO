package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.*;

import org.junit.Test;

import at.uibk.dps.afcl.Workflow;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

public class SeqParAfclTest {

	@Test
	public void test() {
		Workflow seqParWf = Graphs.getSeqParWf();
		EnactmentGraph result = GraphGenerationAfcl.generateEnactmentGraph(seqParWf);
		
		int numTasks = 0;
		int numComms = 0;
		for (Task t : result) {
			if (TaskPropertyService.isCommunication(t)) {
				numComms++;
			}else {
				numTasks++;
			}
		}
		assertEquals(7, numTasks);
		assertEquals(9, numComms);
	}
}
