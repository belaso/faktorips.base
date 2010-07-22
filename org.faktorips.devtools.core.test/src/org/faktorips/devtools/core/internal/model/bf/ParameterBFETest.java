/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.bf;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ParameterBFETest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private TestContentsChangeListener listener;
    private BusinessFunction bf;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        listener = new TestContentsChangeListener();
        ipsProject.getIpsModel().addChangeListener(listener);
        bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");
    }

    public void testInitPropertiesFromXml() throws Exception {
        IParameterBFE p = bf.newParameter();
        p.setDatatype(Datatype.INTEGER.getName());
        Document doc = getDocumentBuilder().newDocument();
        Element el = p.toXml(doc);

        IParameterBFE p2 = bf.newParameter();
        p2.initFromXml(el);
        assertEquals(Datatype.INTEGER.getName(), p2.getDatatype());
    }

    public void testPropertiesToXml() {
        Document doc = getTestDocument();
        IParameterBFE p = bf.newParameter();
        p.initFromXml((Element)doc.getDocumentElement().getElementsByTagName(IParameterBFE.XML_TAG).item(0));
        assertEquals("String", p.getDatatype());
    }

    public void testGetDisplayString() {
        IParameterBFE p = bf.newParameter();
        p.setDatatype("String");
        p.setName("p1");
        assertEquals("String:p1", p.getDisplayString());
    }

    public void testSetDatatype() {
        IParameterBFE p = bf.newParameter();
        listener.clear();
        p.setDatatype("String");
        assertEquals("String", p.getDatatype());
        assertTrue(listener.getIpsObjectParts().contains(p));

        p.setDatatype("");
        assertEquals("", p.getDatatype());

        try {
            p.setDatatype(null);
            fail();
        } catch (NullPointerException e) {

        }
    }

    public void testFindDatatype() throws Exception {
        IParameterBFE p = bf.newParameter();
        p.setDatatype(Datatype.STRING.getName());
        Datatype d = p.findDatatype();
        assertEquals(Datatype.STRING, d);

        p.setDatatype("");
        d = p.findDatatype();
        assertNull(d);

    }

    public void testValidateNameSpecified() throws Exception {
        IParameterBFE p = bf.newParameter();
        MessageList msgList = p.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IParameterBFE.MSGCODE_NAME_NOT_SPECIFIED));

        p.setName("policy");
        msgList = p.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IParameterBFE.MSGCODE_NAME_NOT_SPECIFIED));
    }

    public void testValidateNameValidIdenifier() throws Exception {
        IParameterBFE p = bf.newParameter();
        p.setName("policy");
        MessageList msgList = p.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IParameterBFE.MSGCODE_NAME_NOT_VALID));

        p.setName("policy-");
        msgList = p.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IParameterBFE.MSGCODE_NAME_NOT_VALID));
    }

    public void testValidateDatatypeSpecified() throws Exception {
        IParameterBFE p = bf.newParameter();
        p.setName("policy");
        MessageList msgList = p.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IParameterBFE.MSGCODE_DATATYPE_NOT_SPECIFIED));

        p.setDatatype(Datatype.STRING.getQualifiedName());
        msgList = p.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IParameterBFE.MSGCODE_DATATYPE_NOT_SPECIFIED));
    }

    public void testValidateDatatypeExists() throws Exception {
        IParameterBFE p = bf.newParameter();
        p.setName("policy");
        p.setDatatype(Datatype.STRING.getQualifiedName());
        MessageList msgList = p.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IParameterBFE.MSGCODE_DATATYPE_DOES_NOT_EXISIT));

        p.setDatatype("abc");
        msgList = p.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IParameterBFE.MSGCODE_DATATYPE_DOES_NOT_EXISIT));
    }

    public void testValidateDuplicateNames() throws Exception {
        IParameterBFE p1 = bf.newParameter();
        p1.setName("p1");
        IParameterBFE p2 = bf.newParameter();
        p2.setName("p2");

        MessageList msgList = p1.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IParameterBFE.MSGCODE_NAME_DUBLICATE));

        msgList = p2.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IParameterBFE.MSGCODE_NAME_DUBLICATE));

        p2.setName("p1");
        msgList = p1.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IParameterBFE.MSGCODE_NAME_DUBLICATE));

        msgList = p2.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IParameterBFE.MSGCODE_NAME_DUBLICATE));
    }
}
