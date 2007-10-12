/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IRangeValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class AttributeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IType type;
    private IAttribute attribute;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        type = newProductCmptType(ipsProject, "Product");
        attribute = ((IProductCmptType)type).newAttribute();
    }
    
    public void testValidate_defaultNotInValueset() throws Exception {
        IProductCmptTypeAttribute attributeWithValueSet = ((IProductCmptType)type).newAttribute();
        attributeWithValueSet.setDatatype(Datatype.INTEGER.getQualifiedName());
        attributeWithValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet range = (IRangeValueSet)attributeWithValueSet.getValueSet();
        range.setLowerBound("0");
        range.setUpperBound("10");
        range.setStep("1");
        attributeWithValueSet.setDefaultValue("1");
        MessageList ml = attributeWithValueSet.validate();
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));

        attributeWithValueSet.setDefaultValue("100");
        ml = attributeWithValueSet.validate();
        Message msg = ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET);
        assertNotNull(msg);
        assertEquals(Message.WARNING, msg.getSeverity());
        
        attributeWithValueSet.setDefaultValue(null);
        ml = attributeWithValueSet.validate();
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    public void testValidate_defaultNotParsableUnknownDatatype() throws Exception {
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setDefaultValue("1");
        
        MessageList ml = attribute.validate();
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE));
        
        attribute.setDatatype("a");
        ml = attribute.validate();
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE));
    }

    public void testValidate_defaultNotParsableInvalidDatatype() throws Exception {
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        MessageList ml = attribute.validate();
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE));
        
        attribute.setDatatype("abc");
        ml = attribute.validate();
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE));
    }

    public void testValidate_valueNotParsable() throws Exception {
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setDefaultValue("1");
        MessageList ml = attribute.validate();
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_VALUE_NOT_PARSABLE));
        
        attribute.setDefaultValue("a");
        ml = attribute.validate();
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_VALUE_NOT_PARSABLE));
    }

    public void testValidate_invalidAttributeName() throws Exception {
        attribute.setName("test");
        MessageList ml = attribute.validate();
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_INVALID_ATTRIBUTE_NAME));
        
        attribute.setName("a.b");
        ml = attribute.validate();
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_INVALID_ATTRIBUTE_NAME));
    }

    public void testSetName() {
        testPropertyAccessReadWrite(Attribute.class, IAttribute.PROPERTY_NAME, attribute, "newName");
    }

    public void testSetModifier() {
        testPropertyAccessReadWrite(Attribute.class, IAttribute.PROPERTY_MODIFIER, attribute, Modifier.PUBLIC);
    }

    public void testSetValueDatatype() {
        testPropertyAccessReadWrite(Attribute.class, IAttribute.PROPERTY_DATATYPE, attribute, "newDatatype");
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute#findDatatype()}.
     * @throws CoreException 
     */
    public void testFindValueDatatype() throws CoreException {
        attribute.setDatatype(Datatype.BOOLEAN.getName());
        assertEquals(Datatype.BOOLEAN, attribute.findDatatype(ipsProject));
        attribute.setDatatype("unkown");
        assertNull(attribute.findDatatype(ipsProject));
    }
    
    public void testSetDefaultValue() {
        testPropertyAccessReadWrite(Attribute.class, IAttribute.PROPERTY_DEFAULT_VALUE, attribute, "newDefault");
    }
    
//    public void testDelete() {
//        attribute.delete();
//        assertNull(type.getAttribute(attribute.getName()));
//        assertEquals(0, type.getNumOfAttributes());
//    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute#getImage()}.
     */
    public void testGetImage() {
        assertNotNull(attribute.getImage());
    }
    
    public void testInitFromXml() {
        IProductCmptTypeAttribute attr = ((IProductCmptType)type).newAttribute();
        Element rootEl = getTestDocument().getDocumentElement();
        
        // product attribute
        attr.setModifier(Modifier.PUBLISHED);
        attr.initFromXml(XmlUtil.getElement(rootEl, Attribute.TAG_NAME, 0));
        assertEquals("rate", attr.getName());
        assertEquals(Modifier.PUBLIC, attr.getModifier());
        assertEquals("Integer", attr.getDatatype());
    }
    
    public void testToXml() {
        attribute.setName("a1");
        attribute.setDefaultValue("newDefault");
        attribute.setModifier(Modifier.PUBLIC);
        attribute.setDatatype("Date");
        
        Element el = attribute.toXml(newDocument());
        
        IAttribute copy = ((IProductCmptType)type).newAttribute();
        copy.initFromXml(el);
        assertEquals(attribute.getName(), copy.getName());
        assertEquals(attribute.getModifier(), copy.getModifier());
        assertEquals(attribute.getDatatype(), copy.getDatatype());
        assertEquals(attribute.getDefaultValue(), copy.getDefaultValue());

        // test null as default value
        attribute.setDefaultValue(null);
        el = attribute.toXml(newDocument());
        copy.initFromXml(el);
        assertNull(copy.getDefaultValue());
        
    }
    
}
