/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.junit.Test;

public class DependencyGraphPersistenceManagerTest extends AbstractIpsPluginTest {

    @Test
    public final void testGetDependencyGraph() throws Exception {

        IIpsProject ipsProject = newIpsProject();
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "B");
        IAssociation bToA = b.newAssociation();
        bToA.setTarget(a.getQualifiedName());
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        DependencyGraphPersistenceManager persistenceManager = IpsPlugin.getDefault()
                .getDependencyGraphPersistenceManager();
        assertNull(persistenceManager.getDependencyGraph(ipsProject));
        persistenceManager.saving(new TestSaveContext());
        IDependencyGraph graph = persistenceManager.getDependencyGraph(ipsProject);
        assertNotNull(graph);
        IDependency[] dependencies = graph.getDependants(a.getQualifiedNameType());
        assertEquals(1, dependencies.length);
        assertEquals(b.getQualifiedNameType(), dependencies[0].getSource());
    }

    private static class TestSaveContext implements ISaveContext {

        @Override
        public IPath[] getFiles() {
            return null;
        }

        @Override
        public int getKind() {
            return ISaveContext.FULL_SAVE;
        }

        @Override
        public int getPreviousSaveNumber() {
            return 0;
        }

        @Override
        public IProject getProject() {
            return null;
        }

        @Override
        public int getSaveNumber() {
            return 0;
        }

        @Override
        public IPath lookup(IPath file) {
            return null;
        }

        @Override
        public void map(IPath file, IPath location) {
        }

        @Override
        public void needDelta() {
        }

        @Override
        public void needSaveNumber() {
        }

    }
}
