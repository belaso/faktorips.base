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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LinkCandidateFilterTest {
    private static final String TYPE_NAME = "bla.bla.bla.Type";
    @Mock
    private IProductCmpt prodCmpt;
    @Mock
    private IProductCmptGeneration prodCmptGeneration;
    @Mock
    private IIpsSrcFile srcFile;
    @Mock
    private IProductCmptType type;
    @Mock
    private IProductCmptTypeAssociation association;
    @Mock
    private IIpsProject ipsProject;

    private LinkCandidateFilter filter;
    private GregorianCalendar validAt;

    @Before
    public void setUp() throws CoreException {
        when(prodCmpt.getIpsProject()).thenReturn(ipsProject);

        when(prodCmpt.getGenerationEffectiveOn(any(GregorianCalendar.class))).thenReturn(prodCmptGeneration);

        when(srcFile.isMutable()).thenReturn(true);

        when(association.getName()).thenReturn("association");
        when(association.getIpsProject()).thenReturn(ipsProject);
        when(association.findTargetProductCmptType(ipsProject)).thenReturn(type);
        when(association.getMaxCardinality()).thenReturn(3);

        when(prodCmptGeneration.getLinks(association.getName())).thenReturn(new IProductCmptLink[0]);
        when(prodCmptGeneration.getIpsSrcFile()).thenReturn(srcFile);
        when(prodCmptGeneration.getIpsProject()).thenReturn(ipsProject);

        when(type.isSubtypeOrSameType(eq(type), any(IIpsProject.class))).thenReturn(true);
        validAt = new GregorianCalendar(2013, 4, 1);
    }

    @Test
    public void testWrongObjectType() throws CoreException {
        createFilter();

        IIpsSrcFile sourceFileWrongType = createSourceFile(ipsProject, type);
        when(sourceFileWrongType.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_STRUCTURE);

        assertFalse(filter.filter(sourceFileWrongType));
    }

    @Test
    public void testImmutableProductCmpt() throws CoreException {
        when(srcFile.isReadOnly()).thenReturn(true);

        createFilter();

        assertFalse(filter.filter(createSourceFile(ipsProject, type)));
    }

    @Test
    public void testWorkingModeBrowse() throws CoreException {
        createFilter(true);

        assertFalse(filter.filter(createSourceFile(ipsProject, type)));
    }

    @Test
    public void testInProject() throws CoreException {
        createFilter();

        IIpsSrcFile srcFileSameProject = createSourceFile(ipsProject, type);

        IIpsProject referencedProject = mock(IIpsProject.class);
        IIpsProject notReferencedProject = mock(IIpsProject.class);

        IIpsSrcFile srcFileReferencedProject = createSourceFile(referencedProject, type);
        when(ipsProject.isReferencing(referencedProject)).thenReturn(true);

        IIpsSrcFile srcFileNotReferencedProject = createSourceFile(notReferencedProject, type);
        when(ipsProject.isReferencing(notReferencedProject)).thenReturn(false);

        assertTrue(filter.filter(srcFileSameProject));
        assertTrue(filter.filter(srcFileReferencedProject));
        assertFalse(filter.filter(srcFileNotReferencedProject));
    }

    @Test
    public void testSameTargetType() throws CoreException {
        createFilter();

        IIpsSrcFile srcFileSameType = createSourceFile(ipsProject, type);
        assertTrue(filter.filter(srcFileSameType));
    }

    @Test
    public void testSubOfTargetType() throws CoreException {
        createFilter();

        IProductCmptType subType = mock(IProductCmptType.class);
        when(subType.isSubtypeOrSameType(eq(type), any(IIpsProject.class))).thenReturn(true);

        IIpsSrcFile srcFileSubType = createSourceFile(ipsProject, subType);
        assertTrue(filter.filter(srcFileSubType));
    }

    @Test
    public void testSuperOfTargetType() throws CoreException {
        createFilter();

        IProductCmptType superType = mock(IProductCmptType.class);
        when(superType.isSubtypeOrSameType(eq(type), any(IIpsProject.class))).thenReturn(false);

        IIpsSrcFile srcFileSuperType = createSourceFile(ipsProject, superType);
        assertFalse(filter.filter(srcFileSuperType));
    }

    @Test
    public void testAnotherTargetType() throws CoreException {
        createFilter();

        IProductCmptType anotherType = mock(IProductCmptType.class);
        when(anotherType.isSubtypeOrSameType(eq(type), any(IIpsProject.class))).thenReturn(false);

        IIpsSrcFile srcFileAnyType = createSourceFile(ipsProject, anotherType);

        assertFalse(filter.filter(srcFileAnyType));
    }

    @Test
    public void testAlreadyAssociated() throws CoreException {
        createFilter();

        IIpsSrcFile srcFileNotLinked = createSourceFile(ipsProject, type);
        when(srcFileNotLinked.getName()).thenReturn("de.not.linked.PC");

        IIpsSrcFile srcFileAlreadyLinked = createSourceFile(ipsProject, type);
        String linkedName = "de.linked.PC";
        when(srcFileAlreadyLinked.getName()).thenReturn(linkedName);

        IProductCmptLink link = mock(IProductCmptLink.class);
        when(link.getTarget()).thenReturn(linkedName);

        List<IProductCmptLink> links = Arrays.asList(link);
        when(prodCmptGeneration.getLinksAsList()).thenReturn(links);

        assertTrue(filter.filter(srcFileNotLinked));
        assertFalse(filter.filter(srcFileAlreadyLinked));
    }

    @Test
    public void testAssociationAlreadyFull() throws CoreException {

        String linkedName = "de.linked.PC";

        IProductCmptLink link = mock(IProductCmptLink.class);
        when(link.getTarget()).thenReturn(linkedName);

        IProductCmptLink[] links = { link };

        when(association.getMaxCardinality()).thenReturn(1);
        when(prodCmptGeneration.getLinks(association.getName())).thenReturn(links);

        createFilter();

        IIpsSrcFile srcFileNotLinked = createSourceFile(ipsProject, type);
        when(srcFileNotLinked.getName()).thenReturn("de.not.linked.PC");

        IIpsSrcFile srcFileAlreadyLinked = createSourceFile(ipsProject, type);
        when(srcFileAlreadyLinked.getName()).thenReturn(linkedName);

        assertFalse(filter.filter(srcFileNotLinked));
    }

    private void createFilter() {
        createFilter(false);
    }

    private void createFilter(boolean workingModeBrowse) {
        when(association.getProductCmptType()).thenReturn(type);

        IProductCmptTypeAssociationReference structureReference = mock(IProductCmptTypeAssociationReference.class);
        when(structureReference.getAssociation()).thenReturn(association);

        IProductCmptTreeStructure structure = mock(IProductCmptTreeStructure.class);
        when(structure.getValidAt()).thenReturn(validAt);

        when(structureReference.getStructure()).thenReturn(structure);

        IProductCmptReference parentReference = mock(IProductCmptReference.class);
        when(structureReference.getParent()).thenReturn(parentReference);
        when(parentReference.getProductCmpt()).thenReturn(prodCmpt);

        filter = new LinkCandidateFilter(structureReference, workingModeBrowse);
    }

    private IIpsSrcFile createSourceFile(IIpsProject project, IProductCmptType productCmptType) throws CoreException {
        IpsSrcFile srcFile = mock(IpsSrcFile.class);

        when(srcFile.getIpsProject()).thenReturn(project);

        when(srcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE)).thenReturn(TYPE_NAME);
        when(project.findProductCmptType(TYPE_NAME)).thenReturn(productCmptType);
        when(srcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        return srcFile;
    }
}
