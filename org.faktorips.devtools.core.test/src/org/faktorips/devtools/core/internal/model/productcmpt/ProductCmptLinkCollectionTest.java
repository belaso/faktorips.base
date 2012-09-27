/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptLinkCollectionTest extends AbstractIpsPluginTest {

    private ProductCmptLinkCollection linkCollection;
    private IProductCmptLink link1;
    private IProductCmptLink link2;
    private IProductCmptLink link3;
    private IProductCmptLink link4;
    private IProductCmptLink link5;

    @Override
    @Before
    public void setUp() throws Exception {
        linkCollection = new ProductCmptLinkCollection();
    }

    private void setUpCollectionWithLinks() {
        link1 = mock(IProductCmptLink.class);
        link2 = mock(IProductCmptLink.class);
        link3 = mock(IProductCmptLink.class);
        link4 = mock(IProductCmptLink.class);
        link5 = mock(IProductCmptLink.class);
        // deliberate reverse lexicographical order
        setUpAssociationAndID(link1, "oneAssociation", "id1");
        setUpAssociationAndID(link2, "anotherAssociation", "id2");
        setUpAssociationAndID(link3, "oneAssociation", "id3");
        setUpAssociationAndID(link4, "anotherAssociation", "id4");
        setUpAssociationAndID(link5, "yetAnotherAssociation", "id5");

        linkCollection.addLink(link1);
        linkCollection.addLink(link2);
        linkCollection.addLink(link3);
        linkCollection.addLink(link4);
        linkCollection.addLink(link5);

        assertEquals(5, linkCollection.getLinks().size());
    }

    private void setUpAssociationAndID(IProductCmptLink link, String assoc, String id) {
        when(link.getAssociation()).thenReturn(assoc);
        when(link.getId()).thenReturn(id);
    }

    @Test
    public void testLinkOrder_afterLinkChange() {
        setUpCollectionWithLinks();
        when(link1.getAssociation()).thenReturn("anotherAssociation");

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertTrue(links.contains(link1));
        assertTrue(links.contains(link2));
        assertTrue(links.contains(link3));
        assertTrue(links.contains(link4));
        assertTrue(links.contains(link5));

        assertEquals(link1, links.get(0));
        assertEquals(link2, links.get(1));
        assertEquals(link4, links.get(2));
        assertEquals(link3, links.get(3));
        assertEquals(link5, links.get(4));

    }

    @Test
    public void testLinkOrder_afterLinkChange2() {
        setUpCollectionWithLinks();
        when(link5.getAssociation()).thenReturn("oneAssociation");

        List<IProductCmptLink> links = linkCollection.getLinks();
        assertTrue(links.contains(link1));
        assertTrue(links.contains(link2));
        assertTrue(links.contains(link3));
        assertTrue(links.contains(link4));
        assertTrue(links.contains(link5));

        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
        assertEquals(link5, links.get(2));
        assertEquals(link2, links.get(3));
        assertEquals(link4, links.get(4));

    }

    @Test
    public void testGetLinks_EmptyCollection() {
        List<IProductCmptLink> links = new ProductCmptLinkCollection().getLinks();
        assertNotNull(links);
        assertTrue(links.isEmpty());
    }

    @Test
    public void testGetLinks() {
        setUpCollectionWithLinks();
        List<IProductCmptLink> links = linkCollection.getLinks();
        assertTrue(links.contains(link1));
        assertTrue(links.contains(link2));
        assertTrue(links.contains(link3));
        assertTrue(links.contains(link4));
        assertTrue(links.contains(link5));

        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
        assertEquals(link2, links.get(2));
        assertEquals(link4, links.get(3));
        assertEquals(link5, links.get(4));
    }

    @Test
    public void testGetLinksAsMap() throws Exception {
        setUpCollectionWithLinks();
        Map<String, List<IProductCmptLink>> map = linkCollection.getLinksAsMap();
        assertEquals(3, map.keySet().size());

        List<IProductCmptLink> linksOne = map.get("oneAssociation");
        assertEquals(link1, linksOne.get(0));
        assertEquals(link3, linksOne.get(1));
        assertEquals(2, linksOne.size());

        List<IProductCmptLink> linksAnother = map.get("anotherAssociation");
        assertEquals(link2, linksAnother.get(0));
        assertEquals(link4, linksAnother.get(1));
        assertEquals(2, linksAnother.size());

        List<IProductCmptLink> linksYetAnother = map.get("yetAnotherAssociation");
        assertEquals(link5, linksYetAnother.get(0));
        assertEquals(1, linksYetAnother.size());
    }

    @Test
    public void testGetLinksForAssociation() {
        setUpCollectionWithLinks();
        List<IProductCmptLink> links = linkCollection.getLinks("anotherAssociation");
        assertEquals(2, links.size());
        assertTrue(links.contains(link2));
        assertTrue(links.contains(link4));

        assertEquals(link2, links.get(0));
        assertEquals(link4, links.get(1));
    }

    @Test
    public void testGetLinksForAssociation2() {
        setUpCollectionWithLinks();
        List<IProductCmptLink> links = linkCollection.getLinks("oneAssociation");
        assertEquals(2, links.size());
        assertTrue(links.contains(link1));
        assertTrue(links.contains(link3));

        assertEquals(link1, links.get(0));
        assertEquals(link3, links.get(1));
    }

    @Test
    public void testGetLinksForAssociation3() {
        setUpCollectionWithLinks();
        List<IProductCmptLink> links = linkCollection.getLinks("yetAnotherAssociation");
        assertEquals(1, links.size());
        assertTrue(links.contains(link5));
    }

    @Test
    public void testGetLinksForAssociation_NoAssciations() {
        setUpCollectionWithLinks();
        List<IProductCmptLink> links = linkCollection.getLinks("inexistentAssociation");
        assertNotNull(links);
        assertTrue(links.isEmpty());
    }

    @Test
    public void testNewLink_addsLink() {
        IProductCmptGeneration container = createContainer();
        linkCollection.newLink(container, "oneAssociation", "id1");
        linkCollection.newLink(container, "anotherAssociation", "id2");
        linkCollection.newLink(container, "oneAssociation", "id3");

        assertEquals(3, linkCollection.getLinks().size());
    }

    @Test
    public void testNewLink() {
        IProductCmptGeneration container = createContainer();
        IProductCmptLink newLink = linkCollection.newLink(container, "oneAssociation", "id1");
        assertEquals("id1", newLink.getId());
        assertEquals(container, newLink.getParent());
        assertEquals("oneAssociation", newLink.getAssociation());
    }

    private IProductCmptGeneration createContainer() {
        try {
            IIpsProject ipsProject = newIpsProject();
            ProductCmpt productCmpt = newProductCmpt(ipsProject, "ProdCmpt");
            IProductCmptGeneration gen = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar());
            return gen;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Test
    public void testRemove() {
        setUpCollectionWithLinks();
        linkCollection.remove(link3);
        assertEquals(4, linkCollection.getLinks().size());
        assertFalse(linkCollection.getLinks().contains(link3));
    }

    @Test
    public void testRemoveMultipleTimes() {
        setUpCollectionWithLinks();
        linkCollection.remove(link3);
        assertEquals(4, linkCollection.getLinks().size());
        assertFalse(linkCollection.getLinks().contains(link3));
        linkCollection.remove(link3);
        assertEquals(4, linkCollection.getLinks().size());
        assertFalse(linkCollection.getLinks().contains(link3));
    }

    @Test
    public void testRemove_null() {
        setUpCollectionWithLinks();
        linkCollection.remove(null);
        assertEquals(5, linkCollection.getLinks().size());
        assertTrue(linkCollection.getLinks().contains(link3));
    }

    @Test
    public void testRemove_onEmptyList() {
        linkCollection.remove(link1);
        assertTrue(linkCollection.getLinks().isEmpty());
    }

    @Test
    public void testClear() {
        setUpCollectionWithLinks();
        linkCollection.clear();
        assertEquals(0, linkCollection.getLinks().size());
    }

    @Test
    public void testContainsLink_existingAssociation() {
        setUpCollectionWithLinks();
        IProductCmptLink linkX = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkX, "oneAssociation", "idX");

        assertFalse(linkCollection.containsLink(linkX));
    }

    @Test
    public void testContainsLink_nonexistentAssociation() {
        setUpCollectionWithLinks();
        IProductCmptLink linkX = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkX, "newAssociation", "idX");

        assertFalse(linkCollection.containsLink(linkX));
    }

    @Test
    public void testContainsLink_equalLink() {
        setUpCollectionWithLinks();
        assertTrue(linkCollection.containsLink(link4));
    }

    @Test
    public void testContainsLink_null() {
        setUpCollectionWithLinks();
        assertFalse(linkCollection.containsLink(null));
    }

    @Test
    public void testContainsLink_nullAssociation() {
        setUpCollectionWithLinks();
        IProductCmptLink linkX = mock(IProductCmptLink.class);
        setUpAssociationAndID(linkX, null, "idX");

        assertFalse(linkCollection.containsLink(linkX));
    }

    @Test
    public void testMoveLink() {

    }

    @Test
    public void testInsertLink() {

    }

}
