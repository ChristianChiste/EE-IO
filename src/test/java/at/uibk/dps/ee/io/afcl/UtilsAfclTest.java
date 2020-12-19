package at.uibk.dps.ee.io.afcl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import at.uibk.dps.afcl.functions.AtomicFunction;
import at.uibk.dps.afcl.functions.objects.PropertyConstraint;

public class UtilsAfclTest {

	@Test
	public void testSrcStringOperations() {
		String producerString = "source";
		String dataIdString = "data";
		String srcString = producerString + ConstantsAfcl.SourceAffix + dataIdString;

		assertEquals(producerString, UtilsAfcl.getProducerId(srcString));
		assertEquals(dataIdString, UtilsAfcl.getDataId(srcString));
	}

	@Test
	public void testIsSetResLinkAtomFunc() {
		AtomicFunction atom = new AtomicFunction();
		assertFalse(UtilsAfcl.isResourceSetAtomFunc(atom));
		String resName = "res";
		List<PropertyConstraint> propList = new ArrayList<>();
		PropertyConstraint propConst = new PropertyConstraint();
		propConst.setName(ConstantsAfcl.propertyConstraintResourceLink);
		propConst.setValue(resName);
		propList.add(propConst);
		atom.setProperties(propList);
		assertTrue(UtilsAfcl.isResourceSetAtomFunc(atom));
	}

	@Test
	public void testGetResLink() {
		AtomicFunction atom = new AtomicFunction();
		String resName = "res";
		List<PropertyConstraint> propList = new ArrayList<>();
		PropertyConstraint propConst = new PropertyConstraint();
		propConst.setName(ConstantsAfcl.propertyConstraintResourceLink);
		propConst.setValue(resName);
		propList.add(propConst);
		atom.setProperties(propList);
		assertEquals(resName, UtilsAfcl.getResLinkAtomicFunction(atom));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetResourceNotSet1() {
		AtomicFunction atom = new AtomicFunction();
		UtilsAfcl.getResLinkAtomicFunction(atom);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetResourceNotSet2() {
		AtomicFunction atom = new AtomicFunction();
		List<PropertyConstraint> propList = new ArrayList<>();
		atom.setProperties(propList);
		UtilsAfcl.getResLinkAtomicFunction(atom);
	}
}
