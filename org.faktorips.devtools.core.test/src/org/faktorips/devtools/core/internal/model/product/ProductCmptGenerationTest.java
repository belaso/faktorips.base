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

package org.faktorips.devtools.core.internal.model.product;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptGenerationTest extends AbstractIpsPluginTest {

    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IIpsPackageFragmentRoot root;
    private IIpsProject ipsProject;
    
    public void setUp() throws Exception {
        super.setUp();
        ipsProject =  newIpsProject("TestProject");
        
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        productCmpt = (IProductCmpt)newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT, "testProduct");
        generation = (IProductCmptGeneration)productCmpt.newGeneration();
    }
    
    public void testGetChildren() throws CoreException  {
        IConfigElement cf0 = generation.newConfigElement();
        IProductCmptRelation r0 = generation.newRelation("targetRole");
        IIpsElement[] children = generation.getChildren();
        assertEquals(2, children.length);
        assertSame(cf0, children[0]);
        assertSame(r0, children[1]);
    }
    
    public void testGetConfigElements() {
        assertEquals(0, generation.getNumOfConfigElements());
        
        IConfigElement ce1 = generation.newConfigElement();
        assertEquals(ce1, generation.getConfigElements()[0]);

        IConfigElement ce2 = generation.newConfigElement();
        assertEquals(ce1, generation.getConfigElements()[0]);
        assertEquals(ce2, generation.getConfigElements()[1]);
    }

    public void testGetConfigElements_Type() {
        IConfigElement ce1 = generation.newConfigElement();
        IConfigElement ce2 = generation.newConfigElement();
        ce2.setType(ConfigElementType.FORMULA);
        IConfigElement ce3 = generation.newConfigElement();
        
        IConfigElement[] elements = generation.getConfigElements(ConfigElementType.PRODUCT_ATTRIBUTE);
        assertEquals(2, elements.length);
        assertEquals(ce1, elements[0]);
        assertEquals(ce3, elements[1]);
        
        elements = generation.getConfigElements(ConfigElementType.POLICY_ATTRIBUTE);
        assertEquals(0, elements.length);
    }
    
    public void testGetConfigElement_AttributeName() {
        generation.newConfigElement();
        IConfigElement ce2 = generation.newConfigElement();
        ce2.setPcTypeAttribute("a2");
        
        assertEquals(ce2, generation.getConfigElement("a2"));
        assertNull(generation.getConfigElement("unkown"));
        
    }

    public void testGetNumOfConfigElements() {
        assertEquals(0, generation.getNumOfConfigElements());
        
        generation.newConfigElement();
        assertEquals(1, generation.getNumOfConfigElements());

        generation.newConfigElement();
        assertEquals(2, generation.getNumOfConfigElements());
    }

    public void testNewConfigElement() {
        IConfigElement ce = generation.newConfigElement();
        assertEquals(generation, ce.getParent());
        assertEquals(1, generation.getNumOfConfigElements());
    }

    /*
     * Class under test for ProductCmptRelation[] getRelations()
     */
    public void testGetRelations() {
        IProductCmptRelation r1 = generation.newRelation("coverage");
        assertEquals(r1, generation.getRelations()[0]);

        IProductCmptRelation r2 = generation.newRelation("risk");
        assertEquals(r1, generation.getRelations()[0]);
        assertEquals(r2, generation.getRelations()[1]);
    }

    /*
     * Class under test for ProductCmptRelation[] getRelations(String)
     */
    public void testGetRelations_String() {
        IProductCmptRelation r1 = generation.newRelation("coverage");
        generation.newRelation("risk");
        IProductCmptRelation r3 = generation.newRelation("coverage");
        
        IProductCmptRelation[] relations = generation.getRelations("coverage");
        assertEquals(2, relations.length);
        assertEquals(r1, relations[0]);
        assertEquals(r3, relations[1]);

        relations = generation.getRelations("unknown");
        assertEquals(0, relations.length);
    }

    public void testGetNumOfRelations() {
        assertEquals(0, generation.getNumOfRelations());
        
        generation.newRelation("coverage");
        assertEquals(1, generation.getNumOfRelations());

        generation.newRelation("risk");
        assertEquals(2, generation.getNumOfRelations());
    }

    public void testNewRelation() {
        IProductCmptRelation relation = generation.newRelation("coverage");
        assertEquals(generation, relation.getParent());
        assertEquals(1, generation.getNumOfRelations());
        assertEquals(relation, generation.getRelations()[0]);
    }

    /*
     * Class under test for void toXml(Element)
     */
    public void testToXmlElement() {
        generation.setValidFrom(new GregorianCalendar(2005, 0, 1));
        generation.newConfigElement();
        generation.newConfigElement();
        generation.newRelation("coverage");
        generation.newRelation("coverage");
        generation.newRelation("coverage");
        Element element = generation.toXml(newDocument());
        
        IProductCmptGeneration copy = new ProductCmptGeneration();
        copy.initFromXml(element);
        assertEquals(2, copy.getNumOfConfigElements());
        assertEquals(3, copy.getNumOfRelations());
    }

    public void testInitFromXml() {
        generation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(new GregorianCalendar(2005, 0, 1), generation.getValidFrom());
        
        IConfigElement[] configElements = generation.getConfigElements();
        assertEquals(1, configElements.length);
        
        IProductCmptRelation[] relations = generation.getRelations();
        assertEquals(1, relations.length);
    }

    
    public void testValidate() throws Exception{
        
        IPolicyCmptType a = newPolicyCmptType(root, "A");
        IPolicyCmptType b = newPolicyCmptType(root, "B");
        IAttribute bAttribute = b.newAttribute();
        bAttribute.setAttributeType(AttributeType.CHANGEABLE);
        bAttribute.setName("bAttribute");
        bAttribute.setDatatype("String");
        IAttribute anAttribute = a.newAttribute();
        anAttribute.setName("anAttribute");
        anAttribute.setAttributeType(AttributeType.COMPUTED);
        anAttribute.setDatatype("String");
        Parameter p = new Parameter(0, "b", b.getQualifiedName());
        anAttribute.setFormulaParameters(new Parameter[]{p});
        
        IProductCmpt aProduct = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "aProduct");
        aProduct.setPolicyCmptType(a.getQualifiedName());
        IProductCmptGeneration aProductGen = (IProductCmptGeneration)aProduct.newGeneration();
        IConfigElement configElement = aProductGen.newConfigElement();
        configElement.setPcTypeAttribute("anAttribute");
        configElement.setType(ConfigElementType.FORMULA);
        configElement.setValue("b.bAttribute");
        MessageList msgList = aProductGen.validate();
        assertTrue(msgList.isEmpty());
        
        //change the name of bAttribute. A validation message from the formula validation is expected
        bAttribute.setName("cAttribute");
        msgList = aProductGen.validate();
        assertNotNull(msgList.getMessageByCode(ExprCompiler.UNDEFINED_IDENTIFIER));
    }
    
    public void testValidateDuplicateRelationTarget() throws Exception {
    	DefaultTestContent content = new DefaultTestContent();
    	
        IProductCmpt product = content.getComfortMotorProduct();
        IPolicyCmptType type = product.findPolicyCmptType();
        IRelation relationType = type.getRelation("Vehicle");
        IProductCmptGeneration generation = (IProductCmptGeneration)product.getGenerations()[0];

        MessageList ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_DUPLICATE_RELATION_TARGET));
        
        IProductCmptRelation rel = generation.newRelation(relationType.getTargetRoleSingularProductSide());
        rel.setTarget(content.getStandardVehicle().getQualifiedName());
        
        product.getIpsSrcFile().save(true, null);
        
        ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_DUPLICATE_RELATION_TARGET));
    
        
    }
    
    public void testValidateNotEnougthRelations() throws Exception {
        // test too less relations
        DefaultTestContent content = new DefaultTestContent();
        
        IProductCmpt product = content.getComfortMotorProduct();
        IPolicyCmptType type = product.findPolicyCmptType();
        
        IProductCmptGeneration generation = (IProductCmptGeneration)product.getGenerations()[0];
        IRelation relation = type.getRelation("Vehicle");
        
        assertEquals(0, relation.getMinCardinalityProductSide());
        assertEquals(1, relation.getMaxCardinalityProductSide());
        
        MessageList ml = generation.validate();
        
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NOT_ENOUGH_RELATIONS));
        
        relation.setMaxCardinalityProductSide(2);
        relation.setMinCardinalityProductSide(2);
        
        type.getIpsSrcFile().save(true, null);
        
        ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NOT_ENOUGH_RELATIONS));

        
        generation.newRelation(relation.getTargetRoleSingularProductSide());
        ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NOT_ENOUGH_RELATIONS));
        
    }
    
    public void testValidateTooManyRelations() throws Exception {
        // test too many relations
        DefaultTestContent content = new DefaultTestContent();
        
        IProductCmpt product = content.getComfortMotorProduct();
        IPolicyCmptType type = product.findPolicyCmptType();
        
        IProductCmptGeneration generation = (IProductCmptGeneration)product.getGenerations()[0];
        IRelation relation = type.getRelation("Vehicle");
        
        IProductCmptRelation newRel = generation.newRelation(relation.getTargetRoleSingularProductSide());

        assertEquals(0, relation.getMinCardinalityProductSide());
        assertEquals(1, relation.getMaxCardinalityProductSide());
        type.getIpsSrcFile().save(true, null);


        MessageList ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_TOO_MANY_RELATIONS));
        
        newRel.delete();
        product.getIpsSrcFile().save(true, null);
        
        ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_TOO_MANY_RELATIONS));
    }
    
    public void testValidateNoTemplate() throws Exception {
        DefaultTestContent content = new DefaultTestContent();        
        IProductCmpt product = content.getComfortMotorProduct();
        IProductCmptGeneration generation = (IProductCmptGeneration)product.getGenerations()[0];
    	
        generation.getProductCmpt().setPolicyCmptType("");
        MessageList ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NO_TEMPLATE));
    }
    

    public void testNewPart() {
    	try {
    		assertTrue(productCmpt.newPart(IConfigElement.class) instanceof IConfigElement);
    		assertTrue(productCmpt.newPart(IRelation.class) instanceof IRelation);
    		
    		productCmpt.newPart(Object.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    public void testCanCreateValidRelation() throws Exception {
        assertFalse(generation.canCreateValidRelation(productCmpt, null));
        
        IPolicyCmptType targetType = newPolicyCmptType(ipsProject, "target.TargetPolicy");
        IProductCmpt target = newProductCmpt(ipsProject, "target.Target");
        target.setPolicyCmptType(targetType.getQualifiedName());
        
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "type");
        
        IRelation rel = policyCmptType.newRelation();
        rel.setTarget(targetType.getQualifiedName());
        rel.setTargetRoleSingular("testRelation");
        rel.setTargetRoleSingularProductSide("testRelation");
        
        IProductCmptTypeRelation relation = policyCmptType.findProductCmptType().getRelation("testRelation");
        
        assertTrue(generation.canCreateValidRelation(target, relation));
    }
    
    public void testValidateValidFrom() throws Exception {
        IPolicyCmptType type = newPolicyCmptType(ipsProject, "type");
        generation.getProductCmpt().setPolicyCmptType(type.getQualifiedName());
        generation.getProductCmpt().setValidTo(new GregorianCalendar(2000, 10, 1));
        generation.setValidFrom(new GregorianCalendar(2000, 10, 2));
        
        MessageList ml = generation.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_INVALID_VALID_FROM));
        
        generation.setValidFrom(new GregorianCalendar(2000, 9, 1));
        ml = generation.validate();
        assertNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_INVALID_VALID_FROM));
    }
}