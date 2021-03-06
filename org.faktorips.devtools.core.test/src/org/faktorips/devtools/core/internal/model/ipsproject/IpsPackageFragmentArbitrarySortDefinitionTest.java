/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Markus Blum
 */
public class IpsPackageFragmentArbitrarySortDefinitionTest extends AbstractIpsPluginTest {

    private IpsPackageFragmentArbitrarySortDefinition sorter;
    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot rootPackage;

    // sorted packages by file
    private IIpsPackageFragment packLeistungFix;
    private IIpsPackageFragment packLeistung;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        sorter = new IpsPackageFragmentArbitrarySortDefinition();
        ipsProject = this.newIpsProject("TestProject");
        rootPackage = ipsProject.getIpsPackageFragmentRoots()[0];

        packLeistung = rootPackage.createPackageFragment("products.kranken.leistungsarten", true, null);
        packLeistungFix = rootPackage.createPackageFragment("products.kranken.leistungsarten.fix", true, null);
        rootPackage.createPackageFragment("products.kranken.leistungsarten.optional", true, null);
        rootPackage.createPackageFragment("products.hausrat.deckungen.grundeckung", true, null);
        rootPackage.createPackageFragment("products.hausrat.deckungen.zusatzdeckungen", true, null);

        IIpsPackageFragment packHausrat = rootPackage.getIpsPackageFragment("products.hausrat");
        IIpsPackageFragment packHausratDeckungen = rootPackage.getIpsPackageFragment("products.hausrat.deckungen");

        IIpsPackageFragment products = rootPackage.getIpsPackageFragment("products");

        // create files
        ArrayList<String> list = new ArrayList<String>(2);
        list.add("products");

        createPackageOrderFile((IFolder)rootPackage.getCorrespondingResource(), list);
        list.clear();

        list.add("unfall");
        list.add("kranken");
        list.add("folder");
        list.add("haftpflicht");
        list.add("hausrat");

        createPackageOrderFile((IFolder)products.getCorrespondingResource(), list);
        list.clear();

        list.add("optional");
        list.add("fix");

        createPackageOrderFile((IFolder)packLeistung.getCorrespondingResource(), list);
        list.clear();

        list.add("deckungen");

        createPackageOrderFile((IFolder)packHausrat.getCorrespondingResource(), list);
        list.clear();

        list.add("grunddeckung");
        list.add("zusatzdeckungen");

        createPackageOrderFile((IFolder)packHausratDeckungen.getCorrespondingResource(), list);
        list.clear();
    }

    @Test
    public void testCompare() throws CoreException {
        // not initialized
        assertTrue((sorter.compare("", "") == 0));
        assertTrue((sorter.compare("fix", "optional") == 0));

        String packageNames = getSortDefinitionContent(packLeistungFix);
        sorter.initPersistenceContent(packageNames);

        assertTrue((sorter.compare("fix", "optional") > 0));
        assertTrue((sorter.compare("optional", "fix") < 0));

        // dummy package is not in sort order file
        assertTrue((sorter.compare("dummy", "fix") > 0));
        assertTrue((sorter.compare("fix", "dummy") < 0));

        // identical folder
        IIpsPackageFragment packHausrat = rootPackage.getIpsPackageFragment("products.hausrat.deckungen.grunddeckung");
        packageNames = getSortDefinitionContent(packHausrat);
        sorter.initPersistenceContent(packageNames);

        assertTrue((sorter.compare("grunddeckung", "grunddeckung") == 0));
        assertFalse((sorter.compare("grunddeckung", "dummy") == 0));
    }

    @Test
    public void testCopy() throws CoreException {
        // not initialized
        IIpsPackageFragmentSortDefinition sortDefCopy = sorter.copy();

        assertNotNull(sortDefCopy);
        assertEquals(sorter.toPersistenceContent(), sortDefCopy.toPersistenceContent());

        String packageNames = getSortDefinitionContent(packLeistungFix);
        sorter.initPersistenceContent(packageNames);

        sortDefCopy = sorter.copy();

        assertNotNull(sortDefCopy);
        assertEquals(sorter.toPersistenceContent(), sortDefCopy.toPersistenceContent());
    }

    @Test
    public void testGetSegmentNames() throws CoreException {
        // not initialized
        assertEquals(0, sorter.getSegmentNames().length);

        String packageNames = getSortDefinitionContent(packLeistungFix);
        sorter.initPersistenceContent(packageNames);

        String[] segments = sorter.getSegmentNames();
        assertEquals(2, segments.length);
        assertEquals("optional", segments[0]);
        assertEquals("fix", segments[1]);
    }

    @Test
    public void testInitPersistenceContent() throws CoreException {
        // not initialized
        assertEquals(0, sorter.getSegmentNames().length);

        String packageNames = getSortDefinitionContent(packLeistungFix);

        // to test different line separators, add some fictive packages

        packageNames += "\nunix"; // Unix standard
        packageNames += "\rmac"; // old Mac standard
        packageNames += "\r\nwin";

        sorter.initPersistenceContent(packageNames);

        String[] segments = sorter.getSegmentNames();
        assertEquals(5, segments.length);
        assertEquals("optional", segments[0]);
        assertEquals("fix", segments[1]);

        assertEquals("unix", segments[2]);
        assertEquals("mac", segments[3]);
        assertEquals("win", segments[4]);
    }

    @Test
    public void testSetSegmentNames() throws CoreException {
        String packageNames = getSortDefinitionContent(packLeistungFix);
        sorter.initPersistenceContent(packageNames);

        // empty content
        String[] contentEmpty = new String[0];
        sorter.setSegmentNames(contentEmpty);

        String[] result = sorter.getSegmentNames();
        assertEquals(0, result.length);

        // change content
        String[] content = new String[3];

        content[0] = "folder1";
        content[1] = "folder2";
        content[2] = "folder3";

        sorter.setSegmentNames(content);

        result = sorter.getSegmentNames();

        assertEquals(3, result.length);
        assertEquals("folder1", result[0]);
        assertEquals("folder2", result[1]);
        assertEquals("folder3", result[2]);
    }

    @Test
    public void testToPersistenceContent() throws CoreException {
        // not initialized
        String content = sorter.toPersistenceContent();
        assertNotNull(content);
        assertEquals(Messages.IpsPackageFragmentArbitrarySortDefinition_CommentLine, content);

        String packageNames = getSortDefinitionContent(packLeistungFix);
        sorter.initPersistenceContent(packageNames);

        content = sorter.toPersistenceContent();

        assertNotNull(content);
        assertTrue((content.length() > 0));

        String[] test = StringUtils.split(content, StringUtil.getSystemLineSeparator());
        assertEquals(3, test.length);
        assertEquals(Messages.IpsPackageFragmentArbitrarySortDefinition_CommentLine, test[0]);
        assertEquals("optional", test[1]);
        assertEquals("fix", test[2]);
    }

    private String getSortDefinitionContent(IIpsPackageFragment fragment) throws CoreException {
        IFolder folder;

        if (fragment.isDefaultPackage()) {
            folder = (IFolder)fragment.getRoot().getCorrespondingResource();
        } else {
            folder = (IFolder)fragment.getParentIpsPackageFragment().getCorrespondingResource();
        }

        IFile file = folder.getFile(new Path(IIpsPackageFragment.SORT_ORDER_FILE_NAME));
        String content;

        try {
            content = StringUtil.readFromInputStream(file.getContents(), fragment.getIpsProject()
                    .getPlainTextFileCharset());
        } catch (IOException e) {
            IpsPlugin.log(e);
            return null;
        }

        return content;
    }

}
