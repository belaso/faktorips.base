/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IAllValuesValueSet;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class AllValuesValueSetTest extends IpsPluginTest {

	IConfigElement ce;
	DefaultTestContent content;
	
	public void setUp() throws Exception {
		super.setUp();
		content = new DefaultTestContent();
		IProductCmptGeneration gen = (IProductCmptGeneration)content.getComfortCollisionCoverageA().getGenerations()[0];
		
		ce = gen.newConfigElement();
	}
	
	public void testCreateFromXml() throws CoreException, SAXException, IOException, ParserConfigurationException {
		Document doc = getTestDocument();
		Element root = doc.getDocumentElement();
		Element element = XmlUtil.getFirstElement(root);

		IValueSet allValues = new AllValuesValueSet(ce, 1);
		allValues.initFromXml(element);
		assertNotNull(allValues);
	}

	public void testToXml() {
	    AllValuesValueSet allValues = new AllValuesValueSet(ce, 1);
		Element element = allValues.toXml(this.newDocument());
		IAllValuesValueSet allValues2 = new AllValuesValueSet(ce, 2);
		allValues2.initFromXml(element);
		assertNotNull(allValues2);
	}
	
	public void testContainsValue() throws Exception {
		IProductCmptGeneration gen = (IProductCmptGeneration)content.getStandardTplCoverage().getGenerations()[0];
		IConfigElement config = gen.getConfigElement("sumInsured");
	    AllValuesValueSet allValues = new AllValuesValueSet(config, 1);
	    assertFalse(allValues.containsValue("abc"));
	    assertTrue(allValues.containsValue("1EUR"));

	    config.findPcTypeAttribute().setDatatype(Datatype.INTEGER.getQualifiedName());
	    assertFalse(allValues.containsValue("1EUR"));
	    assertTrue(allValues.containsValue("99"));
 	}
	
	public void testContainsValueSet() throws Exception {
		IProductCmptGeneration gen = (IProductCmptGeneration)content.getStandardTplCoverage().getGenerations()[0];
		IConfigElement config = gen.getConfigElement("sumInsured");
		AllValuesValueSet allValues = (AllValuesValueSet)config.getValueSet();
		
		assertTrue(allValues.containsValueSet(allValues));
		assertTrue(allValues.containsValueSet(new AllValuesValueSet(config, 99)));
		
		gen = (IProductCmptGeneration)content.getBasicMotorProduct().getGenerations()[0];
		config = gen.getConfigElement("inceptionDate");
		
		assertFalse(allValues.containsValueSet(config.getValueSet()));
	}
	
	public void testGetContainsNull() throws Exception {
		IProductCmptGeneration gen = (IProductCmptGeneration)content.getStandardTplCoverage().getGenerations()[0];
		IConfigElement config = gen.getConfigElement("sumInsured");
		AllValuesValueSet allValues = (AllValuesValueSet)config.getValueSet();

		// test with non-primitive datatype
		assertTrue(allValues.getContainsNull());
		
		// test with no datatype
		IAttribute attr = content.getCoverage().getAttribute("sumInsured");
		attr.setDatatype("");
		content.getCoverage().getIpsSrcFile().save(true, null);
		assertTrue(allValues.getContainsNull());
		
		// test with primitive datatype
		ValueDatatype[] vds = content.getProject().getValueDatatypes(false);
		ArrayList list = new ArrayList();
		list.addAll(Arrays.asList(vds));
		list.add(new PrimitiveIntegerDatatype());
		content.getProject().setValueDatatypes((ValueDatatype[])list.toArray(new ValueDatatype[list.size()]));
		attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
		content.getCoverage().getIpsSrcFile().save(true, null);
		assertFalse(allValues.getContainsNull());
		
	}
	
	public void testSetContainsNull() throws Exception {
		IProductCmptGeneration gen = (IProductCmptGeneration)content.getStandardTplCoverage().getGenerations()[0];
		IConfigElement config = gen.getConfigElement("sumInsured");
		AllValuesValueSet allValues = (AllValuesValueSet)config.getValueSet();

		allValues.setContainsNull(true);
		
		try {
			allValues.setContainsNull(false);
			fail();
		} catch (UnsupportedOperationException e) {
			// nothing to do
		}
		
		ValueDatatype[] vds = content.getProject().getValueDatatypes(false);
		ArrayList list = new ArrayList();
		list.addAll(Arrays.asList(vds));
		list.add(new PrimitiveIntegerDatatype());
		content.getProject().setValueDatatypes((ValueDatatype[])list.toArray(new ValueDatatype[list.size()]));
		IAttribute attr = content.getCoverage().getAttribute("sumInsured");
		attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
		content.getCoverage().getIpsSrcFile().save(true, null);
		
		allValues.setContainsNull(false);
		
		try {
			allValues.setContainsNull(true);
			fail();
		} catch (UnsupportedOperationException e) {
			// nothing to do
		}
		
	}
	
}